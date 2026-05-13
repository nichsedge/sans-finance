package com.sans.finance.data.local.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String, // "Cash", "Bank", "Credit Card", "Loan", "Investment"
    val balance: Long,
    val currency: String = "IDR",
    @ColumnInfo(name = "created_at") val createdAt: Long = com.sans.finance.core.util.DateTimeUtils.getNow(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = com.sans.finance.core.util.DateTimeUtils.getNow()
)
