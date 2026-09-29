package com.example.ui.components

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.AccountEntity
import com.example.data.model.LoanEntity
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddLoanDialog(
  availableAccounts: List<AccountEntity> = emptyList(),
  initialType: String = "LENT", // "LENT" or "LOAN"
  currencySymbol: String = "₹",
  onDismiss: () -> Unit,
  onConfirm: (
    personName: String,
    type: String,
    totalAmount: Double,
    account: String,
    dueDate: Long?,
    interestRate: Double,
    note: String,
    updateAccountBalance: Boolean
  ) -> Unit
) {
  var loanType by remember { mutableStateOf(initialType) } // "LENT" or "LOAN"
  var personName by remember { mutableStateOf("") }
  var amountStr by remember { mutableStateOf("") }
  var selectedAccount by remember {
    mutableStateOf(availableAccounts.firstOrNull()?.name ?: "Main Checking")
  }
  var selectedDueDays by remember { mutableStateOf<Int?>(30) } // null = None, or 7, 14, 30, 60, 90
  var interestRateStr by remember { mutableStateOf("") }
  var note by remember { mutableStateOf("") }
  var updateAccountBalance by remember { mutableStateOf(true) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  val scrollState = rememberScrollState()

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = if (loanType == "LENT") "I Lent Money" else "I Borrowed Money",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // Type Selector
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(4.dp)
        ) {
          // Lent Button
          val isLent = loanType == "LENT"
          Surface(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(10.dp))
              .clickable { loanType = "LENT" }
              .testTag("loan_type_lent_btn"),
            color = if (isLent) Color(0xFF1B664B) else Color.Transparent,
            contentColor = if (isLent) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
          ) {
            Row(
              modifier = Modifier.padding(vertical = 10.dp),
              horizontalArrangement = Arrangement.Center,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.ArrowUpward,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "I Lent (Owed to Me)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isLent) FontWeight.Bold else FontWeight.Normal
              )
            }
          }

          // Loan Button
          val isLoan = loanType == "LOAN"
          Surface(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(10.dp))
              .clickable { loanType = "LOAN" }
              .testTag("loan_type_loan_btn"),
            color = if (isLoan) Color(0xFFD63031) else Color.Transparent,
            contentColor = if (isLoan) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
          ) {
            Row(
              modifier = Modifier.padding(vertical = 10.dp),
              horizontalArrangement = Arrangement.Center,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.ArrowDownward,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "I Borrowed (I Owe)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isLoan) FontWeight.Bold else FontWeight.Normal
              )
            }
          }
        }

        // Person / Creditor Name
        OutlinedTextField(
          value = personName,
          onValueChange = { personName = it },
          label = {
            Text(if (loanType == "LENT") "Debtor / Borrower Name" else "Creditor / Lender Name")
          },
          placeholder = { Text(if (loanType == "LENT") "e.g. Sarah Jenkins" else "e.g. Bank of America / David") },
          leadingIcon = {
            Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF1B664B))
          },
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("loan_person_name_input")
        )

        // Total Amount
        OutlinedTextField(
          value = amountStr,
          onValueChange = { input ->
            if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
              amountStr = input
            }
          },
          label = { Text("Total Amount ($currencySymbol)") },
          placeholder = { Text("0.00") },
          leadingIcon = {
            Text(
              currencySymbol,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF1B664B)
            )
          },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("loan_amount_input")
        )

        // Account Selector
        if (availableAccounts.isNotEmpty()) {
          Text(
            text = if (loanType == "LENT") "Disbursed From Account" else "Received In Account",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            availableAccounts.forEach { acc ->
              val isSelected = acc.name == selectedAccount
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) Color(0xFF1B664B).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF1B664B)) else null,
                modifier = Modifier.clickable { selectedAccount = acc.name }
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = Icons.Default.AccountBalance,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = if (isSelected) Color(0xFF1B664B) else MaterialTheme.colorScheme.onSurfaceVariant
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = acc.name,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color(0xFF1B664B) else MaterialTheme.colorScheme.onSurface
                  )
                }
              }
            }
          }
        }

        // Due Date Options
        Text(
          text = "Target Due Date",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          val options = listOf(
            null to "No Deadline",
            7 to "7 Days",
            14 to "14 Days",
            30 to "30 Days",
            60 to "60 Days",
            90 to "90 Days"
          )
          options.forEach { (days, label) ->
            val isSelected = selectedDueDays == days
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (isSelected) Color(0xFF1B664B).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
              border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF1B664B)) else null,
              modifier = Modifier.clickable { selectedDueDays = days }
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.CalendarToday,
                  contentDescription = null,
                  modifier = Modifier.size(12.dp),
                  tint = if (isSelected) Color(0xFF1B664B) else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = label,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  color = if (isSelected) Color(0xFF1B664B) else MaterialTheme.colorScheme.onSurface
                )
              }
            }
          }
        }

        // Optional Interest Rate
        OutlinedTextField(
          value = interestRateStr,
          onValueChange = { input ->
            if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
              interestRateStr = input
            }
          },
          label = { Text("Interest Rate % (Optional)") },
          placeholder = { Text("0.0") },
          leadingIcon = {
            Icon(Icons.Default.Percent, contentDescription = null, tint = Color(0xFF1B664B))
          },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth()
        )

        // Note
        OutlinedTextField(
          value = note,
          onValueChange = { note = it },
          label = { Text("Purpose / Description") },
          placeholder = { Text(if (loanType == "LENT") "e.g. Concert tickets & hotel share" else "e.g. Laptop financing") },
          singleLine = false,
          maxLines = 2,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth()
        )

        // Balance adjustment checkbox
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { updateAccountBalance = !updateAccountBalance }
            .padding(vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Checkbox(
            checked = updateAccountBalance,
            onCheckedChange = { updateAccountBalance = it }
          )
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Text(
              text = "Reflect in Account Balance",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold
            )
            Text(
              text = if (loanType == "LENT")
                "Deduct $currencySymbol$amountStr from $selectedAccount"
              else
                "Add $currencySymbol$amountStr to $selectedAccount",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        if (errorMessage != null) {
          Text(
            text = errorMessage!!,
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
          if (personName.isBlank()) {
            errorMessage = "Please enter the person or entity name"
            return@Button
          }
          if (amt == null || amt <= 0.0) {
            errorMessage = "Please enter a valid amount greater than 0"
            return@Button
          }

          val now = System.currentTimeMillis()
          val dayMillis = 24L * 60 * 60 * 1000
          val dueDate = selectedDueDays?.let { now + (it * dayMillis) }
          val interest = interestRateStr.toDoubleOrNull() ?: 0.0

          onConfirm(
            personName.trim(),
            loanType,
            amt,
            selectedAccount,
            dueDate,
            interest,
            note.trim(),
            updateAccountBalance
          )
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B664B)),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.testTag("save_loan_btn")
      ) {
        Text("Save Record")
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
fun RecordLoanPaymentDialog(
  loan: LoanEntity,
  availableAccounts: List<AccountEntity> = emptyList(),
  currencySymbol: String = "₹",
  onDismiss: () -> Unit,
  onConfirm: (
    paymentAmount: Double,
    account: String,
    updateAccountBalance: Boolean,
    note: String
  ) -> Unit
) {
  val remaining = loan.remainingAmount
  var amountStr by remember { mutableStateOf(String.format(Locale.US, "%.2f", remaining)) }
  var selectedAccount by remember {
    mutableStateOf(availableAccounts.firstOrNull()?.name ?: loan.account)
  }
  var note by remember { mutableStateOf("") }
  var updateAccountBalance by remember { mutableStateOf(true) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  val isLent = loan.type == "LENT"

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = if (isLent) "Collect Repayment" else "Record Repayment",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // Summary Card
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(
            containerColor = (if (isLent) Color(0xFF1B664B) else Color(0xFFD63031)).copy(alpha = 0.08f)
          )
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                  (if (isLent) Color(0xFF1B664B) else Color(0xFFD63031)).copy(alpha = 0.15f)
                ),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = if (isLent) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                contentDescription = null,
                tint = if (isLent) Color(0xFF1B664B) else Color(0xFFD63031),
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = if (isLent) "Owed by ${loan.personName}" else "Owed to ${loan.personName}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "$currencySymbol${String.format(Locale.US, "%.2f", remaining)} remaining",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isLent) Color(0xFF1B664B) else Color(0xFFD63031)
              )
              Text(
                text = "Total original: $currencySymbol${String.format(Locale.US, "%.2f", loan.totalAmount)} ($currencySymbol${String.format(Locale.US, "%.2f", loan.paidAmount)} paid)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }

        // Amount Input
        OutlinedTextField(
          value = amountStr,
          onValueChange = { input ->
            if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
              amountStr = input
            }
          },
          label = { Text("Payment Amount ($currencySymbol)") },
          placeholder = { Text("0.00") },
          leadingIcon = {
            Text(
              currencySymbol,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF1B664B)
            )
          },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("loan_repayment_amount_input")
        )

        // Quick amount chips
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          val quickPicks = listOfNotNull(
            "Pay Full ($currencySymbol${String.format(Locale.US, "%.2f", remaining)})" to remaining,
            if (remaining > 20) "$currencySymbol 20" to 20.0 else null,
            if (remaining > 50) "$currencySymbol 50" to 50.0 else null,
            if (remaining > 100) "$currencySymbol 100" to 100.0 else null
          )
          quickPicks.forEach { (label, amt) ->
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              modifier = Modifier.clickable {
                amountStr = String.format(Locale.US, "%.2f", amt)
              }
            ) {
              Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
              )
            }
          }
        }

        // Account Selector
        if (availableAccounts.isNotEmpty()) {
          Text(
            text = if (isLent) "Deposit Into Account" else "Pay From Account",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            availableAccounts.forEach { acc ->
              val isSelected = acc.name == selectedAccount
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) Color(0xFF1B664B).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF1B664B)) else null,
                modifier = Modifier.clickable { selectedAccount = acc.name }
              ) {
                Text(
                  text = acc.name,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  color = if (isSelected) Color(0xFF1B664B) else MaterialTheme.colorScheme.onSurface,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
              }
            }
          }
        }

        // Note
        OutlinedTextField(
          value = note,
          onValueChange = { note = it },
          label = { Text("Payment Note (Optional)") },
          placeholder = { Text("e.g. Venmo transfer / Cash") },
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth()
        )

        // Balance adjust checkbox
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { updateAccountBalance = !updateAccountBalance }
            .padding(vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Checkbox(
            checked = updateAccountBalance,
            onCheckedChange = { updateAccountBalance = it }
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (isLent)
              "Add payment to $selectedAccount"
            else
              "Deduct payment from $selectedAccount",
            style = MaterialTheme.typography.bodySmall
          )
        }

        if (errorMessage != null) {
          Text(
            text = errorMessage!!,
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
          if (amt == null || amt <= 0.0) {
            errorMessage = "Please enter a valid amount greater than 0"
            return@Button
          }
          onConfirm(amt, selectedAccount, updateAccountBalance, note.trim())
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B664B)),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.testTag("confirm_loan_payment_btn")
      ) {
        Text("Record Payment")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}
