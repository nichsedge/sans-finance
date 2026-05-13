package com.sans.finance.data.local.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val icon: String, // Icon name or resource string
    val orderIndex: Int = 0,
    val type: String = "EXPENSE" // "EXPENSE" or "INCOME"
)
