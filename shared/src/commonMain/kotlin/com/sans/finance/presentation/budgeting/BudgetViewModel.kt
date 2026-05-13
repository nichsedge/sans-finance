package com.sans.finance.presentation.budgeting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sans.finance.data.local.entity.BudgetEntity
import com.sans.finance.domain.model.Category
import com.sans.finance.domain.repository.BudgetRepository
import com.sans.finance.domain.repository.ExpenseRepository

import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime


data class BudgetStatus(
    val budget: BudgetEntity,
    val spent: Long,
    val categoryName: String? = null
)

data class BudgetState(
    val budgetStatuses: List<BudgetStatus> = emptyList(),
    val categories: List<Category> = emptyList(),
    val currentCurrency: String = "USD",
    val isLoading: Boolean = true,
    val isPrivacyModeEnabled: Boolean = false
)


class BudgetViewModel constructor(
    private val budgetRepository: BudgetRepository,
    private val expenseRepository: ExpenseRepository,
    private val localeManager: com.sans.finance.data.util.LocaleManager
) : ViewModel() {

    private val _categories = expenseRepository.getAllCategories().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val state = combine(
        budgetRepository.getAllBudgets(),
        _categories,
        localeManager.privacyMode
    ) { budgets, categories, privacyMode ->
        val now = com.sans.finance.core.util.DateTimeUtils.getNow()
        val start = com.sans.finance.core.util.DateTimeUtils.getStartOfMonth(now)
        val end = com.sans.finance.core.util.DateTimeUtils.addMonths(start, 1)

        val statuses = budgets.map { budget ->
            val spentFlow = if (budget.categoryId != null) {
                expenseRepository.getSpendingByCategoryBetween(start, end)
                    .map { categorySpents ->
                        categorySpents.find { it.categoryId == budget.categoryId }?.totalAmount
                            ?: 0L
                    }
            } else {
                expenseRepository.getTotalSpentBetween(start, end).map { it ?: 0L }
            }

            val spent =
                spentFlow.first() // This might be problematic in a combine block if it's not a cold flow that emits quickly

            BudgetStatus(
                budget = budget,
                spent = spent,
                categoryName = categories.find { it.id == budget.categoryId }?.name
            )
        }

        BudgetState(
            budgetStatuses = statuses,
            categories = categories,
            currentCurrency = localeManager.getCurrency(),
            isLoading = false,
            isPrivacyModeEnabled = privacyMode
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BudgetState()
    )

    fun addBudget(amount: Long, categoryId: Long? = null, accountId: Long? = null) {
        viewModelScope.launch {
            budgetRepository.insertBudget(
                BudgetEntity(
                    amount = amount,
                    categoryId = categoryId,
                    accountId = accountId
                )
            )
        }
    }

    fun deleteBudget(budget: BudgetEntity) {
        viewModelScope.launch {
            budgetRepository.deleteBudget(budget)
        }
    }

    fun togglePrivacyMode() {
        localeManager.setPrivacyModeEnabled(!localeManager.isPrivacyModeEnabled())
    }
}
