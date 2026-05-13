package com.sans.finance.data.local.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "exchange_rates")
data class ExchangeRateEntity(
    @PrimaryKey val code: String, // e.g. "USD", "IDR"
    val rateToIdr: Double,        // Base currency is IDR for this app
    val updatedAt: Long = com.sans.finance.core.util.DateTimeUtils.getNow()
)
