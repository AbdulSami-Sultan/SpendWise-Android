package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room database Entity to store user spending data locally.
 */
@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val amount: Double,
    val category: String,
    val date: String,
    val description: String
)
