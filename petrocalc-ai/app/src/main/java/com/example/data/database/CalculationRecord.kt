package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calculation_history")
data class CalculationRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val user: String,
    val role: String,
    val type: String,
    val formula: String,
    val inputs: String,
    val result: String,
    val isFavorite: Boolean = false
)
