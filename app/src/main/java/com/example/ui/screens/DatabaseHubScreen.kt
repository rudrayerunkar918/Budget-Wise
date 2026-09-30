package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.backup.DataBackupManager
import com.example.data.backup.DatabaseSnapshotInfo
import com.example.data.backup.ImportMode
import com.example.ui.viewmodel.ExpenseViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatabaseHubScreen(
  viewModel: ExpenseViewModel,
  onBackClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  BackHandler { onBackClick() }

  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  val databaseHealth by viewModel.databaseHealth.collectAsStateWithLifecycle()
  val snapshots by viewModel.databaseSnapshots.collectAsStateWithLifecycle()
  val autoBackupEnabled by viewModel.autoBackupEnabled.collectAsStateWithLifecycle()
  val autoBackupInterval by viewModel.autoBackupIntervalHours.collectAsStateWithLifecycle()
  val lastAutoBackupTime by viewModel.lastAutoBackupTimestamp.collectAsStateWithLifecycle()
  val isDatabaseBusy by viewModel.isDatabaseBusy.collectAsStateWithLifecycle()

  var showTableBreakdown by remember { mutableStateOf(false) }
  var showCreateSnapshotDialog by remember { mutableStateOf(false) }
  var snapshotToRestore by remember { mutableStateOf<DatabaseSnapshotInfo?>(null) }
  var snapshotToDelete by remember { mutableStateOf<DatabaseSnapshotInfo?>(null) }
  var showEncryptedExportDialog by remember { mutableStateOf(false) }
  var fileToImportUri by remember { mutableStateOf<Uri?>(null) }
  var importPassphraseRequired by remember { mutableStateOf(false) }
  var importPassphraseInput by remember { mutableStateOf("") }
  var pendingImportMode by remember { mutableStateOf(ImportMode.MERGE) }
  var showResetConfirmDialog by remember { mutableStateOf(false) }

  // Initial load of health and snapshots
  LaunchedEffect(Unit) {
    viewModel.loadDatabaseHealth()
    viewModel.loadSnapshots()
  }

  // File Picker Launcher for Restoring Backup File
  val filePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    if (uri != null) {
      fileToImportUri = uri
      scope.launch {
        try {
          val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
          if (bytes != null) {
            if (DataBackupManager.isEncryptedPayload(bytes)) {
              importPassphraseRequired = true
            } else {
              importPassphraseRequired = false
            }
          }
        } catch (e: Exception) {
          Toast.makeText(context, "Could not inspect file: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
      }
    }
  }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "Database & Backups",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Encrypted Local Storage • Room SQLite v9",
              style = MaterialTheme.typography.bodySmall,
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        },
        navigationIcon = {
          IconButton(
            onClick = onBackClick,
            modifier = Modifier.testTag("btn_back_database_hub")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back"
            )
          }
        },
        actions = {
          IconButton(
            onClick = {
              viewModel.loadDatabaseHealth()
              viewModel.loadSnapshots()
              Toast.makeText(context, "Database metrics updated", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.testTag("btn_refresh_database_hub")
          ) {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = "Refresh Database Metrics"
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    }
  ) { innerPadding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .padding(horizontal = 16.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

      // 1. LIVE DATABASE HEALTH & DIAGNOSTICS CARD
      item {
        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("card_database_health")
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(44.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = Icons.Default.Storage,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                  )
                }
              }

              Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = "Room SQLite Database",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF1B664B).copy(alpha = 0.15f)
                  ) {
                    Text(
                      text = "v9",
                      color = Color(0xFF1B664B),
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                  }
                }
                Text(
                  text = "File: expense_tracker.db",
                  style = MaterialTheme.typography.bodySmall,
                  fontSize = 12.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            // Key Metrics Row
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              MetricChip(
                label = "DB Storage Size",
                value = databaseHealth?.formattedSize ?: "Calculating...",
                icon = Icons.Default.Storage,
                modifier = Modifier.weight(1f)
              )
              MetricChip(
                label = "Total Records",
                value = "${databaseHealth?.totalRecords ?: 0} items",
                icon = Icons.Default.AccountBalance,
                modifier = Modifier.weight(1f)
              )
            }

            // Integrity Check Status
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .background(
                  color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                  shape = RoundedCornerShape(10.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.CheckCircle,
                  contentDescription = null,
                  tint = Color(0xFF1B664B),
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "SQLite Integrity:",
                  style = MaterialTheme.typography.bodySmall,
                  fontWeight = FontWeight.Medium
                )
              }
              Text(
                text = databaseHealth?.integrityStatus ?: "OK (Verified)",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B664B)
              )
            }

            // Actions: Table Breakdown & Vacuum
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              OutlinedButton(
                onClick = { showTableBreakdown = !showTableBreakdown },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
              ) {
                Text(if (showTableBreakdown) "Hide Tables" else "View 11 Tables")
              }

              Button(
                onClick = {
                  viewModel.optimizeDatabase { msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                  }
                },
                enabled = !isDatabaseBusy,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                  containerColor = MaterialTheme.colorScheme.secondary
                )
              ) {
                if (isDatabaseBusy) {
                  CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.onSecondary,
                    strokeWidth = 2.dp
                  )
                } else {
                  Icon(imageVector = Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Vacuum DB")
                }
              }
            }

            // Expandable Table Count Breakdown
            AnimatedVisibility(visible = showTableBreakdown) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                  .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Text(
                  text = "Entities & Table Counts",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
                databaseHealth?.tableCounts?.forEach { (tableName, count) ->
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(text = tableName, style = MaterialTheme.typography.bodySmall)
                    Surface(
                      shape = RoundedCornerShape(6.dp),
                      color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                      Text(
                        text = "$count",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                      )
                    }
                  }
                }
              }
            }
          }
        }
      }

      // 1.5 CLEAN SLATE & STARTER DATA CONTROLS
      item {
        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("card_clean_slate_management")
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.size(36.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(20.dp)
                  )
                }
              }
              Column {
                Text(
                  text = "Clean Slate & Data Management",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "Startup seeding is permanently disabled. Start blank or test with sample data.",
                  style = MaterialTheme.typography.bodySmall,
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              OutlinedButton(
                onClick = { showResetConfirmDialog = true },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                  contentColor = MaterialTheme.colorScheme.error
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
              ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Wipe All Data", fontSize = 12.sp)
              }

              Button(
                onClick = {
                  viewModel.loadSampleDataManually {
                    viewModel.loadDatabaseHealth()
                    Toast.makeText(context, "Sample demo data loaded", Toast.LENGTH_SHORT).show()
                  }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                  containerColor = MaterialTheme.colorScheme.primary
                )
              ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Load Sample Data", fontSize = 12.sp)
              }
            }
          }
        }
      }

      // 2. AUTOMATED BACKGROUND BACKUPS (WORKMANAGER)
      item {
        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("card_auto_backup_settings")
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
              ) {
                Surface(
                  shape = CircleShape,
                  color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                  modifier = Modifier.size(36.dp)
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Icon(
                      imageVector = Icons.Default.AutoAwesome,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.size(20.dp)
                    )
                  }
                }
                Column {
                  Text(
                    text = "Automated Local Snapshots",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = "Rolling background backups via WorkManager",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }

              Switch(
                checked = autoBackupEnabled,
                onCheckedChange = { checked ->
                  viewModel.toggleAutoBackup(checked)
                  val status = if (checked) "Automated snapshots scheduled" else "Automated snapshots disabled"
                  Toast.makeText(context, status, Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.testTag("switch_auto_backup")
              )
            }

            AnimatedVisibility(visible = autoBackupEnabled) {
              Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                  text = "Backup Frequency:",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  FilterChip(
                    selected = autoBackupInterval == 24,
                    onClick = { viewModel.setAutoBackupInterval(24) },
                    label = { Text("Daily (24 Hours)") }
                  )
                  FilterChip(
                    selected = autoBackupInterval == 168,
                    onClick = { viewModel.setAutoBackupInterval(168) },
                    label = { Text("Weekly (7 Days)") }
                  )
                }

                val lastFormatted = if (lastAutoBackupTime > 0) {
                  SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(lastAutoBackupTime))
                } else {
                  "Scheduled (Pending idle execution)"
                }

                Text(
                  text = "• Last background snapshot: $lastFormatted\n• Retains the latest 7 snapshots automatically to protect local storage.",
                  style = MaterialTheme.typography.bodySmall,
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }
      }

      // 3. RECENT LOCAL SNAPSHOTS
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "LOCAL SNAPSHOTS",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
            Text(
              text = "${snapshots.size} snapshots available for 1-tap restore",
              style = MaterialTheme.typography.bodySmall,
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          Button(
            onClick = { showCreateSnapshotDialog = true },
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            modifier = Modifier.testTag("btn_create_snapshot_now")
          ) {
            Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Create Snapshot", fontSize = 12.sp)
          }
        }
      }

      if (snapshots.isEmpty()) {
        item {
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Restore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(36.dp)
              )
              Text(
                text = "No snapshots created yet",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Tap 'Create Snapshot' to take an instant point-in-time image of all your financial records.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
              )
            }
          }
        }
      } else {
        items(snapshots) { snap ->
          SnapshotItemCard(
            snapshot = snap,
            onRestore = { snapshotToRestore = snap },
            onShare = {
              val mime = if (snap.isEncrypted) "application/octet-stream" else "application/json"
              DataBackupManager.shareFile(context, snap.file, mime, "BudgetWise Snapshot ${snap.formattedDate}")
            },
            onDelete = { snapshotToDelete = snap }
          )
        }
      }

      // 4. MANUAL EXPORT & ENCRYPTION OPTIONS
      item {
        Text(
          text = "ENCRYPTED EXPORT & MANUAL BACKUP",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier.padding(top = 8.dp)
        )
      }

      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
          ),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Surface(
                shape = CircleShape,
                color = Color(0xFF1B664B).copy(alpha = 0.15f),
                modifier = Modifier.size(36.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = Color(0xFF1B664B),
                    modifier = Modifier.size(20.dp)
                  )
                }
              }
              Column {
                Text(
                  text = "AES-256 GCM Encrypted Backup",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "Lock your financial export with a password / PIN",
                  style = MaterialTheme.typography.bodySmall,
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Button(
                onClick = { showEncryptedExportDialog = true },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B664B))
              ) {
                Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Export Encrypted", fontSize = 12.sp)
              }

              OutlinedButton(
                onClick = {
                  viewModel.exportAllDataJson(context) { file, _ ->
                    DataBackupManager.shareFile(context, file, "application/json", "BudgetWise Backup JSON")
                  }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
              ) {
                Icon(imageVector = Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Plain JSON", fontSize = 12.sp)
              }
            }

            OutlinedButton(
              onClick = { filePickerLauncher.launch("*/*") },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(10.dp)
            ) {
              Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Import / Restore From Backup File")
            }
          }
        }
      }

      // 5. GOOGLE DRIVE CLOUD STORAGE
      item {
        Text(
          text = "GOOGLE DRIVE CLOUD STORAGE",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier.padding(top = 8.dp)
        )
      }

      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
          ),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Surface(
                shape = CircleShape,
                color = Color(0xFF1A73E8).copy(alpha = 0.15f),
                modifier = Modifier.size(36.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = null,
                    tint = Color(0xFF1A73E8),
                    modifier = Modifier.size(20.dp)
                  )
                }
              }
              Column {
                Text(
                  text = "Google Drive Cloud Storage",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "Sync backups to the cloud or open your Google Drive",
                  style = MaterialTheme.typography.bodySmall,
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            Text(
              text = "Upload your encrypted or plain database backups directly to Google Drive, or quickly jump into your Drive folder to inspect your saved files.",
              style = MaterialTheme.typography.bodySmall,
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Button(
                onClick = {
                  viewModel.exportAllDataJson(context) { file, _ ->
                    DataBackupManager.uploadBackupToGoogleDrive(
                      context = context,
                      file = file,
                      mimeType = "application/json",
                      title = "BudgetWise Backup"
                    )
                  }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A73E8))
              ) {
                Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Upload to Drive", fontSize = 12.sp)
              }

              OutlinedButton(
                onClick = { DataBackupManager.openGoogleDrive(context) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
              ) {
                Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Open Drive", fontSize = 12.sp)
              }
            }
          }
        }
      }

      item { Spacer(modifier = Modifier.height(24.dp)) }
    }
  }

  // ==========================================
  // DIALOGS
  // ==========================================

  // 1. Create Snapshot Dialog (with optional encryption)
  if (showCreateSnapshotDialog) {
    var protectWithPassword by remember { mutableStateOf(false) }
    var passwordInput by remember { mutableStateOf("") }

    AlertDialog(
      onDismissRequest = { showCreateSnapshotDialog = false },
      title = { Text("Create Point-in-Time Snapshot") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "This will capture all current accounts, transactions, investments, and settings into a secure local snapshot.",
            style = MaterialTheme.typography.bodySmall
          )

          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .fillMaxWidth()
              .clickable { protectWithPassword = !protectWithPassword }
              .padding(vertical = 4.dp)
          ) {
            Switch(
              checked = protectWithPassword,
              onCheckedChange = { protectWithPassword = it }
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text("Encrypt with Passphrase", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
              Text("Protects snapshot with AES-256 GCM", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }

          AnimatedVisibility(visible = protectWithPassword) {
            OutlinedTextField(
              value = passwordInput,
              onValueChange = { passwordInput = it },
              label = { Text("Enter Passphrase") },
              visualTransformation = PasswordVisualTransformation(),
              modifier = Modifier.fillMaxWidth(),
              singleLine = true
            )
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val pass = if (protectWithPassword && passwordInput.isNotBlank()) passwordInput.trim() else null
            viewModel.createManualSnapshot(pass) { success, msg ->
              Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
            showCreateSnapshotDialog = false
          },
          enabled = !protectWithPassword || passwordInput.isNotBlank()
        ) {
          Text("Create")
        }
      },
      dismissButton = {
        TextButton(onClick = { showCreateSnapshotDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // 2. Encrypted Export Dialog
  if (showEncryptedExportDialog) {
    var exportPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordMismatch by remember { mutableStateOf(false) }

    AlertDialog(
      onDismissRequest = { showEncryptedExportDialog = false },
      title = { Text("Export AES-256 Encrypted Backup") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "Create a password-protected `.bwise` backup file. The backup cannot be opened without this passphrase.",
            style = MaterialTheme.typography.bodySmall
          )

          OutlinedTextField(
            value = exportPassword,
            onValueChange = {
              exportPassword = it
              passwordMismatch = false
            },
            label = { Text("Passphrase / PIN") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )

          OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
              confirmPassword = it
              passwordMismatch = false
            },
            label = { Text("Confirm Passphrase") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = passwordMismatch
          )

          if (passwordMismatch) {
            Text("Passphrases do not match", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (exportPassword != confirmPassword) {
              passwordMismatch = true
              return@Button
            }
            viewModel.exportEncryptedBackup(context, exportPassword.trim()) { file ->
              DataBackupManager.shareFile(context, file, "application/octet-stream", "BudgetWise Encrypted Backup")
              showEncryptedExportDialog = false
            }
          },
          enabled = exportPassword.isNotBlank() && confirmPassword.isNotBlank()
        ) {
          Text("Export")
        }
      },
      dismissButton = {
        TextButton(onClick = { showEncryptedExportDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // 3. Restore Snapshot Confirmation Dialog
  snapshotToRestore?.let { snap ->
    var restoreMode by remember { mutableStateOf(ImportMode.MERGE) }
    var restorePassphrase by remember { mutableStateOf("") }

    AlertDialog(
      onDismissRequest = { snapshotToRestore = null },
      title = { Text("Restore Snapshot") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "Restore snapshot from ${snap.formattedDate} (${snap.formattedSize})",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
          )

          if (snap.isEncrypted) {
            Text(
              text = "This snapshot is encrypted. Please enter its passphrase:",
              style = MaterialTheme.typography.bodySmall,
              color = Color(0xFF1B664B)
            )
            OutlinedTextField(
              value = restorePassphrase,
              onValueChange = { restorePassphrase = it },
              label = { Text("Passphrase") },
              visualTransformation = PasswordVisualTransformation(),
              modifier = Modifier.fillMaxWidth(),
              singleLine = true
            )
          }

          Text(text = "Restore Mode:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { restoreMode = ImportMode.MERGE },
            verticalAlignment = Alignment.CenterVertically
          ) {
            RadioButton(selected = restoreMode == ImportMode.MERGE, onClick = { restoreMode = ImportMode.MERGE })
            Spacer(modifier = Modifier.width(6.dp))
            Column {
              Text("Merge Data", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
              Text("Adds snapshot records without deleting current entries", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { restoreMode = ImportMode.REPLACE },
            verticalAlignment = Alignment.CenterVertically
          ) {
            RadioButton(selected = restoreMode == ImportMode.REPLACE, onClick = { restoreMode = ImportMode.REPLACE })
            Spacer(modifier = Modifier.width(6.dp))
            Column {
              Text("Replace All Data", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
              Text("Overwrites current database with this snapshot", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.restoreFromSnapshot(
              snapshot = snap,
              mode = restoreMode,
              passphrase = if (snap.isEncrypted) restorePassphrase.trim() else null
            ) { success, msg ->
              Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
              if (success) {
                snapshotToRestore = null
              }
            }
          },
          enabled = !snap.isEncrypted || restorePassphrase.isNotBlank()
        ) {
          Text("Confirm Restore")
        }
      },
      dismissButton = {
        TextButton(onClick = { snapshotToRestore = null }) {
          Text("Cancel")
        }
      }
    )
  }

  // 4. Delete Snapshot Dialog
  snapshotToDelete?.let { snap ->
    AlertDialog(
      onDismissRequest = { snapshotToDelete = null },
      title = { Text("Delete Snapshot") },
      text = {
        Text("Are you sure you want to delete the snapshot from ${snap.formattedDate}? This cannot be undone.")
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.deleteSnapshot(snap)
            Toast.makeText(context, "Snapshot deleted", Toast.LENGTH_SHORT).show()
            snapshotToDelete = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
          Text("Delete")
        }
      },
      dismissButton = {
        TextButton(onClick = { snapshotToDelete = null }) {
          Text("Cancel")
        }
      }
    )
  }

  // 5. Restore From Selected URI Dialog
  fileToImportUri?.let { uri ->
    AlertDialog(
      onDismissRequest = {
        fileToImportUri = null
        importPassphraseInput = ""
      },
      title = { Text("Restore From File") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("Selected file: ${uri.lastPathSegment ?: "backup file"}", style = MaterialTheme.typography.bodySmall)

          if (importPassphraseRequired) {
            Text(
              text = "This file is encrypted with AES-256. Enter the passphrase to decrypt and restore:",
              style = MaterialTheme.typography.bodySmall,
              color = Color(0xFF1B664B),
              fontWeight = FontWeight.SemiBold
            )
            OutlinedTextField(
              value = importPassphraseInput,
              onValueChange = { importPassphraseInput = it },
              label = { Text("Passphrase") },
              visualTransformation = PasswordVisualTransformation(),
              modifier = Modifier.fillMaxWidth(),
              singleLine = true
            )
          }

          Text("Restore Mode:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { pendingImportMode = ImportMode.MERGE },
            verticalAlignment = Alignment.CenterVertically
          ) {
            RadioButton(selected = pendingImportMode == ImportMode.MERGE, onClick = { pendingImportMode = ImportMode.MERGE })
            Spacer(modifier = Modifier.width(6.dp))
            Column {
              Text("Merge Data", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
              Text("Preserves existing database and adds imported records", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { pendingImportMode = ImportMode.REPLACE },
            verticalAlignment = Alignment.CenterVertically
          ) {
            RadioButton(selected = pendingImportMode == ImportMode.REPLACE, onClick = { pendingImportMode = ImportMode.REPLACE })
            Spacer(modifier = Modifier.width(6.dp))
            Column {
              Text("Replace All Data", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
              Text("Clears current database and loads this backup file", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            scope.launch {
              try {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes == null) {
                  Toast.makeText(context, "File is empty", Toast.LENGTH_SHORT).show()
                  return@launch
                }
                val jsonString = if (importPassphraseRequired) {
                  DataBackupManager.decryptPayload(bytes, importPassphraseInput.trim())
                } else {
                  String(bytes, java.nio.charset.StandardCharsets.UTF_8)
                }

                viewModel.importBackupData(context, jsonString, pendingImportMode) { result ->
                  if (result.isSuccess) {
                    val stats = result.getOrNull()
                    Toast.makeText(context, "Restored ${stats?.totalImported ?: 0} records!", Toast.LENGTH_LONG).show()
                    fileToImportUri = null
                    importPassphraseInput = ""
                  } else {
                    Toast.makeText(context, "Restore failed: ${result.exceptionOrNull()?.localizedMessage}", Toast.LENGTH_LONG).show()
                  }
                }
              } catch (e: Exception) {
                Toast.makeText(context, "Decryption error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
              }
            }
          },
          enabled = !importPassphraseRequired || importPassphraseInput.isNotBlank()
        ) {
          Text("Restore Now")
        }
      },
      dismissButton = {
        TextButton(onClick = {
          fileToImportUri = null
          importPassphraseInput = ""
        }) {
          Text("Cancel")
        }
      }
    )
  }

  if (showResetConfirmDialog) {
    AlertDialog(
      onDismissRequest = { showResetConfirmDialog = false },
      title = { Text("Wipe Database to Clean Slate?") },
      text = {
        Text("This will permanently remove all transactions, accounts, stocks, budgets, and loans from your device. You will have a completely clean, empty database.")
      },
      confirmButton = {
        Button(
          onClick = {
            showResetConfirmDialog = false
            viewModel.clearAllDataCompletely {
              viewModel.loadDatabaseHealth()
              Toast.makeText(context, "Database wiped to clean slate", Toast.LENGTH_LONG).show()
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
          Text("Wipe Everything")
        }
      },
      dismissButton = {
        TextButton(onClick = { showResetConfirmDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
private fun MetricChip(
  label: String,
  value: String,
  icon: ImageVector,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = MaterialTheme.colorScheme.surface,
    modifier = modifier
  ) {
    Row(
      modifier = Modifier.padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(20.dp)
      )
      Column {
        Text(
          text = label,
          style = MaterialTheme.typography.bodySmall,
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
          text = value,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold
        )
      }
    }
  }
}

@Composable
private fun SnapshotItemCard(
  snapshot: DatabaseSnapshotInfo,
  onRestore: () -> Unit,
  onShare: () -> Unit,
  onDelete: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    ),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Surface(
        shape = CircleShape,
        color = if (snapshot.isEncrypted) Color(0xFF1B664B).copy(alpha = 0.15f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
        modifier = Modifier.size(38.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = if (snapshot.isEncrypted) Icons.Default.Lock else if (snapshot.isAutomated) Icons.Default.AutoAwesome else Icons.Default.Storage,
            contentDescription = null,
            tint = if (snapshot.isEncrypted) Color(0xFF1B664B) else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
          )
        }
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = snapshot.formattedDate,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.width(6.dp))
          if (snapshot.isAutomated) {
            Surface(
              shape = RoundedCornerShape(4.dp),
              color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            ) {
              Text(
                text = "Auto",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
              )
            }
          }
          if (snapshot.isEncrypted) {
            Spacer(modifier = Modifier.width(4.dp))
            Surface(
              shape = RoundedCornerShape(4.dp),
              color = Color(0xFF1B664B).copy(alpha = 0.15f)
            ) {
              Text(
                text = "AES-256",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B664B),
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
              )
            }
          }
        }

        Text(
          text = "${snapshot.formattedSize} • ${if (snapshot.recordCount > 0) "${snapshot.recordCount} items" else "Snapshot image"}",
          style = MaterialTheme.typography.bodySmall,
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      IconButton(onClick = onRestore, modifier = Modifier.size(36.dp)) {
        Icon(imageVector = Icons.Default.Restore, contentDescription = "Restore", tint = MaterialTheme.colorScheme.primary)
      }

      IconButton(onClick = onShare, modifier = Modifier.size(36.dp)) {
        Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.onSurfaceVariant)
      }

      IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
      }
    }
  }
}
