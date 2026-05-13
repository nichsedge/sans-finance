package com.sans.finance.presentation.expense_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sans.finance.core.util.DateTimeUtils
import com.sans.finance.domain.model.Category
import com.sans.finance.domain.model.Expense
import com.sans.finance.domain.repository.BudgetRepository
import com.sans.finance.domain.usecase.GetExpensesUseCase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime


enum class DateRangeFilter {
    THIS_WEEK,
    THIS_MONTH,
    LAST_MONTH,
    THIS_YEAR,
    ALL_TIME,
    CUSTOM
}

data class ExpenseListState(
    val expenses: List<Expense> = emptyList(),
    val groupedExpenses: Map<Long, List<Expense>> = emptyMap(),
    val thisMonthSpent: Long = 0L,
    val totalFilteredAmount: Long = 0L,
    val totalFilteredIncome: Long = 0L,
    val totalFilteredExpense: Long = 0L,
    val startDate: Long = 0L,
    val endDate: Long = Long.MAX_VALUE,
    val activeDateFilter: DateRangeFilter = DateRangeFilter.THIS_MONTH,
    val isLoading: Boolean = true,
    val error: String? = null,
    val accounts: List<com.sans.finance.data.local.entity.AccountEntity> = emptyList(),
    val categories: List<Category> = emptyList(),
    val availableTags: List<String> = emptyList(),
    val selectedTags: Set<String> = emptySet(),
    val searchQuery: String = "",
    val selectedCategoryIds: Set<Long> = emptySet(),
    val selectedAccountIds: Set<Long> = emptySet(),
    val minAmount: Long? = null,
    val maxAmount: Long? = null,
    val dailySpending: Map<Long, Long> = emptyMap(),
    val monthlyBudget: Long = 0L,
    val currentCurrency: String = "USD",
    val avgMonthlyExpense: Long = 0L,
    val isPrivacyModeEnabled: Boolean = false,
    val selectedTypes: Set<String> = emptySet()
)


class ExpenseListViewModel constructor(
    private val getExpensesUseCase: GetExpensesUseCase,
    private val repository: com.sans.finance.domain.repository.ExpenseRepository,
    private val accountRepository: com.sans.finance.domain.repository.AccountRepository,
    private val installmentRepository: com.sans.finance.domain.repository.InstallmentRepository,
    private val getCategoriesUseCase: com.sans.finance.domain.usecase.GetCategoriesUseCase,
    private val budgetRepository: BudgetRepository,
    private val localeManager: com.sans.finance.data.util.LocaleManager
) : ViewModel() {

    private val _state = MutableStateFlow(ExpenseListState())
    val state: StateFlow<ExpenseListState> = _state.asStateFlow()

    init {
        _state.update { it.copy(currentCurrency = localeManager.getCurrency()) }
        updateDateRange(DateRangeFilter.THIS_MONTH)

        loadExpenses()
        loadHistoricalStats()
        loadCategories()
        loadAccounts()
        loadTags()
        loadBudget()
        observePrivacyMode()
    }

    private fun observePrivacyMode() {
        localeManager.privacyMode
            .onEach { enabled ->
                _state.update { it.copy(isPrivacyModeEnabled = enabled) }
            }
            .launchIn(viewModelScope)
    }

    private fun loadBudget() {
        budgetRepository.getAllBudgets()
            .map { budgets -> budgets.find { it.categoryId == null }?.amount ?: 0L }
            .onEach { budget ->
                _state.update { it.copy(monthlyBudget = budget) }
            }
            .launchIn(viewModelScope)
    }

    private fun loadTags() {
        repository.getAllTags()
            .onEach { tags ->
                _state.update { it.copy(availableTags = tags) }
            }
            .launchIn(viewModelScope)
    }


    private fun loadAccounts() {
        accountRepository.getAllAccounts()
            .onEach { accounts ->
                _state.update { it.copy(accounts = accounts) }
            }
            .launchIn(viewModelScope)
    }

    private fun loadCategories() {
        getCategoriesUseCase()
            .onEach { categories ->
                _state.update { it.copy(categories = categories) }
            }
            .launchIn(viewModelScope)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadExpenses() {
        _state
            .map {
                listOf(
                    it.startDate,
                    it.endDate,
                    it.searchQuery,
                    it.selectedCategoryIds,
                    it.selectedAccountIds,
                    it.minAmount,
                    it.maxAmount,
                    it.selectedTags,
                    it.selectedTypes
                )
            }
            .distinctUntilChanged()
            .flatMapLatest { _ ->
                val s = _state.value
                val expensesFlow = repository.getFilteredExpenses(
                    query = s.searchQuery,
                    categoryIds = s.selectedCategoryIds.toList(),
                    accountIds = s.selectedAccountIds.toList(),
                    since = s.startDate,
                    until = s.endDate,
                    minAmount = s.minAmount,
                    maxAmount = s.maxAmount,
                    tags = s.selectedTags.toList(),
                    types = s.selectedTypes.toList()
                )

                val dailyFlow = repository.getDailySpendingBetween(s.startDate, s.endDate)

                expensesFlow.combine(dailyFlow) { e, d -> Pair(e, d) }
            }
            .onEach { (expenses, dailySpending) ->
                val dailyMap = dailySpending.associate { it.day to it.amount }
                val grouped = groupExpensesByDate(expenses, dailyMap)
                // Filtered item totals: normal items + installment payments (already in the list)
                // We only exclude 'parent' installment plans to avoid double counting with their sub-payments
                val validExpenses = expenses.filter { !it.isInstallment || it.isInstallmentPayment }
                val income = validExpenses.filter { it.type == "INCOME" }.sumOf { it.amount }
                val expense = validExpenses.filter { it.type != "INCOME" }.sumOf { it.amount }
                val periodTotal = income - expense

                var avgMonthlyExpense = 0L
                if (_state.value.activeDateFilter == DateRangeFilter.ALL_TIME && validExpenses.isNotEmpty()) {
                    val firstDate = validExpenses.minOf { it.date }
                    val lastDate = com.sans.finance.core.util.DateTimeUtils.getNow()
                    val diff = lastDate - firstDate
                    val months = (diff / (1000L * 60 * 60 * 24 * 30)).coerceAtLeast(1L)
                    avgMonthlyExpense = expense / months
                }

                _state.update {
                    it.copy(
                        expenses = expenses,
                        groupedExpenses = grouped,
                        totalFilteredAmount = periodTotal,
                        totalFilteredIncome = income,
                        totalFilteredExpense = expense,
                        dailySpending = dailyMap,
                        avgMonthlyExpense = avgMonthlyExpense,
                        isLoading = false
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun updateSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    fun toggleCategoryFilter(categoryId: Long) {
        _state.update { currentState ->
            val newSelectedCategoryIds =
                if (currentState.selectedCategoryIds.contains(categoryId)) {
                    currentState.selectedCategoryIds - categoryId
                } else {
                    currentState.selectedCategoryIds + categoryId
                }
            currentState.copy(selectedCategoryIds = newSelectedCategoryIds)
        }
    }

    fun toggleAccountFilter(accountId: Long) {
        _state.update { currentState ->
            val newSelectedAccountIds =
                if (currentState.selectedAccountIds.contains(accountId)) {
                    currentState.selectedAccountIds - accountId
                } else {
                    currentState.selectedAccountIds + accountId
                }
            currentState.copy(selectedAccountIds = newSelectedAccountIds)
        }
    }

    fun updateAmountFilter(min: Long?, max: Long?) {
        _state.update { it.copy(minAmount = min, maxAmount = max) }
    }

    fun clearFilters() {
        _state.update { currentState ->
            val now = DateTimeUtils.getNow()
            val start = DateTimeUtils.getStartOfMonth(now)
            val end = DateTimeUtils.addMonths(start, 1)

            currentState.copy(
                searchQuery = "",
                selectedCategoryIds = emptySet(),
                selectedAccountIds = emptySet(),
                minAmount = null,
                maxAmount = null,
                selectedTags = emptySet(),
                startDate = start,
                endDate = end,
                activeDateFilter = DateRangeFilter.THIS_MONTH,
                selectedTypes = emptySet()
            )
        }
    }

    fun toggleTypeFilter(type: String) {
        _state.update { currentState ->
            val newSelectedTypes = if (currentState.selectedTypes.contains(type)) {
                currentState.selectedTypes - type
            } else {
                currentState.selectedTypes + type
            }
            currentState.copy(selectedTypes = newSelectedTypes)
        }
    }

    fun toggleTagFilter(tag: String) {
        _state.update { currentState ->
            val newSelectedTags = if (currentState.selectedTags.contains(tag)) {
                currentState.selectedTags - tag
            } else {
                currentState.selectedTags + tag
            }
            currentState.copy(selectedTags = newSelectedTags)
        }
    }

    private fun groupExpensesByDate(
        expenses: List<Expense>,
        dailySpendingMap: Map<Long, Long> = emptyMap()
    ): Map<Long, List<Expense>> {
        return expenses.groupBy { expense ->
            DateTimeUtils.getStartOfDay(expense.date)
        }.entries
            .sortedByDescending { it.key }
            .associate { it.key to it.value }
    }

    fun updateCustomDateRange(start: Long, end: Long) {
        // Simplified custom range
        _state.update {
            it.copy(
                startDate = start,
                endDate = end,
                activeDateFilter = DateRangeFilter.CUSTOM,
                isLoading = true
            )
        }
    }

    fun updateDateRange(filter: DateRangeFilter) {
        val now = DateTimeUtils.getNow()

        val (start, end) = when (filter) {
            DateRangeFilter.THIS_WEEK -> {
                val s = DateTimeUtils.getStartOfDay(now) // Approximation
                val e = DateTimeUtils.addDays(s, 7)
                Pair(s, e)
            }

            DateRangeFilter.THIS_MONTH -> {
                val s = DateTimeUtils.getStartOfMonth(now)
                val e = DateTimeUtils.addMonths(s, 1)
                Pair(s, e)
            }

            DateRangeFilter.LAST_MONTH -> {
                val thisMonth = DateTimeUtils.getStartOfMonth(now)
                val s = DateTimeUtils.addMonths(thisMonth, -1)
                val e = thisMonth
                Pair(s, e)
            }

            DateRangeFilter.THIS_YEAR -> {
                val s = DateTimeUtils.getStartOfYear(now)
                val e = DateTimeUtils.addMonths(s, 12)
                Pair(s, e)
            }

            DateRangeFilter.ALL_TIME -> {
                Pair(0L, Long.MAX_VALUE)
            }

            DateRangeFilter.CUSTOM -> {
                Pair(_state.value.startDate, _state.value.endDate)
            }
        }

        _state.update {
            it.copy(
                startDate = start,
                endDate = end,
                activeDateFilter = filter,
                isLoading = true
            )
        }
    }

    private fun loadHistoricalStats() {
        val now = DateTimeUtils.getNow()
        val monthStart = DateTimeUtils.getStartOfMonth(now)
        val nextMonthStart = DateTimeUtils.addMonths(monthStart, 1)

        val monthExpensesFlow =
            repository.getTotalSpentBetween(monthStart, nextMonthStart)
        val monthInstallmentsFlow =
            installmentRepository.getTotalPaidAmountBetween(
                monthStart,
                nextMonthStart
            )

        monthExpensesFlow.combine(monthInstallmentsFlow) { exp, inst ->
            (exp ?: 0L) + (inst ?: 0L)
        }.onEach { total ->
            _state.update { it.copy(thisMonthSpent = total) }
        }.launchIn(viewModelScope)
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    fun togglePrivacyMode() {
        localeManager.setPrivacyModeEnabled(!localeManager.isPrivacyModeEnabled())
    }

    fun previousMonth() {
        val currentStart = if (_state.value.startDate == 0L) DateTimeUtils.getNow() else _state.value.startDate
        val start = DateTimeUtils.addMonths(DateTimeUtils.getStartOfMonth(currentStart), -1)
        val end = DateTimeUtils.addMonths(start, 1)
        
        _state.update { 
            it.copy(
                startDate = start,
                endDate = end,
                activeDateFilter = DateRangeFilter.CUSTOM,
                isLoading = true
            )
        }
    }

    fun nextMonth() {
        val currentStart = if (_state.value.startDate == 0L) DateTimeUtils.getNow() else _state.value.startDate
        val start = DateTimeUtils.addMonths(DateTimeUtils.getStartOfMonth(currentStart), 1)
        val end = DateTimeUtils.addMonths(start, 1)
        
        _state.update { 
            it.copy(
                startDate = start,
                endDate = end,
                activeDateFilter = DateRangeFilter.CUSTOM,
                isLoading = true
            )
        }
    }

    fun jumpToDate(millis: Long) {
        val start = DateTimeUtils.getStartOfMonth(millis)
        val end = DateTimeUtils.addMonths(start, 1)
        
        _state.update { 
            it.copy(
                startDate = start,
                endDate = end,
                activeDateFilter = DateRangeFilter.CUSTOM,
                isLoading = true
            )
        }
    }
}
