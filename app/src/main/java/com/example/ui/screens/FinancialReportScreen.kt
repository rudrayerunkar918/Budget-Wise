package com.example.ui.screens

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.report.FinancialReportData
import com.example.data.report.FinancialReportGenerator
import com.example.data.report.ReportGenerationResult
import com.example.data.report.ReportSuggestion
import com.example.ui.viewmodel.ExpenseViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class ReportPeriodType(val label: String, val days: Int) {
  ONE_WEEK("1 Week", 7),
  ONE_MONTH("1 Month", 30),
  THREE_MONTHS("3 Months", 90),
  SIX_MONTHS("6 Months", 180),
  ONE_YEAR("1 Year", 365),
  TWO_YEARS("2 Years", 730),
  CUSTOM("Custom", 0)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialReportScreen(
  viewModel: ExpenseViewModel,
  onNavigateBack: () -> Unit
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  val expenses by viewModel.allExpenses.collectAsState(initial = emptyList())
  val accounts by viewModel.allAccounts.collectAsState(initial = emptyList())
  val budgets by viewModel.allBudgets.collectAsState(initial = emptyList())
  val goals by viewModel.allGoals.collectAsState(initial = emptyList())
  val loans by viewModel.allLoans.collectAsState(initial = emptyList())
  val userName by viewModel.userName.collectAsState(initial = "BudgetWise User")
  val currencySymbol by viewModel.currencySymbol.collectAsState(initial = "$")

  val currentTime = System.currentTimeMillis()
  var selectedPeriodType by remember { mutableStateOf(ReportPeriodType.ONE_MONTH) }
  var endDateMs by remember { mutableLongStateOf(currentTime) }
  var startDateMs by remember { mutableLongStateOf(currentTime - (30L * 24 * 60 * 60 * 1000L)) }

  var isGeneratingPdf by remember { mutableStateOf(false) }
  var downloadResult by remember { mutableStateOf<ReportGenerationResult?>(null) }
  var showDownloadDialog by remember { mutableStateOf(false) }
  var rangeErrorText by remember { mutableStateOf<String?>(null) }

  // Recalculate start and end times whenever preset changes
  fun applyPeriodPreset(type: ReportPeriodType) {
    selectedPeriodType = type
    if (type != ReportPeriodType.CUSTOM) {
      val now = System.currentTimeMillis()
      endDateMs = now
      startDateMs = now - (type.days.toLong() * 24 * 60 * 60 * 1000L)
      rangeErrorText = null
    }
  }

  // Compute live report preview
  val reportData = remember(startDateMs, endDateMs, expenses, accounts, budgets, goals, loans, userName, currencySymbol) {
    FinancialReportGenerator.computeReportData(
      startDateMs = startDateMs,
      endDateMs = endDateMs,
      userName = userName,
      currencySymbol = currencySymbol,
      expenses = expenses,
      accounts = accounts,
      budgets = budgets,
      savingsGoals = goals,
      loans = loans
    )
  }

  val dateFmt = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "Financial Report",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "PDF Audit, Charts & Suggestions",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        },
        navigationIcon = {
          IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.testTag("btn_financial_report_back")
          ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          IconButton(
            onClick = {
              scope.launch {
                isGeneratingPdf = true
                try {
                  val result = FinancialReportGenerator.generateAndDownloadPdf(context, reportData)
                  downloadResult = result
                  showDownloadDialog = true
                  Toast.makeText(context, "Report downloaded to Downloads folder", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                  Toast.makeText(context, "Failed to create report: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                  isGeneratingPdf = false
                }
              }
            },
            enabled = !isGeneratingPdf,
            modifier = Modifier.testTag("btn_top_download_pdf")
          ) {
            if (isGeneratingPdf) {
              CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
            } else {
              Icon(Icons.Default.Download, contentDescription = "Download PDF", tint = Color(0xFF1B664B))
            }
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    },
    bottomBar = {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Button(
            onClick = {
              scope.launch {
                isGeneratingPdf = true
                try {
                  val result = FinancialReportGenerator.generateAndDownloadPdf(context, reportData)
                  downloadResult = result
                  showDownloadDialog = true
                  Toast.makeText(context, "Report saved to Downloads/BudgetWiseReports/", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                  Toast.makeText(context, "Download failed: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                  isGeneratingPdf = false
                }
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(52.dp)
              .testTag("btn_generate_download_pdf_report"),
            enabled = !isGeneratingPdf,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B664B))
          ) {
            if (isGeneratingPdf) {
              CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
              Spacer(modifier = Modifier.width(12.dp))
              Text("Generating PDF with Graphs...")
            } else {
              Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.White)
              Spacer(modifier = Modifier.width(8.dp))
              Text("Download Financial Report (PDF)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
          }
        }
      }
    }
  ) { padding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
      contentPadding = PaddingValues(top = 12.dp, bottom = 100.dp)
    ) {

      // SECTION 1: TIME PERIOD SELECTOR (MIN 1 WEEK, MAX 2 YEARS)
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.DateRange,
                  contentDescription = null,
                  tint = Color(0xFF1B664B),
                  modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Choose Report Period",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold
                )
              }
              Text(
                text = "${reportData.totalDays} Days",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B664B)
              )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Min: 1 Week (7 days)  •  Max: 2 Years (730 days)",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Preset Filter Chips
            LazyRow(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              items(ReportPeriodType.entries) { preset ->
                val isSelected = selectedPeriodType == preset
                FilterChip(
                  selected = isSelected,
                  onClick = {
                    if (preset == ReportPeriodType.CUSTOM) {
                      selectedPeriodType = ReportPeriodType.CUSTOM
                    } else {
                      applyPeriodPreset(preset)
                    }
                  },
                  label = {
                    Text(
                      text = preset.label,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                  },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF1B664B),
                    selectedLabelColor = Color.White
                  ),
                  modifier = Modifier.testTag("period_chip_${preset.name.lowercase()}")
                )
              }
            }

            // Custom Date Range Pickers (if CUSTOM selected)
            if (selectedPeriodType == ReportPeriodType.CUSTOM) {
              Spacer(modifier = Modifier.height(12.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
              ) {
                // Start Date Button
                OutlinedButton(
                  onClick = {
                    showDatePicker(context, startDateMs) { pickedMs ->
                      val end = endDateMs
                      val days = ((end - pickedMs) / (1000L * 60 * 60 * 24)).toInt()
                      if (days < FinancialReportGenerator.MIN_PERIOD_DAYS) {
                        rangeErrorText = "Minimum report range is 1 week (7 days)."
                        startDateMs = end - (7L * 24 * 60 * 60 * 1000L)
                      } else if (days > FinancialReportGenerator.MAX_PERIOD_DAYS) {
                        rangeErrorText = "Maximum report range is 2 years (730 days)."
                        startDateMs = end - (730L * 24 * 60 * 60 * 1000L)
                      } else {
                        rangeErrorText = null
                        startDateMs = pickedMs
                      }
                    }
                  },
                  modifier = Modifier
                    .weight(1f)
                    .testTag("btn_custom_start_date")
                ) {
                  Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("FROM", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(dateFmt.format(Date(startDateMs)), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                  }
                }

                // End Date Button
                OutlinedButton(
                  onClick = {
                    showDatePicker(context, endDateMs) { pickedMs ->
                      val start = startDateMs
                      val days = ((pickedMs - start) / (1000L * 60 * 60 * 24)).toInt()
                      if (days < FinancialReportGenerator.MIN_PERIOD_DAYS) {
                        rangeErrorText = "Minimum report range is 1 week (7 days)."
                        endDateMs = start + (7L * 24 * 60 * 60 * 1000L)
                      } else if (days > FinancialReportGenerator.MAX_PERIOD_DAYS) {
                        rangeErrorText = "Maximum report range is 2 years (730 days)."
                        endDateMs = start + (730L * 24 * 60 * 60 * 1000L)
                      } else {
                        rangeErrorText = null
                        endDateMs = pickedMs
                      }
                    }
                  },
                  modifier = Modifier
                    .weight(1f)
                    .testTag("btn_custom_end_date")
                ) {
                  Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("TO", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(dateFmt.format(Date(endDateMs)), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                  }
                }
              }

              if (rangeErrorText != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = rangeErrorText!!,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.error
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
              Text(
                text = "Report Window: ${dateFmt.format(Date(startDateMs))} – ${dateFmt.format(Date(endDateMs))} (${reportData.totalDays} Days)",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF1B664B),
                fontWeight = FontWeight.Medium
              )
            }
          }
        }
      }

      // SECTION 2: EXECUTIVE SUMMARY METRICS
      item {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "EXECUTIVE SUMMARY",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1B664B)
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            ReportMetricCard(
              modifier = Modifier.weight(1f),
              title = "Total Inflow",
              amount = "$currencySymbol${"%,.0f".format(reportData.totalIncome)}",
              icon = Icons.AutoMirrored.Filled.TrendingUp,
              accentColor = Color(0xFF27AE60)
            )

            ReportMetricCard(
              modifier = Modifier.weight(1f),
              title = "Total Outflow",
              amount = "$currencySymbol${"%,.0f".format(reportData.totalExpense)}",
              icon = Icons.AutoMirrored.Filled.TrendingDown,
              accentColor = Color(0xFFE74C3C)
            )
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            ReportMetricCard(
              modifier = Modifier.weight(1f),
              title = "Net Savings",
              amount = "$currencySymbol${"%,.0f".format(reportData.netSavings)}",
              icon = Icons.Default.Savings,
              accentColor = if (reportData.netSavings >= 0) Color(0xFF1B664B) else Color(0xFFE74C3C)
            )

            ReportMetricCard(
              modifier = Modifier.weight(1f),
              title = "Savings Rate",
              amount = "${"%.1f".format(reportData.savingsRate)}%",
              icon = Icons.Default.Assessment,
              accentColor = Color(0xFF2980B9)
            )
          }
        }
      }

      // SECTION 3: FINANCIAL HEALTH SCORECARD
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(54.dp)
                .background(
                  color = when {
                    reportData.healthScore >= 80 -> Color(0xFF27AE60)
                    reportData.healthScore >= 60 -> Color(0xFF2980B9)
                    else -> Color(0xFFF39C12)
                  },
                  shape = CircleShape
                ),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "${reportData.healthScore}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
              )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Financial Health Index",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = reportData.healthRating,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Daily Avg: $currencySymbol${"%,.0f".format(reportData.dailyAverageSpend)}/day  •  ${reportData.totalTransactionsCount} Txns",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }

      // SECTION 4: 50 / 30 / 20 BUDGET ALLOCATION AUDIT
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "50 / 30 / 20 Budget Rule Audit",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Needs (50%) • Wants (30%) • Savings (20%) breakdown",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Multi-segment progress bar
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(7.dp))
            ) {
              val nWeight = (reportData.needsPercent.toFloat()).coerceAtLeast(1f)
              val wWeight = (reportData.wantsPercent.toFloat()).coerceAtLeast(1f)
              val sWeight = (reportData.savingsPercent.toFloat()).coerceAtLeast(1f)

              Box(
                modifier = Modifier
                  .weight(nWeight)
                  .fillMaxSize()
                  .background(Color(0xFF2980B9))
              )
              Box(
                modifier = Modifier
                  .weight(wWeight)
                  .fillMaxSize()
                  .background(Color(0xFFF39C12))
              )
              Box(
                modifier = Modifier
                  .weight(sWeight)
                  .fillMaxSize()
                  .background(Color(0xFF27AE60))
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              BudgetSegmentIndicator("Needs", "${"%.1f".format(reportData.needsPercent)}%", Color(0xFF2980B9))
              BudgetSegmentIndicator("Wants", "${"%.1f".format(reportData.wantsPercent)}%", Color(0xFFF39C12))
              BudgetSegmentIndicator("Savings", "${"%.1f".format(reportData.savingsPercent)}%", Color(0xFF27AE60))
            }
          }
        }
      }

      // SECTION 5: WHERE MONEY WAS SPENT (CATEGORY BREAKDOWN)
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "Where Money Was Spent",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Expenditure distribution across categories",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (reportData.categorySpending.isEmpty()) {
              Text(
                text = "No expense transactions recorded in this period.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 12.dp)
              )
            } else {
              Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                reportData.categorySpending.take(5).forEach { cat ->
                  Column {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                      Text(
                        text = cat.categoryName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                      )
                      Text(
                        text = "$currencySymbol${"%,.0f".format(cat.totalAmount)} (${"%.1f".format(cat.percentageOfSpend)}%)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                      )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                      progress = { (cat.percentageOfSpend.toFloat() / 100f).coerceIn(0f, 1f) },
                      modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                      color = Color(cat.colorHex),
                      trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                  }
                }
              }
            }
          }
        }
      }

      // SECTION 6: SMART SPENDING SUGGESTIONS & RECOMMENDATIONS
      item {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Lightbulb,
              contentDescription = null,
              tint = Color(0xFFF39C12),
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "SMART SPENDING SUGGESTIONS",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF1B664B)
            )
          }

          reportData.spendingSuggestions.forEach { sug ->
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween,
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Text(
                    text = sug.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                  )
                  Box(
                    modifier = Modifier
                      .background(
                        color = when (sug.tag) {
                          "ALERT" -> Color(0xFFFFEBEE)
                          "SAVINGS" -> Color(0xFFE8F5E9)
                          "INVESTMENT" -> Color(0xFFE3F2FD)
                          else -> Color(0xFFFFF8E1)
                        },
                        shape = RoundedCornerShape(6.dp)
                      )
                      .padding(horizontal = 8.dp, vertical = 2.dp)
                  ) {
                    Text(
                      text = sug.impact,
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      color = when (sug.tag) {
                        "ALERT" -> Color(0xFFC62828)
                        "SAVINGS" -> Color(0xFF2E7D32)
                        "INVESTMENT" -> Color(0xFF1565C0)
                        else -> Color(0xFFEF6C00)
                      }
                    )
                  }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = sug.description,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  lineHeight = 18.sp
                )
              }
            }
          }
        }
      }
    }
  }

  // DIALOG AFTER SUCCESSFUL PDF DOWNLOAD
  if (showDownloadDialog && downloadResult != null) {
    val result = downloadResult!!
    AlertDialog(
      onDismissRequest = { showDownloadDialog = false },
      icon = {
        Icon(
          Icons.Default.CheckCircle,
          contentDescription = null,
          tint = Color(0xFF27AE60),
          modifier = Modifier.size(44.dp)
        )
      },
      title = {
        Text("Report Downloaded Successfully!", fontWeight = FontWeight.Bold)
      },
      text = {
        Column {
          Text(
            text = "Your high-resolution 3-page Financial Report with graphs and spending advice has been saved to your device.",
            style = MaterialTheme.typography.bodyMedium
          )
          Spacer(modifier = Modifier.height(10.dp))
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
              .padding(10.dp)
          ) {
            Text(
              text = result.downloadLocation,
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.SemiBold,
              color = Color(0xFF1B664B)
            )
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            openPdfFile(context, result.shareUri)
            showDownloadDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B664B)),
          modifier = Modifier.testTag("btn_dialog_open_pdf")
        ) {
          Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Open PDF")
        }
      },
      dismissButton = {
        OutlinedButton(
          onClick = {
            sharePdfFile(context, result.shareUri, result.pdfFile.name)
            showDownloadDialog = false
          },
          modifier = Modifier.testTag("btn_dialog_share_pdf")
        ) {
          Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Share PDF")
        }
      }
    )
  }
}

@Composable
private fun ReportMetricCard(
  modifier: Modifier = Modifier,
  title: String,
  amount: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  accentColor: Color
) {
  Card(
    modifier = modifier,
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
      }
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = amount,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = accentColor
      )
    }
  }
}

@Composable
private fun BudgetSegmentIndicator(label: String, pct: String, color: Color) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Box(
      modifier = Modifier
        .size(8.dp)
        .background(color, CircleShape)
    )
    Spacer(modifier = Modifier.width(4.dp))
    Text(
      text = "$label: $pct",
      style = MaterialTheme.typography.bodySmall,
      fontWeight = FontWeight.Medium
    )
  }
}

private fun showDatePicker(context: Context, initialMs: Long, onDateSelected: (Long) -> Unit) {
  val cal = Calendar.getInstance().apply { timeInMillis = initialMs }
  DatePickerDialog(
    context,
    { _, year, month, dayOfMonth ->
      val pickedCal = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month)
        set(Calendar.DAY_OF_MONTH, dayOfMonth)
      }
      onDateSelected(pickedCal.timeInMillis)
    },
    cal.get(Calendar.YEAR),
    cal.get(Calendar.MONTH),
    cal.get(Calendar.DAY_OF_MONTH)
  ).show()
}

private fun openPdfFile(context: Context, uri: Uri) {
  try {
    val intent = Intent(Intent.ACTION_VIEW).apply {
      setDataAndType(uri, "application/pdf")
      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
  } catch (_: Exception) {
    Toast.makeText(context, "No PDF viewer app found on device.", Toast.LENGTH_LONG).show()
  }
}

private fun sharePdfFile(context: Context, uri: Uri, fileName: String) {
  try {
    val intent = Intent(Intent.ACTION_SEND).apply {
      type = "application/pdf"
      putExtra(Intent.EXTRA_STREAM, uri)
      putExtra(Intent.EXTRA_SUBJECT, fileName)
      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    val chooser = Intent.createChooser(intent, "Share Financial Report PDF")
    chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(chooser)
  } catch (e: Exception) {
    Toast.makeText(context, "Could not share PDF: ${e.message}", Toast.LENGTH_SHORT).show()
  }
}
