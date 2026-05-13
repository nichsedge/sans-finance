package com.sans.finance.data.local.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "portfolio_holdings",
    indices = [Index(value = ["snapshot_date"])],
    foreignKeys = [
        ForeignKey(
            entity = PortfolioSnapshotHeaderEntity::class,
            parentColumns = ["snapshotDate"],
            childColumns = ["snapshot_date"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PortfolioHoldingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "snapshot_date") val snapshotDate: Long, // FK to header
    val source: String,
    val category: String,
    val asset: String,
    val currency: String,
    val quantity: Double,
    val price: Double?,
    @ColumnInfo(name = "value_idr") val valueIdr: Double,
    @ColumnInfo(name = "asset_class") val assetClass: String,
    val account: String,
    val details: String?
)
