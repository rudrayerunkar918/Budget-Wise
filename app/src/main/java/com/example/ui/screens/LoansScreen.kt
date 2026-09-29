package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountEntity
import com.example.data.model.LoanEntity
import com.example.ui.components.AddLoanDialog
import com.example.ui.components.RecordLoanPaymentDialog
import com.example.ui.viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LoansScreen(
  viewModel: ExpenseViewModel,
  modifier: Modifier = Modifier
) {
  val loans by viewModel.filteredLoans.collectAsState()
  val allAccounts by viewModel.allAccounts.collectAsState()
  val totalLent by viewModel.totalLentActive.collectAsState()
  val totalBorrowed by viewModel.totalBorrowedActive.collectAsState()
  val netDebt by viewModel.netDebtPosition.collectAsState()
  val currencySymbol by viewModel.currencySymbol.collectAsState()

  val selectedTypeFilter by viewModel.selectedLoanTypeFilter.collectAsState()
  val selectedStatusFilter by viewModel.selectedLoanStatusFilter.collectAsState()

  var showAddDialog by remember { mutableStateOf(false) }
  var loanToRepay by remember { mutableStateOf<LoanEntity?>(null) }
  var addDialogInitialType by remember { mutableStateOf("LENT") }

  Box(modifier = modifier.fillMaxSize()) {
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Top Metric Cards: Lent (Asset), Borrowed (Debt), Net
      item {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text(
            text = "Debts & Loans Overview",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            // Lent Card (Money Owed to You)
            Card(
              modifier = Modifier
                .weight(1f)
                .clickable {
                  addDialogInitialType = "LENT"
                  showAddDialog = true
                },
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = Color(0xFF1B664B).copy(alpha = 0.08f))
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(
                    modifier = Modifier
                      .size(28.dp)
                      .clip(CircleShape)
                      .background(Color(0xFF1B664B).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = Icons.Default.ArrowUpward,
                      contentDescription = null,
                      tint = Color(0xFF1B664B),
                      modifier = Modifier.size(16.dp)
                    )
                  }
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "Lent to Others",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                  text = "$currencySymbol${String.format(Locale.US, "%.2f", totalLent)}",
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF1B664B)
                )
                Text(
                  text = "Owed to you",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            // Borrowed Card (Loans You Owe)
            Card(
              modifier = Modifier
                .weight(1f)
                .clickable {
                  addDialogInitialType = "LOAN"
                  showAddDialog = true
                },
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = Color(0xFFD63031).copy(alpha = 0.08f))
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(
                    modifier = Modifier
                      .size(28.dp)
                      .clip(CircleShape)
                      .background(Color(0xFFD63031).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = Icons.Default.ArrowDownward,
                      contentDescription = null,
                      tint = Color(0xFFD63031),
                      modifier = Modifier.size(16.dp)
                    )
                  }
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "You Borrowed",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                  text = "$currencySymbol${String.format(Locale.US, "%.2f", totalBorrowed)}",
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFFD63031)
                )
                Text(
                  text = "Your liabilities",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }

          // Net Debt Banner
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = if (netDebt >= 0) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                  contentDescription = null,
                  tint = if (netDebt >= 0) Color(0xFF1B664B) else Color(0xFFD63031),
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Net Debt Position",
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.SemiBold
                )
              }
              Text(
                text = "${if (netDebt >= 0) "+$currencySymbol" else "-$currencySymbol"}${String.format(Locale.US, "%.2f", Math.abs(netDebt))}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (netDebt >= 0) Color(0xFF1B664B) else Color(0xFFD63031)
              )
            }
          }
        }
      }

      // Filter Chips (Type & Status)
      item {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          // Type filters
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            val typeFilters = listOf(
              null to "All Debts",
              "LENT" to "Lent (Owed to Me)",
              "LOAN" to "Loans (I Owe)"
            )
            typeFilters.forEach { (type, label) ->
              val isSelected = selectedTypeFilter == type
              Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) Color(0xFF1B664B) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier
                  .clickable { viewModel.selectedLoanTypeFilter.value = type }
                  .testTag("loan_filter_${type ?: "all"}")
              ) {
                Text(
                  text = label,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
              }
            }
          }

          // Status filter: Active vs Settled
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            val statusFilters = listOf(
              "ACTIVE" to "Active Only",
              "SETTLED" to "Settled",
              null to "Show All"
            )
            statusFilters.forEach { (status, label) ->
              val isSelected = selectedStatusFilter == status
              Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)) else null,
                modifier = Modifier.clickable { viewModel.selectedLoanStatusFilter.value = status }
              ) {
                Text(
                  text = label,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
              }
            }
          }
        }
      }

      // Empty State or List
      if (loans.isEmpty()) {
        item {
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 24.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(64.dp)
                  .clip(CircleShape)
                  .background(Color(0xFF1B664B).copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Handshake,
                  contentDescription = null,
                  tint = Color(0xFF1B664B),
                  modifier = Modifier.size(32.dp)
                )
              }
              Text(
                text = "No Loan or Lent Records",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Track money you've lent to friends, shared expenses, or personal loans with progress bars and due date reminders.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
              )
              Spacer(modifier = Modifier.height(4.dp))
              Button(
                onClick = {
                  addDialogInitialType = "LENT"
                  showAddDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B664B)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("empty_state_add_loan_btn")
              ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add First Record")
              }
            }
          }
        }
      } else {
        items(loans, key = { it.id }) { loan ->
          LoanCard(
            loan = loan,
            currencySymbol = currencySymbol,
            onRecordPayment = { loanToRepay = loan },
            onSettleInFull = {
              viewModel.settleLoanInFull(loan, loan.account, updateAccountBalance = true)
            },
            onDelete = { viewModel.deleteLoan(loan) }
          )
        }
      }
    }

    // Floating Action Button
    FloatingActionButton(
      onClick = {
        addDialogInitialType = "LENT"
        showAddDialog = true
      },
      containerColor = Color(0xFF1B664B),
      contentColor = Color.White,
      shape = RoundedCornerShape(16.dp),
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(16.dp)
        .testTag("add_loan_fab")
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(Icons.Default.Add, contentDescription = "Add Loan or Lent")
        Spacer(modifier = Modifier.width(8.dp))
        Text("Add Loan / Lent", fontWeight = FontWeight.Bold)
      }
    }
  }

  // Dialogs
  if (showAddDialog) {
    AddLoanDialog(
      availableAccounts = allAccounts,
      initialType = addDialogInitialType,
      currencySymbol = currencySymbol,
      onDismiss = { showAddDialog = false },
      onConfirm = { person, type, total, account, dueDate, interest, note, updateBalance ->
        viewModel.addLoan(
          personName = person,
          type = type,
          totalAmount = total,
          account = account,
          dueDate = dueDate,
          interestRate = interest,
          note = note,
          updateAccountBalance = updateBalance
        )
        showAddDialog = false
      }
    )
  }

  loanToRepay?.let { loan ->
    RecordLoanPaymentDialog(
      loan = loan,
      availableAccounts = allAccounts,
      currencySymbol = currencySymbol,
      onDismiss = { loanToRepay = null },
      onConfirm = { paymentAmt, account, updateBalance, note ->
        viewModel.recordLoanPayment(
          loan = loan,
          paymentAmount = paymentAmt,
          account = account,
          updateAccountBalance = updateBalance,
          note = note
        )
        loanToRepay = null
      }
    )
  }
}

@Composable
fun LoanCard(
  loan: LoanEntity,
  onRecordPayment: () -> Unit,
  onSettleInFull: () -> Unit,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier,
  currencySymbol: String = "₹"
) {
  val isLent = loan.type == "LENT"
  val primaryColor = if (isLent) Color(0xFF1B664B) else Color(0xFFD63031)
  val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.US) }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("loan_card_${loan.id}"),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // Header: Counterparty, Type Badge, Settled Status & Delete
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f)
        ) {
          Box(
            modifier = Modifier
              .size(42.dp)
              .clip(CircleShape)
              .background(primaryColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (isLent) Icons.Default.Person else Icons.Default.AccountBalance,
              contentDescription = null,
              tint = primaryColor,
              modifier = Modifier.size(22.dp)
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column {
            Text(
              text = loan.personName,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              // Type Badge
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = primaryColor.copy(alpha = 0.12f)
              ) {
                Text(
                  text = if (isLent) "LENT" else "BORROWED",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = primaryColor,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }

              if (loan.isSettled) {
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = Color(0xFF00B894).copy(alpha = 0.15f)
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(
                      Icons.Default.Check,
                      contentDescription = null,
                      modifier = Modifier.size(10.dp),
                      tint = Color(0xFF00B894)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                      text = "SETTLED",
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.Bold,
                      color = Color(0xFF00B894)
                    )
                  }
                }
              }
            }
          }
        }

        IconButton(
          onClick = onDelete,
          modifier = Modifier.testTag("loan_delete_${loan.id}")
        ) {
          Icon(
            imageVector = Icons.Default.DeleteOutline,
            contentDescription = "Delete",
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(20.dp)
          )
        }
      }

      // Amounts Breakdown
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
      ) {
        Column {
          Text(
            text = if (loan.isSettled) "Total Repaid" else "Remaining Balance",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "$currencySymbol${String.format(Locale.US, "%.2f", if (loan.isSettled) loan.totalAmount else loan.remainingAmount)}",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = if (loan.isSettled) Color(0xFF00B894) else primaryColor
          )
        }

        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = "Total: $currencySymbol${String.format(Locale.US, "%.2f", loan.totalAmount)}",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "Paid: $currencySymbol${String.format(Locale.US, "%.2f", loan.paidAmount)} (${loan.progressPercent}%)",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      // Progress Bar
      LinearProgressIndicator(
        progress = { loan.progressFraction },
        modifier = Modifier
          .fillMaxWidth()
          .height(8.dp)
          .clip(RoundedCornerShape(4.dp)),
        color = if (loan.isSettled) Color(0xFF00B894) else primaryColor,
        trackColor = primaryColor.copy(alpha = 0.12f)
      )

      // Meta tags: Due Date, Interest Rate, Account & Note
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Due Date Chip
          loan.dueDate?.let { due ->
            val now = System.currentTimeMillis()
            val isOverdue = due < now && !loan.isSettled
            val diffDays = ((due - now) / (1000 * 60 * 60 * 24)).toInt()

            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = when {
                  loan.isSettled -> "Finished"
                  isOverdue -> "Overdue"
                  diffDays == 0 -> "Due today"
                  diffDays == 1 -> "Due tomorrow"
                  diffDays > 0 -> "Due in $diffDays d"
                  else -> dateFormat.format(Date(due))
                },
                style = MaterialTheme.typography.labelSmall,
                color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Normal
              )
            }
          }

          // Interest Rate
          if (loan.interestRate > 0.0) {
            Text(
              text = "•  ${loan.interestRate}% APR",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          // Account
          Text(
            text = "•  ${loan.account}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      // Note (if any)
      if (loan.note.isNotBlank()) {
        Text(
          text = "\"${loan.note}\"",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
          fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
        )
      }

      // Action Buttons (if active)
      if (!loan.isSettled) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedButton(
            onClick = onRecordPayment,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .weight(1f)
              .testTag("loan_repay_${loan.id}")
          ) {
            Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (isLent) "Collect Payment" else "Make Payment", style = MaterialTheme.typography.labelSmall)
          }

          Button(
            onClick = onSettleInFull,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
            modifier = Modifier
              .weight(1f)
              .testTag("loan_settle_${loan.id}")
          ) {
            Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Settle in Full", style = MaterialTheme.typography.labelSmall)
          }
        }
      }
    }
  }
}
