package com.sans.finance.presentation.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sans.finance.core.util.DateTimeUtils
import com.sans.finance.domain.repository.AccountRepository
import com.sans.finance.domain.repository.ExpenseRepository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime


data class AccountStatsState(
    val selectedDate: Long = DateTimeUtils.getNow(),
    val totalBalance: Long = 0L,
    val balanceHistory: List<Pair<String, Long>> = emptyList(),
    val incomeExpenseHistory: List<Triple<String, Long, Long>> = emptyList(),
    val currentCurrency: String = "USD",
    val isLoading: Boolean = true
)


class AccountStatsViewModel constructor(
    private val accountRepository: AccountRepository,
    private val expenseRepository: ExpenseRepository,
    private val localeManager: com.sans.finance.data.util.LocaleManager
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(DateTimeUtils.getNow())

    val state: StateFlow<AccountStatsState> = combine(
        _selectedDate,
        accountRepository.getAllAccounts(),
        expenseRepository.getExpensesBetween(0, Long.MAX_VALUE)
    ) { date, accounts, transactions ->
        val currentAssets =
            accounts.filter { it.type != "Credit Card" && it.type != "Loan" }.sumOf { it.balance }
        val currentLiabilities =
            accounts.filter { it.type == "Credit Card" || it.type == "Loan" }.sumOf { it.balance }
        val currentTotal = currentAssets - currentLiabilities

        val balanceHistory = mutableListOf<Pair<String, Long>>()
        val incomeExpenseHistory = mutableListOf<Triple<String, Long, Long>>()

        // Simulated balance calculation using DateTimeUtils
        val now = DateTimeUtils.getNow()
        val nowMonthStart = DateTimeUtils.getStartOfMonth(now)
        val selectedMonthStart = DateTimeUtils.getStartOfMonth(date)

        // Count months between now and selected date
        var simulatedBalanceAtSelectedDate = currentTotal
        
        // Simple approximation for month difference
        val diffMillis = nowMonthStart - selectedMonthStart
        val approxMonths = (diffMillis / (30L * 24 * 60 * 60 * 1000)).toInt()
        
        var tempMonthStart = nowMonthStart
        for (i in 0 until approxMonths) {
            val start = tempMonthStart
            val end = DateTimeUtils.addMonths(start, 1)

            val txns =
                transactions.filter { it.date in start until end && (!it.isInstallment || it.isInstallmentPayment) }
            val netFlow = txns.filter { it.type == "INCOME" }
                .sumOf { it.amount } - txns.filter { it.type == "EXPENSE" }.sumOf { it.amount }

            simulatedBalanceAtSelectedDate -= netFlow
            tempMonthStart = DateTimeUtils.addMonths(tempMonthStart, -1)
        }

        var historyMonthStart = selectedMonthStart
        var historyBalance = simulatedBalanceAtSelectedDate

        for (i in 0 until 6) {
            val start = historyMonthStart
            val end = DateTimeUtils.addMonths(start, 1)

            val txns =
                transactions.filter { it.date in start until end && (!it.isInstallment || it.isInstallmentPayment) }
            val income = txns.filter { it.type == "INCOME" }.sumOf { it.amount }
            val expense = txns.filter { it.type == "EXPENSE" }.sumOf { it.amount }

            val dateStr = DateTimeUtils.formatDate(start) // Or custom format

            balanceHistory.add(0, Pair(dateStr, historyBalance))
            incomeExpenseHistory.add(
                0,
                Triple(dateStr, income, expense)
            )

            val netFlow = income - expense
            historyBalance -= netFlow
            historyMonthStart = DateTimeUtils.addMonths(historyMonthStart, -1)
        }

        AccountStatsState(
            selectedDate = date,
            totalBalance = simulatedBalanceAtSelectedDate,
            balanceHistory = balanceHistory,
            incomeExpenseHistory = incomeExpenseHistory,
            currentCurrency = localeManager.getCurrency(),
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AccountStatsState()
    )

    fun onPreviousMonth() {
        _selectedDate.update {
            DateTimeUtils.addMonths(it, -1)
        }
    }

    fun onNextMonth() {
        _selectedDate.update {
            DateTimeUtils.addMonths(it, 1)
        }
    }
}
