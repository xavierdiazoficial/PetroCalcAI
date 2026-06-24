package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SpecializedFormulaDao {
    @Query("SELECT * FROM specialized_formulas ORDER BY category, name")
    fun getAllFormulas(): Flow<List<SpecializedFormula>>

    @Query("SELECT * FROM specialized_formulas WHERE category = :category ORDER BY name")
    fun getFormulasByCategory(category: String): Flow<List<SpecializedFormula>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFormula(formula: SpecializedFormula): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFormulas(formulas: List<SpecializedFormula>)

    @Delete
    suspend fun deleteFormula(formula: SpecializedFormula)

    @Query("SELECT COUNT(*) FROM specialized_formulas")
    suspend fun getCount(): Int

    @Query("DELETE FROM specialized_formulas")
    suspend fun clearAll()
}
