package com.sans.finance.data.local.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Long,
    val categoryId: Long? = null,
    val accountId: Long? = null,
    val period: String = "MONTHLY", // MONTHLY, WEEKLY, YEARLY
    val createdAt: Long = com.sans.finance.core.util.DateTimeUtils.getNow()
)
