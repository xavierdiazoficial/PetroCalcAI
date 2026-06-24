package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CalculationDao {
    @Query("SELECT * FROM calculation_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<CalculationRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: CalculationRecord): Long

    @Update
    suspend fun updateRecord(record: CalculationRecord)

    @Delete
    suspend fun deleteRecord(record: CalculationRecord)

    @Query("DELETE FROM calculation_history")
    suspend fun clearAll()
}
