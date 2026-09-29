package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conditional_mandates")
data class ConditionalMandateEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val title: String,
  val sourceAccount: String,
  val targetAccount: String,
  val conditionType: String = "BALANCE_BELOW", // "BALANCE_BELOW" (Refill), "BALANCE_ABOVE" (Sweep)
  val thresholdAmount: Double,
  val transferAmount: Double,
  val isEnabled: Boolean = true,
  val lastTriggeredAt: Long = 0L,
  val totalTriggeredCount: Int = 0,
  val notes: String = ""
)
