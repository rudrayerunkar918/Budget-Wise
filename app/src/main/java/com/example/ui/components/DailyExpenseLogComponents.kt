package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.ExpenseCategory
import com.example.data.model.ExpenseLogEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Card component for displaying today's personal expense logs backed by Room database.
 */
@Composable
fun DailyExpenseLogSectionCard(
  dailyLogs: List<ExpenseLogEntity>,
  todayTotal: Double,
  currencySymbol: String = "₹",
  onOpenAddLog: () -> Unit,
  onDeleteLog: (ExpenseLogEntity) -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    ),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // Header row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(32.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Default.ReceiptLong,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(18.dp)
              )
            }
          }
          Column {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Text(
                text = "Today's Expense Logs",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              Surface(
                shape = RoundedCornerShape(4.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
              ) {
                Text(
                  text = "Room DB",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSecondaryContainer,
                  modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
              }
            }
            Text(
              text = "${dailyLogs.size} logs recorded today",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        // Quick Log Button
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier.clickable { onOpenAddLog() }
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Add,
              contentDescription = "Log",
              tint = MaterialTheme.colorScheme.onPrimary,
              modifier = Modifier.size(16.dp)
            )
            Text(
              text = "Log Expense",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onPrimary
            )
          }
        }
      }

      // Today's total summary banner
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "TODAY'S SPENT",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = "$currencySymbol${String.format(Locale.US, "%,.2f", todayTotal)}",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
          }
          val essentialCount = dailyLogs.count { it.isEssential }
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
          ) {
            Text(
              text = "$essentialCount Essentials",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Medium,
              color = MaterialTheme.colorScheme.onTertiaryContainer,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        }
      }

      // Daily Logs List
      if (dailyLogs.isEmpty()) {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Text(
              text = "No personal expenses logged today yet",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Medium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = "Tap 'Log Expense' above to record meals, travel, or groceries.",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
          }
        }
      } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          dailyLogs.take(5).forEach { log ->
            DailyExpenseLogRow(
              log = log,
              currencySymbol = currencySymbol,
              onDelete = { onDeleteLog(log) }
            )
          }
          if (dailyLogs.size > 5) {
            Text(
              text = "+ ${dailyLogs.size - 5} more logs recorded today in Room DB",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(start = 4.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
fun DailyExpenseLogRow(
  log: ExpenseLogEntity,
  currencySymbol: String,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier
) {
  val cat = ExpenseCategory.fromName(log.category)
  val timeStr = remember(log.timestamp) {
    SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(log.timestamp))
  }

  Surface(
    shape = RoundedCornerShape(10.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
    modifier = modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.weight(1f)
      ) {
        // Emoji avatar
        Surface(
          shape = CircleShape,
          color = MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier.size(36.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Text(text = cat.emoji, style = MaterialTheme.typography.bodyLarge)
          }
        }

        Column {
          Text(
            text = log.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
          )
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Surface(
              shape = RoundedCornerShape(4.dp),
              color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            ) {
              Text(
                text = log.paymentMode,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
              )
            }
            Text(
              text = timeStr,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (log.note.isNotBlank()) {
              Text(
                text = "• ${log.note}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
              )
            }
          }
        }
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Text(
          text = "-$currencySymbol${String.format(Locale.US, "%,.2f", log.amount)}",
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.error
        )
        IconButton(
          onClick = onDelete,
          modifier = Modifier.size(28.dp)
        ) {
          Icon(
            imageVector = Icons.Default.DeleteOutline,
            contentDescription = "Delete",
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(16.dp)
          )
        }
      }
    }
  }
}

/**
 * Interactive Dialog to quickly log a daily personal expense with Room persistence.
 */
@Composable
fun AddDailyExpenseLogDialog(
  onDismiss: () -> Unit,
  onSave: (title: String, amount: Double, category: String, paymentMode: String, isEssential: Boolean) -> Unit
) {
  var title by remember { mutableStateOf("") }
  var amountText by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf("FOOD") }
  var selectedPaymentMode by remember { mutableStateOf("UPI") }
  var isEssential by remember { mutableStateOf(true) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  val quickTitles = listOf(
    Pair("Tea & Coffee ☕", "FOOD"),
    Pair("Team Lunch 🍱", "FOOD"),
    Pair("Metro Transit 🚇", "TRANSPORT"),
    Pair("Groceries 🛒", "GROCERIES"),
    Pair("Medicines 💊", "HEALTH")
  )

  val categories = listOf(
    Pair("FOOD", "🍔 Food"),
    Pair("GROCERIES", "🛒 Groceries"),
    Pair("TRANSPORT", "🚗 Transport"),
    Pair("SHOPPING", "🛍️ Shopping"),
    Pair("HEALTH", "💊 Health"),
    Pair("HOUSING", "🏠 Bills"),
    Pair("OTHER", "🏷️ Other")
  )

  val paymentModes = listOf("UPI", "CASH", "DEBIT_CARD", "CREDIT_CARD")

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Icon(
          imageVector = Icons.Default.FlashOn,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary
        )
        Text(
          text = "Log Daily Personal Expense",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
      }
    },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        // Quick Title Chips
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(
            text = "Quick Presets:",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            quickTitles.take(3).forEach { (presetTitle, presetCat) ->
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier
                  .weight(1f)
                  .clickable {
                    title = presetTitle.substringBefore(" ")
                    selectedCategory = presetCat
                  }
              ) {
                Text(
                  text = presetTitle,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Medium,
                  modifier = Modifier.padding(vertical = 4.dp, horizontal = 2.dp),
                  textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
              }
            }
          }
        }

        // Title Input
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Expense Title") },
          placeholder = { Text("e.g. Lunch at Cafe, Auto fare") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        // Amount Input with Quick Amount Buttons
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          OutlinedTextField(
            value = amountText,
            onValueChange = {
              if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}\$"))) {
                amountText = it
              }
            },
            label = { Text("Amount (₹ INR)") },
            placeholder = { Text("e.g. 150.00") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )

          // Quick Amount Adders
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            listOf(50, 100, 200, 500).forEach { addVal ->
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                modifier = Modifier
                  .weight(1f)
                  .clickable {
                    val curr = amountText.toDoubleOrNull() ?: 0.0
                    amountText = String.format(Locale.US, "%.0f", curr + addVal)
                  }
              ) {
                Text(
                  text = "+₹$addVal",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onPrimaryContainer,
                  modifier = Modifier.padding(vertical = 4.dp),
                  textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
              }
            }
          }
        }

        // Category Picker
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(
            text = "Category:",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            categories.take(4).forEach { (catKey, catLabel) ->
              val isSelected = selectedCategory == catKey
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                  .weight(1f)
                  .clickable { selectedCategory = catKey }
              ) {
                Text(
                  text = catLabel,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.padding(vertical = 4.dp),
                  textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
              }
            }
          }
        }

        // Payment Mode (UPI, Cash, Card)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(
            text = "Payment Mode:",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            paymentModes.forEach { mode ->
              val isSelected = selectedPaymentMode == mode
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                  .weight(1f)
                  .clickable { selectedPaymentMode = mode }
              ) {
                Text(
                  text = mode,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  color = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.padding(vertical = 6.dp),
                  textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
              }
            }
          }
        }

        if (errorMessage != null) {
          Text(
            text = errorMessage!!,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.error
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val amt = amountText.toDoubleOrNull()
          if (title.isBlank()) {
            errorMessage = "Please enter an expense title"
          } else if (amt == null || amt <= 0.0) {
            errorMessage = "Please enter a valid amount"
          } else {
            onSave(title, amt, selectedCategory, selectedPaymentMode, isEssential)
          }
        }
      ) {
        Text("Save Log")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}
