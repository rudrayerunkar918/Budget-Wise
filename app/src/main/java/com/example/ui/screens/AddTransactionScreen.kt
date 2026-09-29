package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AccountEntity
import com.example.data.model.ExpenseCategory
import com.example.data.model.SavingsGoalEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Full-screen Add Transaction screen matching the provided UI mockups:
 * - Top bar with back arrow and overflow menu
 * - 3-Segment selector: Expense, Income, Transfer
 * - Category icon box and large amount display
 * - Clicking the amount opens the Inbuilt Calculator Keyboard bottom sheet
 * - Date & Time selectors (Today, HH:mm)
 * - Nature/Type chips (Default, Upcoming, Subscription, Mandate)
 * - Account chips with dropdown chevron
 * - Goal chips (No goal, Savings, +)
 * - Title pill text field with 'T' icon
 * - Notes & Attachment card with photo/receipt picker
 * - More options expandable section
 * - Bottom "Select Category" / "Save Transaction" primary action button
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
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
  val context = LocalContext.current
  val calendar = remember { Calendar.getInstance() }

  // Form State
  var transactionType by remember { mutableStateOf(initialType) } // "EXPENSE", "INCOME", "TRANSFER"
  var amountValue by remember { mutableStateOf(0.0) }
  var amountDisplayStr by remember { mutableStateOf("") }
  var selectedCategory by remember {
    mutableStateOf(if (initialType == "INCOME") ExpenseCategory.SALARY else ExpenseCategory.FOOD)
  }
  var hasExplicitCategory by remember { mutableStateOf(false) }

  // Date & Time state
  var selectedTimestamp by remember { mutableStateOf(calendar.timeInMillis) }
  var selectedHour by remember { mutableStateOf(calendar.get(Calendar.HOUR_OF_DAY)) }
  var selectedMinute by remember { mutableStateOf(calendar.get(Calendar.MINUTE)) }
  var isScheduleEnabled by remember { mutableStateOf(false) }

  // Nature / Type chip state
  val natureOptions = listOf("Default", "Upcoming", "Subscription", "Mandate")
  var selectedNature by remember { mutableStateOf("Default") }

  // Accounts state
  val defaultAccountList = remember(availableAccounts) {
    if (availableAccounts.isNotEmpty()) availableAccounts.map { it.name }
    else listOf("Bank", "Cash", "Shares", "Mutual Funds")
  }
  var selectedAccount by remember {
    mutableStateOf(defaultAccountList.firstOrNull() ?: "Bank")
  }
  var selectedToAccount by remember {
    mutableStateOf(defaultAccountList.getOrNull(1) ?: "Cash")
  }

  // Goal state
  var selectedGoal by remember { mutableStateOf("No goal") }

  // Text inputs
  var title by remember { mutableStateOf("") }
  var note by remember { mutableStateOf("") }
  var attachmentUri by remember { mutableStateOf<Uri?>(null) }

  // More options state
  var showMoreOptions by remember { mutableStateOf(false) }
  var payeeMerchant by remember { mutableStateOf("") }
  var isTaxDeductible by remember { mutableStateOf(false) }
  var showTopMenu by remember { mutableStateOf(false) }

  // Bottom sheets & dialogs
  var showCalculatorSheet by remember { mutableStateOf(false) }
  var showCategorySheet by remember { mutableStateOf(false) }

  // Photo / receipt picker
  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    if (uri != null) {
      attachmentUri = uri
      Toast.makeText(context, "Attachment added", Toast.LENGTH_SHORT).show()
    }
  }

  // Format date label
  val dateLabel = remember(selectedTimestamp) {
    val cal = Calendar.getInstance().apply { timeInMillis = selectedTimestamp }
    val todayCal = Calendar.getInstance()
    val isToday = cal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) &&
        cal.get(Calendar.DAY_OF_YEAR) == todayCal.get(Calendar.DAY_OF_YEAR)
    val yesterdayCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    val isYesterday = cal.get(Calendar.YEAR) == yesterdayCal.get(Calendar.YEAR) &&
        cal.get(Calendar.DAY_OF_YEAR) == yesterdayCal.get(Calendar.DAY_OF_YEAR)
    val tomorrowCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
    val isTomorrow = cal.get(Calendar.YEAR) == tomorrowCal.get(Calendar.YEAR) &&
        cal.get(Calendar.DAY_OF_YEAR) == tomorrowCal.get(Calendar.DAY_OF_YEAR)

    when {
      isToday -> "Today"
      isTomorrow -> "Tomorrow"
      isYesterday -> "Yesterday"
      else -> SimpleDateFormat("dd MMM", Locale.getDefault()).format(cal.time)
    }
  }

  val isFutureDateTime = remember(selectedTimestamp, selectedHour, selectedMinute) {
    val cal = Calendar.getInstance().apply {
      timeInMillis = selectedTimestamp
      set(Calendar.HOUR_OF_DAY, selectedHour)
      set(Calendar.MINUTE, selectedMinute)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }
    cal.timeInMillis > System.currentTimeMillis()
  }

  val formattedAmountDisplay = remember(amountValue) {
    if (amountValue <= 0.0) "${currencySymbol}0"
    else if (amountValue % 1.0 == 0.0) "$currencySymbol${amountValue.toLong()}"
    else "$currencySymbol${String.format(Locale.US, "%.2f", amountValue)}"
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(
      usePlatformDefaultWidth = false,
      decorFitsSystemWindows = false
    )
  ) {
    Surface(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .imePadding(),
      color = MaterialTheme.colorScheme.background
    ) {
      Column(
        modifier = Modifier.fillMaxSize()
      ) {
        // --- TOP BAR ---
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          IconButton(
            onClick = onDismiss,
            modifier = Modifier.testTag("btn_close_add_transaction")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = MaterialTheme.colorScheme.onBackground
            )
          }

          Text(
            text = "Add Transaction",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
              .weight(1f)
              .padding(start = 4.dp)
          )

          Box {
            IconButton(
              onClick = { showTopMenu = true },
              modifier = Modifier.testTag("btn_add_transaction_overflow")
            ) {
              Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More Options",
                tint = MaterialTheme.colorScheme.onBackground
              )
            }

            DropdownMenu(
              expanded = showTopMenu,
              onDismissRequest = { showTopMenu = false }
            ) {
              DropdownMenuItem(
                text = { Text("Clear Form") },
                onClick = {
                  amountValue = 0.0
                  amountDisplayStr = ""
                  title = ""
                  note = ""
                  attachmentUri = null
                  hasExplicitCategory = false
                  showTopMenu = false
                }
              )
              DropdownMenuItem(
                text = { Text("Reset to Current Time") },
                onClick = {
                  val now = Calendar.getInstance()
                  selectedTimestamp = now.timeInMillis
                  selectedHour = now.get(Calendar.HOUR_OF_DAY)
                  selectedMinute = now.get(Calendar.MINUTE)
                  showTopMenu = false
                }
              )
            }
          }
        }

        // --- 3-SEGMENT TYPE SELECTOR: [ Expense | Income | Transfer ] ---
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
          border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
          ),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
          Row(
            modifier = Modifier.padding(3.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
          ) {
            // Expense Tab
            val isExpense = transactionType == "EXPENSE"
            Surface(
              shape = RoundedCornerShape(11.dp),
              color = if (isExpense) MaterialTheme.colorScheme.surface else Color.Transparent,
              border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isExpense) Color(0xFFE74C3C).copy(alpha = 0.35f) else Color.Transparent
              ),
              shadowElevation = if (isExpense) 2.dp else 0.dp,
              modifier = Modifier
                .weight(1f)
                .clickable {
                  transactionType = "EXPENSE"
                  if (selectedCategory.isIncome) {
                    selectedCategory = ExpenseCategory.FOOD
                  }
                }
                .testTag("tab_type_expense")
            ) {
              Row(
                modifier = Modifier.padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "▼ ",
                  fontSize = 11.sp,
                  color = if (isExpense) Color(0xFFE74C3C) else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = "Expense",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = if (isExpense) FontWeight.Bold else FontWeight.Medium,
                  color = if (isExpense) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            // Income Tab
            val isIncome = transactionType == "INCOME"
            Surface(
              shape = RoundedCornerShape(11.dp),
              color = if (isIncome) MaterialTheme.colorScheme.surface else Color.Transparent,
              border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isIncome) Color(0xFF27AE60).copy(alpha = 0.35f) else Color.Transparent
              ),
              shadowElevation = if (isIncome) 2.dp else 0.dp,
              modifier = Modifier
                .weight(1f)
                .clickable {
                  transactionType = "INCOME"
                  if (!selectedCategory.isIncome) {
                    selectedCategory = ExpenseCategory.SALARY
                  }
                }
                .testTag("tab_type_income")
            ) {
              Row(
                modifier = Modifier.padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "▲ ",
                  fontSize = 11.sp,
                  color = if (isIncome) Color(0xFF27AE60) else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = "Income",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = if (isIncome) FontWeight.Bold else FontWeight.Medium,
                  color = if (isIncome) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            // Transfer Tab
            val isTransfer = transactionType == "TRANSFER"
            Surface(
              shape = RoundedCornerShape(11.dp),
              color = if (isTransfer) MaterialTheme.colorScheme.surface else Color.Transparent,
              border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isTransfer) Color(0xFF2980B9).copy(alpha = 0.35f) else Color.Transparent
              ),
              shadowElevation = if (isTransfer) 2.dp else 0.dp,
              modifier = Modifier
                .weight(1f)
                .clickable {
                  transactionType = "TRANSFER"
                }
                .testTag("tab_type_transfer")
            ) {
              Row(
                modifier = Modifier.padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "⇄ ",
                  fontSize = 13.sp,
                  color = if (isTransfer) Color(0xFF2980B9) else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = "Transfer",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = if (isTransfer) FontWeight.Bold else FontWeight.Medium,
                  color = if (isTransfer) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }

        // --- SCROLLABLE FORM CONTENT ---
        Column(
          modifier = Modifier
            .weight(1f)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          Spacer(modifier = Modifier.height(4.dp))

          // --- HEADER: CATEGORY ICON BOX (LEFT) & LARGE AMOUNT (RIGHT) ---
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            // Category Box (Clickable -> Opens Category Picker)
            Surface(
              shape = RoundedCornerShape(18.dp),
              color = if (hasExplicitCategory || transactionType != "TRANSFER") {
                selectedCategory.color.copy(alpha = 0.16f)
              } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
              },
              border = androidx.compose.foundation.BorderStroke(
                1.5.dp,
                if (hasExplicitCategory) selectedCategory.color.copy(alpha = 0.6f)
                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
              ),
              shadowElevation = 1.dp,
              modifier = Modifier
                .size(72.dp)
                .clickable {
                  if (transactionType != "TRANSFER") {
                    showCategorySheet = true
                  }
                }
                .testTag("category_icon_box")
            ) {
              Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
              ) {
                if (transactionType == "TRANSFER") {
                  Text(
                    text = "⇄",
                    fontSize = 32.sp,
                    color = Color(0xFF2980B9)
                  )
                } else {
                  Icon(
                    imageVector = selectedCategory.icon,
                    contentDescription = selectedCategory.displayName,
                    tint = selectedCategory.color,
                    modifier = Modifier.size(34.dp)
                  )
                }
              }
            }

            // Large Amount Display (Framed container clickable -> Opens Inbuilt Calculator Keyboard!)
            Surface(
              shape = RoundedCornerShape(16.dp),
              color = MaterialTheme.colorScheme.surface,
              border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant
              ),
              shadowElevation = 2.dp,
              modifier = Modifier
                .weight(1f)
                .padding(start = 14.dp)
                .clickable {
                  showCalculatorSheet = true
                }
                .testTag("amount_clickable_area")
            ) {
              Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
              ) {
                Text(
                  text = formattedAmountDisplay,
                  style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 36.sp
                  ),
                  color = MaterialTheme.colorScheme.onBackground,
                  textAlign = TextAlign.End,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Text(
                  text = "Tap to open calculator ⌨",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.primary,
                  fontWeight = FontWeight.Medium
                )
              }
            }
          }

          // --- DATE & TIME ROW ---
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Date Pill (e.g. [📅 Today])
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = MaterialTheme.colorScheme.surface,
              border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant
              ),
              shadowElevation = 1.dp,
              modifier = Modifier
                .clickable {
                  showDatePickerDialog(context, selectedTimestamp) { pickedMs ->
                    selectedTimestamp = pickedMs
                    val checkCal = Calendar.getInstance().apply {
                      timeInMillis = pickedMs
                      set(Calendar.HOUR_OF_DAY, selectedHour)
                      set(Calendar.MINUTE, selectedMinute)
                    }
                    if (checkCal.timeInMillis > System.currentTimeMillis()) {
                      selectedNature = "Upcoming"
                      isScheduleEnabled = true
                    }
                  }
                }
                .testTag("btn_select_date")
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = MaterialTheme.colorScheme.primaryContainer,
                  modifier = Modifier.size(32.dp)
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Icon(
                      imageVector = Icons.Default.CalendarToday,
                      contentDescription = "Date",
                      tint = MaterialTheme.colorScheme.onPrimaryContainer,
                      modifier = Modifier.size(16.dp)
                    )
                  }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                  text = dateLabel,
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
            }

            // Time Pill (e.g. [ 13 ] : [ 27 ])
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = MaterialTheme.colorScheme.surface,
              border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant
              ),
              shadowElevation = 1.dp,
              modifier = Modifier
                .clickable {
                  showTimePickerDialog(context, selectedHour, selectedMinute) { hour, min ->
                    selectedHour = hour
                    selectedMinute = min
                    val checkCal = Calendar.getInstance().apply {
                      timeInMillis = selectedTimestamp
                      set(Calendar.HOUR_OF_DAY, hour)
                      set(Calendar.MINUTE, min)
                    }
                    if (checkCal.timeInMillis > System.currentTimeMillis()) {
                      selectedNature = "Upcoming"
                      isScheduleEnabled = true
                    }
                  }
                }
                .testTag("btn_select_time")
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = MaterialTheme.colorScheme.surface,
                  border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                  ),
                  modifier = Modifier.padding(2.dp)
                ) {
                  Text(
                    text = String.format(Locale.US, "%02d", selectedHour),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = MaterialTheme.colorScheme.onSurface
                  )
                }

                Text(
                  text = " : ",
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.sp,
                  color = MaterialTheme.colorScheme.onSurface
                )

                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = MaterialTheme.colorScheme.surface,
                  border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                  ),
                  modifier = Modifier.padding(2.dp)
                ) {
                  Text(
                    text = String.format(Locale.US, "%02d", selectedMinute),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = MaterialTheme.colorScheme.onSurface
                  )
                }
              }
            }
          }

          // Scheduled Transaction Switch Card & Shortcuts
          val isScheduledMode = isScheduleEnabled || isFutureDateTime || selectedNature == "Upcoming"
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (isScheduledMode) Color(0xFF0984E3).copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            border = androidx.compose.foundation.BorderStroke(
              1.dp,
              if (isScheduledMode) Color(0xFF0984E3).copy(alpha = 0.45f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("scheduled_tx_indicator_banner")
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(
                  modifier = Modifier.weight(1f),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Surface(
                    shape = CircleShape,
                    color = if (isScheduledMode) Color(0xFF0984E3) else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(32.dp)
                  ) {
                    Box(contentAlignment = Alignment.Center) {
                      Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Schedule",
                        tint = if (isScheduledMode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(17.dp)
                      )
                    }
                  }
                  Spacer(modifier = Modifier.width(10.dp))
                  Column {
                    Text(
                      text = if (isScheduledMode) "Scheduled Transaction" else "Schedule Transaction",
                      style = MaterialTheme.typography.titleSmall,
                      fontWeight = FontWeight.Bold,
                      color = if (isScheduledMode) Color(0xFF0984E3) else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                      text = if (isScheduledMode)
                        "Will execute on $dateLabel at ${String.format(Locale.US, "%02d:%02d", selectedHour, selectedMinute)}"
                      else "Toggle or choose future date/time to schedule",
                      style = MaterialTheme.typography.bodySmall,
                      fontSize = 11.sp,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                }

                Switch(
                  checked = isScheduledMode,
                  onCheckedChange = { checked ->
                    isScheduleEnabled = checked
                    if (checked) {
                      selectedNature = "Upcoming"
                      val checkCal = Calendar.getInstance().apply {
                        timeInMillis = selectedTimestamp
                        set(Calendar.HOUR_OF_DAY, selectedHour)
                        set(Calendar.MINUTE, selectedMinute)
                      }
                      if (checkCal.timeInMillis <= System.currentTimeMillis()) {
                        val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
                        selectedTimestamp = tomorrow.timeInMillis
                        selectedHour = 9
                        selectedMinute = 0
                      }
                    } else {
                      selectedNature = "Default"
                      val now = Calendar.getInstance()
                      selectedTimestamp = now.timeInMillis
                      selectedHour = now.get(Calendar.HOUR_OF_DAY)
                      selectedMinute = now.get(Calendar.MINUTE)
                    }
                  },
                  modifier = Modifier.testTag("switch_schedule_transaction")
                )
              }

              // Quick Schedule Shortcuts Row
              AnimatedVisibility(
                visible = isScheduledMode,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
              ) {
                Column {
                  Spacer(modifier = Modifier.height(10.dp))
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                  ) {
                    listOf(
                      "Tomorrow 9 AM" to {
                        val cal = Calendar.getInstance().apply {
                          add(Calendar.DAY_OF_YEAR, 1)
                          set(Calendar.HOUR_OF_DAY, 9)
                          set(Calendar.MINUTE, 0)
                        }
                        selectedTimestamp = cal.timeInMillis
                        selectedHour = 9
                        selectedMinute = 0
                      },
                      "Tomorrow 6 PM" to {
                        val cal = Calendar.getInstance().apply {
                          add(Calendar.DAY_OF_YEAR, 1)
                          set(Calendar.HOUR_OF_DAY, 18)
                          set(Calendar.MINUTE, 0)
                        }
                        selectedTimestamp = cal.timeInMillis
                        selectedHour = 18
                        selectedMinute = 0
                      },
                      "In 3 Days" to {
                        val cal = Calendar.getInstance().apply {
                          add(Calendar.DAY_OF_YEAR, 3)
                        }
                        selectedTimestamp = cal.timeInMillis
                      },
                      "1st Next Mo" to {
                        val cal = Calendar.getInstance().apply {
                          add(Calendar.MONTH, 1)
                          set(Calendar.DAY_OF_MONTH, 1)
                          set(Calendar.HOUR_OF_DAY, 9)
                          set(Calendar.MINUTE, 0)
                        }
                        selectedTimestamp = cal.timeInMillis
                        selectedHour = 9
                        selectedMinute = 0
                      }
                    ).forEach { (label, action) ->
                      Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0984E3).copy(alpha = 0.35f)),
                        modifier = Modifier
                          .weight(1f)
                          .clickable { action() }
                      ) {
                        Text(
                          text = label,
                          style = MaterialTheme.typography.labelSmall,
                          color = Color(0xFF0984E3),
                          fontWeight = FontWeight.SemiBold,
                          fontSize = 9.5.sp,
                          textAlign = TextAlign.Center,
                          modifier = Modifier.padding(vertical = 5.dp, horizontal = 2.dp)
                        )
                      }
                    }
                  }
                }
              }
            }
          }

          // --- NATURE / TRANSACTION TYPE CHIPS ROW ---
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            // Info Icon Button
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
              border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
              ),
              modifier = Modifier.size(36.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Default.Info,
                  contentDescription = "Transaction Nature Info",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(18.dp)
                )
              }
            }

            natureOptions.forEach { nature ->
              val isSelected = selectedNature == nature
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(
                  1.dp,
                  if (isSelected) MaterialTheme.colorScheme.primary
                  else MaterialTheme.colorScheme.outlineVariant
                ),
                shadowElevation = if (isSelected) 1.dp else 0.5.dp,
                modifier = Modifier
                  .clickable {
                    selectedNature = nature
                    if (nature == "Upcoming" && !isFutureDateTime) {
                      val tomorrowCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
                      selectedTimestamp = tomorrowCal.timeInMillis
                    }
                  }
                  .testTag("nature_chip_$nature")
              ) {
                Text(
                  text = nature,
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                  else MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
              }
            }
          }

          // --- ACCOUNTS CHIPS ROW ---
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (transactionType == "TRANSFER") {
              Text(
                text = "From Account",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              // Dropdown chevron button
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                border = androidx.compose.foundation.BorderStroke(
                  1.dp,
                  MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
                ),
                modifier = Modifier.size(36.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "More Accounts",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                  )
                }
              }

              defaultAccountList.forEach { accName ->
                val isSelected = selectedAccount == accName
                Surface(
                  shape = RoundedCornerShape(10.dp),
                  color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                  else MaterialTheme.colorScheme.surface,
                  border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outlineVariant
                  ),
                  shadowElevation = if (isSelected) 1.dp else 0.5.dp,
                  modifier = Modifier
                    .clickable { selectedAccount = accName }
                    .testTag("account_chip_$accName")
                ) {
                  Text(
                    text = accName,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                  )
                }
              }
            }

            // If Transfer mode, show destination account selector
            if (transactionType == "TRANSFER") {
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "To Destination Account",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                defaultAccountList.forEach { accName ->
                  val isSelected = selectedToAccount == accName
                  Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) Color(0xFF0984E3).copy(alpha = 0.2f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    border = androidx.compose.foundation.BorderStroke(
                      1.dp,
                      if (isSelected) Color(0xFF0984E3)
                      else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier
                      .clickable { selectedToAccount = accName }
                      .testTag("to_account_chip_$accName")
                  ) {
                    Text(
                      text = accName,
                      style = MaterialTheme.typography.labelMedium,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                      color = if (isSelected) Color(0xFF0984E3)
                      else MaterialTheme.colorScheme.onSurfaceVariant,
                      modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                  }
                }
              }
            }
          }

          // --- GOALS CHIPS ROW ---
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            // "No goal" chip
            val isNoGoal = selectedGoal == "No goal"
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = if (isNoGoal) MaterialTheme.colorScheme.primaryContainer
              else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
              border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isNoGoal) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
              ),
              modifier = Modifier.clickable { selectedGoal = "No goal" }
            ) {
              Text(
                text = "No goal",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isNoGoal) FontWeight.Bold else FontWeight.Medium,
                color = if (isNoGoal) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
              )
            }

            // Available goals from DB or default
            val goalsList = if (availableGoals.isNotEmpty()) availableGoals.map { it.title }
            else listOf("Savings")

            goalsList.forEach { goalTitle ->
              val isSelected = selectedGoal == goalTitle
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                border = androidx.compose.foundation.BorderStroke(
                  1.dp,
                  if (isSelected) MaterialTheme.colorScheme.primary
                  else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
                ),
                modifier = Modifier.clickable { selectedGoal = goalTitle }
              ) {
                Text(
                  text = goalTitle,
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                  else MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
              }
            }

            // '+' add goal shortcut
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
              border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
              ),
              modifier = Modifier
                .size(36.dp)
                .clickable {
                  Toast
                    .makeText(context, "Link to Goal selected", Toast.LENGTH_SHORT)
                    .show()
                }
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Default.Add,
                  contentDescription = "Add Goal",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }

          // --- TITLE TEXT FIELD ---
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(
              1.dp,
              MaterialTheme.colorScheme.outlineVariant
            ),
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(modifier = Modifier.weight(1f)) {
                if (title.isEmpty()) {
                  Text(
                    text = "Title",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodyLarge
                  )
                }
                BasicTextField(
                  value = title,
                  onValueChange = { title = it },
                  textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp
                  ),
                  cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                  singleLine = true,
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tx_title_input")
                )
              }

              Icon(
                imageVector = Icons.Default.Title,
                contentDescription = "Title Icon",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
              )
            }
          }

          // --- NOTES & ATTACHMENT CARD ---
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(
              1.dp,
              MaterialTheme.colorScheme.outlineVariant
            ),
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
          ) {
            Column {
              // Upper: Notes text field
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.Top
              ) {
                Box(
                  modifier = Modifier
                    .weight(1f)
                    .height(60.dp)
                ) {
                  if (note.isEmpty()) {
                    Text(
                      text = "Notes",
                      color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                      style = MaterialTheme.typography.bodyMedium
                    )
                  }
                  BasicTextField(
                    value = note,
                    onValueChange = { note = it },
                    textStyle = TextStyle(
                      color = MaterialTheme.colorScheme.onSurface,
                      fontSize = 15.sp
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    maxLines = 3,
                    modifier = Modifier
                      .fillMaxSize()
                      .testTag("tx_notes_input")
                  )
                }

                Icon(
                  imageVector = Icons.Default.Description,
                  contentDescription = "Notes",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                  modifier = Modifier.size(20.dp)
                )
              }

              HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                thickness = 1.dp
              )

              // Lower: Add Attachment Row
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable {
                    photoPickerLauncher.launch(
                      PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                  }
                  .padding(horizontal = 16.dp, vertical = 12.dp)
                  .testTag("btn_add_attachment"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.AttachFile,
                    contentDescription = "Attachment",
                    tint = if (attachmentUri != null) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                  )
                  Spacer(modifier = Modifier.width(10.dp))
                  Text(
                    text = if (attachmentUri != null) "Attachment linked" else "Add attachment",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (attachmentUri != null) FontWeight.Bold else FontWeight.Normal,
                    color = if (attachmentUri != null) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface
                  )
                }

                if (attachmentUri != null) {
                  IconButton(
                    onClick = { attachmentUri = null },
                    modifier = Modifier.size(24.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Default.Close,
                      contentDescription = "Remove attachment",
                      tint = MaterialTheme.colorScheme.error,
                      modifier = Modifier.size(16.dp)
                    )
                  }
                } else {
                  Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                  )
                }
              }
            }
          }

          // --- MORE OPTIONS PILL ---
          Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
          ) {
            Surface(
              shape = RoundedCornerShape(20.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
              border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
              ),
              modifier = Modifier
                .clickable { showMoreOptions = !showMoreOptions }
                .testTag("btn_more_options")
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "More Options",
                  style = MaterialTheme.typography.labelMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                  imageVector = if (showMoreOptions) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(16.dp)
                )
              }
            }
          }

          // More Options Expandable Content
          AnimatedVisibility(
            visible = showMoreOptions,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
          ) {
            Column(
              modifier = Modifier.fillMaxWidth(),
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              // Payee / Merchant
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                border = androidx.compose.foundation.BorderStroke(
                  1.dp,
                  MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                ),
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "Payee/Merchant: ",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                  BasicTextField(
                    value = payeeMerchant,
                    onValueChange = { payeeMerchant = it },
                    textStyle = TextStyle(
                      color = MaterialTheme.colorScheme.onSurface,
                      fontSize = 14.sp
                    ),
                    modifier = Modifier.weight(1f)
                  )
                }
              }

              // Tax Deductible Switch
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                border = androidx.compose.foundation.BorderStroke(
                  1.dp,
                  MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                ),
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(
                    text = "Tax Deductible",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  Switch(
                    checked = isTaxDeductible,
                    onCheckedChange = { isTaxDeductible = it }
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))
        }

        // --- BOTTOM ACTION BUTTON ---
        // As shown in Screenshot 1: says "Select Category" (or "Save Transaction" when category selected and amount > 0)
        Surface(
          color = MaterialTheme.colorScheme.background,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
          val isReadyToSave = amountValue > 0 && (transactionType == "TRANSFER" || hasExplicitCategory)
          val isScheduledMode = isScheduleEnabled || isFutureDateTime || selectedNature == "Upcoming"
          val buttonText = when {
            transactionType == "TRANSFER" -> if (isScheduledMode) "Schedule Transfer" else "Save Transfer"
            hasExplicitCategory && amountValue > 0 -> if (isScheduledMode) "Schedule Transaction" else "Save Transaction"
            hasExplicitCategory && amountValue <= 0 -> "Enter Amount"
            else -> "Select Category"
          }

          Button(
            onClick = {
              if (transactionType != "TRANSFER" && !hasExplicitCategory) {
                showCategorySheet = true
              } else if (amountValue <= 0.0) {
                showCalculatorSheet = true
              } else {
                val finalCal = Calendar.getInstance().apply {
                  timeInMillis = selectedTimestamp
                  set(Calendar.HOUR_OF_DAY, selectedHour)
                  set(Calendar.MINUTE, selectedMinute)
                  set(Calendar.SECOND, 0)
                  set(Calendar.MILLISECOND, 0)
                }
                val finalTimestamp = finalCal.timeInMillis
                val isScheduledTx = finalTimestamp > System.currentTimeMillis() || selectedNature == "Upcoming" || isScheduleEnabled

                // Confirm & Save
                val finalTitle = title.ifBlank {
                  when (transactionType) {
                    "TRANSFER" -> "Transfer to $selectedToAccount"
                    else -> selectedCategory.displayName
                  }
                }
                val extraNotes = buildString {
                  if (note.isNotBlank()) append(note.trim())
                  val effNature = if (isScheduledTx && selectedNature == "Default") "Upcoming" else selectedNature
                  if (effNature != "Default") {
                    if (isNotEmpty()) append(" • ")
                    append("Type: $effNature")
                  }
                  if (isScheduledTx && !contains("Scheduled")) {
                    if (isNotEmpty()) append(" • ")
                    append("Scheduled")
                  }
                  if (selectedGoal != "No goal") {
                    if (isNotEmpty()) append(" • ")
                    append("Goal: $selectedGoal")
                  }
                  if (payeeMerchant.isNotBlank()) {
                    if (isNotEmpty()) append(" • ")
                    append("Payee: $payeeMerchant")
                  }
                  if (isTaxDeductible) {
                    if (isNotEmpty()) append(" • ")
                    append("Tax Deductible")
                  }
                }

                onConfirm(
                  finalTitle,
                  amountValue,
                  selectedCategory,
                  extraNotes,
                  transactionType,
                  selectedAccount,
                  if (transactionType == "TRANSFER") selectedToAccount else "",
                  finalTimestamp
                )
                onDismiss()
              }
            },
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = if (isReadyToSave) {
                when (transactionType) {
                  "INCOME" -> Color(0xFF27AE60)
                  "TRANSFER" -> Color(0xFF2980B9)
                  else -> MaterialTheme.colorScheme.primary
                }
              } else {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
              }
            ),
            modifier = Modifier
              .fillMaxWidth()
              .height(52.dp)
              .testTag("btn_bottom_action")
          ) {
            Text(
              text = buttonText,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onPrimary
            )
          }
        }
      }
    }
  }

  // --- INBUILT CALCULATOR KEYBOARD BOTTOM SHEET (SCREENSHOT 2) ---
  if (showCalculatorSheet) {
    InbuiltCalculatorSheet(
      currencySymbol = currencySymbol,
      initialAmount = amountValue,
      selectedAccount = selectedAccount,
      accountsList = defaultAccountList,
      onAccountSelected = { selectedAccount = it },
      onDismiss = { showCalculatorSheet = false },
      onSetAmount = { evaluatedAmount ->
        amountValue = evaluatedAmount
        showCalculatorSheet = false
      }
    )
  }

  // --- CATEGORY PICKER BOTTOM SHEET ---
  if (showCategorySheet) {
    CategoryPickerSheet(
      isIncome = transactionType == "INCOME",
      currentSelected = selectedCategory,
      onDismiss = { showCategorySheet = false },
      onCategorySelected = { cat ->
        selectedCategory = cat
        hasExplicitCategory = true
        showCategorySheet = false
        // If amount hasn't been set yet, prompt calculator
        if (amountValue <= 0.0) {
          showCalculatorSheet = true
        }
      }
    )
  }
}

/**
 * Inbuilt Calculator Keyboard Modal Bottom Sheet matching Screenshot 2:
 * - "Enter Amount" title on left
 * - Large live evaluated Amount / expression on right
 * - Account chips row above keypad
 * - 4x4 keypad:
 *   [ 1 ] [ 2 ] [ 3 ] [ ÷ ]
 *   [ 4 ] [ 5 ] [ 6 ] [ × ]
 *   [ 7 ] [ 8 ] [ 9 ] [ - ]
 *   [ . ] [ 0 ] [ ⌫ ] [ + ]
 * - "Set Amount" bottom action button
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InbuiltCalculatorSheet(
  currencySymbol: String,
  initialAmount: Double,
  selectedAccount: String,
  accountsList: List<String>,
  onAccountSelected: (String) -> Unit,
  onDismiss: () -> Unit,
  onSetAmount: (Double) -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  // Math expression string
  var expression by remember {
    mutableStateOf(
      if (initialAmount > 0.0) {
        if (initialAmount % 1.0 == 0.0) initialAmount.toLong().toString()
        else String.format(Locale.US, "%.2f", initialAmount)
      } else ""
    )
  }

  // Live evaluated amount
  val liveResult = remember(expression) {
    evaluateExpression(expression)
  }

  val displayAmount = remember(expression, liveResult) {
    if (expression.isBlank()) "${currencySymbol}0"
    else if (liveResult % 1.0 == 0.0) "$currencySymbol${liveResult.toLong()}"
    else "$currencySymbol${String.format(Locale.US, "%.2f", liveResult)}"
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    modifier = Modifier.testTag("inbuilt_calculator_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 8.dp)
        .navigationBarsPadding(),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // --- HEADER: "Enter Amount" & Large Amount / Expression Display ---
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = androidx.compose.foundation.BorderStroke(
          1.dp,
          MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        ),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Enter Amount",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )

          Column(horizontalAlignment = Alignment.End) {
            if (expression.contains("+") || expression.contains("-") ||
              expression.contains("×") || expression.contains("÷")
            ) {
              Text(
                text = expression,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }

            Text(
              text = displayAmount,
              style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp
              ),
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }
      }

      // --- ACCOUNT CHIPS ROW ---
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Chevron button
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
          border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
          ),
          modifier = Modifier.size(34.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Default.KeyboardArrowDown,
              contentDescription = "Dropdown",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(16.dp)
            )
          }
        }

        accountsList.forEach { acc ->
          val isSelected = acc == selectedAccount
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            border = androidx.compose.foundation.BorderStroke(
              1.dp,
              if (isSelected) MaterialTheme.colorScheme.primary
              else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
            ),
            modifier = Modifier.clickable { onAccountSelected(acc) }
          ) {
            Text(
              text = acc,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
              else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
            )
          }
        }
      }

      // --- CALCULATOR KEYPAD (4x4 GRID) ---
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = androidx.compose.foundation.BorderStroke(
          1.dp,
          MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        ),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // Row 1: 1, 2, 3, ÷
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            CalculatorKey(text = "1", modifier = Modifier.weight(1f)) {
              expression += "1"
            }
            CalculatorKey(text = "2", modifier = Modifier.weight(1f)) {
              expression += "2"
            }
            CalculatorKey(text = "3", modifier = Modifier.weight(1f)) {
              expression += "3"
            }
            CalculatorKey(
              text = "÷",
              isOperator = true,
              modifier = Modifier.weight(1f)
            ) {
              expression = appendOperator(expression, "÷")
            }
          }

          // Row 2: 4, 5, 6, ×
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            CalculatorKey(text = "4", modifier = Modifier.weight(1f)) {
              expression += "4"
            }
            CalculatorKey(text = "5", modifier = Modifier.weight(1f)) {
              expression += "5"
            }
            CalculatorKey(text = "6", modifier = Modifier.weight(1f)) {
              expression += "6"
            }
            CalculatorKey(
              text = "×",
              isOperator = true,
              modifier = Modifier.weight(1f)
            ) {
              expression = appendOperator(expression, "×")
            }
          }

          // Row 3: 7, 8, 9, -
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            CalculatorKey(text = "7", modifier = Modifier.weight(1f)) {
              expression += "7"
            }
            CalculatorKey(text = "8", modifier = Modifier.weight(1f)) {
              expression += "8"
            }
            CalculatorKey(text = "9", modifier = Modifier.weight(1f)) {
              expression += "9"
            }
            CalculatorKey(
              text = "-",
              isOperator = true,
              modifier = Modifier.weight(1f)
            ) {
              expression = appendOperator(expression, "-")
            }
          }

          // Row 4: ., 0, ⌫, +
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            CalculatorKey(text = ".", modifier = Modifier.weight(1f)) {
              expression = appendDecimal(expression)
            }
            CalculatorKey(text = "0", modifier = Modifier.weight(1f)) {
              if (expression.isNotEmpty()) expression += "0"
            }
            // Backspace Key
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.surface,
              border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
              ),
              shadowElevation = 1.dp,
              modifier = Modifier
                .weight(1f)
                .height(54.dp)
                .clickable {
                  if (expression.isNotEmpty()) {
                    expression = expression.dropLast(1)
                  }
                }
                .testTag("calc_key_backspace")
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.Backspace,
                  contentDescription = "Backspace",
                  tint = MaterialTheme.colorScheme.error,
                  modifier = Modifier.size(22.dp)
                )
              }
            }
            CalculatorKey(
              text = "+",
              isOperator = true,
              modifier = Modifier.weight(1f)
            ) {
              expression = appendOperator(expression, "+")
            }
          }
        }
      }

      // --- "Set Amount" BOTTOM BUTTON ---
      Button(
        onClick = {
          val finalVal = evaluateExpression(expression)
          onSetAmount(finalVal)
        },
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.primary
        ),
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("btn_set_amount")
      ) {
        Text(
          text = "Set Amount",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onPrimary
        )
      }

      Spacer(modifier = Modifier.height(6.dp))
    }
  }
}

@Composable
private fun CalculatorKey(
  text: String,
  isOperator: Boolean = false,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = if (isOperator) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
    else MaterialTheme.colorScheme.surface,
    border = androidx.compose.foundation.BorderStroke(
      1.dp,
      if (isOperator) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
      else MaterialTheme.colorScheme.outlineVariant
    ),
    shadowElevation = 1.5.dp,
    modifier = modifier
      .height(54.dp)
      .clickable(onClick = onClick)
      .testTag("calc_key_$text")
  ) {
    Box(contentAlignment = Alignment.Center) {
      Text(
        text = text,
        fontSize = if (isOperator) 22.sp else 20.sp,
        fontWeight = if (isOperator) FontWeight.Bold else FontWeight.SemiBold,
        color = if (isOperator) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurface
      )
    }
  }
}

/**
 * Category Picker Bottom Sheet with colorful grid of icons
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryPickerSheet(
  isIncome: Boolean,
  currentSelected: ExpenseCategory,
  onDismiss: () -> Unit,
  onCategorySelected: (ExpenseCategory) -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val categories = remember(isIncome) {
    if (isIncome) ExpenseCategory.incomeCategories()
    else ExpenseCategory.expenseCategories()
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 12.dp)
        .navigationBarsPadding()
    ) {
      Text(
        text = if (isIncome) "Select Income Category" else "Select Expense Category",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 16.dp)
      )

      LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 16.dp)
      ) {
        items(categories) { cat ->
          val isSelected = cat == currentSelected
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (isSelected) cat.color.copy(alpha = 0.22f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            border = androidx.compose.foundation.BorderStroke(
              if (isSelected) 2.dp else 1.dp,
              if (isSelected) cat.color else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
            ),
            modifier = Modifier
              .clickable { onCategorySelected(cat) }
              .testTag("cat_pick_${cat.name.lowercase()}")
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center,
              modifier = Modifier.padding(vertical = 14.dp, horizontal = 8.dp)
            ) {
              Surface(
                shape = CircleShape,
                color = cat.color.copy(alpha = 0.18f),
                modifier = Modifier.size(46.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = cat.icon,
                    contentDescription = cat.displayName,
                    tint = cat.color,
                    modifier = Modifier.size(24.dp)
                  )
                }
              }

              Spacer(modifier = Modifier.height(8.dp))

              Text(
                text = cat.displayName,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
              )
            }
          }
        }
      }
    }
  }
}

// --- HELPER MATH EVALUATION FUNCTIONS ---

private fun appendOperator(expr: String, op: String): String {
  if (expr.isEmpty()) return ""
  val last = expr.last()
  return if (last == '+' || last == '-' || last == '×' || last == '÷') {
    expr.dropLast(1) + op
  } else {
    expr + op
  }
}

private fun appendDecimal(expr: String): String {
  if (expr.isEmpty()) return "0."
  val lastToken = expr.split("+", "-", "×", "÷").lastOrNull() ?: ""
  return if (lastToken.contains(".")) {
    expr
  } else if (expr.last() in "+-×÷") {
    expr + "0."
  } else {
    expr + "."
  }
}

/**
 * Robust mathematical expression parser supporting +, -, ×, ÷ with precedence.
 */
fun evaluateExpression(expr: String): Double {
  val clean = expr.replace('×', '*').replace('÷', '/').replace(" ", "")
  if (clean.isEmpty()) return 0.0

  // Trim trailing operator for live feedback
  var trimmed = clean
  while (trimmed.isNotEmpty() && trimmed.last() in "+-*/") {
    trimmed = trimmed.dropLast(1)
  }
  if (trimmed.isEmpty()) return 0.0

  return try {
    parseAdditionSubtraction(trimmed)
  } catch (_: Exception) {
    0.0
  }
}

private fun parseAdditionSubtraction(expr: String): Double {
  val tokens = mutableListOf<String>()
  val ops = mutableListOf<Char>()
  var current = StringBuilder()
  var i = 0
  while (i < expr.length) {
    val c = expr[i]
    if ((c == '+' || c == '-') && i > 0 && expr[i - 1] !in "+-*/") {
      tokens.add(current.toString())
      ops.add(c)
      current = StringBuilder()
    } else {
      current.append(c)
    }
    i++
  }
  if (current.isNotEmpty()) tokens.add(current.toString())

  var result = parseMultiplicationDivision(tokens.firstOrNull() ?: "0")
  for (j in ops.indices) {
    val nextVal = parseMultiplicationDivision(tokens.getOrElse(j + 1) { "0" })
    if (ops[j] == '+') result += nextVal
    else result -= nextVal
  }
  return result
}

private fun parseMultiplicationDivision(expr: String): Double {
  val tokens = mutableListOf<String>()
  val ops = mutableListOf<Char>()
  var current = StringBuilder()
  for (c in expr) {
    if (c == '*' || c == '/') {
      tokens.add(current.toString())
      ops.add(c)
      current = StringBuilder()
    } else {
      current.append(c)
    }
  }
  if (current.isNotEmpty()) tokens.add(current.toString())

  var result = tokens.firstOrNull()?.toDoubleOrNull() ?: 0.0
  for (j in ops.indices) {
    val nextVal = tokens.getOrElse(j + 1) { "1" }.toDoubleOrNull() ?: 1.0
    if (ops[j] == '*') result *= nextVal
    else if (nextVal != 0.0) result /= nextVal
  }
  return result
}

fun showDatePickerDialog(context: Context, initialMs: Long, onDateSelected: (Long) -> Unit) {
  val cal = Calendar.getInstance().apply { timeInMillis = initialMs }
  DatePickerDialog(
    context,
    { _, year, month, dayOfMonth ->
      val picked = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month)
        set(Calendar.DAY_OF_MONTH, dayOfMonth)
      }
      onDateSelected(picked.timeInMillis)
    },
    cal.get(Calendar.YEAR),
    cal.get(Calendar.MONTH),
    cal.get(Calendar.DAY_OF_MONTH)
  ).show()
}

fun showTimePickerDialog(
  context: Context,
  initialHour: Int,
  initialMinute: Int,
  onTimeSelected: (Int, Int) -> Unit
) {
  TimePickerDialog(
    context,
    { _, hourOfDay, minute ->
      onTimeSelected(hourOfDay, minute)
    },
    initialHour,
    initialMinute,
    true
  ).show()
}
