package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountEntity
import com.example.data.model.ConditionalMandateEntity
import com.example.ui.viewmodel.ExpenseViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConditionalMandatesScreen(
  viewModel: ExpenseViewModel,
  onBackClick: () -> Unit
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val snackbarHostState = remember { SnackbarHostState() }

  val mandates by viewModel.allMandates.collectAsState()
  val accounts by viewModel.allAccounts.collectAsState()
  val currencySymbol by viewModel.currencySymbol.collectAsState()

  var showAddEditDialog by remember { mutableStateOf(false) }
  var editingMandate by remember { mutableStateOf<ConditionalMandateEntity?>(null) }
  var mandateToDelete by remember { mutableStateOf<ConditionalMandateEntity?>(null) }
  var showInfoDialog by remember { mutableStateOf(false) }
  var isEvaluating by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "Transfer Mandates",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Automated balance triggers & sweeps",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        },
        navigationIcon = {
          IconButton(
            onClick = onBackClick,
            modifier = Modifier.testTag("mandates_back_btn")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back"
            )
          }
        },
        actions = {
          IconButton(
            onClick = { showInfoDialog = true },
            modifier = Modifier.testTag("mandates_info_btn")
          ) {
            Icon(
              imageVector = Icons.Default.Info,
              contentDescription = "About Mandates",
              tint = MaterialTheme.colorScheme.primary
            )
          }
          IconButton(
            onClick = {
              if (isEvaluating) return@IconButton
              isEvaluating = true
              viewModel.evaluateMandatesNow { messages ->
                isEvaluating = false
                scope.launch {
                  if (messages.isEmpty()) {
                    snackbarHostState.showSnackbar("All accounts evaluated. No mandate transfers were needed.")
                  } else {
                    snackbarHostState.showSnackbar("Executed ${messages.size} automated mandate transfer(s)!")
                  }
                }
              }
            },
            modifier = Modifier.testTag("evaluate_mandates_top_btn")
          ) {
            Icon(
              imageVector = Icons.Default.PlayArrow,
              contentDescription = "Evaluate Rules Now",
              tint = MaterialTheme.colorScheme.primary
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    },
    floatingActionButton = {
      FloatingActionButton(
        onClick = {
          editingMandate = null
          showAddEditDialog = true
        },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier.testTag("add_mandate_fab")
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.Add, contentDescription = "Add Mandate")
          Spacer(modifier = Modifier.width(8.dp))
          Text("New Mandate", fontWeight = FontWeight.Bold)
        }
      }
    },
    snackbarHost = { SnackbarHost(snackbarHostState) }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .testTag("mandates_lazy_column"),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. Overview & Action Banner
      item {
        MandateOverviewCard(
          activeCount = mandates.count { it.isEnabled },
          totalCount = mandates.size,
          totalExecutions = mandates.sumOf { it.totalTriggeredCount },
          isEvaluating = isEvaluating,
          onEvaluateClick = {
            if (isEvaluating) return@MandateOverviewCard
            isEvaluating = true
            viewModel.evaluateMandatesNow { messages ->
              isEvaluating = false
              scope.launch {
                if (messages.isEmpty()) {
                  snackbarHostState.showSnackbar("All accounts evaluated. No mandate transfers required.")
                } else {
                  snackbarHostState.showSnackbar("Executed ${messages.size} mandate transfer(s) successfully!")
                }
              }
            }
          }
        )
      }

      // 2. Quick Starter Templates (if few or no mandates)
      if (mandates.size < 3) {
        item {
          MandateTemplatesSection(
            accounts = accounts,
            onSelectTemplate = { title, source, target, condition, threshold, transfer ->
              editingMandate = ConditionalMandateEntity(
                title = title,
                sourceAccount = source,
                targetAccount = target,
                conditionType = condition,
                thresholdAmount = threshold,
                transferAmount = transfer,
                isEnabled = true
              )
              showAddEditDialog = true
            }
          )
        }
      }

      // 3. Header
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Active Rules (${mandates.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          if (mandates.isNotEmpty()) {
            Text(
              text = "${mandates.count { it.isEnabled }} enabled",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.SemiBold
            )
          }
        }
      }

      // 4. List of Mandates
      if (mandates.isEmpty()) {
        item {
          EmptyMandatesCard(
            onCreateClick = {
              editingMandate = null
              showAddEditDialog = true
            }
          )
        }
      } else {
        items(mandates, key = { it.id }) { mandate ->
          MandateItemCard(
            mandate = mandate,
            currencySymbol = currencySymbol,
            onToggle = { viewModel.toggleMandateEnabled(mandate) },
            onEdit = {
              editingMandate = mandate
              showAddEditDialog = true
            },
            onDelete = { mandateToDelete = mandate }
          )
        }
      }

      item {
        Spacer(modifier = Modifier.height(72.dp))
      }
    }
  }

  // Add / Edit Mandate Dialog
  if (showAddEditDialog) {
    AddEditMandateDialog(
      mandate = editingMandate,
      accounts = accounts,
      currencySymbol = currencySymbol,
      onDismiss = {
        showAddEditDialog = false
        editingMandate = null
      },
      onSave = { newMandate ->
        if (newMandate.id == 0L) {
          viewModel.insertMandate(newMandate) {
            Toast.makeText(context, "Mandate created successfully", Toast.LENGTH_SHORT).show()
          }
        } else {
          viewModel.updateMandate(newMandate) {
            Toast.makeText(context, "Mandate updated successfully", Toast.LENGTH_SHORT).show()
          }
        }
        showAddEditDialog = false
        editingMandate = null
      }
    )
  }

  // Delete Confirmation Dialog
  mandateToDelete?.let { mandate ->
    AlertDialog(
      onDismissRequest = { mandateToDelete = null },
      title = { Text("Delete Mandate") },
      text = { Text("Are you sure you want to delete '${mandate.title}'? Automated transfers under this rule will stop.") },
      confirmButton = {
        Button(
          onClick = {
            viewModel.deleteMandate(mandate)
            mandateToDelete = null
            Toast.makeText(context, "Mandate deleted", Toast.LENGTH_SHORT).show()
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
          Text("Delete")
        }
      },
      dismissButton = {
        TextButton(onClick = { mandateToDelete = null }) {
          Text("Cancel")
        }
      }
    )
  }

  // Info Dialog
  if (showInfoDialog) {
    AlertDialog(
      onDismissRequest = { showInfoDialog = false },
      icon = { Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
      title = { Text("How Mandates Work", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("Mandates automatically monitor account balances and execute transfers when specified financial conditions are met:")
          Spacer(modifier = Modifier.height(4.dp))
          Text("• Low-Balance Refill (Shield): When your Target Account (e.g. Checking) falls below the threshold, money is automatically transferred from your Source Account (e.g. Savings) to restore funds.", fontSize = 13.sp)
          Text("• Surplus Auto-Sweep: When your Source Account exceeds a ceiling, excess funds are swept into high-interest Savings or Investment accounts.", fontSize = 13.sp)
          Text("• Instant Audit & Logs: Every automated transfer creates a clear transaction note and system notification.", fontSize = 13.sp)
        }
      },
      confirmButton = {
        TextButton(onClick = { showInfoDialog = false }) {
          Text("Got It")
        }
      }
    )
  }
}

@Composable
private fun MandateOverviewCard(
  activeCount: Int,
  totalCount: Int,
  totalExecutions: Int,
  isEvaluating: Boolean,
  onEvaluateClick: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
    ),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(18.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.SwapHoriz,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onPrimary,
              modifier = Modifier.size(22.dp)
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = "Smart Automation Engine",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "$activeCount of $totalCount rules active",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Button(
          onClick = onEvaluateClick,
          enabled = !isEvaluating,
          shape = RoundedCornerShape(12.dp),
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
          modifier = Modifier.testTag("evaluate_rules_btn")
        ) {
          Icon(
            imageVector = if (isEvaluating) Icons.Default.Refresh else Icons.Default.FlashOn,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = if (isEvaluating) "Checking..." else "Run Check",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))
      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
      Spacer(modifier = Modifier.height(12.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = "$activeCount",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
          )
          Text(
            text = "Active Rules",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = "$totalExecutions",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.tertiary
          )
          Text(
            text = "Total Auto Transfers",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }
  }
}

@Composable
private fun MandateTemplatesSection(
  accounts: List<AccountEntity>,
  onSelectTemplate: (title: String, source: String, target: String, condition: String, threshold: Double, transfer: Double) -> Unit
) {
  val defaultChecking = accounts.firstOrNull { it.type == "CHECKING" }?.name ?: accounts.firstOrNull()?.name ?: "Checking Account"
  val defaultSavings = accounts.firstOrNull { it.type == "SAVINGS" }?.name ?: accounts.getOrNull(1)?.name ?: "Savings"
  val defaultCash = accounts.firstOrNull { it.type == "CASH" }?.name ?: "Cash"

  Column(modifier = Modifier.fillMaxWidth()) {
    Text(
      text = "Quick Starter Templates",
      style = MaterialTheme.typography.labelLarge,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.primary,
      modifier = Modifier.padding(bottom = 8.dp)
    )

    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier.padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        TemplateRow(
          icon = Icons.Default.AccountBalance,
          title = "🛡️ Low-Balance Shield",
          desc = "If $defaultChecking < ₹5,000, transfer ₹10,000 from $defaultSavings",
          onClick = {
            onSelectTemplate(
              "Checking Low-Balance Shield",
              defaultSavings,
              defaultChecking,
              "BALANCE_BELOW",
              5000.0,
              10000.0
            )
          }
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

        TemplateRow(
          icon = Icons.Default.Savings,
          title = "💰 Surplus Auto-Sweep",
          desc = "If $defaultChecking > ₹50,000, sweep excess into $defaultSavings",
          onClick = {
            onSelectTemplate(
              "Excess Cash Auto-Sweep",
              defaultChecking,
              defaultSavings,
              "BALANCE_ABOVE",
              50000.0,
              15000.0
            )
          }
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

        TemplateRow(
          icon = Icons.Default.FlashOn,
          title = "⚡ Cash Wallet Top-Up",
          desc = "If $defaultCash < ₹1,000, refill ₹2,500 from $defaultChecking",
          onClick = {
            onSelectTemplate(
              "Cash Wallet Auto-Refill",
              defaultChecking,
              defaultCash,
              "BALANCE_BELOW",
              1000.0,
              2500.0
            )
          }
        )
      }
    }
  }
}

@Composable
private fun TemplateRow(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  title: String,
  desc: String,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(10.dp))
      .clickable(onClick = onClick)
      .padding(8.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(18.dp)
        )
      }
      Spacer(modifier = Modifier.width(10.dp))
      Column {
        Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        Text(text = desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
      }
    }
    Text(
      text = "Use",
      style = MaterialTheme.typography.labelSmall,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.primary,
      modifier = Modifier.padding(start = 8.dp)
    )
  }
}

@Composable
private fun MandateItemCard(
  mandate: ConditionalMandateEntity,
  currencySymbol: String,
  onToggle: () -> Unit,
  onEdit: () -> Unit,
  onDelete: () -> Unit
) {
  val isRefill = mandate.conditionType.uppercase() == "BALANCE_BELOW"
  val badgeColor = if (isRefill) Color(0xFFF59E0B) else Color(0xFF10B981)
  val badgeLabel = if (isRefill) "LOW-BALANCE REFILL" else "SURPLUS SWEEP"

  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (mandate.isEnabled) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = if (mandate.isEnabled) 2.dp else 0.dp),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("mandate_card_${mandate.id}")
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      // Top header: Badge + Switch
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = badgeColor.copy(alpha = 0.15f)
        ) {
          Text(
            text = badgeLabel,
            color = badgeColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }

        Switch(
          checked = mandate.isEnabled,
          onCheckedChange = { onToggle() },
          colors = SwitchDefaults.colors(
            checkedThumbColor = MaterialTheme.colorScheme.primary,
            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
          ),
          modifier = Modifier.testTag("mandate_switch_${mandate.id}")
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = mandate.title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Visual Account Flow: Source -> Amount -> Target
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "FROM",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 10.sp
            )
            Text(
              text = mandate.sourceAccount,
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }

          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 8.dp)
          ) {
            Text(
              text = "$currencySymbol${"%,.0f".format(mandate.transferAmount)}",
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(16.dp)
            )
          }

          Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.End
          ) {
            Text(
              text = "TO",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 10.sp
            )
            Text(
              text = mandate.targetAccount,
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Trigger condition explanation
      Text(
        text = if (isRefill) {
          "Condition: When ${mandate.targetAccount} balance is below $currencySymbol${"%,.0f".format(mandate.thresholdAmount)}"
        } else {
          "Condition: When ${mandate.sourceAccount} balance exceeds $currencySymbol${"%,.0f".format(mandate.thresholdAmount)}"
        },
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      if (mandate.notes.isNotBlank()) {
        Text(
          text = mandate.notes,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.outline,
          modifier = Modifier.padding(top = 2.dp)
        )
      }

      Spacer(modifier = Modifier.height(10.dp))
      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
      Spacer(modifier = Modifier.height(8.dp))

      // Footer: Stats & Actions
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          val lastTriggeredStr = if (mandate.lastTriggeredAt == 0L) {
            "Never triggered"
          } else {
            "Last: " + SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(mandate.lastTriggeredAt))
          }
          Text(
            text = lastTriggeredStr,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "${mandate.totalTriggeredCount} transfer(s) executed",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
          )
        }

        Row {
          IconButton(
            onClick = onEdit,
            modifier = Modifier.size(36.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Edit,
              contentDescription = "Edit Mandate",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(18.dp)
            )
          }
          IconButton(
            onClick = onDelete,
            modifier = Modifier.size(36.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Delete,
              contentDescription = "Delete Mandate",
              tint = MaterialTheme.colorScheme.error,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
private fun EmptyMandatesCard(onCreateClick: () -> Unit) {
  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(32.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Box(
        modifier = Modifier
          .size(64.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.SwapHoriz,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(32.dp)
        )
      }
      Spacer(modifier = Modifier.height(16.dp))
      Text(
        text = "No Conditional Mandates Active",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = "Set up automated transfer rules to protect your accounts from low balances or automatically sweep surplus into savings.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
      )
      Spacer(modifier = Modifier.height(18.dp))
      Button(
        onClick = onCreateClick,
        shape = RoundedCornerShape(12.dp)
      ) {
        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Create First Mandate")
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMandateDialog(
  mandate: ConditionalMandateEntity?,
  accounts: List<AccountEntity>,
  currencySymbol: String,
  onDismiss: () -> Unit,
  onSave: (ConditionalMandateEntity) -> Unit
) {
  val accountNames = accounts.map { it.name }.ifEmpty { listOf("Checking Account", "Savings", "Cash") }

  var title by remember { mutableStateOf(mandate?.title ?: "") }
  var conditionType by remember { mutableStateOf(mandate?.conditionType ?: "BALANCE_BELOW") }
  var sourceAccount by remember {
    mutableStateOf(
      mandate?.sourceAccount ?: accountNames.firstOrNull { it.contains("Savings", ignoreCase = true) } ?: accountNames.first()
    )
  }
  var targetAccount by remember {
    mutableStateOf(
      mandate?.targetAccount ?: accountNames.firstOrNull { it.contains("Checking", ignoreCase = true) } ?: accountNames.getOrElse(1) { accountNames.first() }
    )
  }
  var thresholdAmount by remember { mutableStateOf(mandate?.thresholdAmount?.let { if (it > 0) it.toLong().toString() else "" } ?: "5000") }
  var transferAmount by remember { mutableStateOf(mandate?.transferAmount?.let { if (it > 0) it.toLong().toString() else "" } ?: "10000") }
  var isEnabled by remember { mutableStateOf(mandate?.isEnabled ?: true) }
  var notes by remember { mutableStateOf(mandate?.notes ?: "") }

  var sourceExpanded by remember { mutableStateOf(false) }
  var targetExpanded by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = if (mandate == null || mandate.id == 0L) "New Transfer Mandate" else "Edit Mandate",
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // Condition Type Picker
        item {
          Text(text = "Rule Type", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            ConditionTypeChip(
              title = "Low-Balance Refill",
              subtitle = "Refill when below target",
              isSelected = conditionType == "BALANCE_BELOW",
              onClick = { conditionType = "BALANCE_BELOW" },
              modifier = Modifier.weight(1f)
            )
            ConditionTypeChip(
              title = "Surplus Sweep",
              subtitle = "Sweep excess into savings",
              isSelected = conditionType == "BALANCE_ABOVE",
              onClick = { conditionType = "BALANCE_ABOVE" },
              modifier = Modifier.weight(1f)
            )
          }
        }

        // Title
        item {
          OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Mandate Name") },
            placeholder = { Text(if (conditionType == "BALANCE_BELOW") "Checking Balance Shield" else "Surplus Cash Sweep") },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("mandate_title_input")
          )
        }

        // Target Account (the account monitored for BALANCE_BELOW, or receiving in BALANCE_ABOVE)
        item {
          ExposedDropdownMenuBox(
            expanded = targetExpanded,
            onExpandedChange = { targetExpanded = it },
            modifier = Modifier.fillMaxWidth()
          ) {
            OutlinedTextField(
              value = targetAccount,
              onValueChange = {},
              readOnly = true,
              label = {
                Text(if (conditionType == "BALANCE_BELOW") "Protected Target Account (Monitored)" else "Destination Account (Receives Sweep)")
              },
              trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = targetExpanded) },
              modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
            )
            ExposedDropdownMenu(
              expanded = targetExpanded,
              onDismissRequest = { targetExpanded = false }
            ) {
              accountNames.forEach { name ->
                DropdownMenuItem(
                  text = { Text(name) },
                  onClick = {
                    targetAccount = name
                    targetExpanded = false
                  }
                )
              }
            }
          }
        }

        // Source Account (the account that funds the transfer)
        item {
          ExposedDropdownMenuBox(
            expanded = sourceExpanded,
            onExpandedChange = { sourceExpanded = it },
            modifier = Modifier.fillMaxWidth()
          ) {
            OutlinedTextField(
              value = sourceAccount,
              onValueChange = {},
              readOnly = true,
              label = {
                Text(if (conditionType == "BALANCE_BELOW") "Funding Source Account" else "Surplus Source Account (Monitored)")
              },
              trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sourceExpanded) },
              modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
            )
            ExposedDropdownMenu(
              expanded = sourceExpanded,
              onDismissRequest = { sourceExpanded = false }
            ) {
              accountNames.forEach { name ->
                DropdownMenuItem(
                  text = { Text(name) },
                  onClick = {
                    sourceAccount = name
                    sourceExpanded = false
                  }
                )
              }
            }
          }
        }

        // Threshold Amount
        item {
          OutlinedTextField(
            value = thresholdAmount,
            onValueChange = { thresholdAmount = it },
            label = {
              Text(if (conditionType == "BALANCE_BELOW") "Trigger when balance falls below ($currencySymbol)" else "Trigger when balance exceeds ($currencySymbol)")
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("mandate_threshold_input")
          )
        }

        // Transfer Amount
        item {
          OutlinedTextField(
            value = transferAmount,
            onValueChange = { transferAmount = it },
            label = { Text("Transfer Amount ($currencySymbol)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("mandate_amount_input")
          )
        }

        // Optional Notes
        item {
          OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes (Optional)") },
            placeholder = { Text("Keep checking balance above minimum to avoid penalties") },
            maxLines = 2,
            modifier = Modifier.fillMaxWidth()
          )
        }

        // Enabled Toggle
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("Enable Mandate Immediately", style = MaterialTheme.typography.bodyMedium)
            Switch(
              checked = isEnabled,
              onCheckedChange = { isEnabled = it }
            )
          }
        }

        // Error message if any
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
          val thresh = thresholdAmount.toDoubleOrNull() ?: 0.0
          val amt = transferAmount.toDoubleOrNull() ?: 0.0
          if (title.isBlank()) {
            title = if (conditionType == "BALANCE_BELOW") "Auto-Refill to $targetAccount" else "Auto-Sweep to $targetAccount"
          }
          if (sourceAccount.equals(targetAccount, ignoreCase = true)) {
            errorMessage = "Source and Target accounts cannot be the same."
            return@Button
          }
          if (thresh <= 0.0 || amt <= 0.0) {
            errorMessage = "Please enter valid threshold and transfer amounts."
            return@Button
          }

          onSave(
            ConditionalMandateEntity(
              id = mandate?.id ?: 0L,
              title = title.trim(),
              sourceAccount = sourceAccount,
              targetAccount = targetAccount,
              conditionType = conditionType,
              thresholdAmount = thresh,
              transferAmount = amt,
              isEnabled = isEnabled,
              lastTriggeredAt = mandate?.lastTriggeredAt ?: 0L,
              totalTriggeredCount = mandate?.totalTriggeredCount ?: 0,
              notes = notes.trim()
            )
          )
        },
        modifier = Modifier.testTag("mandate_save_btn")
      ) {
        Text("Save Mandate")
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
private fun ConditionTypeChip(
  title: String,
  subtitle: String,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
    modifier = modifier
      .clip(RoundedCornerShape(12.dp))
      .clickable(onClick = onClick)
  ) {
    Column(modifier = Modifier.padding(10.dp)) {
      Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall,
        fontSize = 11.sp,
        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}
