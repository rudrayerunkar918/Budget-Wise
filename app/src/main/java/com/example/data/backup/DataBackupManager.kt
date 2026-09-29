package com.example.data.backup

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.db.AppDatabase
import com.example.data.model.AccountEntity
import com.example.data.model.BudgetEntity
import com.example.data.model.ExpenseEntity
import com.example.data.model.LoanEntity
import com.example.data.model.SavingsGoalEntity
import com.example.data.model.StockEntity
import com.example.data.model.SubscriptionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ImportMode {
  MERGE,
  REPLACE
}

data class BackupSummary(
  val version: Int,
  val exportDate: String,
  val expensesCount: Int,
  val accountsCount: Int,
  val budgetsCount: Int,
  val goalsCount: Int,
  val subscriptionsCount: Int,
  val loansCount: Int,
  val stocksCount: Int,
  val rawJson: String
)

data class ImportResultStats(
  val importedExpenses: Int,
  val importedAccounts: Int,
  val importedBudgets: Int,
  val importedGoals: Int,
  val importedSubscriptions: Int,
  val importedLoans: Int,
  val importedStocks: Int,
  val isReplaceMode: Boolean
)

object DataBackupManager {

  suspend fun exportAllDataToJson(context: Context): Pair<File, String> = withContext(Dispatchers.IO) {
    val db = AppDatabase.getDatabase(context)

    val expenses = db.expenseDao().getAllExpensesList()
    val accounts = db.accountDao().getAllAccountsList()
    val budgets = db.budgetDao().getAllBudgetsList()
    val goals = db.savingsGoalDao().getAllGoalsList()
    val subscriptions = db.subscriptionDao().getAllSubscriptionsList()
    val loans = db.loanDao().getAllLoansList()
    val stocks = db.stockDao().getAllStocksList()

    val root = JSONObject()
    root.put("version", 1)
    root.put("appName", "BudgetWise Financial & Stock Tracker")
    val now = System.currentTimeMillis()
    root.put("exportTimestamp", now)
    val dateFormatted = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(now))
    root.put("exportDate", dateFormatted)

    // Summary counts
    val countsObj = JSONObject()
    countsObj.put("expenses", expenses.size)
    countsObj.put("accounts", accounts.size)
    countsObj.put("budgets", budgets.size)
    countsObj.put("goals", goals.size)
    countsObj.put("subscriptions", subscriptions.size)
    countsObj.put("loans", loans.size)
    countsObj.put("stocks", stocks.size)
    root.put("counts", countsObj)

    // 1. Expenses
    val expArray = JSONArray()
    for (e in expenses) {
      val obj = JSONObject()
      obj.put("id", e.id)
      obj.put("title", e.title)
      obj.put("amount", e.amount)
      obj.put("category", e.category)
      obj.put("timestamp", e.timestamp)
      obj.put("note", e.note)
      obj.put("type", e.type)
      obj.put("account", e.account)
      obj.put("toAccount", e.toAccount)
      expArray.put(obj)
    }
    root.put("expenses", expArray)

    // 2. Accounts
    val accArray = JSONArray()
    for (a in accounts) {
      val obj = JSONObject()
      obj.put("id", a.id)
      obj.put("name", a.name)
      obj.put("type", a.type)
      obj.put("balance", a.balance)
      obj.put("colorHex", a.colorHex)
      obj.put("iconName", a.iconName)
      accArray.put(obj)
    }
    root.put("accounts", accArray)

    // 3. Budgets
    val budArray = JSONArray()
    for (b in budgets) {
      val obj = JSONObject()
      obj.put("id", b.id)
      obj.put("category", b.category)
      obj.put("monthlyLimit", b.monthlyLimit)
      obj.put("monthYear", b.monthYear)
      budArray.put(obj)
    }
    root.put("budgets", budArray)

    // 4. Savings Goals
    val goalArray = JSONArray()
    for (g in goals) {
      val obj = JSONObject()
      obj.put("id", g.id)
      obj.put("title", g.title)
      obj.put("targetAmount", g.targetAmount)
      obj.put("currentAmount", g.currentAmount)
      obj.put("deadlineTimestamp", g.deadlineTimestamp)
      obj.put("category", g.category)
      obj.put("isCompleted", g.isCompleted)
      goalArray.put(obj)
    }
    root.put("savingsGoals", goalArray)

    // 5. Subscriptions
    val subArray = JSONArray()
    for (s in subscriptions) {
      val obj = JSONObject()
      obj.put("id", s.id)
      obj.put("title", s.title)
      obj.put("amount", s.amount)
      obj.put("billingCycle", s.billingCycle)
      obj.put("category", s.category)
      obj.put("account", s.account)
      obj.put("nextDueDate", s.nextDueDate)
      obj.put("note", s.note)
      subArray.put(obj)
    }
    root.put("subscriptions", subArray)

    // 6. Loans
    val loanArray = JSONArray()
    for (l in loans) {
      val obj = JSONObject()
      obj.put("id", l.id)
      obj.put("personName", l.personName)
      obj.put("type", l.type)
      obj.put("totalAmount", l.totalAmount)
      obj.put("paidAmount", l.paidAmount)
      obj.put("dueDate", l.dueDate ?: -1L)
      obj.put("account", l.account)
      obj.put("note", l.note)
      obj.put("isSettled", l.isSettled)
      obj.put("creationDate", l.creationDate)
      obj.put("interestRate", l.interestRate)
      loanArray.put(obj)
    }
    root.put("loans", loanArray)

    // 7. Stocks
    val stockArray = JSONArray()
    for (st in stocks) {
      val obj = JSONObject()
      obj.put("id", st.id)
      obj.put("symbol", st.symbol)
      obj.put("companyName", st.companyName)
      obj.put("exchange", st.exchange)
      obj.put("shares", st.shares)
      obj.put("avgBuyPrice", st.avgBuyPrice)
      obj.put("currentPrice", st.currentPrice)
      obj.put("purchaseDate", st.purchaseDate)
      obj.put("currencySymbol", st.currencySymbol)
      obj.put("notes", st.notes)
      obj.put("dailyChangePercent", st.dailyChangePercent)
      obj.put("lastPriceUpdated", st.lastPriceUpdated)
      obj.put("assetType", st.assetType)
      stockArray.put(obj)
    }
    root.put("stocks", stockArray)

    val jsonString = root.toString(2)
    val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val file = File(exportDir, "budgetwise_backup_$timestamp.json")
    file.writeText(jsonString)

    Pair(file, jsonString)
  }

  suspend fun exportTransactionsCsv(context: Context): File = withContext(Dispatchers.IO) {
    val db = AppDatabase.getDatabase(context)
    val expenses = db.expenseDao().getAllExpensesList()

    val sb = StringBuilder()
    sb.append("\"Date\",\"Type\",\"Title\",\"Amount\",\"Category\",\"Account\",\"ToAccount\",\"Note\"\n")

    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    for (e in expenses) {
      val dateStr = dateFormat.format(Date(e.timestamp))
      val title = e.title.replace("\"", "\"\"")
      val note = e.note.replace("\"", "\"\"")
      val category = e.category.replace("\"", "\"\"")
      val account = e.account.replace("\"", "\"\"")
      val toAccount = e.toAccount.replace("\"", "\"\"")

      sb.append("\"$dateStr\",")
      sb.append("\"${e.type}\",")
      sb.append("\"$title\",")
      sb.append("${e.amount},")
      sb.append("\"$category\",")
      sb.append("\"$account\",")
      sb.append("\"$toAccount\",")
      sb.append("\"$note\"\n")
    }

    val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val file = File(exportDir, "budgetwise_transactions_$timestamp.csv")
    file.writeText(sb.toString())
    file
  }

  fun shareFile(context: Context, file: File, mimeType: String, title: String) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
      type = mimeType
      putExtra(Intent.EXTRA_STREAM, uri)
      putExtra(Intent.EXTRA_SUBJECT, title)
      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    val chooser = Intent.createChooser(shareIntent, title)
    chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(chooser)
  }

  fun copyToClipboard(context: Context, text: String, label: String = "BudgetWise Backup") {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
  }

  fun parseBackupJson(jsonString: String): BackupSummary {
    val obj = JSONObject(jsonString)
    val version = obj.optInt("version", 1)
    val exportDate = obj.optString("exportDate", "Unknown date")

    val expensesCount = obj.optJSONArray("expenses")?.length() ?: 0
    val accountsCount = obj.optJSONArray("accounts")?.length() ?: 0
    val budgetsCount = obj.optJSONArray("budgets")?.length() ?: 0
    val goalsCount = obj.optJSONArray("savingsGoals")?.length() ?: 0
    val subscriptionsCount = obj.optJSONArray("subscriptions")?.length() ?: 0
    val loansCount = obj.optJSONArray("loans")?.length() ?: 0
    val stocksCount = obj.optJSONArray("stocks")?.length() ?: 0

    return BackupSummary(
      version = version,
      exportDate = exportDate,
      expensesCount = expensesCount,
      accountsCount = accountsCount,
      budgetsCount = budgetsCount,
      goalsCount = goalsCount,
      subscriptionsCount = subscriptionsCount,
      loansCount = loansCount,
      stocksCount = stocksCount,
      rawJson = jsonString
    )
  }

  suspend fun restoreBackup(context: Context, jsonString: String, mode: ImportMode): ImportResultStats = withContext(Dispatchers.IO) {
    val db = AppDatabase.getDatabase(context)
    val root = JSONObject(jsonString)

    if (mode == ImportMode.REPLACE) {
      db.expenseDao().clearAll()
      db.accountDao().clearAll()
      db.budgetDao().clearAll()
      db.savingsGoalDao().clearAll()
      db.subscriptionDao().clearAll()
      db.loanDao().clearAll()
      db.stockDao().clearAll()
    }

    // 1. Accounts
    var importedAccounts = 0
    val accArray = root.optJSONArray("accounts")
    if (accArray != null) {
      val accountsList = mutableListOf<AccountEntity>()
      for (i in 0 until accArray.length()) {
        val a = accArray.getJSONObject(i)
        accountsList.add(
          AccountEntity(
            id = if (mode == ImportMode.REPLACE) a.optLong("id", 0L) else 0L,
            name = a.optString("name", "Account"),
            type = a.optString("type", "CHECKING"),
            balance = a.optDouble("balance", 0.0),
            colorHex = a.optLong("colorHex", 0xFF1B664BL),
            iconName = a.optString("iconName", "ACCOUNT")
          )
        )
      }
      db.accountDao().insertAccounts(accountsList)
      importedAccounts = accountsList.size
    }

    // 2. Expenses
    var importedExpenses = 0
    val expArray = root.optJSONArray("expenses")
    if (expArray != null) {
      val expensesList = mutableListOf<ExpenseEntity>()
      for (i in 0 until expArray.length()) {
        val e = expArray.getJSONObject(i)
        expensesList.add(
          ExpenseEntity(
            id = if (mode == ImportMode.REPLACE) e.optLong("id", 0L) else 0L,
            title = e.optString("title", "Expense"),
            amount = e.optDouble("amount", 0.0),
            category = e.optString("category", "OTHER"),
            timestamp = e.optLong("timestamp", System.currentTimeMillis()),
            note = e.optString("note", ""),
            type = e.optString("type", "EXPENSE"),
            account = e.optString("account", "Main Checking"),
            toAccount = e.optString("toAccount", "")
          )
        )
      }
      db.expenseDao().insertExpenses(expensesList)
      importedExpenses = expensesList.size
    }

    // 3. Budgets
    var importedBudgets = 0
    val budArray = root.optJSONArray("budgets")
    if (budArray != null) {
      val budgetsList = mutableListOf<BudgetEntity>()
      for (i in 0 until budArray.length()) {
        val b = budArray.getJSONObject(i)
        budgetsList.add(
          BudgetEntity(
            id = if (mode == ImportMode.REPLACE) b.optLong("id", 0L) else 0L,
            category = b.optString("category", "TOTAL"),
            monthlyLimit = b.optDouble("monthlyLimit", 1000.0),
            monthYear = b.optString("monthYear", "2026-09")
          )
        )
      }
      db.budgetDao().insertBudgets(budgetsList)
      importedBudgets = budgetsList.size
    }

    // 4. Savings Goals
    var importedGoals = 0
    val goalArray = root.optJSONArray("savingsGoals")
    if (goalArray != null) {
      val goalsList = mutableListOf<SavingsGoalEntity>()
      for (i in 0 until goalArray.length()) {
        val g = goalArray.getJSONObject(i)
        goalsList.add(
          SavingsGoalEntity(
            id = if (mode == ImportMode.REPLACE) g.optLong("id", 0L) else 0L,
            title = g.optString("title", "Savings Goal"),
            targetAmount = g.optDouble("targetAmount", 5000.0),
            currentAmount = g.optDouble("currentAmount", 0.0),
            deadlineTimestamp = g.optLong("deadlineTimestamp", System.currentTimeMillis() + 86400000L * 30),
            category = g.optString("category", "SAVINGS"),
            isCompleted = g.optBoolean("isCompleted", false)
          )
        )
      }
      db.savingsGoalDao().insertGoals(goalsList)
      importedGoals = goalsList.size
    }

    // 5. Subscriptions
    var importedSubscriptions = 0
    val subArray = root.optJSONArray("subscriptions")
    if (subArray != null) {
      val subsList = mutableListOf<SubscriptionEntity>()
      for (i in 0 until subArray.length()) {
        val s = subArray.getJSONObject(i)
        subsList.add(
          SubscriptionEntity(
            id = if (mode == ImportMode.REPLACE) s.optLong("id", 0L) else 0L,
            title = s.optString("title", "Subscription"),
            amount = s.optDouble("amount", 9.99),
            billingCycle = s.optString("billingCycle", "Monthly"),
            category = s.optString("category", "ENTERTAINMENT"),
            account = s.optString("account", "Main Checking"),
            nextDueDate = s.optLong("nextDueDate", System.currentTimeMillis() + 86400000L * 30),
            note = s.optString("note", "")
          )
        )
      }
      db.subscriptionDao().insertSubscriptions(subsList)
      importedSubscriptions = subsList.size
    }

    // 6. Loans
    var importedLoans = 0
    val loanArray = root.optJSONArray("loans")
    if (loanArray != null) {
      val loansList = mutableListOf<LoanEntity>()
      for (i in 0 until loanArray.length()) {
        val l = loanArray.getJSONObject(i)
        val rawDue = l.optLong("dueDate", -1L)
        loansList.add(
          LoanEntity(
            id = if (mode == ImportMode.REPLACE) l.optLong("id", 0L) else 0L,
            personName = l.optString("personName", "Contact"),
            type = l.optString("type", "LENT"),
            totalAmount = l.optDouble("totalAmount", 100.0),
            paidAmount = l.optDouble("paidAmount", 0.0),
            account = l.optString("account", "Main Checking"),
            dueDate = if (rawDue > 0) rawDue else null,
            note = l.optString("note", ""),
            isSettled = l.optBoolean("isSettled", false),
            creationDate = l.optLong("creationDate", System.currentTimeMillis()),
            interestRate = l.optDouble("interestRate", 0.0)
          )
        )
      }
      db.loanDao().insertLoans(loansList)
      importedLoans = loansList.size
    }

    // 7. Stocks
    var importedStocks = 0
    val stockArray = root.optJSONArray("stocks")
    if (stockArray != null) {
      val stocksList = mutableListOf<StockEntity>()
      for (i in 0 until stockArray.length()) {
        val st = stockArray.getJSONObject(i)
        stocksList.add(
          StockEntity(
            id = if (mode == ImportMode.REPLACE) st.optLong("id", 0L) else 0L,
            symbol = st.optString("symbol", "RELIANCE.NS"),
            companyName = st.optString("companyName", "Reliance Industries"),
            shares = st.optDouble("shares", 1.0),
            avgBuyPrice = st.optDouble("avgBuyPrice", 100.0),
            currentPrice = st.optDouble("currentPrice", 100.0),
            purchaseDate = st.optLong("purchaseDate", System.currentTimeMillis()),
            currencySymbol = st.optString("currencySymbol", "₹"),
            notes = st.optString("notes", ""),
            lastPriceUpdated = st.optLong("lastPriceUpdated", System.currentTimeMillis()),
            dailyChangePercent = st.optDouble("dailyChangePercent", 0.0),
            exchange = st.optString("exchange", "NSE"),
            assetType = st.optString("assetType", "STOCK")
          )
        )
      }
      db.stockDao().insertStocks(stocksList)
      importedStocks = stocksList.size
    }

    ImportResultStats(
      importedExpenses = importedExpenses,
      importedAccounts = importedAccounts,
      importedBudgets = importedBudgets,
      importedGoals = importedGoals,
      importedSubscriptions = importedSubscriptions,
      importedLoans = importedLoans,
      importedStocks = importedStocks,
      isReplaceMode = mode == ImportMode.REPLACE
    )
  }
}
