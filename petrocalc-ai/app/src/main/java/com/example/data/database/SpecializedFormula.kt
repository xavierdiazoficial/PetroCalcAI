package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "specialized_formulas")
data class SpecializedFormula(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val category: String, // Perforación, Cementación, Workover, Producción
    val mathFormula: String,
    val technicalSource: String,
    val inputUnits: String,
    val outputUnits: String
)
