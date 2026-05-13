package com.sans.finance.presentation.settings

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sans.finance.data.local.AppDatabase
import com.sans.finance.data.local.entity.BudgetEntity
import com.sans.finance.domain.repository.BudgetRepository
import com.sans.finance.domain.repository.ExpenseRepository

import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch



class SettingsViewModel constructor(
    private val repository: ExpenseRepository,
    private val localeManager: com.sans.finance.data.util.LocaleManager,
    private val db: AppDatabase,
    private val budgetRepository: BudgetRepository
) : ViewModel() {

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    private val _syncMessage = mutableStateOf<String?>(null)
    val syncMessage: State<String?> = _syncMessage

    private val _currentLanguage = mutableStateOf(localeManager.getLocale())
    val currentLanguage: State<String> = _currentLanguage

    private val _currentCurrency = mutableStateOf(localeManager.getCurrency())
    val currentCurrency: State<String> = _currentCurrency

    private val _enabledCurrencies = mutableStateOf(localeManager.getEnabledCurrencies())
    val enabledCurrencies: State<List<String>> = _enabledCurrencies

    private val _isPrivacyModeEnabled = mutableStateOf(localeManager.isPrivacyModeEnabled())
    val isPrivacyModeEnabled: State<Boolean> = _isPrivacyModeEnabled

    fun updateLanguage(lang: String) {
        _currentLanguage.value = lang
    }

    fun toggleCurrency() {
        val enabled = _enabledCurrencies.value
        val currentIndex = enabled.indexOf(_currentCurrency.value)
        val next =
            if (currentIndex != -1 && currentIndex + 1 < enabled.size) enabled[currentIndex + 1] else enabled.firstOrNull()
                ?: "USD"
        localeManager.setCurrency(next)
        _currentCurrency.value = next
    }

    fun toggleEnabledCurrency(currency: String) {
        val currentList = _enabledCurrencies.value.toMutableList()
        if (currentList.contains(currency)) {
            if (currentList.size > 1) { // Keep at least one
                currentList.remove(currency)
            }
        } else {
            currentList.add(currency)
        }
        localeManager.setEnabledCurrencies(currentList)
        _enabledCurrencies.value = currentList
    }

    fun togglePrivacyMode() {
        val next = !localeManager.isPrivacyModeEnabled()
        localeManager.setPrivacyModeEnabled(next)
        _isPrivacyModeEnabled.value = next
    }

    val monthlyBudget = budgetRepository.getAllBudgets().map { budgets ->
        budgets.find { it.categoryId == null }?.amount ?: 0L
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0L
    )

    fun updateMonthlyBudget(amount: Long) {
        viewModelScope.launch {
            val budgets = budgetRepository.getAllBudgets().first()
            val existingGlobal = budgets.find { it.categoryId == null }
            if (existingGlobal != null) {
                budgetRepository.insertBudget(existingGlobal.copy(amount = amount))
            } else {
                budgetRepository.insertBudget(BudgetEntity(amount = amount, categoryId = null))
            }
        }
    }

    fun performMaintenance() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                repository.performDatabaseMaintenance()
                _syncMessage.value = "Maintenance completed successfully"
            } catch (e: Exception) {
                _error.value = "Maintenance failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }


    /*
    fun exportFullBackup(context: android.content.Context) {
        // This will be handled by platform-specific implementations
    }
    */

    fun clearMessages() {
        _error.value = null
        _syncMessage.value = null
    }
}
