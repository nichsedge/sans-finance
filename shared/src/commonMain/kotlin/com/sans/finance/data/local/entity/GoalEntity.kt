package com.sans.finance.data.local.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val targetAmount: Long,
    val targetType: String = "TOTAL", // TOTAL, CATEGORY, ASSET_CLASS
    val targetName: String? = null,   // Specific category or asset class name
    val currency: String = "IDR",
    val deadline: Long? = null,
    val createdAt: Long = com.sans.finance.core.util.DateTimeUtils.getNow(),
    val updatedAt: Long = com.sans.finance.core.util.DateTimeUtils.getNow()
)
