package com.sans.finance.data.local.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "installments",
    foreignKeys = [
        ForeignKey(
            entity = ExpenseEntity::class,
            parentColumns = ["id"],
            childColumns = ["expense_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["expense_id"])]
)
data class InstallmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "expense_id") val expenseId: Long,
    val status: String,
    @ColumnInfo(name = "duration_months") val durationMonths: Int,
    @ColumnInfo(name = "created_at") val createdAt: Long = com.sans.finance.core.util.DateTimeUtils.getNow()
)
