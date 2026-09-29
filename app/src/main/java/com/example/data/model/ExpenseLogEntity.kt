package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Room Entity representing a daily personal expense log entry.
 * Stores local expense records for daily personal budgeting, category breakdown,
 * payment mode (UPI, Cash, Card), and daily essential tracking.
 */
@Entity(tableName = "expense_logs")
data class ExpenseLogEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val title: String,
  val amount: Double, // in INR (₹)
  val category: String, // e.g. "FOOD", "GROCERIES", "TRANSPORT", "BILLS", "SHOPPING", "ENTERTAINMENT", "HEALTH"
  val timestamp: Long = System.currentTimeMillis(),
  val dateString: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(timestamp)),
  val paymentMode: String = "UPI", // "UPI", "CASH", "DEBIT_CARD", "CREDIT_CARD", "NET_BANKING"
  val accountName: String = "Main Account",
  val note: String = "",
  val isEssential: Boolean = true, // Daily essentials (Food, Travel) vs discretionary spend
  val tags: String = "" // Comma-separated tags, e.g. "Daily,Office,Tea"
)
