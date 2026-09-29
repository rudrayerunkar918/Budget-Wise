package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import com.example.data.api.MutualFundCatalog
import com.example.data.model.MutualFundSipEntity
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.platform.LocalContext
import com.example.ui.screens.showDatePickerDialog
import com.example.ui.screens.showTimePickerDialog
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.data.model.AccountEntity
import com.example.data.model.ExpenseCategory
import com.example.data.model.ExpenseEntity
import com.example.data.model.NotificationLogEntity
import com.example.data.model.SavingsGoalEntity
import com.example.data.model.SubscriptionEntity
import com.example.data.model.BudgetEntity
import com.example.data.model.RecurringFrequencyHelper
import com.example.data.model.TransactionShortcutEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

import com.example.ui.screens.AddTransactionScreen
import com.example.ui.screens.InbuiltCalculatorSheet

@Composable
fun AddTransactionDialog(
  availableAccounts: List<AccountEntity> = emptyList(),
  availableGoals: List<SavingsGoalEntity> = emptyList(),
  initialType: String = "EXPENSE",
  currencySymbol: String = "₹",
  onDismiss: () -> Unit,
  onConfirm: (
    title: String,
    amount: Double,
    category: ExpenseCategory,
    note: String,
    type: String,
    account: String,
    toAccount: String,
    timestamp: Long
  ) -> Unit
) {
  AddTransactionScreen(
    availableAccounts = availableAccounts,
    availableGoals = availableGoals,
    initialType = initialType,
    currencySymbol = currencySymbol,
    onDismiss = onDismiss,
    onConfirm = onConfirm
  )
}

// Backward compatibility alias for existing tests and components
@Composable
fun AddExpenseDialog(
  onDismiss: () -> Unit,
  onConfirm: (title: String, amount: Double, category: ExpenseCategory, note: String) -> Unit
) {
  AddTransactionDialog(
    initialType = "EXPENSE",
    onDismiss = onDismiss,
    onConfirm = { title, amount, category, note, _, _, _, _ ->
      onConfirm(title, amount, category, note)
    }
  )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditTransactionDialog(
  expense: ExpenseEntity,
  availableAccounts: List<AccountEntity> = emptyList(),
  currencySymbol: String = "₹",
  onDismiss: () -> Unit,
  onConfirm: (updatedExpense: ExpenseEntity) -> Unit,
  onDelete: (() -> Unit)? = null
) {
  var transactionType by remember { mutableStateOf(expense.type) }
  var title by remember { mutableStateOf(expense.title) }
  var amountStr by remember {
    mutableStateOf(
      if (expense.amount % 1.0 == 0.0) expense.amount.toLong().toString()
      else String.format(Locale.US, "%.2f", expense.amount)
    )
  }
  var selectedCategory by remember {
    mutableStateOf(ExpenseCategory.fromName(expense.category))
  }
  var selectedAccount by remember {
    mutableStateOf(expense.account.ifBlank { availableAccounts.firstOrNull()?.name ?: "Main Checking" })
  }
  var selectedToAccount by remember {
    mutableStateOf(expense.toAccount.ifBlank { availableAccounts.getOrNull(1)?.name ?: "Cash Wallet" })
  }
  var note by remember { mutableStateOf(expense.note) }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var showCalculatorSheet by remember { mutableStateOf(false) }

  val context = LocalContext.current
  var selectedTimestamp by remember { mutableStateOf(expense.timestamp) }
  val initialCal = remember(expense.timestamp) { Calendar.getInstance().apply { timeInMillis = expense.timestamp } }
  var selectedHour by remember { mutableStateOf(initialCal.get(Calendar.HOUR_OF_DAY)) }
  var selectedMinute by remember { mutableStateOf(initialCal.get(Calendar.MINUTE)) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Column {
        Text(
          text = "Edit Transaction",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))

        // 3-Way Segmented Control: [ Expense | Income | Transfer ]
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(modifier = Modifier.padding(4.dp)) {
            listOf("EXPENSE" to "Expense", "INCOME" to "Income", "TRANSFER" to "Transfer").forEach { (typeKey, typeLabel) ->
              val isSelected = transactionType == typeKey
              val tabColor = when {
                !isSelected -> Color.Transparent
                typeKey == "INCOME" -> Color(0xFF00B894)
                typeKey == "TRANSFER" -> Color(0xFF0984E3)
                else -> Color(0xFF1B664B)
              }
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = tabColor,
                border = BorderStroke(
                  1.dp,
                  if (isSelected) Color.Transparent else Color.Transparent
                ),
                contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                  .weight(1f)
                  .clickable {
                    transactionType = typeKey
                    if (typeKey == "INCOME" && !selectedCategory.isIncome) {
                      selectedCategory = ExpenseCategory.SALARY
                    } else if (typeKey == "EXPENSE" && selectedCategory.isIncome) {
                      selectedCategory = ExpenseCategory.FOOD
                    }
                  }
              ) {
                Text(
                  text = typeLabel,
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  modifier = Modifier.padding(vertical = 8.dp),
                  textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
              }
            }
          }
        }
      }
    },
    text = {
      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(max = 440.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Investment Details Card (Stock / Mutual Fund / SIP purchase breakdown)
        if (expense.isInvestmentTransaction || expense.stockSymbol != null || expense.category.equals("INVESTMENT", ignoreCase = true)) {
          item {
            Card(
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1B664B).copy(alpha = 0.08f)
              ),
              border = BorderStroke(1.dp, Color(0xFF1B664B).copy(alpha = 0.35f)),
              modifier = Modifier.fillMaxWidth().testTag("card_investment_transaction_detail")
            ) {
              Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                      shape = CircleShape,
                      color = Color(0xFF1B664B),
                      modifier = Modifier.size(24.dp)
                    ) {
                      Box(contentAlignment = Alignment.Center) {
                        Icon(
                          imageVector = Icons.AutoMirrored.Filled.ShowChart,
                          contentDescription = null,
                          tint = Color.White,
                          modifier = Modifier.size(14.dp)
                        )
                      }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      text = if (expense.assetType == "MUTUAL_FUND" || expense.title.contains("Fund", ignoreCase = true) || expense.title.contains("SIP", ignoreCase = true))
                        "MUTUAL FUND INVESTMENT" else "STOCK PURCHASE",
                      style = MaterialTheme.typography.labelMedium,
                      fontWeight = FontWeight.Bold,
                      color = Color(0xFF1B664B)
                    )
                  }
                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF1B664B).copy(alpha = 0.15f)
                  ) {
                    Text(
                      text = "DEBITED",
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.Bold,
                      color = Color(0xFF1B664B),
                      fontSize = 10.sp,
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                  }
                }

                HorizontalDivider(color = Color(0xFF1B664B).copy(alpha = 0.15f), thickness = 0.8.dp)

                // Asset details grid
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Column {
                    Text(
                      text = "Asset / Symbol",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      fontSize = 11.sp
                    )
                    Text(
                      text = expense.stockSymbol ?: expense.title.replace("Bought ", ""),
                      style = MaterialTheme.typography.titleSmall,
                      fontWeight = FontWeight.Bold
                    )
                  }

                  Column(horizontalAlignment = Alignment.End) {
                    Text(
                      text = "Bought At Price / NAV",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      fontSize = 11.sp
                    )
                    val price = expense.stockPrice ?: (if (expense.stockShares != null && expense.stockShares > 0) expense.amount / expense.stockShares else 0.0)
                    Text(
                      text = if (price > 0) "$currencySymbol${String.format(Locale.US, "%,.2f", price)}" else "$currencySymbol${String.format(Locale.US, "%,.2f", expense.amount)}",
                      style = MaterialTheme.typography.titleSmall,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.primary
                    )
                  }
                }

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Column {
                    Text(
                      text = if (expense.assetType == "MUTUAL_FUND") "Units Purchased" else "Shares Bought",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      fontSize = 11.sp
                    )
                    val unitsText = expense.stockShares?.let { 
                      if (it % 1.0 == 0.0) "${it.toInt()}" else String.format(Locale.US, "%.3f", it)
                    } ?: "1"
                    Text(
                      text = "$unitsText ${if (expense.assetType == "MUTUAL_FUND") "units" else "shares"}",
                      style = MaterialTheme.typography.bodyMedium,
                      fontWeight = FontWeight.SemiBold
                    )
                  }

                  Column(horizontalAlignment = Alignment.End) {
                    Text(
                      text = "Debited From Account",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      fontSize = 11.sp
                    )
                    Surface(
                      shape = RoundedCornerShape(6.dp),
                      color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                      Text(
                        text = "💳 ${expense.account}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                      )
                    }
                  }
                }

                // Date & Time when bought
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "Purchase Date & Time:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                  )
                  Text(
                    text = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(Date(expense.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium
                  )
                }
              }
            }
          }
        }

        item {
          OutlinedTextField(
            value = amountStr,
            onValueChange = {
              amountStr = it
              errorMessage = null
            },
            label = { Text("Amount ($currencySymbol)") },
            placeholder = { Text("0.00") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            prefix = { Text("$currencySymbol ") },
            trailingIcon = {
              IconButton(onClick = { showCalculatorSheet = true }) {
                Icon(
                  imageVector = Icons.Default.Tune,
                  contentDescription = "Open Inbuilt Calculator",
                  tint = MaterialTheme.colorScheme.primary
                )
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("edit_expense_amount_input")
          )
        }

        item {
          OutlinedTextField(
            value = title,
            onValueChange = {
              title = it
              errorMessage = null
            },
            label = {
              Text(
                when (transactionType) {
                  "INCOME" -> "Income Source / Employer"
                  "TRANSFER" -> "Transfer Description"
                  else -> "Merchant / Payee"
                }
              )
            },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("edit_expense_title_input")
          )
        }

        // Date & Time Picker Item
        item {
          Text(
            text = "Date & Time",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(4.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            val editDateLabel = SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date(selectedTimestamp))
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
              modifier = Modifier
                .weight(1f)
                .clickable {
                  showDatePickerDialog(context, selectedTimestamp) { pickedMs ->
                    selectedTimestamp = pickedMs
                  }
                }
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.CalendarToday,
                  contentDescription = "Edit Date",
                  modifier = Modifier.size(16.dp),
                  tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = editDateLabel,
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Medium
                )
              }
            }

            val editTimeLabel = String.format(Locale.US, "%02d:%02d", selectedHour, selectedMinute)
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
              modifier = Modifier
                .clickable {
                  showTimePickerDialog(context, selectedHour, selectedMinute) { hour, min ->
                    selectedHour = hour
                    selectedMinute = min
                  }
                }
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.Schedule,
                  contentDescription = "Edit Time",
                  modifier = Modifier.size(16.dp),
                  tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = editTimeLabel,
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Medium
                )
              }
            }
          }
        }

        // Account Selector Chips
        item {
          Text(
            text = if (transactionType == "TRANSFER") "From Account" else "Account / Wallet",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(4.dp))
          FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            val accountsList = if (availableAccounts.isNotEmpty()) availableAccounts.map { it.name }
            else listOf("Main Checking", "Cash Wallet", "High-Yield Savings", "Sapphire Credit Card")

            accountsList.forEach { accName ->
              val isSelected = accName == selectedAccount
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) Color(0xFF1B664B) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                border = BorderStroke(
                  1.dp,
                  if (isSelected) Color(0xFF1B664B) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                ),
                contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.clickable { selectedAccount = accName }
              ) {
                Text(
                  text = accName,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                )
              }
            }
          }
        }

        if (transactionType == "TRANSFER") {
          item {
            Text(
              text = "To Destination Account",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            FlowRow(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              val accountsList = if (availableAccounts.isNotEmpty()) availableAccounts.map { it.name }
              else listOf("Main Checking", "Cash Wallet", "High-Yield Savings", "Sapphire Credit Card")

              accountsList.forEach { accName ->
                val isSelected = accName == selectedToAccount
                Surface(
                  shape = RoundedCornerShape(10.dp),
                  color = if (isSelected) Color(0xFF0984E3) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                  border = BorderStroke(
                    1.dp,
                    if (isSelected) Color(0xFF0984E3) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                  ),
                  contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                  modifier = Modifier.clickable { selectedToAccount = accName }
                ) {
                  Text(
                    text = accName,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                  )
                }
              }
            }
          }
        } else {
          // Category Selector
          item {
            Text(
              text = "Category",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            val categoriesToShow = if (transactionType == "INCOME") ExpenseCategory.incomeCategories()
            else ExpenseCategory.expenseCategories()

            FlowRow(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              categoriesToShow.forEach { cat ->
                val isSelected = cat == selectedCategory
                Surface(
                  shape = RoundedCornerShape(10.dp),
                  color = if (isSelected) cat.color else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                  border = BorderStroke(
                    1.dp,
                    if (isSelected) cat.color else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                  ),
                  contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                  modifier = Modifier
                    .clickable { selectedCategory = cat }
                    .testTag("edit_category_chip_${cat.name.lowercase()}")
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(
                      imageVector = cat.icon,
                      contentDescription = cat.displayName,
                      modifier = Modifier.size(16.dp),
                      tint = if (isSelected) Color.White else cat.color
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = cat.displayName,
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                  }
                }
              }
            }
          }
        }

        item {
          OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Note (Optional)") },
            placeholder = { Text("Additional notes...") },
            maxLines = 2,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("edit_expense_note_input")
          )
        }

        if (errorMessage != null) {
          item {
            Text(
              text = errorMessage ?: "",
              color = MaterialTheme.colorScheme.error,
              style = MaterialTheme.typography.bodySmall
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val parsedAmount = amountStr.toDoubleOrNull()
          val finalTitle = title.ifBlank {
            if (transactionType == "TRANSFER") "Transfer to $selectedToAccount"
            else selectedCategory.displayName
          }
          if (parsedAmount == null || parsedAmount <= 0) {
            errorMessage = "Please enter a valid amount greater than 0"
          } else {
            val finalCal = Calendar.getInstance().apply {
              timeInMillis = selectedTimestamp
              set(Calendar.HOUR_OF_DAY, selectedHour)
              set(Calendar.MINUTE, selectedMinute)
              set(Calendar.SECOND, 0)
              set(Calendar.MILLISECOND, 0)
            }
            val updated = expense.copy(
              title = finalTitle,
              amount = parsedAmount,
              category = selectedCategory.name,
              type = transactionType,
              account = selectedAccount,
              toAccount = if (transactionType == "TRANSFER") selectedToAccount else "",
              note = note,
              timestamp = finalCal.timeInMillis
            )
            onConfirm(updated)
          }
        },
        colors = ButtonDefaults.buttonColors(
          containerColor = when (transactionType) {
            "INCOME" -> Color(0xFF00B894)
            "TRANSFER" -> Color(0xFF0984E3)
            else -> Color(0xFF1B664B)
          }
        ),
        modifier = Modifier.testTag("save_edited_expense_button")
      ) {
        Text("Save Changes")
      }
    },
    dismissButton = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        if (onDelete != null) {
          TextButton(
            onClick = onDelete,
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
          ) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Delete")
          }
          Spacer(modifier = Modifier.width(4.dp))
        }
        TextButton(onClick = onDismiss) {
          Text("Cancel")
        }
      }
    }
  )

  if (showCalculatorSheet) {
    InbuiltCalculatorSheet(
      currencySymbol = currencySymbol,
      initialAmount = amountStr.toDoubleOrNull() ?: 0.0,
      selectedAccount = selectedAccount,
      accountsList = if (availableAccounts.isNotEmpty()) availableAccounts.map { it.name } else listOf("Bank", "Cash"),
      onAccountSelected = { selectedAccount = it },
      onDismiss = { showCalculatorSheet = false },
      onSetAmount = { evaluated ->
        amountStr = if (evaluated % 1.0 == 0.0) evaluated.toLong().toString() else String.format(Locale.US, "%.2f", evaluated)
        showCalculatorSheet = false
      }
    )
  }
}

@Composable
fun FrequencySelector(
  selectedCycle: String,
  amount: Double?,
  currencySymbol: String,
  onSelect: (String) -> Unit
) {
  Column(modifier = Modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Billing Frequency (${RecurringFrequencyHelper.FREQUENCIES.size} Options)",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      if (amount != null && amount > 0) {
        val monthlyEst = RecurringFrequencyHelper.getMonthlyCost(amount, selectedCycle)
        Text(
          text = "~$currencySymbol${String.format(Locale.US, "%.1f", monthlyEst)}/mo",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF1B664B)
        )
      }
    }
    Spacer(modifier = Modifier.height(6.dp))

    LazyRow(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      items(RecurringFrequencyHelper.FREQUENCIES) { freq ->
        val isSelected = selectedCycle.equals(freq.id, ignoreCase = true) || selectedCycle.equals(freq.displayName, ignoreCase = true)
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = if (isSelected) Color(0xFF1B664B) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
          modifier = Modifier
            .clickable { onSelect(freq.id) }
            .testTag("freq_chip_${freq.id.lowercase().replace(" ", "_")}")
        ) {
          Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = freq.shortLabel,
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = freq.displayName,
              style = MaterialTheme.typography.labelSmall,
              color = if (isSelected) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }

    val currentItem = RecurringFrequencyHelper.FREQUENCIES.firstOrNull {
      it.id.equals(selectedCycle, ignoreCase = true) || it.displayName.equals(selectedCycle, ignoreCase = true)
    }
    if (currentItem != null) {
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "ℹ️ ${currentItem.description}",
        style = MaterialTheme.typography.bodySmall,
        color = Color(0xFF1B664B)
      )
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddSubscriptionDialog(
  availableAccounts: List<AccountEntity> = emptyList(),
  currencySymbol: String = "₹",
  onDismiss: () -> Unit,
  onConfirm: (
    title: String,
    amount: Double,
    billingCycle: String,
    category: ExpenseCategory,
    account: String,
    nextDueDays: Int,
    note: String
  ) -> Unit
) {
  var title by remember { mutableStateOf("") }
  var amountStr by remember { mutableStateOf("") }
  var billingCycle by remember { mutableStateOf("Monthly") }
  var selectedCategory by remember { mutableStateOf(ExpenseCategory.ENTERTAINMENT) }
  var selectedAccount by remember {
    mutableStateOf(availableAccounts.firstOrNull()?.name ?: "Main Checking")
  }
  var nextDueDays by remember { mutableStateOf(7) }
  var note by remember { mutableStateOf("") }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Repeat,
          contentDescription = null,
          tint = Color(0xFF1B664B)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "New Subscription / Bill",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        OutlinedTextField(
          value = title,
          onValueChange = {
            title = it
            errorMessage = null
          },
          label = { Text("Service / Bill Name") },
          placeholder = { Text("e.g. Netflix, Spotify, Gym, Rent") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = amountStr,
          onValueChange = {
            amountStr = it
            errorMessage = null
          },
          label = { Text("Billing Amount ($currencySymbol)") },
          placeholder = { Text("0.00") },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          prefix = { Text("$currencySymbol ") },
          modifier = Modifier.fillMaxWidth()
        )

        // Billing Cycle Chips (10+ frequencies supported)
        FrequencySelector(
          selectedCycle = billingCycle,
          amount = amountStr.toDoubleOrNull(),
          currencySymbol = currencySymbol,
          onSelect = { billingCycle = it }
        )

        // Due In
        Text(
          text = "First Payment Due",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf(3 to "3 days", 7 to "1 week", 14 to "2 weeks", 30 to "1 month").forEach { (days, label) ->
            val isSelected = nextDueDays == days
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
              modifier = Modifier
                .weight(1f)
                .clickable { nextDueDays = days }
            ) {
              Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.padding(vertical = 8.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
              )
            }
          }
        }

        if (errorMessage != null) {
          Text(
            text = errorMessage ?: "",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val parsedAmount = amountStr.toDoubleOrNull()
          if (title.isBlank()) {
            errorMessage = "Please enter a subscription name"
          } else if (parsedAmount == null || parsedAmount <= 0) {
            errorMessage = "Please enter a valid amount"
          } else {
            onConfirm(
              title.trim(),
              parsedAmount,
              billingCycle,
              selectedCategory,
              selectedAccount,
              nextDueDays,
              note.trim()
            )
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B664B))
      ) {
        Text("Save Subscription")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditSubscriptionDialog(
  subscription: SubscriptionEntity,
  availableAccounts: List<AccountEntity> = emptyList(),
  currencySymbol: String = "₹",
  onDismiss: () -> Unit,
  onConfirm: (updatedSubscription: SubscriptionEntity) -> Unit,
  onDelete: (() -> Unit)? = null
) {
  var title by remember { mutableStateOf(subscription.title) }
  var amountStr by remember {
    mutableStateOf(
      if (subscription.amount % 1.0 == 0.0) subscription.amount.toLong().toString()
      else String.format(Locale.US, "%.2f", subscription.amount)
    )
  }
  var billingCycle by remember { mutableStateOf(subscription.billingCycle) }
  var selectedCategory by remember { mutableStateOf(ExpenseCategory.fromName(subscription.category)) }
  var selectedAccount by remember {
    mutableStateOf(subscription.account.ifBlank { availableAccounts.firstOrNull()?.name ?: "Main Checking" })
  }
  var note by remember { mutableStateOf(subscription.note) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "Edit Subscription",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        OutlinedTextField(
          value = title,
          onValueChange = {
            title = it
            errorMessage = null
          },
          label = { Text("Subscription Title") },
          placeholder = { Text("e.g. Netflix, Spotify, Gym") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = amountStr,
          onValueChange = {
            amountStr = it
            errorMessage = null
          },
          label = { Text("Billing Amount ($currencySymbol)") },
          placeholder = { Text("15.99") },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          prefix = { Text("$currencySymbol ") },
          modifier = Modifier.fillMaxWidth()
        )

        // Billing Cycle (10+ frequencies supported)
        FrequencySelector(
          selectedCycle = billingCycle,
          amount = amountStr.toDoubleOrNull(),
          currencySymbol = currencySymbol,
          onSelect = { billingCycle = it }
        )

        // Account Chips
        Text(
          text = "Charge to Account",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          val accountsList = if (availableAccounts.isNotEmpty()) availableAccounts.map { it.name }
          else listOf("Main Checking", "Cash Wallet", "High-Yield Savings", "Sapphire Credit Card")

          accountsList.forEach { accName ->
            val isSelected = accName == selectedAccount
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = if (isSelected) Color(0xFF1B664B) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.clickable { selectedAccount = accName }
            ) {
              Text(
                text = accName,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
              )
            }
          }
        }

        OutlinedTextField(
          value = note,
          onValueChange = { note = it },
          label = { Text("Note (Optional)") },
          placeholder = { Text("Family plan, auto-renew, etc.") },
          modifier = Modifier.fillMaxWidth()
        )

        if (errorMessage != null) {
          Text(
            text = errorMessage ?: "",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val parsedAmount = amountStr.toDoubleOrNull()
          if (title.isBlank()) {
            errorMessage = "Please enter a subscription name"
          } else if (parsedAmount == null || parsedAmount <= 0) {
            errorMessage = "Please enter a valid amount"
          } else {
            val updated = subscription.copy(
              title = title.trim(),
              amount = parsedAmount,
              billingCycle = billingCycle,
              category = selectedCategory.name,
              account = selectedAccount,
              note = note.trim()
            )
            onConfirm(updated)
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B664B))
      ) {
        Text("Save Changes")
      }
    },
    dismissButton = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        if (onDelete != null) {
          TextButton(
            onClick = onDelete,
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
          ) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Delete")
          }
          Spacer(modifier = Modifier.width(4.dp))
        }
        TextButton(onClick = onDismiss) {
          Text("Cancel")
        }
      }
    }
  )
}

@Composable
fun AddAccountDialog(
  currencySymbol: String = "₹",
  onDismiss: () -> Unit,
  onConfirm: (name: String, type: String, startingBalance: Double, colorHex: Long) -> Unit
) {
  var name by remember { mutableStateOf("") }
  var accountType by remember { mutableStateOf("CHECKING") }
  var balanceStr by remember { mutableStateOf("") }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "Add Account / Wallet",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        OutlinedTextField(
          value = name,
          onValueChange = {
            name = it
            errorMessage = null
          },
          label = { Text("Account Name") },
          placeholder = { Text("e.g. Primary Checking, Crypto Wallet") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        // Account Type
        Text(
          text = "Account Type",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf(
            "CHECKING" to "Checking",
            "CASH" to "Cash",
            "SAVINGS" to "Savings",
            "CREDIT" to "Credit Card"
          ).forEach { (typeKey, label) ->
            val isSelected = accountType == typeKey
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = if (isSelected) Color(0xFF1B664B) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
              modifier = Modifier
                .weight(1f)
                .clickable { accountType = typeKey }
            ) {
              Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.padding(vertical = 8.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
              )
            }
          }
        }

        OutlinedTextField(
          value = balanceStr,
          onValueChange = {
            balanceStr = it
            errorMessage = null
          },
          label = { Text("Current Balance ($currencySymbol)") },
          placeholder = { Text("0.00") },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          prefix = { Text("$currencySymbol ") },
          modifier = Modifier.fillMaxWidth()
        )

        if (errorMessage != null) {
          Text(
            text = errorMessage ?: "",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val bal = balanceStr.toDoubleOrNull() ?: 0.0
          if (name.isBlank()) {
            errorMessage = "Please enter an account name"
          } else {
            val color = when (accountType) {
              "CASH" -> 0xFFE09A26L
              "SAVINGS" -> 0xFF2E86DEL
              "CREDIT" -> 0xFFE74C3CL
              else -> 0xFF1B664BL
            }
            onConfirm(name.trim(), accountType, bal, color)
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B664B))
      ) {
        Text("Create Account")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}

@Composable
fun EditAccountDialog(
  account: AccountEntity,
  currencySymbol: String = "₹",
  onDismiss: () -> Unit,
  onConfirm: (AccountEntity) -> Unit,
  onDelete: (AccountEntity) -> Unit
) {
  var name by remember { mutableStateOf(account.name) }
  var accountType by remember { mutableStateOf(account.type) }
  var balanceStr by remember { mutableStateOf(String.format(java.util.Locale.US, "%.2f", account.balance)) }
  var selectedColorHex by remember { mutableStateOf(account.colorHex) }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var showDeleteConfirmation by remember { mutableStateOf(false) }

  if (showDeleteConfirmation) {
    AlertDialog(
      onDismissRequest = { showDeleteConfirmation = false },
      title = { Text("Delete Account?") },
      text = {
        Text("Are you sure you want to delete \"${account.name}\"? Recorded transactions will remain in your history, but this account will be removed.")
      },
      confirmButton = {
        Button(
          onClick = {
            showDeleteConfirmation = false
            onDelete(account)
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
          Text("Delete")
        }
      },
      dismissButton = {
        TextButton(onClick = { showDeleteConfirmation = false }) {
          Text("Cancel")
        }
      }
    )
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Edit Account",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )
        IconButton(
          onClick = { showDeleteConfirmation = true },
          modifier = Modifier.testTag("btn_delete_account_dialog")
        ) {
          Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Delete Account",
            tint = MaterialTheme.colorScheme.error
          )
        }
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        OutlinedTextField(
          value = name,
          onValueChange = {
            name = it
            errorMessage = null
          },
          label = { Text("Account Name") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        // Account Type
        Text(
          text = "Account Type",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          listOf(
            "CHECKING" to "Checking",
            "CASH" to "Cash",
            "SAVINGS" to "Savings",
            "CREDIT" to "Credit",
            "INVESTMENT" to "Invest"
          ).forEach { (typeKey, label) ->
            val isSelected = accountType == typeKey
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = if (isSelected) Color(0xFF1B664B) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
              modifier = Modifier
                .weight(1f)
                .clickable {
                  accountType = typeKey
                  if (selectedColorHex == account.colorHex) {
                    selectedColorHex = when (typeKey) {
                      "CASH" -> 0xFFE09A26L
                      "SAVINGS" -> 0xFF2E86DEL
                      "CREDIT" -> 0xFFE74C3CL
                      "INVESTMENT" -> 0xFF9B59B6L
                      else -> 0xFF1B664BL
                    }
                  }
                }
            ) {
              Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.padding(vertical = 8.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
              )
            }
          }
        }

        OutlinedTextField(
          value = balanceStr,
          onValueChange = {
            balanceStr = it
            errorMessage = null
          },
          label = { Text("Current Balance ($currencySymbol)") },
          placeholder = { Text("0.00") },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          prefix = { Text("$currencySymbol ") },
          modifier = Modifier.fillMaxWidth()
        )

        // Color Choices
        Text(
          text = "Accent Color",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          listOf(
            0xFF1B664BL, // Emerald Green
            0xFF2E86DEL, // Ocean Blue
            0xFFE09A26L, // Gold Amber
            0xFFE74C3CL, // Crimson
            0xFF9B59B6L, // Royal Purple
            0xFF16A085L  // Mountain Teal
          ).forEach { colorHex ->
            val isSelected = selectedColorHex == colorHex
            Box(
              modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(colorHex))
                .clickable { selectedColorHex = colorHex },
              contentAlignment = Alignment.Center
            ) {
              if (isSelected) {
                Box(
                  modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                )
              }
            }
          }
        }

        if (errorMessage != null) {
          Text(
            text = errorMessage ?: "",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val bal = balanceStr.toDoubleOrNull()
          if (name.isBlank()) {
            errorMessage = "Please enter an account name"
          } else if (bal == null) {
            errorMessage = "Please enter a valid numeric balance"
          } else {
            onConfirm(
              account.copy(
                name = name.trim(),
                type = accountType,
                balance = bal,
                colorHex = selectedColorHex
              )
            )
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B664B))
      ) {
        Text("Save Changes")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}

private fun formatDialogAmount(amount: Double): String {
  return if (amount % 1.0 == 0.0) {
    amount.toLong().toString()
  } else {
    String.format(Locale.US, "%.2f", amount)
  }
}

@Composable
fun SetBudgetDialog(
  initialCategory: String = "TOTAL",
  initialLimit: Double? = null,
  categorySum: Double = 0.0,
  currencySymbol: String = "₹",
  onDismiss: () -> Unit,
  onConfirm: (category: String, limit: Double) -> Unit
) {
  var limitStr by remember { mutableStateOf(initialLimit?.let { formatDialogAmount(it) } ?: "") }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = if (initialLimit != null) "Overall Monthly Budget" else "Set Overall Budget",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Text(
          text = "Set your total overall monthly budget cap. Category budgets can be matched or scaled to equal this amount.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
          value = limitStr,
          onValueChange = {
            limitStr = it
            errorMessage = null
          },
          label = { Text("Overall Budget ($currencySymbol)") },
          placeholder = { Text("e.g. 50000") },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          prefix = { Text("$currencySymbol ") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("budget_limit_input")
        )

        if (categorySum > 0) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF1B664B).copy(alpha = 0.1f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(
                    text = "Current Categories Sum",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                  Text(
                    text = "$currencySymbol${formatDialogAmount(categorySum)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B664B)
                  )
                }
                Button(
                  onClick = { limitStr = formatDialogAmount(categorySum) },
                  shape = RoundedCornerShape(8.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B664B)),
                  contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                  Text("Match Sum", style = MaterialTheme.typography.labelSmall)
                }
              }
            }
          }
        }

        if (errorMessage != null) {
          Text(
            text = errorMessage ?: "",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val parsed = limitStr.toDoubleOrNull()
          if (parsed == null || parsed <= 0) {
            errorMessage = "Please enter a valid budget amount"
          } else {
            onConfirm("TOTAL", parsed)
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B664B)),
        modifier = Modifier.testTag("save_budget_button")
      ) {
        Text("Save Budget")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryBudgetDialog(
  initialCategory: String = "",
  initialLimit: Double? = null,
  overallBudget: Double? = null,
  otherCategoriesSum: Double = 0.0,
  currencySymbol: String = "₹",
  onDismiss: () -> Unit,
  onConfirm: (category: String, limit: Double, syncOverallToNewSum: Boolean) -> Unit,
  onDelete: (() -> Unit)? = null
) {
  val isEditing = initialLimit != null
  var isCustomCategory by remember {
    mutableStateOf(initialCategory.isNotEmpty() && ExpenseCategory.entries.none { it.name.equals(initialCategory, ignoreCase = true) })
  }
  var selectedPresetCategory by remember {
    mutableStateOf(
      if (!isCustomCategory && initialCategory.isNotEmpty()) initialCategory else ExpenseCategory.FOOD.name
    )
  }
  var customCategoryName by remember {
    mutableStateOf(if (isCustomCategory) initialCategory else "")
  }
  var limitStr by remember { mutableStateOf(initialLimit?.let { formatDialogAmount(it) } ?: "") }
  var syncOverall by remember { mutableStateOf(true) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  val unallocatedRemainder = if (overallBudget != null) {
    (overallBudget - otherCategoriesSum).coerceAtLeast(0.0)
  } else 0.0

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = if (isEditing) "Edit Category Budget" else "New Category Budget",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Preset vs Custom Category Tab Selector
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (!isCustomCategory) Color(0xFF1B664B) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            contentColor = if (!isCustomCategory) Color.White else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
              .weight(1f)
              .clickable { isCustomCategory = false }
          ) {
            Text(
              text = "Preset Category",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.SemiBold,
              modifier = Modifier.padding(vertical = 8.dp),
              textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
          }

          Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (isCustomCategory) Color(0xFF1B664B) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            contentColor = if (isCustomCategory) Color.White else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
              .weight(1f)
              .clickable { isCustomCategory = true }
          ) {
            Text(
              text = "Custom Category",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.SemiBold,
              modifier = Modifier.padding(vertical = 8.dp),
              textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
          }
        }

        if (isCustomCategory) {
          OutlinedTextField(
            value = customCategoryName,
            onValueChange = {
              customCategoryName = it
              errorMessage = null
            },
            label = { Text("Custom Category Name") },
            placeholder = { Text("e.g. Gym, Pet Care, Electronics, Gifts") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
        } else {
          FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            ExpenseCategory.entries.filter { !it.isIncome }.forEach { cat ->
              val isSelected = selectedPresetCategory.equals(cat.name, ignoreCase = true)
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) Color(0xFF1B664B) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.clickable { selectedPresetCategory = cat.name }
              ) {
                Text(
                  text = "${cat.emoji} ${cat.displayName}",
                  style = MaterialTheme.typography.labelSmall,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                )
              }
            }
          }
        }

        // Limit input
        OutlinedTextField(
          value = limitStr,
          onValueChange = {
            limitStr = it
            errorMessage = null
          },
          label = { Text("Monthly Limit ($currencySymbol)") },
          placeholder = { Text("e.g. 5000") },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          prefix = { Text("$currencySymbol ") },
          modifier = Modifier.fillMaxWidth()
        )

        // Sum & Overall Budget Balance Helper
        if (overallBudget != null && overallBudget > 0) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text("Overall Monthly Budget:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("$currencySymbol${formatDialogAmount(overallBudget)}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
              }
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text("Other Categories Sum:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("$currencySymbol${formatDialogAmount(otherCategoriesSum)}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
              }
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text("Unallocated Balance:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                  text = "$currencySymbol${formatDialogAmount(unallocatedRemainder)}",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = if (unallocatedRemainder > 0) Color(0xFF1B664B) else MaterialTheme.colorScheme.error
                )
              }

              if (unallocatedRemainder > 0) {
                Spacer(modifier = Modifier.height(2.dp))
                OutlinedButton(
                  onClick = { limitStr = formatDialogAmount(unallocatedRemainder) },
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Text(
                    text = "Fill Unallocated Remainder ($currencySymbol${formatDialogAmount(unallocatedRemainder)})",
                    style = MaterialTheme.typography.labelSmall
                  )
                }
              }
            }
          }
        }

        // Sum synchronization checkbox
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { syncOverall = !syncOverall },
          verticalAlignment = Alignment.CenterVertically
        ) {
          Checkbox(
            checked = syncOverall,
            onCheckedChange = { syncOverall = it },
            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF1B664B))
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "Ensure overall budget equals sum of category budgets",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        if (errorMessage != null) {
          Text(
            text = errorMessage ?: "",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val category = if (isCustomCategory) customCategoryName.trim() else selectedPresetCategory
          val parsed = limitStr.toDoubleOrNull()
          if (category.isBlank()) {
            errorMessage = "Please choose or enter a category name"
          } else if (parsed == null || parsed <= 0) {
            errorMessage = "Please enter a valid monthly limit amount"
          } else {
            onConfirm(category, parsed, syncOverall)
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B664B))
      ) {
        Text("Save Category Budget")
      }
    },
    dismissButton = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        if (onDelete != null) {
          TextButton(
            onClick = onDelete,
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
          ) {
            Text("Delete")
          }
          Spacer(modifier = Modifier.width(4.dp))
        }
        TextButton(onClick = onDismiss) {
          Text("Cancel")
        }
      }
    }
  )
}

@Composable
fun BudgetAllocationPlannerDialog(
  overallBudget: Double?,
  categoryBudgets: List<BudgetEntity>,
  currencySymbol: String = "₹",
  onDismiss: () -> Unit,
  onSyncOverallToSum: () -> Unit,
  onScaleCategoriesToOverall: (Double) -> Unit,
  onEditCategoryBudget: (BudgetEntity) -> Unit,
  onAddCategoryBudget: () -> Unit
) {
  val categorySum = categoryBudgets.sumOf { it.monthlyLimit }
  val targetOverall = overallBudget ?: 0.0
  val diff = categorySum - targetOverall
  val isBalanced = kotlin.math.abs(diff) < 0.01

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Budget Allocator", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        IconButton(onClick = onAddCategoryBudget) {
          Icon(Icons.Default.Add, contentDescription = "Add Category Budget", tint = Color(0xFF1B664B))
        }
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Summary Card
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
        ) {
          Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("Overall Monthly Budget", style = MaterialTheme.typography.labelMedium)
              Text("$currencySymbol${formatDialogAmount(targetOverall)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("Categories Sum", style = MaterialTheme.typography.labelMedium)
              Text("$currencySymbol${formatDialogAmount(categorySum)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1B664B))
            }

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (isBalanced) Color(0xFF1B664B).copy(alpha = 0.15f)
              else if (diff > 0) MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
              else Color(0xFFE09A26).copy(alpha = 0.15f),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = if (isBalanced) "✓ Perfect Balance: Category sum equals overall budget!"
                else if (diff > 0) "⚠️ Exceeds overall budget by $currencySymbol${formatDialogAmount(diff)}"
                else "ℹ️ $currencySymbol${formatDialogAmount(-diff)} unallocated remaining",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (isBalanced) Color(0xFF1B664B)
                else if (diff > 0) MaterialTheme.colorScheme.error
                else Color(0xFFE09A26),
                modifier = Modifier.padding(8.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
              )
            }
          }
        }

        // Quick Balancing Actions
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Button(
            onClick = {
              onSyncOverallToSum()
              onDismiss()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B664B)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Set Overall Budget = Sum ($currencySymbol${formatDialogAmount(categorySum)})", style = MaterialTheme.typography.labelMedium)
          }

          if (targetOverall > 0) {
            OutlinedButton(
              onClick = {
                onScaleCategoriesToOverall(targetOverall)
                onDismiss()
              },
              modifier = Modifier.fillMaxWidth()
            ) {
              Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Scale Categories to Overall ($currencySymbol${formatDialogAmount(targetOverall)})", style = MaterialTheme.typography.labelMedium)
            }
          }
        }

        // Categories List
        Text("Current Category Budgets (${categoryBudgets.size})", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        if (categoryBudgets.isEmpty()) {
          Text("No category budgets set yet. Tap + to add one.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
          categoryBudgets.forEach { budget ->
            val percentOfTotal = if (categorySum > 0) (budget.monthlyLimit / categorySum * 100) else 0.0
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = MaterialTheme.colorScheme.surface,
              shadowElevation = 1.dp,
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  onDismiss()
                  onEditCategoryBudget(budget)
                }
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(budget.category, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                  Text(
                    text = String.format(Locale.US, "%.1f%% of category sum", percentOfTotal),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = "$currencySymbol${formatDialogAmount(budget.monthlyLimit)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B664B)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B664B))) {
        Text("Done")
      }
    }
  )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditShortcutDialog(
  shortcut: TransactionShortcutEntity? = null,
  availableAccounts: List<AccountEntity> = emptyList(),
  currencySymbol: String = "₹",
  onDismiss: () -> Unit,
  onSave: (title: String, amount: Double, category: String, type: String, account: String, iconEmoji: String) -> Unit,
  onDelete: (() -> Unit)? = null
) {
  val isEditing = shortcut != null
  var title by remember { mutableStateOf(shortcut?.title ?: "") }
  var amountStr by remember { mutableStateOf(shortcut?.let { formatDialogAmount(it.amount) } ?: "") }
  var selectedCategory by remember { mutableStateOf(shortcut?.category ?: ExpenseCategory.FOOD.name) }
  var selectedType by remember { mutableStateOf(shortcut?.type ?: "EXPENSE") }
  var selectedAccount by remember {
    mutableStateOf(shortcut?.account ?: availableAccounts.firstOrNull()?.name ?: "Main Checking")
  }
  var selectedEmoji by remember { mutableStateOf(shortcut?.iconEmoji ?: "☕") }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  val emojiOptions = listOf(
    "☕", "🍔", "🚌", "🛒", "⛽", "🍕", "💻", "🎬", "🏋️", "💊", "⚡", "🎁", "💵", "🚕", "🍿", "👕", "💡", "🏷️"
  )

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = if (isEditing) "Edit Quick Shortcut" else "New Quick Shortcut",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        OutlinedTextField(
          value = title,
          onValueChange = {
            title = it
            errorMessage = null
          },
          label = { Text("Shortcut Name") },
          placeholder = { Text("e.g. Morning Coffee, Metro Ticket") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = amountStr,
          onValueChange = {
            amountStr = it
            errorMessage = null
          },
          label = { Text("Amount ($currencySymbol)") },
          placeholder = { Text("0.00") },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          prefix = { Text("$currencySymbol ") },
          modifier = Modifier.fillMaxWidth()
        )

        // Emoji Selector
        Text("Choose Icon Emoji", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        LazyRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(emojiOptions) { emoji ->
            val isSelected = selectedEmoji == emoji
            Surface(
              shape = CircleShape,
              color = if (isSelected) Color(0xFF1B664B) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
              modifier = Modifier
                .size(40.dp)
                .clickable { selectedEmoji = emoji }
            ) {
              Box(contentAlignment = Alignment.Center) {
                Text(text = emoji, style = MaterialTheme.typography.titleMedium)
              }
            }
          }
        }

        // Transaction Type
        Text("Transaction Type", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf("EXPENSE" to "Expense", "INCOME" to "Income").forEach { (typeKey, label) ->
            val isSelected = selectedType == typeKey
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = if (isSelected) Color(0xFF1B664B) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
              modifier = Modifier
                .weight(1f)
                .clickable { selectedType = typeKey }
            ) {
              Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.padding(vertical = 8.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
              )
            }
          }
        }

        // Category Chips
        Text("Category", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          ExpenseCategory.entries.filter { if (selectedType == "EXPENSE") !it.isIncome else it.isIncome }.forEach { cat ->
            val isSelected = selectedCategory.equals(cat.name, ignoreCase = true)
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (isSelected) Color(0xFF1B664B) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
              contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.clickable { selectedCategory = cat.name }
            ) {
              Text(
                text = "${cat.emoji} ${cat.displayName}",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
              )
            }
          }
        }

        // Account Chips
        Text("Account", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          val accountsList = if (availableAccounts.isNotEmpty()) availableAccounts.map { it.name }
          else listOf("Main Checking", "Cash Wallet", "High-Yield Savings", "Sapphire Credit Card")
          accountsList.forEach { accName ->
            val isSelected = selectedAccount == accName
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (isSelected) Color(0xFF1B664B) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
              contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.clickable { selectedAccount = accName }
            ) {
              Text(
                text = accName,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
              )
            }
          }
        }

        if (errorMessage != null) {
          Text(
            text = errorMessage ?: "",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val amt = amountStr.toDoubleOrNull()
          if (title.isBlank()) {
            errorMessage = "Please enter a shortcut name"
          } else if (amt == null || amt <= 0) {
            errorMessage = "Please enter a valid amount"
          } else {
            onSave(title.trim(), amt, selectedCategory, selectedType, selectedAccount, selectedEmoji)
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B664B))
      ) {
        Text(if (isEditing) "Save Changes" else "Create Shortcut")
      }
    },
    dismissButton = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        if (onDelete != null) {
          TextButton(
            onClick = onDelete,
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
          ) {
            Text("Delete")
          }
          Spacer(modifier = Modifier.width(4.dp))
        }
        TextButton(onClick = onDismiss) {
          Text("Cancel")
        }
      }
    }
  )
}

@Composable
fun AddGoalDialog(
  currencySymbol: String = "₹",
  onDismiss: () -> Unit,
  onConfirm: (title: String, targetAmount: Double, initialSaved: Double, deadlineDays: Int) -> Unit
) {
  var title by remember { mutableStateOf("") }
  var targetStr by remember { mutableStateOf("") }
  var savedStr by remember { mutableStateOf("0") }
  var deadlineDays by remember { mutableStateOf(60) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "New Savings Goal",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
          value = title,
          onValueChange = {
            title = it
            errorMessage = null
          },
          label = { Text("Goal Name") },
          placeholder = { Text("e.g. Emergency Fund, Vacation, Car") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("goal_title_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
          value = targetStr,
          onValueChange = {
            targetStr = it
            errorMessage = null
          },
          label = { Text("Target Goal Amount ($currencySymbol)") },
          placeholder = { Text("1000.00") },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          prefix = { Text("$currencySymbol ") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("goal_target_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
          value = savedStr,
          onValueChange = {
            savedStr = it
            errorMessage = null
          },
          label = { Text("Currently Saved ($currencySymbol)") },
          placeholder = { Text("0.00") },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          prefix = { Text("$currencySymbol ") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("goal_saved_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = "Target Timeline",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf(30 to "1 Mo", 60 to "2 Mos", 90 to "3 Mos", 180 to "6 Mos").forEach { (days, label) ->
            val isSelected = deadlineDays == days
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (isSelected) Color(0xFF1B664B) else MaterialTheme.colorScheme.surfaceVariant,
              contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
              modifier = Modifier
                .weight(1f)
                .clickable { deadlineDays = days }
            ) {
              Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
              )
            }
          }
        }

        if (errorMessage != null) {
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = errorMessage ?: "",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val target = targetStr.toDoubleOrNull()
          val saved = savedStr.toDoubleOrNull() ?: 0.0
          if (title.isBlank()) {
            errorMessage = "Please enter a goal title"
          } else if (target == null || target <= 0) {
            errorMessage = "Please enter a valid target amount"
          } else {
            onConfirm(title, target, saved, deadlineDays)
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B664B)),
        modifier = Modifier.testTag("save_goal_button")
      ) {
        Text("Create Goal")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}

@Composable
fun EditSavingsGoalDialog(
  goal: SavingsGoalEntity,
  currencySymbol: String = "₹",
  onDismiss: () -> Unit,
  onConfirm: (updatedGoal: SavingsGoalEntity) -> Unit,
  onDelete: (() -> Unit)? = null
) {
  var title by remember { mutableStateOf(goal.title) }
  var targetStr by remember {
    mutableStateOf(
      if (goal.targetAmount % 1.0 == 0.0) goal.targetAmount.toLong().toString()
      else String.format(Locale.US, "%.2f", goal.targetAmount)
    )
  }
  var savedStr by remember {
    mutableStateOf(
      if (goal.currentAmount % 1.0 == 0.0) goal.currentAmount.toLong().toString()
      else String.format(Locale.US, "%.2f", goal.currentAmount)
    )
  }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "Edit Savings Goal",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
          value = title,
          onValueChange = {
            title = it
            errorMessage = null
          },
          label = { Text("Goal Name") },
          placeholder = { Text("e.g. Emergency Fund, Vacation, Car") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("edit_goal_title_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
          value = targetStr,
          onValueChange = {
            targetStr = it
            errorMessage = null
          },
          label = { Text("Target Goal Amount ($currencySymbol)") },
          placeholder = { Text("1000.00") },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          prefix = { Text("$currencySymbol ") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("edit_goal_target_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
          value = savedStr,
          onValueChange = {
            savedStr = it
            errorMessage = null
          },
          label = { Text("Currently Saved ($currencySymbol)") },
          placeholder = { Text("0.00") },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          prefix = { Text("$currencySymbol ") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("edit_goal_saved_input")
        )

        if (errorMessage != null) {
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = errorMessage ?: "",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val target = targetStr.toDoubleOrNull()
          val saved = savedStr.toDoubleOrNull() ?: 0.0
          if (title.isBlank()) {
            errorMessage = "Please enter a goal title"
          } else if (target == null || target <= 0) {
            errorMessage = "Please enter a valid target amount"
          } else {
            val updated = goal.copy(
              title = title.trim(),
              targetAmount = target,
              currentAmount = saved,
              isCompleted = saved >= target
            )
            onConfirm(updated)
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B664B)),
        modifier = Modifier.testTag("save_edited_goal_button")
      ) {
        Text("Save Changes")
      }
    },
    dismissButton = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        if (onDelete != null) {
          TextButton(
            onClick = onDelete,
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
          ) {
            Icon(Icons.Default.Delete, contentDescription = "Delete Goal", modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Delete")
          }
          Spacer(modifier = Modifier.width(4.dp))
        }
        TextButton(onClick = onDismiss) {
          Text("Cancel")
        }
      }
    }
  )
}

@Composable
fun ContributeGoalDialog(
  goal: SavingsGoalEntity,
  currencySymbol: String = "₹",
  onDismiss: () -> Unit,
  onConfirm: (amount: Double) -> Unit
) {
  var amountStr by remember { mutableStateOf("") }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "Contribute to ${goal.title}",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "Current: $currencySymbol${String.format(Locale.US, "%,.2f", goal.currentAmount)} / $currencySymbol${String.format(Locale.US, "%,.2f", goal.targetAmount)} (${goal.progressPercent}%)",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Quick deposit chips
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf(20.0, 50.0, 100.0, 250.0).forEach { quickAmt ->
            OutlinedButton(
              onClick = { amountStr = quickAmt.toInt().toString() },
              modifier = Modifier.weight(1f)
            ) {
              Text("+$currencySymbol${quickAmt.toInt()}", style = MaterialTheme.typography.labelSmall)
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
          value = amountStr,
          onValueChange = {
            amountStr = it
            errorMessage = null
          },
          label = { Text("Deposit Amount ($currencySymbol)") },
          placeholder = { Text("0.00") },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          prefix = { Text("$currencySymbol ") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("deposit_amount_input")
        )

        if (errorMessage != null) {
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = errorMessage ?: "",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val amt = amountStr.toDoubleOrNull()
          if (amt == null || amt <= 0) {
            errorMessage = "Enter a valid deposit amount"
          } else {
            onConfirm(amt)
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B664B)),
        modifier = Modifier.testTag("confirm_deposit_button")
      ) {
        Text("Deposit")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationHistorySheet(
  notifications: List<NotificationLogEntity>,
  onDismiss: () -> Unit,
  onSendTestNotification: () -> Unit,
  onMarkAllRead: () -> Unit,
  onClearAll: () -> Unit
) {
  ModalBottomSheet(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp)
        .padding(bottom = 32.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.NotificationsActive,
            contentDescription = "Notifications",
            tint = Color(0xFF1B664B),
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "BudgetWise Alerts & Milestones",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
        }

        Row {
          if (notifications.isNotEmpty()) {
            IconButton(onClick = onMarkAllRead) {
              Icon(
                imageVector = Icons.Default.DoneAll,
                contentDescription = "Mark All Read",
                tint = Color(0xFF1B664B)
              )
            }
            IconButton(onClick = onClearAll) {
              Icon(
                imageVector = Icons.Default.DeleteOutline,
                contentDescription = "Clear All",
                tint = MaterialTheme.colorScheme.error
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF1B664B).copy(alpha = 0.12f),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Test Goal Notifications",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF1B664B)
            )
            Text(
              text = "Trigger a real-time Android system notification",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          Button(
            onClick = onSendTestNotification,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B664B)),
            modifier = Modifier.testTag("send_test_notification_btn")
          ) {
            Text("Send", style = MaterialTheme.typography.labelSmall)
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      if (notifications.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "No notification history yet.\nYou will receive alerts when reaching goal milestones or exceeding budgets!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
          )
        }
      } else {
        val dateFormat = remember { SimpleDateFormat("MMM dd, h:mm a", Locale.US) }
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 400.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(notifications, key = { it.id }) { log ->
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(
                containerColor = if (log.isRead) {
                  MaterialTheme.colorScheme.surface
                } else {
                  Color(0xFF1B664B).copy(alpha = 0.08f)
                }
              ),
              elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(14.dp),
                verticalAlignment = Alignment.Top
              ) {
                val iconTint = when (log.type) {
                  "GOAL_REACHED" -> Color(0xFF00B894)
                  "GOAL_MILESTONE" -> Color(0xFF0984E3)
                  "BUDGET_EXCEEDED" -> MaterialTheme.colorScheme.error
                  "BUDGET_WARNING" -> Color(0xFFE67E22)
                  else -> Color(0xFF1B664B)
                }

                Box(
                  modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.15f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = when (log.type) {
                      "BUDGET_EXCEEDED", "BUDGET_WARNING" -> Icons.Default.Warning
                      "GOAL_REACHED" -> Icons.Default.Check
                      else -> Icons.Default.Notifications
                    },
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                  )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = log.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(
                    text = log.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = dateFormat.format(Date(log.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}

// ==========================================
// 19. Create Mutual Fund SIP Dialog
// ==========================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateMutualFundSipDialog(
  availableAccounts: List<AccountEntity> = emptyList(),
  currencySymbol: String = "₹",
  onDismiss: () -> Unit,
  onConfirm: (
    schemeCode: String,
    schemeName: String,
    installmentAmount: Double,
    frequency: String,
    debitAccount: String,
    sipDayOfMonth: Int,
    notes: String
  ) -> Unit
) {
  var schemeCode by remember { mutableStateOf("119551") }
  var schemeName by remember { mutableStateOf("Parag Parikh Flexi Cap Fund") }
  var amountStr by remember { mutableStateOf("5000") }
  var selectedFrequency by remember { mutableStateOf("Monthly") }
  var selectedDebitAccount by remember {
    mutableStateOf(availableAccounts.firstOrNull()?.name ?: "Main Checking")
  }
  var selectedSipDay by remember { mutableStateOf(5) }
  var notes by remember { mutableStateOf("") }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var showCatalogPicker by remember { mutableStateOf(false) }
  var catalogQuery by remember { mutableStateOf("") }

  val popularFunds = remember {
    listOf(
      "119551" to "Parag Parikh Flexi Cap Fund",
      "100345" to "HDFC Top 100 Fund",
      "120823" to "Quant Small Cap Fund",
      "102885" to "Mirae Asset Large Cap Fund",
      "103176" to "SBI Bluechip Fund",
      "105758" to "Nippon India Growth Fund"
    )
  }

  val frequencies = listOf("Monthly", "Weekly", "Bi-weekly", "Quarterly")
  val sipDays = listOf(1, 5, 10, 15, 20, 25)
  val amountPresets = listOf(1000, 2500, 5000, 10000)

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
          shape = CircleShape,
          color = Color(0xFF8E44AD).copy(alpha = 0.15f),
          modifier = Modifier.size(36.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Default.Savings,
              contentDescription = null,
              tint = Color(0xFF8E44AD),
              modifier = Modifier.size(20.dp)
            )
          }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(
            text = "Create Mutual Fund SIP",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Systematic Investment Plan",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    },
    text = {
      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(max = 440.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // AMFI Scheme Selector
        item {
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(10.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "Select Mutual Fund Scheme",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold
                )
                TextButton(
                  onClick = { showCatalogPicker = !showCatalogPicker },
                  contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                  modifier = Modifier.height(26.dp)
                ) {
                  Text(if (showCatalogPicker) "Custom Input" else "Browse AMFI")
                }
              }

              if (showCatalogPicker) {
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                  value = catalogQuery,
                  onValueChange = { catalogQuery = it },
                  placeholder = { Text("Filter schemes...", fontSize = 11.sp) },
                  singleLine = true,
                  modifier = Modifier.fillMaxWidth().height(46.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))

                val searchResults = remember(catalogQuery) {
                  MutualFundCatalog.searchMutualFunds(catalogQuery).take(12)
                }

                searchResults.forEach { mfItem ->
                  val isSelected = schemeCode == mfItem.schemeCode
                  Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) Color(0xFF8E44AD).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                    border = if (isSelected) BorderStroke(1.dp, Color(0xFF8E44AD)) else null,
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(vertical = 2.dp)
                      .clickable {
                        schemeCode = mfItem.schemeCode
                        schemeName = mfItem.schemeName
                        showCatalogPicker = false
                      }
                  ) {
                    Row(
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Column(modifier = Modifier.weight(1f)) {
                        Text(
                          text = mfItem.schemeName,
                          style = MaterialTheme.typography.labelSmall,
                          fontWeight = FontWeight.Bold
                        )
                        Text(
                          text = "${mfItem.amc} • NAV: ₹${mfItem.nav} • ${mfItem.category.replace("_", " ")}",
                          style = MaterialTheme.typography.bodySmall,
                          fontSize = 10.sp,
                          color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                      }
                      if (isSelected) {
                        Icon(
                          imageVector = Icons.Default.Check,
                          contentDescription = null,
                          tint = Color(0xFF8E44AD),
                          modifier = Modifier.size(16.dp)
                        )
                      }
                    }
                  }
                }
              } else {
                Spacer(modifier = Modifier.height(4.dp))
                // Popular Quick Chips
                FlowRow(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(6.dp),
                  verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  popularFunds.take(4).forEach { (code, name) ->
                    val isSelected = schemeCode == code
                    Surface(
                      shape = RoundedCornerShape(6.dp),
                      color = if (isSelected) Color(0xFF8E44AD) else MaterialTheme.colorScheme.surface,
                      contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                      modifier = Modifier.clickable {
                        schemeCode = code
                        schemeName = name
                      }
                    ) {
                      Text(
                        text = name.split(" ").take(2).joinToString(" "),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                      )
                    }
                  }
                }
              }
            }
          }
        }

        // Scheme Name Input
        item {
          OutlinedTextField(
            value = schemeName,
            onValueChange = {
              schemeName = it
              errorMessage = null
            },
            label = { Text("Scheme Name *") },
            placeholder = { Text("e.g. Parag Parikh Flexi Cap Fund") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("input_sip_scheme_name")
          )
        }

        // Scheme Code Input
        item {
          OutlinedTextField(
            value = schemeCode,
            onValueChange = { schemeCode = it },
            label = { Text("AMFI Scheme Code / ID") },
            placeholder = { Text("e.g. 119551") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
        }

        // Installment Amount
        item {
          Column {
            OutlinedTextField(
              value = amountStr,
              onValueChange = {
                amountStr = it
                errorMessage = null
              },
              label = { Text("Installment Amount ($currencySymbol) *") },
              placeholder = { Text("5000") },
              singleLine = true,
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              prefix = { Text("$currencySymbol ") },
              modifier = Modifier.fillMaxWidth().testTag("input_sip_amount")
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Quick Amount Presets
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              amountPresets.forEach { preset ->
                val isSelected = amountStr == preset.toString()
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                  modifier = Modifier
                    .weight(1f)
                    .clickable { amountStr = preset.toString() }
                ) {
                  Text(
                    text = "$currencySymbol$preset",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(vertical = 4.dp)
                  )
                }
              }
            }
          }
        }

        // Frequency Selector
        item {
          Column {
            Text(
              text = "SIP Frequency",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(modifier = Modifier.padding(3.dp)) {
                frequencies.forEach { freq ->
                  val isSelected = selectedFrequency == freq
                  Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) Color(0xFF8E44AD) else Color.Transparent,
                    contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                      .weight(1f)
                      .clickable { selectedFrequency = freq }
                  ) {
                    Text(
                      text = freq,
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                      textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                      modifier = Modifier.padding(vertical = 6.dp)
                    )
                  }
                }
              }
            }
          }
        }

        // Debit Payment From Account Selector
        item {
          Column {
            Text(
              text = "Debit Account *",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Account from which SIP installments will be debited",
              style = MaterialTheme.typography.bodySmall,
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              val accountsList = if (availableAccounts.isNotEmpty()) availableAccounts
              else listOf(AccountEntity(name = "Main Checking", type = "CHECKING", balance = 0.0))

              accountsList.forEach { acc ->
                val isSelected = acc.name == selectedDebitAccount
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = if (isSelected) Color(0xFF1B664B) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                  contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                  modifier = Modifier.clickable { selectedDebitAccount = acc.name }
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = acc.name,
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = "($currencySymbol${String.format(Locale.US, "%.0f", acc.balance)})",
                      style = MaterialTheme.typography.labelSmall,
                      fontSize = 10.sp,
                      color = if (isSelected) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                }
              }
            }
          }
        }

        // SIP Day of Month
        item {
          Column {
            Text(
              text = "SIP Execution Day",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              sipDays.forEach { day ->
                val isSelected = selectedSipDay == day
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = if (isSelected) Color(0xFF8E44AD) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                  contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                  modifier = Modifier
                    .weight(1f)
                    .clickable { selectedSipDay = day }
                ) {
                  Text(
                    text = "${day}th",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(vertical = 6.dp)
                  )
                }
              }
            }
          }
        }

        // Notes
        item {
          OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes / Goal Tag (Optional)") },
            placeholder = { Text("e.g. Retirement 2040, Child Education") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
        }

        // SIP Summary Card
        item {
          val parsedAmt = amountStr.toDoubleOrNull() ?: 0.0
          Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(
              containerColor = Color(0xFF8E44AD).copy(alpha = 0.08f)
            ),
            border = BorderStroke(1.dp, Color(0xFF8E44AD).copy(alpha = 0.25f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(10.dp)) {
              Text(
                text = "✨ SIP Plan Summary",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8E44AD)
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Every $selectedFrequency on day $selectedSipDay, $currencySymbol${String.format(Locale.US, "%,.2f", parsedAmt)} will be automatically debited from $selectedDebitAccount to purchase units of ${schemeName.ifBlank { "selected fund" }} at live NAV.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
              )
            }
          }
        }

        errorMessage?.let { msg ->
          item {
            Text(
              text = msg,
              color = MaterialTheme.colorScheme.error,
              style = MaterialTheme.typography.bodySmall
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val code = schemeCode.trim()
          val name = schemeName.trim()
          val amount = amountStr.toDoubleOrNull()

          if (name.isBlank()) {
            errorMessage = "Please enter or select a scheme name"
          } else if (amount == null || amount <= 0) {
            errorMessage = "Please enter a valid installment amount greater than 0"
          } else {
            onConfirm(
              code.ifBlank { "MF-${name.take(6).uppercase()}" },
              name,
              amount,
              selectedFrequency,
              selectedDebitAccount,
              selectedSipDay,
              notes.trim()
            )
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E44AD)),
        modifier = Modifier.testTag("button_create_sip_confirm")
      ) {
        Text("Create SIP")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}
