package com.example.data.repository

import com.example.data.database.CalculationDao
import com.example.data.database.CalculationRecord
import kotlinx.coroutines.flow.Flow

class CalculationRepository(private val calculationDao: CalculationDao) {
    val allHistory: Flow<List<CalculationRecord>> = calculationDao.getAllHistory()

    suspend fun insert(record: CalculationRecord): Long {
        return calculationDao.insertRecord(record)
    }

    suspend fun update(record: CalculationRecord) {
        calculationDao.updateRecord(record)
    }

    suspend fun delete(record: CalculationRecord) {
        calculationDao.deleteRecord(record)
    }

    suspend fun clearAll() {
        calculationDao.clearAll()
    }
}
