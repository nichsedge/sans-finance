package com.sans.finance.data.util

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LocaleManager(
    private val settings: Settings
) {
    fun setLocale(language: String) {
        settings["language"] = language
    }

    fun getLocale(): String {
        return settings["language", "en"]
    }

    fun setCurrency(currency: String) {
        settings["currency"] = currency
    }

    fun getCurrency(): String {
        return settings["currency", "USD"]
    }

    fun getEnabledCurrencies(): List<String> {
        val currencies = settings["enabled_currencies", "USD,IDR,CNY"]
        return currencies.split(",").filter { it.isNotBlank() }
    }

    fun setEnabledCurrencies(currencies: List<String>) {
        settings["enabled_currencies"] = currencies.joinToString(",")
    }

    private val _privacyMode = MutableStateFlow(isPrivacyModeEnabled())
    val privacyMode: StateFlow<Boolean> = _privacyMode.asStateFlow()

    fun isPrivacyModeEnabled(): Boolean {
        return settings["privacy_mode", false]
    }

    fun setPrivacyModeEnabled(enabled: Boolean) {
        settings["privacy_mode"] = enabled
        _privacyMode.value = enabled
    }

    // FIRE Settings
    private val _fireManualEnabled = MutableStateFlow(isFireManualEnabled())
    val fireManualEnabled = _fireManualEnabled.asStateFlow()

    private val _manualFireAnnualExpense = MutableStateFlow(getManualFireAnnualExpense())
    val manualFireAnnualExpense = _manualFireAnnualExpense.asStateFlow()

    fun isFireManualEnabled(): Boolean {
        return settings["fire_manual_enabled", false]
    }

    fun setFireManualEnabled(enabled: Boolean) {
        settings["fire_manual_enabled"] = enabled
        _fireManualEnabled.value = enabled
    }

    fun getManualFireAnnualExpense(): Long {
        return settings["fire_manual_annual_expense", 0L]
    }

    fun setManualFireAnnualExpense(amount: Long) {
        settings["fire_manual_annual_expense"] = amount
        _manualFireAnnualExpense.value = amount
    }
}
