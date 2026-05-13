package com.sans.finance.data.local.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "installment_items",
    foreignKeys = [
        ForeignKey(
            entity = InstallmentEntity::class,
            parentColumns = ["id"],
            childColumns = ["installment_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["installment_id"])]
)
data class InstallmentItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "installment_id") val installmentId: Long,
    val amount: Long,
    @ColumnInfo(name = "due_date") val dueDate: Long,
    @ColumnInfo(name = "status") val status: String, // Pending, Paid
    @ColumnInfo(name = "month_number") val monthNumber: Int
)
