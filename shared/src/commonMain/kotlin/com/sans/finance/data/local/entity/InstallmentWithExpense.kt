package com.sans.finance.data.local.entity

import androidx.room3.Embedded
import androidx.room3.Relation

data class InstallmentWithExpense(
    @Embedded val installment: InstallmentEntity,
    @Relation(
        parentColumn = "expense_id",
        entityColumn = "id"
    )
    val expense: ExpenseEntity
)
