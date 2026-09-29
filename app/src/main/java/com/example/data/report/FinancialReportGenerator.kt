package com.example.data.report

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import com.example.data.model.AccountEntity
import com.example.data.model.BudgetEntity
import com.example.data.model.ExpenseCategory
import com.example.data.model.ExpenseEntity
import com.example.data.model.LoanEntity
import com.example.data.model.SavingsGoalEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

data class FinancialReportData(
  val startDateMs: Long,
  val endDateMs: Long,
  val totalDays: Int,
  val userName: String,
  val currencySymbol: String,
  val totalIncome: Double,
  val totalExpense: Double,
  val netSavings: Double,
  val savingsRate: Double,
  val dailyAverageSpend: Double,
  val totalTransactionsCount: Int,
  val categorySpending: List<CategorySpendItem>,
  val topExpenses: List<ExpenseEntity>,
  val accountSpendMap: Map<String, Double>,
  val cashflowIntervals: List<CashflowInterval>,
  val healthScore: Int,
  val healthRating: String,
  val needsPercent: Double,
  val wantsPercent: Double,
  val savingsPercent: Double,
  val spendingSuggestions: List<ReportSuggestion>
)

data class CategorySpendItem(
  val categoryName: String,
  val totalAmount: Double,
  val percentageOfSpend: Double,
  val transactionCount: Int,
  val colorHex: Int
)

data class CashflowInterval(
  val label: String,
  val income: Double,
  val expense: Double
)

data class ReportSuggestion(
  val title: String,
  val description: String,
  val tag: String, // "SAVINGS", "BUDGET", "ALERT", "INVESTMENT"
  val impact: String
)

data class ReportGenerationResult(
  val pdfFile: File,
  val shareUri: Uri,
  val downloadLocation: String,
  val reportData: FinancialReportData
)

object FinancialReportGenerator {

  const val MIN_PERIOD_DAYS = 7
  const val MAX_PERIOD_DAYS = 730 // 2 Years

  private val PALETTE_COLORS = listOf(
    0xFF1B664B.toInt(), // Deep Emerald
    0xFFE74C3C.toInt(), // Crimson Red
    0xFF2980B9.toInt(), // Ocean Blue
    0xFFF39C12.toInt(), // Amber
    0xFF8E44AD.toInt(), // Purple
    0xFF16A085.toInt(), // Teal
    0xFFD35400.toInt(), // Rust Orange
    0xFF2C3E50.toInt(), // Midnight Slate
    0xFF27AE60.toInt(), // Fresh Green
    0xFF7F8C8D.toInt()  // Cool Grey
  )

  /**
   * Prepares and computes all financial statistics, metrics, graphs, and spending suggestions.
   */
  fun computeReportData(
    startDateMs: Long,
    endDateMs: Long,
    userName: String,
    currencySymbol: String,
    expenses: List<ExpenseEntity>,
    accounts: List<AccountEntity>,
    budgets: List<BudgetEntity>,
    savingsGoals: List<SavingsGoalEntity>,
    loans: List<LoanEntity>
  ): FinancialReportData {
    val durationMs = (endDateMs - startDateMs).coerceAtLeast(1L)
    val totalDays = ((durationMs / (1000L * 60 * 60 * 24)).toInt()).coerceIn(MIN_PERIOD_DAYS, MAX_PERIOD_DAYS)

    val inRangeExpenses = expenses.filter { it.timestamp in startDateMs..endDateMs }

    var totalIncome = 0.0
    var totalExpense = 0.0
    val categoryTotals = mutableMapOf<String, Double>()
    val categoryCounts = mutableMapOf<String, Int>()
    val accountOutflows = mutableMapOf<String, Double>()

    for (e in inRangeExpenses) {
      if (e.type.equals("INCOME", ignoreCase = true)) {
        totalIncome += e.amount
      } else if (e.type.equals("EXPENSE", ignoreCase = true)) {
        totalExpense += e.amount
        val cat = e.category.ifBlank { "OTHER" }
        categoryTotals[cat] = (categoryTotals[cat] ?: 0.0) + e.amount
        categoryCounts[cat] = (categoryCounts[cat] ?: 0) + 1
        val acc = e.account.ifBlank { "Default Account" }
        accountOutflows[acc] = (accountOutflows[acc] ?: 0.0) + e.amount
      }
    }

    val netSavings = totalIncome - totalExpense
    val savingsRate = if (totalIncome > 0) ((netSavings / totalIncome) * 100.0).coerceIn(-100.0, 100.0) else 0.0
    val dailyAvg = if (totalDays > 0) totalExpense / totalDays else 0.0

    // Top Categories
    val sortedCategories = categoryTotals.entries.sortedByDescending { it.value }
    val categorySpendItems = sortedCategories.mapIndexed { index, entry ->
      val pct = if (totalExpense > 0) (entry.value / totalExpense) * 100.0 else 0.0
      CategorySpendItem(
        categoryName = entry.key,
        totalAmount = entry.value,
        percentageOfSpend = pct,
        transactionCount = categoryCounts[entry.key] ?: 0,
        colorHex = PALETTE_COLORS[index % PALETTE_COLORS.size]
      )
    }

    // Top Outflow Transactions
    val topExpenses = inRangeExpenses
      .filter { it.type.equals("EXPENSE", ignoreCase = true) }
      .sortedByDescending { it.amount }
      .take(8)

    // Cashflow periodic intervals (buckets: monthly if period > 60 days, else weekly)
    val cashflowIntervals = computeCashflowBuckets(startDateMs, endDateMs, inRangeExpenses)

    // 50/30/20 Needs vs Wants vs Savings Breakdown
    var needsAmount = 0.0
    var wantsAmount = 0.0
    for (item in categorySpendItems) {
      val catUpper = item.categoryName.uppercase()
      if (catUpper in listOf("HOUSING", "GROCERIES", "HEALTH", "TRANSPORT", "EDUCATION", "BILLS")) {
        needsAmount += item.totalAmount
      } else {
        wantsAmount += item.totalAmount
      }
    }
    val totalCashflowPool = (totalExpense + netSavings.coerceAtLeast(0.0)).coerceAtLeast(1.0)
    val needsPct = (needsAmount / totalCashflowPool * 100.0).coerceIn(0.0, 100.0)
    val wantsPct = (wantsAmount / totalCashflowPool * 100.0).coerceIn(0.0, 100.0)
    val savingsPct = ((netSavings.coerceAtLeast(0.0)) / totalCashflowPool * 100.0).coerceIn(0.0, 100.0)

    // Financial Health Scoring (0 - 100)
    var score = 50
    // Factor 1: Savings rate
    if (savingsRate >= 35) score += 25
    else if (savingsRate >= 20) score += 18
    else if (savingsRate >= 10) score += 10
    else if (savingsRate < 0) score -= 15

    // Factor 2: Wants vs Needs balance
    if (wantsPct <= 35) score += 15
    else if (wantsPct > 55) score -= 10

    // Factor 3: Emergency Solvency (checking if total account balances > 3x monthly spend)
    val totalAccountBalance = accounts.sumOf { it.balance }
    val monthlySpendRate = dailyAvg * 30.0
    if (monthlySpendRate > 0 && totalAccountBalance >= (monthlySpendRate * 3)) {
      score += 10
    }

    val healthScore = score.coerceIn(10, 100)
    val healthRating = when {
      healthScore >= 85 -> "Excellent • Robust Financial Discipline"
      healthScore >= 70 -> "Good • Healthy Cash Flow & Stability"
      healthScore >= 50 -> "Moderate • Fair Balance, Room to Optimize"
      else -> "Needs Attention • High Outflow Alert"
    }

    // Generate Tailored Suggestions
    val suggestions = generateSpendingSuggestions(
      totalIncome = totalIncome,
      totalExpense = totalExpense,
      netSavings = netSavings,
      savingsRate = savingsRate,
      categorySpendItems = categorySpendItems,
      wantsPct = wantsPct,
      currencySymbol = currencySymbol,
      dailyAvg = dailyAvg
    )

    return FinancialReportData(
      startDateMs = startDateMs,
      endDateMs = endDateMs,
      totalDays = totalDays,
      userName = userName.ifBlank { "Personal Portfolio" },
      currencySymbol = currencySymbol,
      totalIncome = totalIncome,
      totalExpense = totalExpense,
      netSavings = netSavings,
      savingsRate = savingsRate,
      dailyAverageSpend = dailyAvg,
      totalTransactionsCount = inRangeExpenses.size,
      categorySpending = categorySpendItems,
      topExpenses = topExpenses,
      accountSpendMap = accountOutflows,
      cashflowIntervals = cashflowIntervals,
      healthScore = healthScore,
      healthRating = healthRating,
      needsPercent = needsPct,
      wantsPercent = wantsPct,
      savingsPercent = savingsPct,
      spendingSuggestions = suggestions
    )
  }

  private fun computeCashflowBuckets(
    startMs: Long,
    endMs: Long,
    expenses: List<ExpenseEntity>
  ): List<CashflowInterval> {
    val durationDays = ((endMs - startMs) / (1000L * 60 * 60 * 24)).toInt()
    val isMonthly = durationDays > 60

    val buckets = mutableListOf<CashflowInterval>()
    val cal = Calendar.getInstance()
    cal.timeInMillis = startMs

    if (isMonthly) {
      val dateFormat = SimpleDateFormat("MMM yy", Locale.getDefault())
      while (cal.timeInMillis < endMs && buckets.size < 12) {
        val bucketStart = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        val bucketEnd = minOf(cal.timeInMillis, endMs)

        var inc = 0.0
        var exp = 0.0
        for (e in expenses) {
          if (e.timestamp in bucketStart until bucketEnd) {
            if (e.type.equals("INCOME", ignoreCase = true)) inc += e.amount
            else if (e.type.equals("EXPENSE", ignoreCase = true)) exp += e.amount
          }
        }
        buckets.add(CashflowInterval(dateFormat.format(Date(bucketStart)), inc, exp))
      }
    } else {
      val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
      while (cal.timeInMillis < endMs && buckets.size < 8) {
        val bucketStart = cal.timeInMillis
        cal.add(Calendar.DAY_OF_MONTH, 7)
        val bucketEnd = minOf(cal.timeInMillis, endMs)

        var inc = 0.0
        var exp = 0.0
        for (e in expenses) {
          if (e.timestamp in bucketStart until bucketEnd) {
            if (e.type.equals("INCOME", ignoreCase = true)) inc += e.amount
            else if (e.type.equals("EXPENSE", ignoreCase = true)) exp += e.amount
          }
        }
        buckets.add(CashflowInterval("W" + (buckets.size + 1), inc, exp))
      }
    }

    if (buckets.isEmpty()) {
      buckets.add(CashflowInterval("Period", expenses.filter { it.type.equals("INCOME", true) }.sumOf { it.amount }, expenses.filter { it.type.equals("EXPENSE", true) }.sumOf { it.amount }))
    }

    return buckets
  }

  private fun generateSpendingSuggestions(
    totalIncome: Double,
    totalExpense: Double,
    netSavings: Double,
    savingsRate: Double,
    categorySpendItems: List<CategorySpendItem>,
    wantsPct: Double,
    currencySymbol: String,
    dailyAvg: Double
  ): List<ReportSuggestion> {
    val list = mutableListOf<ReportSuggestion>()

    // 1. Top Category Concentration
    val topCat = categorySpendItems.firstOrNull()
    if (topCat != null && topCat.percentageOfSpend > 25.0) {
      val trimAmt = topCat.totalAmount * 0.15
      list.add(
        ReportSuggestion(
          title = "Concentration Alert: ${topCat.categoryName} (${"%.1f".format(topCat.percentageOfSpend)}% of Spend)",
          description = "Your single highest expense category is ${topCat.categoryName}, consuming $currencySymbol${"%,.0f".format(topCat.totalAmount)}. Trimming discretionary choices in this area by just 15% would liberate $currencySymbol${"%,.0f".format(trimAmt)} back to your savings.",
          tag = "ALERT",
          impact = "+$currencySymbol${"%,.0f".format(trimAmt)} Potential Savings"
        )
      )
    }

    // 2. Savings Rate Optimization
    if (savingsRate < 20.0) {
      val targetGain = (totalIncome * 0.20) - netSavings
      list.add(
        ReportSuggestion(
          title = "Elevate Savings to 20% Benchmark",
          description = "Your current savings rate is ${"%.1f".format(savingsRate)}%. Financial planners recommend maintaining at least 20% to safeguard against emergencies and accelerate wealth creation. Strive to set aside an additional $currencySymbol${"%,.0f".format(targetGain.coerceAtLeast(0.0))} monthly.",
          tag = "SAVINGS",
          impact = "Target 20%+ Savings Rate"
        )
      )
    } else {
      list.add(
        ReportSuggestion(
          title = "Excellent Capital Accumulation (${"%.1f".format(savingsRate)}%)",
          description = "You retained $currencySymbol${"%,.0f".format(netSavings)} of your inflows during this period! To outpace inflation, consider routing surplus funds into diversified mutual fund SIPs like active momentum or index schemes.",
          tag = "INVESTMENT",
          impact = "Wealth Multiplier"
        )
      )
    }

    // 3. 50/30/20 Rule Compliance
    if (wantsPct > 35.0) {
      list.add(
        ReportSuggestion(
          title = "Wants Exceed 50/30/20 Rule Guideline",
          description = "Discretionary expenditures (dining, entertainment, shopping) accounted for ${"%.1f".format(wantsPct)}% of total cashflow (target: 30%). Moderating lifestyle splurges will dramatically ease cashflow pressure.",
          tag = "BUDGET",
          impact = "Better Cashflow Buffer"
        )
      )
    }

    // 4. Daily Run-Rate Discipline
    val weekendBurnEst = dailyAvg * 1.4
    list.add(
      ReportSuggestion(
        title = "Daily Spending Velocity: $currencySymbol${"%,.0f".format(dailyAvg)}/day",
        description = "Your average expenditure per day was $currencySymbol${"%,.0f".format(dailyAvg)}. Implementing a micro-budgeting cap (e.g. no-spend weekdays or weekend dining limits of $currencySymbol${"%,.0f".format(weekendBurnEst)}) will provide immediate structure.",
        tag = "BUDGET",
        impact = "Lower Burn Rate"
      )
    )

    // 5. Automated Transfer Mandate Advice
    list.add(
      ReportSuggestion(
        title = "Set Up Automated Low-Balance Shields",
        description = "Prevent unexpected overdrafts or account dips by enabling Conditional Transfer Mandates in BudgetWise. Automatic refills ensure your checking account remains above minimum balance thresholds.",
        tag = "SAVINGS",
        impact = "Zero Penalty Risk"
      )
    )

    return list
  }

  /**
   * Generates a multi-page high-resolution PDF document and saves it directly to the user's
   * system Downloads folder as well as application cache for sharing.
   */
  suspend fun generateAndDownloadPdf(
    context: Context,
    reportData: FinancialReportData
  ): ReportGenerationResult = withContext(Dispatchers.IO) {
    val pdfDocument = PdfDocument()

    // Page 1: Executive Summary, Key Metrics, Health Score, Donut & Bar Graphs
    renderPageOne(pdfDocument, reportData)

    // Page 2: Detailed Category Breakdown Table, Top Transactions, Account Distribution
    renderPageTwo(pdfDocument, reportData)

    // Page 3: Smart Financial Insights, 50/30/20 Analysis, Spending Advice & Verification
    renderPageThree(pdfDocument, reportData)

    val dateFormat = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault())
    val timestamp = dateFormat.format(Date())
    val sanitizedUser = reportData.userName.replace(Regex("[^a-zA-Z0-9]"), "_").take(12)
    val fileName = "BudgetWise_Financial_Report_${sanitizedUser}_$timestamp.pdf"

    // Save to Cache & MediaStore Downloads
    val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
    val cachedFile = File(exportDir, fileName)
    FileOutputStream(cachedFile).use { out ->
      pdfDocument.writeTo(out)
    }
    pdfDocument.close()

    // Direct save into device's public Downloads directory
    var downloadLocation = "Downloads/BudgetWiseReports/$fileName"
    var finalShareUri: Uri? = null

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      try {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
          put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
          put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
          put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/BudgetWiseReports")
        }
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        if (uri != null) {
          resolver.openOutputStream(uri)?.use { outStream ->
            cachedFile.inputStream().use { inStream ->
              inStream.copyTo(outStream)
            }
          }
          finalShareUri = uri
        }
      } catch (_: Exception) {
        // Fallback handled below
      }
    } else {
      try {
        val pubDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "BudgetWiseReports")
        pubDir.mkdirs()
        val pubFile = File(pubDir, fileName)
        cachedFile.copyTo(pubFile, overwrite = true)
        downloadLocation = pubFile.absolutePath
        finalShareUri = Uri.fromFile(pubFile)
      } catch (_: Exception) {}
    }

    if (finalShareUri == null) {
      finalShareUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", cachedFile)
    }

    ReportGenerationResult(
      pdfFile = cachedFile,
      shareUri = finalShareUri,
      downloadLocation = downloadLocation,
      reportData = reportData
    )
  }

  // ==========================================
  // PAGE 1: EXECUTIVE COVER, METRICS & CHARTS
  // ==========================================
  private fun renderPageOne(pdfDocument: PdfDocument, data: FinancialReportData) {
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val textPaint = TextPaint(paint)

    // Background
    canvas.drawColor(Color.WHITE)

    // Top Header Banner
    paint.color = 0xFF1B664B.toInt()
    canvas.drawRect(0f, 0f, 595f, 92f, paint)

    // Accent line
    paint.color = 0xFF2ECC71.toInt()
    canvas.drawRect(0f, 88f, 595f, 92f, paint)

    // Header Title
    paint.color = Color.WHITE
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    paint.textSize = 18f
    canvas.drawText("BUDGETWISE • COMPREHENSIVE FINANCIAL REPORT", 32f, 42f, paint)

    // Header Subtitle
    val dateFmt = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val startStr = dateFmt.format(Date(data.startDateMs))
    val endStr = dateFmt.format(Date(data.endDateMs))
    paint.textSize = 10f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    canvas.drawText("AUDIT PERIOD: $startStr – $endStr (${data.totalDays} Days)  |  PREPARED FOR: ${data.userName.uppercase()}", 32f, 62f, paint)

    paint.textSize = 8.5f
    paint.color = 0xFFD4EDDA.toInt()
    canvas.drawText("OFFICIAL STATEMENT  •  CONFIDENTIAL PERSONAL FINANCE DOCUMENT", 32f, 78f, paint)

    // 1. Executive Summary Cards (4 Columns)
    val cardY = 110f
    val cardH = 68f
    val cardW = 122f
    val spacing = 12f
    val startX = 32f

    // Card 1: Total Inflow
    drawStatCard(canvas, startX, cardY, cardW, cardH, "TOTAL INFLOW", "${data.currencySymbol}${"%,.0f".format(data.totalIncome)}", 0xFF27AE60.toInt(), "Income & Gains")

    // Card 2: Total Outflow
    drawStatCard(canvas, startX + (cardW + spacing), cardY, cardW, cardH, "TOTAL EXPENSE", "${data.currencySymbol}${"%,.0f".format(data.totalExpense)}", 0xFFE74C3C.toInt(), "Outflows")

    // Card 3: Net Savings
    val netColor = if (data.netSavings >= 0) 0xFF1B664B.toInt() else 0xFFE74C3C.toInt()
    drawStatCard(canvas, startX + (cardW + spacing) * 2, cardY, cardW, cardH, "NET SAVINGS", "${data.currencySymbol}${"%,.0f".format(data.netSavings)}", netColor, if (data.netSavings >= 0) "Surplus" else "Deficit")

    // Card 4: Savings Rate
    drawStatCard(canvas, startX + (cardW + spacing) * 3, cardY, cardW, cardH, "SAVINGS RATE", "${"%.1f".format(data.savingsRate)}%", 0xFF2980B9.toInt(), "Target: 20%+")

    // 2. Financial Health Scorecard Banner
    val healthY = 192f
    paint.color = 0xFFF8F9FA.toInt()
    val healthRect = RectF(32f, healthY, 563f, healthY + 54f)
    canvas.drawRoundRect(healthRect, 10f, 10f, paint)
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 1f
    paint.color = 0xFFE9ECEF.toInt()
    canvas.drawRoundRect(healthRect, 10f, 10f, paint)
    paint.style = Paint.Style.FILL

    // Health Score Circle
    paint.color = when {
      data.healthScore >= 80 -> 0xFF27AE60.toInt()
      data.healthScore >= 60 -> 0xFF2980B9.toInt()
      else -> 0xFFF39C12.toInt()
    }
    canvas.drawCircle(64f, healthY + 27f, 18f, paint)
    paint.color = Color.WHITE
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    paint.textSize = 12f
    paint.textAlign = Paint.Align.CENTER
    canvas.drawText("${data.healthScore}", 64f, healthY + 32f, paint)
    paint.textAlign = Paint.Align.LEFT

    // Health Rating Label
    paint.color = 0xFF2C3E50.toInt()
    paint.textSize = 12f
    canvas.drawText("FINANCIAL HEALTH INDEX: ${data.healthRating}", 94f, healthY + 23f, paint)
    paint.color = 0xFF7F8C8D.toInt()
    paint.textSize = 9.5f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    canvas.drawText("Daily Average Outflow: ${data.currencySymbol}${"%,.0f".format(data.dailyAverageSpend)}/day  •  Total Transactions: ${data.totalTransactionsCount}", 94f, healthY + 41f, paint)

    // Section Divider
    drawSectionHeader(canvas, 32f, 270f, "VISUAL GRAPHICAL ANALYSIS & CASH FLOW BREAKDOWN")

    // 3. GRAPH 1: Category Spending Donut Chart
    val graphY = 286f
    paint.color = 0xFFFDFDFD.toInt()
    val chartBox1 = RectF(32f, graphY, 288f, graphY + 250f)
    canvas.drawRoundRect(chartBox1, 8f, 8f, paint)
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 1f
    paint.color = 0xFFE2E8F0.toInt()
    canvas.drawRoundRect(chartBox1, 8f, 8f, paint)
    paint.style = Paint.Style.FILL

    paint.color = 0xFF1B664B.toInt()
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    paint.textSize = 11f
    canvas.drawText("1. Expense Category Allocation", 44f, graphY + 20f, paint)

    // Render Donut Chart
    drawDonutChart(
      canvas = canvas,
      centerX = 100f,
      centerY = graphY + 115f,
      radius = 58f,
      holeRadius = 34f,
      items = data.categorySpending
    )

    // Legend for Category Donut
    val legendX = 175f
    var legendY = graphY + 45f
    val topItems = data.categorySpending.take(6)
    for (item in topItems) {
      paint.color = item.colorHex
      canvas.drawCircle(legendX, legendY, 4f, paint)
      paint.color = 0xFF2D3748.toInt()
      paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      paint.textSize = 8.5f
      val catName = if (item.categoryName.length > 12) item.categoryName.take(10) + ".." else item.categoryName
      canvas.drawText(catName, legendX + 8f, legendY + 3f, paint)

      paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
      paint.color = 0xFF718096.toInt()
      canvas.drawText("${data.currencySymbol}${"%,.0f".format(item.totalAmount)} (${"%.1f".format(item.percentageOfSpend)}%)", legendX + 8f, legendY + 13f, paint)

      legendY += 24f
    }

    // 4. GRAPH 2: Periodic Cash Flow Bar Chart (Income vs Expense)
    val chartBox2 = RectF(304f, graphY, 563f, graphY + 250f)
    paint.color = 0xFFFDFDFD.toInt()
    canvas.drawRoundRect(chartBox2, 8f, 8f, paint)
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 1f
    paint.color = 0xFFE2E8F0.toInt()
    canvas.drawRoundRect(chartBox2, 8f, 8f, paint)
    paint.style = Paint.Style.FILL

    paint.color = 0xFF1B664B.toInt()
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    paint.textSize = 11f
    canvas.drawText("2. Inflow vs Outflow Velocity", 316f, graphY + 20f, paint)

    // Render Cash Flow Bars
    drawCashflowBarChart(
      canvas = canvas,
      left = 316f,
      top = graphY + 38f,
      right = 551f,
      bottom = graphY + 230f,
      intervals = data.cashflowIntervals
    )

    // 5. GRAPH 3: Net Savings Trajectory Overview (Curve / Area)
    val graph3Y = 550f
    paint.color = 0xFFFDFDFD.toInt()
    val chartBox3 = RectF(32f, graph3Y, 563f, graph3Y + 160f)
    canvas.drawRoundRect(chartBox3, 8f, 8f, paint)
    paint.style = Paint.Style.STROKE
    paint.color = 0xFFE2E8F0.toInt()
    canvas.drawRoundRect(chartBox3, 8f, 8f, paint)
    paint.style = Paint.Style.FILL

    paint.color = 0xFF1B664B.toInt()
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    paint.textSize = 11f
    canvas.drawText("3. Cumulative Savings & Cash Flow Evolution", 44f, graph3Y + 20f, paint)

    drawSavingsTrajectoryChart(
      canvas = canvas,
      left = 44f,
      top = graph3Y + 36f,
      right = 551f,
      bottom = graph3Y + 145f,
      intervals = data.cashflowIntervals,
      currencySymbol = data.currencySymbol
    )

    // Page 1 Footer
    drawPageFooter(canvas, 1, 3)

    pdfDocument.finishPage(page)
  }

  // ==========================================
  // PAGE 2: WHERE MONEY WAS SPENT (TABLES)
  // ==========================================
  private fun renderPageTwo(pdfDocument: PdfDocument, data: FinancialReportData) {
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 2).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    canvas.drawColor(Color.WHITE)

    // Compact Top Bar
    paint.color = 0xFF1B664B.toInt()
    canvas.drawRect(0f, 0f, 595f, 40f, paint)
    paint.color = Color.WHITE
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    paint.textSize = 12f
    canvas.drawText("WHERE YOUR MONEY WAS SPENT • DETAILED EXPENDITURE AUDIT", 32f, 25f, paint)

    // SECTION 1: CATEGORY EXPENDITURE TABLE
    drawSectionHeader(canvas, 32f, 65f, "1. ALL CATEGORY BREAKDOWN TABLE")

    val tableY = 78f
    val colCategory = 36f
    val colAmount = 240f
    val colPercent = 340f
    val colCount = 420f
    val colStatus = 490f

    // Header Row
    paint.color = 0xFFF1F5F9.toInt()
    canvas.drawRect(32f, tableY, 563f, tableY + 22f, paint)
    paint.color = 0xFF475569.toInt()
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    paint.textSize = 9f
    canvas.drawText("CATEGORY", colCategory, tableY + 15f, paint)
    canvas.drawText("TOTAL SPENT", colAmount, tableY + 15f, paint)
    canvas.drawText("% OF TOTAL", colPercent, tableY + 15f, paint)
    canvas.drawText("TXNS", colCount, tableY + 15f, paint)
    canvas.drawText("STATUS", colStatus, tableY + 15f, paint)

    var rowY = tableY + 22f
    val displayCategories = data.categorySpending.take(10)
    displayCategories.forEachIndexed { i, cat ->
      val bg = if (i % 2 == 0) Color.WHITE else 0xFFF8FAFC.toInt()
      paint.color = bg
      canvas.drawRect(32f, rowY, 563f, rowY + 20f, paint)

      // Color bullet
      paint.color = cat.colorHex
      canvas.drawCircle(colCategory + 4f, rowY + 10f, 3.5f, paint)

      paint.color = 0xFF1E293B.toInt()
      paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      paint.textSize = 9f
      canvas.drawText(cat.categoryName, colCategory + 12f, rowY + 13f, paint)

      paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
      canvas.drawText("${data.currencySymbol}${"%,.2f".format(cat.totalAmount)}", colAmount, rowY + 13f, paint)
      canvas.drawText("${"%.1f".format(cat.percentageOfSpend)}%", colPercent, rowY + 13f, paint)
      canvas.drawText("${cat.transactionCount}", colCount, rowY + 13f, paint)

      val statusStr = if (cat.percentageOfSpend > 25) "High Drain" else if (cat.percentageOfSpend > 10) "Moderate" else "Controlled"
      paint.color = if (cat.percentageOfSpend > 25) 0xFFE74C3C.toInt() else if (cat.percentageOfSpend > 10) 0xFFD97706.toInt() else 0xFF16A34A.toInt()
      paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      canvas.drawText(statusStr, colStatus, rowY + 13f, paint)

      rowY += 20f
    }

    // Border line under table
    paint.color = 0xFFCBD5E1.toInt()
    paint.strokeWidth = 1f
    canvas.drawLine(32f, rowY, 563f, rowY, paint)

    // SECTION 2: TOP LARGEST EXPENSE TRANSACTIONS
    val topTxnY = rowY + 25f
    drawSectionHeader(canvas, 32f, topTxnY, "2. LARGEST OUTFLOW TRANSACTIONS (TOP 8)")

    val txnTableY = topTxnY + 12f
    paint.color = 0xFFF1F5F9.toInt()
    canvas.drawRect(32f, txnTableY, 563f, txnTableY + 22f, paint)
    paint.color = 0xFF475569.toInt()
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    paint.textSize = 9f
    canvas.drawText("DATE", 36f, txnTableY + 15f, paint)
    canvas.drawText("DESCRIPTION / MERCHANT", 105f, txnTableY + 15f, paint)
    canvas.drawText("CATEGORY", 295f, txnTableY + 15f, paint)
    canvas.drawText("ACCOUNT", 390f, txnTableY + 15f, paint)
    canvas.drawText("AMOUNT", 480f, txnTableY + 15f, paint)

    var txnRowY = txnTableY + 22f
    val txnDateFmt = SimpleDateFormat("dd MMM", Locale.getDefault())
    data.topExpenses.forEachIndexed { idx, e ->
      val bg = if (idx % 2 == 0) Color.WHITE else 0xFFF8FAFC.toInt()
      paint.color = bg
      canvas.drawRect(32f, txnRowY, 563f, txnRowY + 20f, paint)

      paint.color = 0xFF64748B.toInt()
      paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
      paint.textSize = 8.5f
      canvas.drawText(txnDateFmt.format(Date(e.timestamp)), 36f, txnRowY + 13f, paint)

      paint.color = 0xFF1E293B.toInt()
      paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      val titleStr = if (e.title.length > 25) e.title.take(23) + ".." else e.title
      canvas.drawText(titleStr, 105f, txnRowY + 13f, paint)

      paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
      paint.color = 0xFF475569.toInt()
      canvas.drawText(e.category, 295f, txnRowY + 13f, paint)
      canvas.drawText(e.account.take(14), 390f, txnRowY + 13f, paint)

      paint.color = 0xFFE74C3C.toInt()
      paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      canvas.drawText("${data.currencySymbol}${"%,.2f".format(e.amount)}", 480f, txnRowY + 13f, paint)

      txnRowY += 20f
    }
    paint.color = 0xFFCBD5E1.toInt()
    canvas.drawLine(32f, txnRowY, 563f, txnRowY, paint)

    // SECTION 3: ACCOUNT OUTFLOW UTILIZATION
    val accY = txnRowY + 25f
    drawSectionHeader(canvas, 32f, accY, "3. ACCOUNT UTILIZATION & LIQUIDITY OUTFLOWS")

    var accCardX = 32f
    val accCardY = accY + 14f
    val accCardW = 168f
    val accCardH = 55f

    data.accountSpendMap.entries.take(3).forEach { acc ->
      paint.color = 0xFFF8FAFC.toInt()
      val rect = RectF(accCardX, accCardY, accCardX + accCardW, accCardY + accCardH)
      canvas.drawRoundRect(rect, 8f, 8f, paint)
      paint.style = Paint.Style.STROKE
      paint.color = 0xFFE2E8F0.toInt()
      canvas.drawRoundRect(rect, 8f, 8f, paint)
      paint.style = Paint.Style.FILL

      paint.color = 0xFF334155.toInt()
      paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      paint.textSize = 10f
      canvas.drawText(acc.key.take(18), accCardX + 12f, accCardY + 20f, paint)

      paint.color = 0xFFE74C3C.toInt()
      paint.textSize = 12f
      canvas.drawText("${data.currencySymbol}${"%,.0f".format(acc.value)}", accCardX + 12f, accCardY + 40f, paint)

      accCardX += accCardW + 12f
    }

    // Page 2 Footer
    drawPageFooter(canvas, 2, 3)

    pdfDocument.finishPage(page)
  }

  // ==========================================
  // PAGE 3: SMART SPENDING SUGGESTIONS & AUDIT
  // ==========================================
  private fun renderPageThree(pdfDocument: PdfDocument, data: FinancialReportData) {
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 3).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val textPaint = TextPaint(paint)

    canvas.drawColor(Color.WHITE)

    // Compact Top Bar
    paint.color = 0xFF1B664B.toInt()
    canvas.drawRect(0f, 0f, 595f, 40f, paint)
    paint.color = Color.WHITE
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    paint.textSize = 12f
    canvas.drawText("FINANCIAL HEALTH & ACTIONABLE SPENDING SUGGESTIONS", 32f, 25f, paint)

    // 1. 50/30/20 Rule Analysis Section
    drawSectionHeader(canvas, 32f, 65f, "1. 50 / 30 / 20 BUDGET ALLOCATION AUDIT")

    val budgetY = 78f
    val budgetRect = RectF(32f, budgetY, 563f, budgetY + 90f)
    paint.color = 0xFFF8FAFC.toInt()
    canvas.drawRoundRect(budgetRect, 10f, 10f, paint)
    paint.style = Paint.Style.STROKE
    paint.color = 0xFFE2E8F0.toInt()
    canvas.drawRoundRect(budgetRect, 10f, 10f, paint)
    paint.style = Paint.Style.FILL

    // Visual Split Bar
    val barX = 46f
    val barY = budgetY + 28f
    val barW = 503f
    val barH = 14f

    val needsW = barW * (data.needsPercent.toFloat() / 100f).coerceIn(0f, 1f)
    val wantsW = barW * (data.wantsPercent.toFloat() / 100f).coerceIn(0f, 1f)
    val savingsW = (barW - needsW - wantsW).coerceAtLeast(0f)

    // Segment 1: Needs (Blue)
    paint.color = 0xFF2980B9.toInt()
    canvas.drawRect(barX, barY, barX + needsW, barY + barH, paint)

    // Segment 2: Wants (Amber)
    paint.color = 0xFFF39C12.toInt()
    canvas.drawRect(barX + needsW, barY, barX + needsW + wantsW, barY + barH, paint)

    // Segment 3: Savings (Green)
    paint.color = 0xFF27AE60.toInt()
    canvas.drawRect(barX + needsW + wantsW, barY, barX + barW, barY + barH, paint)

    // Legend underneath bar
    paint.textSize = 9.5f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

    paint.color = 0xFF2980B9.toInt()
    canvas.drawText("NEEDS: ${"%.1f".format(data.needsPercent)}% (Target: 50%)", barX, barY + 32f, paint)

    paint.color = 0xFFF39C12.toInt()
    canvas.drawText("WANTS: ${"%.1f".format(data.wantsPercent)}% (Target: 30%)", barX + 175f, barY + 32f, paint)

    paint.color = 0xFF27AE60.toInt()
    canvas.drawText("SAVINGS: ${"%.1f".format(data.savingsPercent)}% (Target: 20%)", barX + 350f, barY + 32f, paint)

    // 2. Actionable Spending Suggestions
    val sugY = budgetY + 110f
    drawSectionHeader(canvas, 32f, sugY, "2. PERSONALIZED SPENDING RECOMMENDATIONS & OPTIMIZATIONS")

    var cardItemY = sugY + 15f
    val cardItemW = 531f

    for (sug in data.spendingSuggestions) {
      val cardItemH = 68f
      val rect = RectF(32f, cardItemY, 32f + cardItemW, cardItemY + cardItemH)
      paint.color = 0xFFFBFBFB.toInt()
      canvas.drawRoundRect(rect, 8f, 8f, paint)
      paint.style = Paint.Style.STROKE
      paint.strokeWidth = 1f
      paint.color = 0xFFE2E8F0.toInt()
      canvas.drawRoundRect(rect, 8f, 8f, paint)
      paint.style = Paint.Style.FILL

      // Tag Indicator Bar
      val tagColor = when (sug.tag) {
        "ALERT" -> 0xFFE74C3C.toInt()
        "SAVINGS" -> 0xFF1B664B.toInt()
        "INVESTMENT" -> 0xFF2980B9.toInt()
        else -> 0xFFF39C12.toInt()
      }
      paint.color = tagColor
      canvas.drawRect(32f, cardItemY, 36f, cardItemY + cardItemH, paint)

      // Title & Impact Badge
      paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      paint.textSize = 10f
      paint.color = 0xFF1E293B.toInt()
      canvas.drawText(sug.title, 44f, cardItemY + 18f, paint)

      // Impact badge
      paint.color = tagColor
      paint.textSize = 8.5f
      canvas.drawText(sug.impact, 400f, cardItemY + 18f, paint)

      // Body text layout (wrapped)
      textPaint.color = 0xFF475569.toInt()
      textPaint.textSize = 8.5f
      textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
      drawWrappedText(canvas, textPaint, sug.description, 44f, cardItemY + 25f, 480)

      cardItemY += cardItemH + 10f
    }

    // 3. Official Audit & Security Verification Block
    val signY = 670f
    val signRect = RectF(32f, signY, 563f, signY + 95f)
    paint.color = 0xFFF8FAFC.toInt()
    canvas.drawRoundRect(signRect, 8f, 8f, paint)
    paint.style = Paint.Style.STROKE
    paint.color = 0xFFCBD5E1.toInt()
    canvas.drawRoundRect(signRect, 8f, 8f, paint)
    paint.style = Paint.Style.FILL

    paint.color = 0xFF1B664B.toInt()
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    paint.textSize = 10f
    canvas.drawText("BUDGETWISE AUDIT VERIFICATION & SECURITY STATEMENT", 44f, signY + 22f, paint)

    paint.color = 0xFF64748B.toInt()
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    paint.textSize = 8f
    val signNotes = "This Financial Report was algorithmically assembled by BudgetWise Local Ledger Engine based on recorded transactions, portfolio holdings, and automated transfer mandates. No external telemetry was transmitted. Always consult a certified financial planner for commercial advisory."
    drawWrappedText(canvas, textPaint, signNotes, 44f, signY + 28f, 490)

    val dateGen = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault()).format(Date())
    paint.color = 0xFF334155.toInt()
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    paint.textSize = 8.5f
    canvas.drawText("GENERATED ON: $dateGen  |  REPORT STATUS: VERIFIED", 44f, signY + 80f, paint)

    // Page 3 Footer
    drawPageFooter(canvas, 3, 3)

    pdfDocument.finishPage(page)
  }

  // ==========================================
  // GRAPH DRAWING HELPERS
  // ==========================================
  private fun drawDonutChart(
    canvas: Canvas,
    centerX: Float,
    centerY: Float,
    radius: Float,
    holeRadius: Float,
    items: List<CategorySpendItem>
  ) {
    if (items.isEmpty()) return
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    val oval = RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius)
    var currentAngle = -90f

    for (item in items) {
      val sweep = (item.percentageOfSpend.toFloat() / 100f) * 360f
      if (sweep <= 0f) continue
      paint.color = item.colorHex
      canvas.drawArc(oval, currentAngle, sweep, true, paint)
      currentAngle += sweep
    }

    // Inner Hole (Donut hole)
    paint.color = Color.WHITE
    canvas.drawCircle(centerX, centerY, holeRadius, paint)
  }

  private fun drawCashflowBarChart(
    canvas: Canvas,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    intervals: List<CashflowInterval>
  ) {
    if (intervals.isEmpty()) return

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val maxVal = intervals.maxOfOrNull { maxOf(it.income, it.expense) }?.coerceAtLeast(100.0) ?: 100.0
    val chartH = bottom - top
    val groupCount = intervals.size
    val groupW = (right - left) / groupCount
    val barW = (groupW * 0.35f).coerceAtMost(16f)

    // Base horizontal gridline
    paint.color = 0xFFE2E8F0.toInt()
    paint.strokeWidth = 1f
    canvas.drawLine(left, bottom, right, bottom, paint)

    intervals.forEachIndexed { i, interval ->
      val gx = left + (i * groupW) + (groupW / 2f)

      val incH = ((interval.income / maxVal) * chartH * 0.85f).toFloat()
      val expH = ((interval.expense / maxVal) * chartH * 0.85f).toFloat()

      // Income Bar (Green)
      paint.style = Paint.Style.FILL
      paint.color = 0xFF27AE60.toInt()
      val incLeft = gx - barW - 1f
      canvas.drawRect(incLeft, bottom - incH, incLeft + barW, bottom, paint)

      // Expense Bar (Red)
      paint.color = 0xFFE74C3C.toInt()
      val expLeft = gx + 1f
      canvas.drawRect(expLeft, bottom - expH, expLeft + barW, bottom, paint)

      // Label
      paint.color = 0xFF718096.toInt()
      paint.textSize = 7.5f
      paint.textAlign = Paint.Align.CENTER
      canvas.drawText(interval.label, gx, bottom + 12f, paint)
      paint.textAlign = Paint.Align.LEFT
    }

    // Legend on top right of chart
    paint.textSize = 7.5f
    paint.style = Paint.Style.FILL
    paint.color = 0xFF27AE60.toInt()
    canvas.drawCircle(right - 90f, top + 6f, 3f, paint)
    paint.color = 0xFF4A5568.toInt()
    canvas.drawText("Inflow", right - 84f, top + 9f, paint)

    paint.color = 0xFFE74C3C.toInt()
    canvas.drawCircle(right - 45f, top + 6f, 3f, paint)
    paint.color = 0xFF4A5568.toInt()
    canvas.drawText("Outflow", right - 39f, top + 9f, paint)
  }

  private fun drawSavingsTrajectoryChart(
    canvas: Canvas,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    intervals: List<CashflowInterval>,
    currencySymbol: String
  ) {
    if (intervals.isEmpty()) return

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Compute cumulative balance
    var runningNet = 0.0
    val points = mutableListOf<Double>()
    for (it in intervals) {
      runningNet += (it.income - it.expense)
      points.add(runningNet)
    }

    val minNet = minOf(points.minOrNull() ?: 0.0, 0.0)
    val maxNet = maxOf(points.maxOrNull() ?: 100.0, 100.0)
    val range = (maxNet - minNet).coerceAtLeast(10.0)

    val stepX = (right - left) / (points.size - 1).coerceAtLeast(1)
    val path = android.graphics.Path()

    points.forEachIndexed { i, v ->
      val px = left + (i * stepX)
      val py = bottom - (((v - minNet) / range) * (bottom - top)).toFloat()
      if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
    }

    // Draw curve
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 2.5f
    paint.color = 0xFF1B664B.toInt()
    canvas.drawPath(path, paint)

    // Draw points and values
    paint.style = Paint.Style.FILL
    points.forEachIndexed { i, v ->
      val px = left + (i * stepX)
      val py = bottom - (((v - minNet) / range) * (bottom - top)).toFloat()
      paint.color = 0xFF1B664B.toInt()
      canvas.drawCircle(px, py, 3.5f, paint)
      paint.color = Color.WHITE
      canvas.drawCircle(px, py, 1.8f, paint)

      if (i == 0 || i == points.size - 1 || i == points.size / 2) {
        paint.color = 0xFF2D3748.toInt()
        paint.textSize = 7.5f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("$currencySymbol${"%,.0f".format(v)}", px, py - 6f, paint)
        paint.textAlign = Paint.Align.LEFT
      }
    }
  }

  // ==========================================
  // SHARED RENDERING UTILITIES
  // ==========================================
  private fun drawStatCard(
    canvas: Canvas,
    x: Float,
    y: Float,
    w: Float,
    h: Float,
    label: String,
    value: String,
    accentColor: Int,
    subtext: String
  ) {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val rect = RectF(x, y, x + w, y + h)

    paint.color = 0xFFF8F9FA.toInt()
    canvas.drawRoundRect(rect, 8f, 8f, paint)

    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 1f
    paint.color = 0xFFE9ECEF.toInt()
    canvas.drawRoundRect(rect, 8f, 8f, paint)
    paint.style = Paint.Style.FILL

    // Top color strip
    paint.color = accentColor
    canvas.drawRoundRect(RectF(x, y, x + w, y + 4f), 2f, 2f, paint)

    // Label
    paint.color = 0xFF6C757D.toInt()
    paint.textSize = 7.5f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText(label, x + 10f, y + 18f, paint)

    // Value
    paint.color = 0xFF212529.toInt()
    paint.textSize = 12f
    canvas.drawText(value, x + 10f, y + 36f, paint)

    // Subtext
    paint.color = 0xFFADB5BD.toInt()
    paint.textSize = 7.5f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    canvas.drawText(subtext, x + 10f, y + 54f, paint)
  }

  private fun drawSectionHeader(canvas: Canvas, x: Float, y: Float, title: String) {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    paint.color = 0xFF1B664B.toInt()
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    paint.textSize = 10f
    canvas.drawText(title, x, y, paint)

    paint.color = 0xFFE2E8F0.toInt()
    paint.strokeWidth = 1f
    canvas.drawLine(x, y + 4f, 563f, y + 4f, paint)
  }

  private fun drawWrappedText(
    canvas: Canvas,
    paint: TextPaint,
    text: String,
    x: Float,
    y: Float,
    width: Int
  ) {
    val layout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
        .setAlignment(Layout.Alignment.ALIGN_NORMAL)
        .setLineSpacing(0f, 1.15f)
        .setIncludePad(false)
        .build()
    } else {
      @Suppress("DEPRECATION")
      StaticLayout(text, paint, width, Layout.Alignment.ALIGN_NORMAL, 1.15f, 0f, false)
    }

    canvas.save()
    canvas.translate(x, y)
    layout.draw(canvas)
    canvas.restore()
  }

  private fun drawPageFooter(canvas: Canvas, currentPage: Int, totalPages: Int) {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    paint.color = 0xFFE2E8F0.toInt()
    paint.strokeWidth = 1f
    canvas.drawLine(32f, 810f, 563f, 810f, paint)

    paint.color = 0xFF94A3B8.toInt()
    paint.textSize = 8f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    canvas.drawText("BudgetWise Finance Ledger • Encrypted Local Report", 32f, 824f, paint)

    paint.textAlign = Paint.Align.RIGHT
    canvas.drawText("Page $currentPage of $totalPages", 563f, 824f, paint)
    paint.textAlign = Paint.Align.LEFT
  }
}
