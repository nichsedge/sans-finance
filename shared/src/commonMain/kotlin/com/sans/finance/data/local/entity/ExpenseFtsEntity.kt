package com.sans.finance.data.local.entity

import androidx.room3.Entity
import androidx.room3.Fts4

@Fts4(contentEntity = ExpenseEntity::class)
@Entity(tableName = "expenses_fts")
data class ExpenseFtsEntity(
    val title: String,
    val details: String?
)
