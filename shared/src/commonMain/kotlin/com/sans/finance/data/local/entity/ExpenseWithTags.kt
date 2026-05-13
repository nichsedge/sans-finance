package com.sans.finance.data.local.entity

import androidx.room3.Embedded
import androidx.room3.Junction
import androidx.room3.Relation

data class ExpenseWithTags(
    @Embedded val expense: ExpenseEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = ExpenseTagCrossRef::class,
            parentColumn = "expenseId",
            entityColumn = "tagId"
        )
    )
    val tags: List<TagEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "expense_id"
    )
    val installment: InstallmentEntity?,
    @Relation(
        parentColumn = "category_id",
        entityColumn = "id"
    )
    val category: CategoryEntity?
)
