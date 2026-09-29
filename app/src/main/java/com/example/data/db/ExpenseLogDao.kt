package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ExpenseLogEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for ExpenseLogEntity.
 * Provides reactive Room queries for tracking and analyzing daily personal expenses.
 */
@Dao
interface ExpenseLogDao {

  @Query("SELECT * FROM expense_logs ORDER BY timestamp DESC")
  fun getAllExpenseLogs(): Flow<List<ExpenseLogEntity>>

  @Query("SELECT * FROM expense_logs ORDER BY timestamp DESC")
  suspend fun getAllExpenseLogsList(): List<ExpenseLogEntity>

  @Query("SELECT * FROM expense_logs WHERE dateString = :dateString ORDER BY timestamp DESC")
  fun getExpenseLogsForDate(dateString: String): Flow<List<ExpenseLogEntity>>

  @Query("SELECT * FROM expense_logs WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
  fun getExpenseLogsBetween(startTime: Long, endTime: Long): Flow<List<ExpenseLogEntity>>

  @Query("SELECT * FROM expense_logs WHERE category = :category ORDER BY timestamp DESC")
  fun getExpenseLogsByCategory(category: String): Flow<List<ExpenseLogEntity>>

  @Query("SELECT SUM(amount) FROM expense_logs WHERE dateString = :dateString")
  fun getDailyTotalSpend(dateString: String): Flow<Double?>

  @Query("SELECT SUM(amount) FROM expense_logs WHERE timestamp >= :startTime AND timestamp <= :endTime")
  fun getTotalSpendBetween(startTime: Long, endTime: Long): Flow<Double?>

  @Query("SELECT * FROM expense_logs WHERE id = :id")
  suspend fun getExpenseLogById(id: Long): ExpenseLogEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertExpenseLog(expenseLog: ExpenseLogEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertExpenseLogs(expenseLogs: List<ExpenseLogEntity>): List<Long>

  @Update
  suspend fun updateExpenseLog(expenseLog: ExpenseLogEntity)

  @Delete
  suspend fun deleteExpenseLog(expenseLog: ExpenseLogEntity)

  @Query("DELETE FROM expense_logs WHERE id = :id")
  suspend fun deleteExpenseLogById(id: Long)

  @Query("DELETE FROM expense_logs")
  suspend fun clearAll()
}
