package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import android.content.Intent
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.MutualFundCatalog
import com.example.data.api.MutualFundDirectoryManager
import com.example.data.api.StockDatabaseCatalog
import com.example.data.api.StockDirectoryManager
import com.example.data.backup.BackupSummary
import com.example.data.backup.DataBackupManager
import com.example.data.backup.ImportMode
import com.example.data.backup.ImportResultStats
import com.example.data.preferences.NavTabDestination
import com.example.data.preferences.ThemeMode
import com.example.ui.viewmodel.ExpenseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

enum class DeleteTargetOption {
  ALL,
  TRANSACTIONS,
  PORTFOLIO,
  LOANS_SUBS
}

@Composable
fun MoreScreen(
  themeMode: ThemeMode,
  currencySymbol: String,
  userName: String,
  onOpenThemeDialog: () -> Unit,
  onOpenNavBarDialog: () -> Unit,
  onOpenCurrencyDialog: () -> Unit,
  onOpenEditHomeScreen: () -> Unit,
  onNavigateToTab: (NavTabDestination) -> Unit,
  onOpenNotifications: () -> Unit,
  onOpenGeminiAssistant: () -> Unit = {},
  viewModel: ExpenseViewModel? = null,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  var showExportDialog by remember { mutableStateOf(false) }
  var showImportDialog by remember { mutableStateOf(false) }
  var showEditUserNameDialog by remember { mutableStateOf(false) }
  var showDeleteDataDialog by remember { mutableStateOf(false) }
  var showMandatesScreen by remember { mutableStateOf(false) }
  var showFinancialReportScreen by remember { mutableStateOf(false) }
  var showDatabaseHubScreen by remember { mutableStateOf(false) }
  var showStockApiKeysDialog by remember { mutableStateOf(false) }

  if (showDatabaseHubScreen && viewModel != null) {
    DatabaseHubScreen(
      viewModel = viewModel,
      onBackClick = { showDatabaseHubScreen = false }
    )
    return
  }

  if (showFinancialReportScreen && viewModel != null) {
    FinancialReportScreen(
      viewModel = viewModel,
      onNavigateBack = { showFinancialReportScreen = false }
    )
    return
  }

  if (showMandatesScreen && viewModel != null) {
    ConditionalMandatesScreen(
      viewModel = viewModel,
      onBackClick = { showMandatesScreen = false }
    )
    return
  }

  // Stock directory states
  val isSyncingIpos by viewModel?.isSyncingIpos?.collectAsState() ?: remember { mutableStateOf(false) }
  val totalDirectoryCount by viewModel?.totalDirectoryCount?.collectAsState() ?: remember {
    mutableStateOf(StockDirectoryManager.getTotalDirectoryCount())
  }
  val totalIpoCount by viewModel?.totalIpoCount?.collectAsState() ?: remember {
    mutableStateOf(StockDirectoryManager.getIpoCount())
  }
  val lastIpoSyncDate by viewModel?.lastIpoSyncDate?.collectAsState() ?: remember {
    mutableStateOf(StockDirectoryManager.getLastSyncDate(context))
  }
  val ipoSyncMessage by viewModel?.ipoSyncMessage?.collectAsState() ?: remember { mutableStateOf<String?>(null) }

  // Mutual Fund directory states
  val isSyncingMfs by viewModel?.isSyncingMfs?.collectAsState() ?: remember { mutableStateOf(false) }
  val mfSyncMessage by viewModel?.mfSyncMessage?.collectAsState() ?: remember { mutableStateOf<String?>(null) }
  val totalMfCount by viewModel?.totalMfCount?.collectAsState() ?: remember {
    mutableStateOf(MutualFundDirectoryManager.getTotalCount())
  }
  val totalNfoCount by viewModel?.totalNfoCount?.collectAsState() ?: remember {
    mutableStateOf(MutualFundDirectoryManager.getNfoCount())
  }
  val lastMfSyncDate by viewModel?.lastMfSyncDate?.collectAsState() ?: remember {
    mutableStateOf(MutualFundDirectoryManager.getLastSyncDate(context))
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Top Profile / Greeting Header
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier
          .fillMaxWidth()
          .clickable { showEditUserNameDialog = true }
          .testTag("card_more_profile")
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(44.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Person,
              contentDescription = "BudgetWise User Profile",
              tint = MaterialTheme.colorScheme.onPrimary,
              modifier = Modifier.padding(10.dp)
            )
          }

          Spacer(modifier = Modifier.width(14.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = if (userName.isBlank()) "BudgetWise User" else userName,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Tap to edit user name & preferences",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          IconButton(
            onClick = { showEditUserNameDialog = true },
            modifier = Modifier.testTag("btn_edit_user_name_profile")
          ) {
            Icon(
              imageVector = Icons.Default.Edit,
              contentDescription = "Edit User Name",
              tint = MaterialTheme.colorScheme.primary
            )
          }
        }
      }
    }

    // SECTION 1: DATA MANAGEMENT (IMPORT, EXPORT & RESET)
    item {
      Text(
        text = "DATA MANAGEMENT",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
      )
    }

    item {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        MoreSettingRow(
          title = "Database & Backups Hub",
          subtitle = "Room SQLite health, auto-snapshots, AES-256 encrypted backups & restore",
          icon = Icons.Default.Storage,
          onClick = { showDatabaseHubScreen = true },
          testTag = "more_item_database_hub"
        )

        MoreSettingRow(
          title = "Google Drive Cloud Storage",
          subtitle = "Upload backups to Google Drive or browse Drive storage",
          icon = Icons.Default.CloudUpload,
          onClick = { DataBackupManager.openGoogleDrive(context) },
          testTag = "more_item_google_drive"
        )

        MoreSettingRow(
          title = "Financial Report (PDF)",
          subtitle = "Download comprehensive audit with graphs & advice (1 wk – 2 yrs)",
          icon = Icons.Default.PictureAsPdf,
          onClick = { showFinancialReportScreen = true },
          testTag = "more_item_financial_report"
        )

        MoreSettingRow(
          title = "Export Data",
          subtitle = "Backup all expenses, accounts, stocks to JSON or CSV",
          icon = Icons.Default.Upload,
          onClick = { showExportDialog = true },
          testTag = "more_item_export"
        )

        MoreSettingRow(
          title = "Import Data",
          subtitle = "Restore or merge financial data from backup file",
          icon = Icons.Default.Download,
          onClick = { showImportDialog = true },
          testTag = "more_item_import"
        )

        MoreSettingRow(
          title = "Delete & Reset Data",
          subtitle = "Clear transactions, portfolios, loans, or reset all financial data",
          icon = Icons.Default.DeleteForever,
          onClick = { showDeleteDataDialog = true },
          testTag = "more_item_delete_data"
        )
      }
    }

    // SECTION: FINANCIAL AUDIT & PDF REPORTS
    item {
      Text(
        text = "FINANCIAL AUDIT & PDF REPORTS",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
      )
    }

    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("card_financial_reports_pdf")
      ) {
        Column(
          modifier = Modifier.padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Box(
              modifier = Modifier
                .size(44.dp)
                .background(Color(0xFF1B664B), CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.PictureAsPdf,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
              )
            }

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Financial Report (PDF)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Multi-page statement with graphs, category spend & AI suggestions (1 week – 2 years)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          Button(
            onClick = { showFinancialReportScreen = true },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("btn_open_financial_report_card"),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B664B))
          ) {
            Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create & Download PDF Report", fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    // SECTION: AUTOMATION & CONDITIONAL MANDATES
    item {
      Text(
        text = "AUTOMATION & CONDITIONAL MANDATES",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
      )
    }

    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        modifier = Modifier.fillMaxWidth().testTag("card_automation_mandates")
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          MoreSettingRow(
            title = "Conditional Transfer Mandates",
            subtitle = "Auto-refill low balances, surplus auto-sweeps",
            icon = Icons.Default.SwapHoriz,
            onClick = { showMandatesScreen = true },
            testTag = "more_item_mandates"
          )
        }
      }
    }

    // SECTION 2: STOCK DIRECTORY & DAILY IPO UPDATES
    item {
      Text(
        text = "STOCK DIRECTORY & IPO UPDATES",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
      )
    }

    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        modifier = Modifier.fillMaxWidth().testTag("card_stock_directory_status")
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(34.dp)
                  .clip(CircleShape)
                  .background(Color(0xFF2ECC71).copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.CloudSync,
                  contentDescription = null,
                  tint = Color(0xFF27AE60),
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "NSE & BSE Stock Directory",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "Auto-updated every 24h for new IPO listings",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  fontSize = 11.sp
                )
              }
            }
          }

          // Statistics Badges
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.surface,
              modifier = Modifier.weight(1f)
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Text(
                  text = "TOTAL EQUITIES",
                  style = MaterialTheme.typography.labelSmall,
                  fontSize = 9.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = "$totalDirectoryCount+ Stocks",
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
              }
            }

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.surface,
              modifier = Modifier.weight(1f)
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Text(
                  text = "TRACKED IPOS",
                  style = MaterialTheme.typography.labelSmall,
                  fontSize = 9.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = "$totalIpoCount Listed IPOs",
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF2E7D32)
                )
              }
            }
          }

          Text(
            text = "Last Synced: $lastIpoSyncDate",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
          )

          if (!ipoSyncMessage.isNullOrEmpty()) {
            Text(
              text = ipoSyncMessage ?: "",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.primary,
              fontSize = 11.sp,
              fontWeight = FontWeight.Medium
            )
          }

          Button(
            onClick = {
              if (viewModel != null) {
                viewModel.syncStockDirectoryNow(context) { msg ->
                  Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
              } else {
                scope.launch {
                  val summary = StockDirectoryManager.syncDailyIpos(context)
                  Toast.makeText(context, summary.statusMessage, Toast.LENGTH_SHORT).show()
                }
              }
            },
            enabled = !isSyncingIpos,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().testTag("btn_sync_ipo_directory")
          ) {
            if (isSyncingIpos) {
              CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text("Checking for New IPOs...")
            } else {
              Icon(
                imageVector = Icons.Default.CloudSync,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text("Check for New IPOs & Update Directory Now")
            }
          }
        }
      }
    }

    // SECTION 2B: MUTUAL FUND DIRECTORY (AMFI & NFOs)
    item {
      Text(
        text = "MUTUAL FUND DIRECTORY & NFOs",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF8E44AD),
        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
      )
    }

    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        modifier = Modifier.fillMaxWidth().testTag("card_mf_directory_status")
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(34.dp)
                  .clip(CircleShape)
                  .background(Color(0xFF8E44AD).copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Savings,
                  contentDescription = null,
                  tint = Color(0xFF8E44AD),
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "AMFI Mutual Fund Directory",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "Expanded 130+ schemes across Flexi, Small, Mid, Large & Debt",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  fontSize = 11.sp
                )
              }
            }
          }

          // Statistics Badges
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.surface,
              modifier = Modifier.weight(1f)
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Text(
                  text = "TOTAL MF SCHEMES",
                  style = MaterialTheme.typography.labelSmall,
                  fontSize = 9.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = "$totalMfCount+ Schemes",
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF8E44AD)
                )
              }
            }

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.surface,
              modifier = Modifier.weight(1f)
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Text(
                  text = "ACTIVE NFOS",
                  style = MaterialTheme.typography.labelSmall,
                  fontSize = 9.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = "${MutualFundDirectoryManager.getNfoCount()} Listed NFOs",
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF2E7D32)
                )
              }
            }
          }

          Text(
            text = "Last Synced: $lastMfSyncDate",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
          )

          if (!mfSyncMessage.isNullOrEmpty()) {
            Text(
              text = mfSyncMessage ?: "",
              style = MaterialTheme.typography.bodySmall,
              color = Color(0xFF8E44AD),
              fontSize = 11.sp,
              fontWeight = FontWeight.Medium
            )
          }

          Button(
            onClick = {
              if (viewModel != null) {
                viewModel.syncMutualFundDirectoryNow(context) { msg ->
                  Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
              } else {
                scope.launch {
                  val summary = MutualFundDirectoryManager.syncDailyNfos(context)
                  Toast.makeText(context, summary.statusMessage, Toast.LENGTH_SHORT).show()
                }
              }
            },
            enabled = !isSyncingMfs,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E44AD)),
            modifier = Modifier.fillMaxWidth().testTag("btn_sync_mf_directory")
          ) {
            if (isSyncingMfs) {
              CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = Color.White
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text("Checking for New Schemes & NFOs...", color = Color.White)
            } else {
              Icon(
                imageVector = Icons.Default.CloudSync,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = Color.White
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text("Check for New AMFI Schemes & NFOs", color = Color.White)
            }
          }
        }
      }
    }

    // SECTION: STOCK MARKET API KEYS & REAL-TIME INR DATA
    item {
      Text(
        text = "REAL-TIME STOCK MARKET DATA (INR)",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF0D47A1),
        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
      )
    }

    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        modifier = Modifier.fillMaxWidth().testTag("card_stock_api_keys")
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(34.dp)
                  .clip(CircleShape)
                  .background(Color(0xFF0D47A1).copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Key,
                  contentDescription = null,
                  tint = Color(0xFF0D47A1),
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "Finnhub & Alpha Vantage APIs",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "Live quotes & real-time USD/INR forex conversion",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  fontSize = 11.sp
                )
              }
            }
          }

          val finnhubKey by (viewModel?.finnhubApiKey?.collectAsState() ?: remember { mutableStateOf("") })
          val alphaKey by (viewModel?.alphaVantageApiKey?.collectAsState() ?: remember { mutableStateOf("") })
          val liveForex by (viewModel?.liveUsdInrRate?.collectAsState() ?: remember { mutableStateOf(85.50) })

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.surface,
              modifier = Modifier.weight(1f)
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Text(
                  text = "FINNHUB",
                  style = MaterialTheme.typography.labelSmall,
                  fontSize = 9.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = if (finnhubKey.isNotBlank()) "Linked" else "Not Set",
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Bold,
                  color = if (finnhubKey.isNotBlank()) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.surface,
              modifier = Modifier.weight(1f)
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Text(
                  text = "ALPHA VANTAGE",
                  style = MaterialTheme.typography.labelSmall,
                  fontSize = 9.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = if (alphaKey.isNotBlank()) "Linked" else "Not Set",
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Bold,
                  color = if (alphaKey.isNotBlank()) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.surface,
              modifier = Modifier.weight(1f)
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Text(
                  text = "LIVE USD/INR",
                  style = MaterialTheme.typography.labelSmall,
                  fontSize = 9.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = "₹${String.format("%.2f", liveForex)}",
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF0D47A1)
                )
              }
            }
          }

          Text(
            text = "All foreign/US stocks (e.g. AAPL, NVDA, TSLA) are automatically displayed in INR using real-time forex rates.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
          )

          Button(
            onClick = { showStockApiKeysDialog = true },
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1)),
            modifier = Modifier.fillMaxWidth().testTag("btn_configure_stock_api_keys")
          ) {
            Icon(
              imageVector = Icons.Default.Key,
              contentDescription = null,
              modifier = Modifier.size(16.dp),
              tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Configure Finnhub & Alpha Vantage Keys", color = Color.White)
          }
        }
      }
    }

    // SECTION 3: PREFERENCES & CUSTOMIZATION
    item {
      Text(
        text = "PREFERENCES & THEME",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
      )
    }

    item {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        MoreSettingRow(
          title = "User Name",
          subtitle = if (userName.isBlank()) "BudgetWise User" else userName,
          icon = Icons.Default.Person,
          onClick = { showEditUserNameDialog = true },
          testTag = "more_item_user_name"
        )

        val themeSubtitle = when (themeMode) {
          ThemeMode.LIGHT -> "Light Theme"
          ThemeMode.DARK -> "Dark Theme"
          ThemeMode.SYSTEM -> "Follow System"
        }
        val themeIcon = when (themeMode) {
          ThemeMode.LIGHT -> Icons.Default.LightMode
          ThemeMode.DARK -> Icons.Default.DarkMode
          ThemeMode.SYSTEM -> Icons.Default.SettingsBrightness
        }

        MoreSettingRow(
          title = "Theme & Appearance",
          subtitle = themeSubtitle,
          icon = themeIcon,
          onClick = onOpenThemeDialog,
          testTag = "more_item_theme"
        )

        MoreSettingRow(
          title = "Currency Symbol",
          subtitle = "Currently using $currencySymbol",
          icon = Icons.Default.AttachMoney,
          onClick = onOpenCurrencyDialog,
          testTag = "more_item_currency"
        )

        MoreSettingRow(
          title = "Customize Navigation Bar",
          subtitle = "Rearrange and choose bottom tabs",
          icon = Icons.Default.Navigation,
          onClick = onOpenNavBarDialog,
          testTag = "more_item_navbar"
        )

        MoreSettingRow(
          title = "Customize Home Dashboard",
          subtitle = "Reorder sections and manage visibility",
          icon = Icons.Default.DashboardCustomize,
          onClick = onOpenEditHomeScreen,
          testTag = "more_item_edit_home"
        )
      }
    }

    // SECTION 4: ALL FEATURES
    item {
      Text(
        text = "ALL FINANCIAL MODULES",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
      )
    }

    item {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        MoreSettingRow(
          title = "Gemini AI Assistant & Chatbot",
          subtitle = "Ask questions, edit finances, add stocks & analyze spending",
          icon = Icons.Default.AutoAwesome,
          onClick = { onOpenGeminiAssistant() },
          testTag = "more_item_gemini_assistant"
        )

        MoreSettingRow(
          title = "Stock Portfolio & Market",
          subtitle = "NSE & BSE holdings, live quotes, Gemini BUY/SELL advisor",
          icon = Icons.AutoMirrored.Filled.TrendingUp,
          onClick = { onNavigateToTab(NavTabDestination.STOCKS) },
          testTag = "more_item_stocks"
        )

        MoreSettingRow(
          title = "Accounts & Wallets",
          subtitle = "Manage bank accounts, cash, and balances",
          icon = Icons.Default.AccountBalance,
          onClick = { onNavigateToTab(NavTabDestination.HOME) },
          testTag = "more_item_accounts"
        )

        MoreSettingRow(
          title = "Monthly Budgets",
          subtitle = "Category spending limits and progress tracking",
          icon = Icons.Default.PieChart,
          onClick = { onNavigateToTab(NavTabDestination.BUDGETS) },
          testTag = "more_item_budgets"
        )

        MoreSettingRow(
          title = "Loans & Lent",
          subtitle = "Track debts, receivables and repayments",
          icon = Icons.Default.Handshake,
          onClick = { onNavigateToTab(NavTabDestination.LOANS) },
          testTag = "more_item_loans"
        )

        MoreSettingRow(
          title = "Recurring Subscriptions",
          subtitle = "Manage recurring bills and renewal cycles",
          icon = Icons.Default.Repeat,
          onClick = { onNavigateToTab(NavTabDestination.RECURRING) },
          testTag = "more_item_recurring"
        )

        MoreSettingRow(
          title = "Savings Goals",
          subtitle = "Target funds, target dates and milestones",
          icon = Icons.Default.EmojiEvents,
          onClick = { onNavigateToTab(NavTabDestination.GOALS) },
          testTag = "more_item_goals"
        )

        MoreSettingRow(
          title = "Analytics & Charts",
          subtitle = "Visual category spending and trends",
          icon = Icons.AutoMirrored.Filled.ReceiptLong,
          onClick = { onNavigateToTab(NavTabDestination.ANALYTICS) },
          testTag = "more_item_analytics"
        )

        MoreSettingRow(
          title = "Goal & Budget Notifications",
          subtitle = "View notification log history and alerts",
          icon = Icons.Default.Notifications,
          onClick = onOpenNotifications,
          testTag = "more_item_notifications"
        )
      }
    }

    item {
      Spacer(modifier = Modifier.height(24.dp))
    }
  }

  // DIALOG: EXPORT DATA
  if (showExportDialog) {
    ExportDataDialog(
      onDismiss = { showExportDialog = false },
      onExportJson = {
        scope.launch {
          try {
            val (file, _) = DataBackupManager.exportAllDataToJson(context)
            DataBackupManager.shareFile(context, file, "application/json", "BudgetWise Financial Backup")
            Toast.makeText(context, "Export ready: ${file.name}", Toast.LENGTH_SHORT).show()
            showExportDialog = false
          } catch (e: Exception) {
            Toast.makeText(context, "Export error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
          }
        }
      },
      onExportCsv = {
        scope.launch {
          try {
            val file = DataBackupManager.exportTransactionsCsv(context)
            DataBackupManager.shareFile(context, file, "text/csv", "BudgetWise Transactions CSV")
            Toast.makeText(context, "CSV ready: ${file.name}", Toast.LENGTH_SHORT).show()
            showExportDialog = false
          } catch (e: Exception) {
            Toast.makeText(context, "CSV Export error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
          }
        }
      },
      onCopyJson = {
        scope.launch {
          try {
            val (_, json) = DataBackupManager.exportAllDataToJson(context)
            DataBackupManager.copyToClipboard(context, json)
            Toast.makeText(context, "JSON backup copied to clipboard!", Toast.LENGTH_SHORT).show()
            showExportDialog = false
          } catch (e: Exception) {
            Toast.makeText(context, "Copy failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
          }
        }
      }
    )
  }

  // DIALOG: IMPORT DATA
  if (showImportDialog) {
    ImportDataDialog(
      onDismiss = { showImportDialog = false },
      onImportConfirmed = { jsonString, mode, onDone ->
        scope.launch {
          try {
            val stats = DataBackupManager.restoreBackup(context, jsonString, mode)
            withContext(Dispatchers.Main) {
              onDone(Result.success(stats))
              Toast.makeText(
                context,
                "Restored: ${stats.importedExpenses} expenses, ${stats.importedAccounts} accounts, ${stats.importedStocks} stocks.",
                Toast.LENGTH_LONG
              ).show()
              showImportDialog = false
            }
          } catch (e: Exception) {
            withContext(Dispatchers.Main) {
              onDone(Result.failure(e))
              Toast.makeText(context, "Import failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
          }
        }
      }
    )
  }

  // DIALOG: EDIT USER NAME
  if (showEditUserNameDialog) {
    EditUserNameDialog(
      currentName = userName,
      onDismiss = { showEditUserNameDialog = false },
      onSave = { newName ->
        viewModel?.setUserName(newName)
        Toast.makeText(context, "Display name set to '$newName'", Toast.LENGTH_SHORT).show()
        showEditUserNameDialog = false
      }
    )
  }

  // DIALOG: DELETE DATA
  if (showDeleteDataDialog) {
    DeleteDataDialog(
      onDismiss = { showDeleteDataDialog = false },
      onConfirmDelete = { option ->
        when (option) {
          DeleteTargetOption.ALL -> {
            viewModel?.resetAllFinancialData {
              Toast.makeText(context, "All financial data has been reset", Toast.LENGTH_LONG).show()
              showDeleteDataDialog = false
            }
          }
          DeleteTargetOption.TRANSACTIONS -> {
            viewModel?.clearTransactionsAndBudgets {
              Toast.makeText(context, "Transactions and budgets cleared", Toast.LENGTH_SHORT).show()
              showDeleteDataDialog = false
            }
          }
          DeleteTargetOption.PORTFOLIO -> {
            viewModel?.clearStocksAndSips {
              Toast.makeText(context, "Stocks and SIP portfolio cleared", Toast.LENGTH_SHORT).show()
              showDeleteDataDialog = false
            }
          }
          DeleteTargetOption.LOANS_SUBS -> {
            viewModel?.clearLoansAndSubscriptions {
              Toast.makeText(context, "Loans and subscriptions cleared", Toast.LENGTH_SHORT).show()
              showDeleteDataDialog = false
            }
          }
        }
      }
    )
  }

  // DIALOG: STOCK MARKET API KEYS
  if (showStockApiKeysDialog && viewModel != null) {
    StockMarketApiKeysDialog(
      viewModel = viewModel,
      onDismiss = { showStockApiKeysDialog = false }
    )
  }
}

@Composable
private fun ExportDataDialog(
  onDismiss: () -> Unit,
  onExportJson: () -> Unit,
  onExportCsv: () -> Unit,
  onCopyJson: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Upload,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Export Data & Backup", fontWeight = FontWeight.Bold)
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Text(
          text = "Choose your preferred export format to save or transfer your financial records and stock holdings.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Option 1: Full JSON Backup
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
          ),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
              text = "Full App Backup (.json)",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Includes all transactions, accounts, budgets, goals, recurring subscriptions, loans, and stocks. Can be restored anytime.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 11.sp
            )
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Button(
                onClick = onExportJson,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f).testTag("btn_export_json_share")
              ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Share File", fontSize = 12.sp)
              }
              OutlinedButton(
                onClick = onCopyJson,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f).testTag("btn_export_json_copy")
              ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Copy JSON", fontSize = 12.sp)
              }
            }
          }
        }

        // Option 2: Transactions Spreadsheet CSV
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
          ),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
              text = "Transactions Spreadsheet (.csv)",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Export all expense and income records to Excel, Google Sheets, or CSV readers.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 11.sp
            )
            Button(
              onClick = onExportCsv,
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.fillMaxWidth().testTag("btn_export_csv")
            ) {
              Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Export & Share CSV", fontSize = 12.sp)
            }
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text("Close")
      }
    }
  )
}

@Composable
private fun ImportDataDialog(
  onDismiss: () -> Unit,
  onImportConfirmed: (String, ImportMode, (Result<ImportResultStats>) -> Unit) -> Unit
) {
  val context = LocalContext.current
  var rawJsonInput by remember { mutableStateOf("") }
  var selectedMode by remember { mutableStateOf(ImportMode.MERGE) }
  var parsedSummary by remember { mutableStateOf<BackupSummary?>(null) }
  var parseError by remember { mutableStateOf<String?>(null) }
  var isProcessing by remember { mutableStateOf(false) }

  // Android File Picker for .json
  val filePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    if (uri != null) {
      try {
        context.contentResolver.openInputStream(uri)?.use { stream ->
          val content = stream.bufferedReader().use { it.readText() }
          rawJsonInput = content
          try {
            val summary = DataBackupManager.parseBackupJson(content)
            parsedSummary = summary
            parseError = null
          } catch (ex: Exception) {
            parseError = "Invalid JSON backup file: ${ex.localizedMessage}"
            parsedSummary = null
          }
        }
      } catch (e: Exception) {
        parseError = "Failed to read file: ${e.localizedMessage}"
      }
    }
  }

  AlertDialog(
    onDismissRequest = { if (!isProcessing) onDismiss() },
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Download,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Import Financial Data", fontWeight = FontWeight.Bold)
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Text(
          text = "Select a backup JSON file or paste the JSON content directly below.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // File Selector Button
        OutlinedButton(
          onClick = { filePickerLauncher.launch("*/*") },
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.fillMaxWidth().testTag("btn_select_backup_file")
        ) {
          Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Select Backup File (.json)")
        }

        // Paste JSON Text Field
        OutlinedTextField(
          value = rawJsonInput,
          onValueChange = {
            rawJsonInput = it
            if (it.isNotBlank()) {
              try {
                parsedSummary = DataBackupManager.parseBackupJson(it)
                parseError = null
              } catch (e: Exception) {
                parseError = "Invalid JSON structure"
                parsedSummary = null
              }
            } else {
              parsedSummary = null
              parseError = null
            }
          },
          label = { Text("Or Paste JSON Backup") },
          placeholder = { Text("{\"version\": 1, \"expenses\": [...]}") },
          maxLines = 3,
          singleLine = false,
          modifier = Modifier.fillMaxWidth().height(84.dp).testTag("input_import_json")
        )

        // Parsing error feedback
        if (parseError != null) {
          Text(
            text = parseError ?: "",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            fontSize = 11.sp
          )
        }

        // Parsed Backup Summary Preview
        AnimatedVisibility(visible = parsedSummary != null) {
          val summary = parsedSummary
          if (summary != null) {
            Card(
              shape = RoundedCornerShape(10.dp),
              colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
              ),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "Backup Validated (${summary.exportDate})",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                  )
                }
                Text(
                  text = "• ${summary.expensesCount} Expenses & Income\n" +
                      "• ${summary.accountsCount} Accounts & Wallets\n" +
                      "• ${summary.stocksCount} Stocks & Holdings\n" +
                      "• ${summary.budgetsCount} Budgets • ${summary.goalsCount} Goals\n" +
                      "• ${summary.subscriptionsCount} Subscriptions • ${summary.loansCount} Loans" +
                      (if (summary.sipsCount > 0 || summary.mandatesCount > 0) "\n• ${summary.sipsCount} Mutual Fund SIPs • ${summary.mandatesCount} Auto-Mandates" else ""),
                  style = MaterialTheme.typography.bodySmall,
                  fontSize = 11.sp
                )
              }
            }
          }
        }

        // Restore Mode Selection
        Text(
          text = "Restore Mode:",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold
        )

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { selectedMode = ImportMode.MERGE },
          verticalAlignment = Alignment.CenterVertically
        ) {
          RadioButton(
            selected = selectedMode == ImportMode.MERGE,
            onClick = { selectedMode = ImportMode.MERGE }
          )
          Spacer(modifier = Modifier.width(6.dp))
          Column {
            Text("Merge with Existing Data", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text("Adds imported records without deleting current entries", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { selectedMode = ImportMode.REPLACE },
          verticalAlignment = Alignment.CenterVertically
        ) {
          RadioButton(
            selected = selectedMode == ImportMode.REPLACE,
            onClick = { selectedMode = ImportMode.REPLACE }
          )
          Spacer(modifier = Modifier.width(6.dp))
          Column {
            Text("Replace All Data", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error)
            Text("Overwrites current records with backup", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val summary = parsedSummary
          if (summary != null) {
            isProcessing = true
            onImportConfirmed(summary.rawJson, selectedMode) {
              isProcessing = false
            }
          }
        },
        enabled = parsedSummary != null && !isProcessing,
        modifier = Modifier.testTag("btn_confirm_import")
      ) {
        if (isProcessing) {
          CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
          Spacer(modifier = Modifier.width(6.dp))
          Text("Restoring...")
        } else {
          Text("Start Import")
        }
      }
    },
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        enabled = !isProcessing
      ) {
        Text("Cancel")
      }
    }
  )
}

@Composable
private fun MoreSettingRow(
  title: String,
  subtitle: String,
  icon: ImageVector,
  onClick: () -> Unit,
  testTag: String
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 2.dp,
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .clickable(onClick = onClick)
      .testTag(testTag)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier.size(38.dp)
      ) {
        Icon(
          imageVector = icon,
          contentDescription = title,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.padding(8.dp)
        )
      }

      Spacer(modifier = Modifier.width(14.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          style = MaterialTheme.typography.bodyLarge,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Icon(
        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
        contentDescription = "Open",
        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.size(14.dp)
      )
    }
  }
}

// ==========================================
// DIALOG: EDIT USER NAME
// ==========================================
@Composable
private fun EditUserNameDialog(
  currentName: String,
  onDismiss: () -> Unit,
  onSave: (String) -> Unit
) {
  var nameText by remember { mutableStateOf(currentName) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
          shape = CircleShape,
          color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
          modifier = Modifier.size(36.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Default.Person,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(20.dp)
            )
          }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = "Edit Display Name",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Text(
          text = "Personalize how BudgetWise addresses you on your home dashboard and financial reports.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
          value = nameText,
          onValueChange = { nameText = it },
          label = { Text("Display Name") },
          placeholder = { Text("e.g. Alex, Maya") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_edit_user_name")
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val trimmed = nameText.trim()
          if (trimmed.isNotEmpty()) {
            onSave(trimmed)
          }
        },
        enabled = nameText.trim().isNotEmpty(),
        modifier = Modifier.testTag("btn_save_user_name")
      ) {
        Text("Save Name")
      }
    },
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        modifier = Modifier.testTag("btn_cancel_user_name")
      ) {
        Text("Cancel")
      }
    }
  )
}

// ==========================================
// DIALOG: DELETE DATA & CLEAN SLATE
// ==========================================
@Composable
private fun DeleteDataDialog(
  onDismiss: () -> Unit,
  onConfirmDelete: (DeleteTargetOption) -> Unit
) {
  var selectedOption by remember { mutableStateOf(DeleteTargetOption.ALL) }
  var isConfirmed by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.DeleteForever,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(20.dp)
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = "Delete Financial Records",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.error
        )
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Text(
          text = "Choose what financial records you would like to permanently delete from your local storage.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Surface(
          shape = RoundedCornerShape(10.dp),
          color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Warning,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.error,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "This action is irreversible. Export a JSON backup before proceeding if you want to keep records.",
              style = MaterialTheme.typography.bodySmall,
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onErrorContainer
            )
          }
        }

        val options = listOf(
          Triple(
            DeleteTargetOption.ALL,
            "Reset All Financial Data",
            "Clears all expenses, income, budgets, stocks, sips, loans & subscriptions (Clean Slate)"
          ),
          Triple(
            DeleteTargetOption.TRANSACTIONS,
            "Transactions & Budgets Only",
            "Removes income/expense history and budget plans while retaining accounts and investments"
          ),
          Triple(
            DeleteTargetOption.PORTFOLIO,
            "Stock & Mutual Fund Portfolio",
            "Removes all equity holdings, mutual fund units, and automated SIP setups"
          ),
          Triple(
            DeleteTargetOption.LOANS_SUBS,
            "Loans & Subscriptions Only",
            "Clears debt ledgers, receivables, and recurring bill reminders"
          )
        )

        options.forEach { (opt, title, desc) ->
          val isSelected = selectedOption == opt
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (isSelected) MaterialTheme.colorScheme.error.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface,
            border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.error) else BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier
              .fillMaxWidth()
              .clickable { selectedOption = opt }
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              RadioButton(
                selected = isSelected,
                onClick = { selectedOption = opt }
              )
              Spacer(modifier = Modifier.width(6.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = title,
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = if (isSelected) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = desc,
                  style = MaterialTheme.typography.bodySmall,
                  fontSize = 10.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { isConfirmed = !isConfirmed }
            .padding(top = 4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Checkbox(
            checked = isConfirmed,
            onCheckedChange = { isConfirmed = it },
            modifier = Modifier.testTag("chk_confirm_delete_data")
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "I understand this data will be permanently erased",
            style = MaterialTheme.typography.bodySmall,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.Medium
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = { onConfirmDelete(selectedOption) },
        enabled = isConfirmed,
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.error,
          contentColor = MaterialTheme.colorScheme.onError
        ),
        modifier = Modifier.testTag("btn_execute_delete_data")
      ) {
        Text("Delete Data")
      }
    },
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        modifier = Modifier.testTag("btn_cancel_delete_data")
      ) {
        Text("Cancel")
      }
    }
  )
}

@Composable
private fun StockMarketApiKeysDialog(
  viewModel: ExpenseViewModel,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val currentFinnhub by viewModel.finnhubApiKey.collectAsState()
  val currentAlpha by viewModel.alphaVantageApiKey.collectAsState()
  val liveForex by viewModel.liveUsdInrRate.collectAsState()

  var finnhubInput by remember(currentFinnhub) { mutableStateOf(currentFinnhub) }
  var alphaInput by remember(currentAlpha) { mutableStateOf(currentAlpha) }

  var finnhubVisible by remember { mutableStateOf(false) }
  var alphaVisible by remember { mutableStateOf(false) }

  var finnhubTesting by remember { mutableStateOf(false) }
  var finnhubStatus by remember { mutableStateOf<String?>(null) }
  var finnhubStatusSuccess by remember { mutableStateOf(false) }

  var alphaTesting by remember { mutableStateOf(false) }
  var alphaStatus by remember { mutableStateOf<String?>(null) }
  var alphaStatusSuccess by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Key,
          contentDescription = null,
          tint = Color(0xFF0D47A1),
          modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Stock Market API Keys", fontWeight = FontWeight.Bold)
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Text(
          text = "Enter your Finnhub and Alpha Vantage API keys to stream live stock prices. All US/global equities are automatically converted to INR using real-time forex rates.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Live INR Conversion Banner
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = Color(0xFF1B664B).copy(alpha = 0.12f),
          border = BorderStroke(1.dp, Color(0xFF1B664B).copy(alpha = 0.3f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Universal INR Conversion: Active",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B664B)
              )
              Text(
                text = "Live Rate: 1 USD = ₹${String.format("%.2f", liveForex)}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B664B)
              )
            }
            IconButton(
              onClick = { viewModel.refreshLiveUsdInrRate() },
              modifier = Modifier.size(32.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refresh live rate",
                tint = Color(0xFF1B664B),
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }

        // Finnhub API Key Section
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Finnhub API Key",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Get Free Key ↗",
              color = Color(0xFF0D47A1),
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.clickable {
                try {
                  val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://finnhub.io/register"))
                  intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                  context.startActivity(intent)
                } catch (_: Exception) {}
              }
            )
          }

          OutlinedTextField(
            value = finnhubInput,
            onValueChange = { finnhubInput = it; finnhubStatus = null },
            modifier = Modifier.fillMaxWidth().testTag("input_finnhub_key"),
            placeholder = { Text("Enter Finnhub API key", fontSize = 12.sp) },
            singleLine = true,
            visualTransformation = if (finnhubVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
              IconButton(onClick = { finnhubVisible = !finnhubVisible }) {
                Icon(
                  imageVector = if (finnhubVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                  contentDescription = "Toggle key visibility",
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            if (finnhubStatus != null) {
              Text(
                text = finnhubStatus ?: "",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (finnhubStatusSuccess) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f)
              )
            } else {
              Spacer(modifier = Modifier.weight(1f))
            }

            OutlinedButton(
              onClick = {
                finnhubTesting = true
                finnhubStatus = null
                viewModel.testFinnhubApiKey(finnhubInput) { success, msg ->
                  finnhubTesting = false
                  finnhubStatusSuccess = success
                  finnhubStatus = msg
                }
              },
              enabled = !finnhubTesting && finnhubInput.isNotBlank(),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.testTag("btn_test_finnhub_key")
            ) {
              if (finnhubTesting) {
                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(6.dp))
              }
              Text("Test Key", fontSize = 11.sp)
            }
          }
        }

        // Alpha Vantage API Key Section
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Alpha Vantage API Key",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Get Free Key ↗",
              color = Color(0xFF0D47A1),
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.clickable {
                try {
                  val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.alphavantage.co/support/#api-key"))
                  intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                  context.startActivity(intent)
                } catch (_: Exception) {}
              }
            )
          }

          OutlinedTextField(
            value = alphaInput,
            onValueChange = { alphaInput = it; alphaStatus = null },
            modifier = Modifier.fillMaxWidth().testTag("input_alpha_vantage_key"),
            placeholder = { Text("Enter Alpha Vantage key", fontSize = 12.sp) },
            singleLine = true,
            visualTransformation = if (alphaVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
              IconButton(onClick = { alphaVisible = !alphaVisible }) {
                Icon(
                  imageVector = if (alphaVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                  contentDescription = "Toggle key visibility",
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            if (alphaStatus != null) {
              Text(
                text = alphaStatus ?: "",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (alphaStatusSuccess) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f)
              )
            } else {
              Spacer(modifier = Modifier.weight(1f))
            }

            OutlinedButton(
              onClick = {
                alphaTesting = true
                alphaStatus = null
                viewModel.testAlphaVantageApiKey(alphaInput) { success, msg ->
                  alphaTesting = false
                  alphaStatusSuccess = success
                  alphaStatus = msg
                }
              },
              enabled = !alphaTesting && alphaInput.isNotBlank(),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.testTag("btn_test_alpha_key")
            ) {
              if (alphaTesting) {
                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(6.dp))
              }
              Text("Test Key", fontSize = 11.sp)
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          viewModel.setFinnhubApiKey(finnhubInput)
          viewModel.setAlphaVantageApiKey(alphaInput)
          viewModel.refreshAllStockPrices(force = true)
          Toast.makeText(context, "API keys saved! Live stock prices updating in INR.", Toast.LENGTH_SHORT).show()
          onDismiss()
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1)),
        modifier = Modifier.testTag("btn_save_stock_api_keys")
      ) {
        Text("Save & Sync in INR")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}

