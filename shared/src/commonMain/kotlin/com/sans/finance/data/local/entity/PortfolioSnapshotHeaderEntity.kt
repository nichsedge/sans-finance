package com.sans.finance.data.local.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "portfolio_snapshot_headers")
data class PortfolioSnapshotHeaderEntity(
    @PrimaryKey val snapshotDate: Long, // epoch millis (start of day)
    val exchangeRateUsd: Double,        // IDR per USD at this date
    val totalValueIdr: Double,          // Pre-calculated total for faster history queries
    val totalValueUsd: Double,          // Pre-calculated total
    val createdAt: Long = com.sans.finance.core.util.DateTimeUtils.getNow()
)
