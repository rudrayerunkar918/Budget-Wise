package com.example.data.backup

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.db.AppDatabase
import com.example.data.model.AccountEntity
import com.example.data.model.BudgetEntity
import com.example.data.model.ConditionalMandateEntity
import com.example.data.model.ExpenseEntity
import com.example.data.model.LoanEntity
import com.example.data.model.MutualFundSipEntity
import com.example.data.model.SavingsGoalEntity
import com.example.data.model.StockEntity
import com.example.data.model.SubscriptionEntity
import com.example.data.model.TransactionShortcutEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

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
  val sipsCount: Int = 0,
  val mandatesCount: Int = 0,
  val shortcutsCount: Int = 0,
  val isEncrypted: Boolean = false,
  val rawJson: String
) {
  val totalRecords: Int
    get() = expensesCount + accountsCount + budgetsCount + goalsCount +
        subscriptionsCount + loansCount + stocksCount + sipsCount + mandatesCount + shortcutsCount
}

data class ImportResultStats(
  val importedExpenses: Int,
  val importedAccounts: Int,
  val importedBudgets: Int,
  val importedGoals: Int,
  val importedSubscriptions: Int,
  val importedLoans: Int,
  val importedStocks: Int,
  val importedSips: Int = 0,
  val importedMandates: Int = 0,
  val importedShortcuts: Int = 0,
  val isReplaceMode: Boolean
) {
  val totalImported: Int
    get() = importedExpenses + importedAccounts + importedBudgets + importedGoals +
        importedSubscriptions + importedLoans + importedStocks + importedSips + importedMandates + importedShortcuts
}

data class DatabaseSnapshotInfo(
  val file: File,
  val fileName: String,
  val timestamp: Long,
  val formattedDate: String,
  val sizeBytes: Long,
  val formattedSize: String,
  val recordCount: Int,
  val isAutomated: Boolean,
  val isEncrypted: Boolean
)

data class DatabaseHealthInfo(
  val dbName: String,
  val dbSizeBytes: Long,
  val formattedSize: String,
  val version: Int,
  val tableCounts: Map<String, Int>,
  val totalRecords: Int,
  val integrityStatus: String,
  val lastSnapshotTime: Long?
)

object DataBackupManager {

  private const val MAGIC_HEADER = "BWISE_ENC_V1"
  private const val ITERATION_COUNT = 65536
  private const val KEY_LENGTH = 256
  private const val GCM_TAG_LENGTH = 128
  private const val SALT_LENGTH = 16
  private const val IV_LENGTH = 12

  // ==========================================
  // 1. AES-256 GCM Authenticated Encryption
  // ==========================================

  fun isEncryptedPayload(bytes: ByteArray): Boolean {
    val magicBytes = MAGIC_HEADER.toByteArray(StandardCharsets.UTF_8)
    if (bytes.size < magicBytes.size) return false
    for (i in magicBytes.indices) {
      if (bytes[i] != magicBytes[i]) return false
    }
    return true
  }

  fun encryptPayload(plaintext: String, passphrase: String): ByteArray {
    require(passphrase.isNotBlank()) { "Passphrase cannot be empty" }
    val random = SecureRandom()
    val salt = ByteArray(SALT_LENGTH).apply { random.nextBytes(this) }
    val iv = ByteArray(IV_LENGTH).apply { random.nextBytes(this) }

    val keySpec = PBEKeySpec(passphrase.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH)
    val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
    val keyBytes = factory.generateSecret(keySpec).encoded
    val secretKey = SecretKeySpec(keyBytes, "AES")

    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))
    val cipherBytes = cipher.doFinal(plaintext.toByteArray(StandardCharsets.UTF_8))

    val outputStream = ByteArrayOutputStream()
    outputStream.write(MAGIC_HEADER.toByteArray(StandardCharsets.UTF_8))
    outputStream.write(salt.size)
    outputStream.write(salt)
    outputStream.write(iv.size)
    outputStream.write(iv)
    outputStream.write(cipherBytes)
    return outputStream.toByteArray()
  }

  fun decryptPayload(bytes: ByteArray, passphrase: String): String {
    require(isEncryptedPayload(bytes)) { "Data is not in encrypted BudgetWise format" }
    require(passphrase.isNotBlank()) { "Passphrase cannot be empty" }

    val magicBytes = MAGIC_HEADER.toByteArray(StandardCharsets.UTF_8)
    val inputStream = ByteArrayInputStream(bytes)
    inputStream.skip(magicBytes.size.toLong())

    val saltSize = inputStream.read()
    val salt = ByteArray(saltSize)
    inputStream.read(salt)

    val ivSize = inputStream.read()
    val iv = ByteArray(ivSize)
    inputStream.read(iv)

    val cipherBytes = inputStream.readBytes()

    val keySpec = PBEKeySpec(passphrase.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH)
    val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
    val keyBytes = factory.generateSecret(keySpec).encoded
    val secretKey = SecretKeySpec(keyBytes, "AES")

    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))
    val plainBytes = cipher.doFinal(cipherBytes)
    return String(plainBytes, StandardCharsets.UTF_8)
  }

  // ==========================================
  // 2. Export Database to JSON / Encrypted File
  // ==========================================

  suspend fun exportAllDataToJson(context: Context, passphrase: String? = null): Pair<File, String> = withContext(Dispatchers.IO) {
    val db = AppDatabase.getDatabase(context)

    val expenses = db.expenseDao().getAllExpensesList()
    val accounts = db.accountDao().getAllAccountsList()
    val budgets = db.budgetDao().getAllBudgetsList()
    val goals = db.savingsGoalDao().getAllGoalsList()
    val subscriptions = db.subscriptionDao().getAllSubscriptionsList()
    val loans = db.loanDao().getAllLoansList()
    val stocks = db.stockDao().getAllStocksList()
    val sips = db.mutualFundSipDao().getAllSipsList()
    val mandates = db.conditionalMandateDao().getAllMandatesList()
    val shortcuts = db.transactionShortcutDao().getAllShortcutsList()

    val root = JSONObject()
    root.put("version", 2)
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
    countsObj.put("sips", sips.size)
    countsObj.put("mandates", mandates.size)
    countsObj.put("shortcuts", shortcuts.size)
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

    // 8. Mutual Fund SIPs
    val sipArray = JSONArray()
    for (sip in sips) {
      val obj = JSONObject()
      obj.put("id", sip.id)
      obj.put("schemeCode", sip.schemeCode)
      obj.put("schemeName", sip.schemeName)
      obj.put("installmentAmount", sip.installmentAmount)
      obj.put("frequency", sip.frequency)
      obj.put("debitAccount", sip.debitAccount)
      obj.put("sipDayOfMonth", sip.sipDayOfMonth)
      obj.put("nextExecutionDate", sip.nextExecutionDate)
      obj.put("isActive", sip.isActive)
      obj.put("totalInvested", sip.totalInvested)
      obj.put("installmentsCompleted", sip.installmentsCompleted)
      obj.put("notes", sip.notes)
      sipArray.put(obj)
    }
    root.put("sips", sipArray)

    // 9. Conditional Mandates
    val mandateArray = JSONArray()
    for (m in mandates) {
      val obj = JSONObject()
      obj.put("id", m.id)
      obj.put("title", m.title)
      obj.put("sourceAccount", m.sourceAccount)
      obj.put("targetAccount", m.targetAccount)
      obj.put("conditionType", m.conditionType)
      obj.put("thresholdAmount", m.thresholdAmount)
      obj.put("transferAmount", m.transferAmount)
      obj.put("isEnabled", m.isEnabled)
      obj.put("lastTriggeredAt", m.lastTriggeredAt)
      obj.put("totalTriggeredCount", m.totalTriggeredCount)
      obj.put("notes", m.notes)
      mandateArray.put(obj)
    }
    root.put("mandates", mandateArray)

    // 10. Quick Shortcuts
    val shortcutArray = JSONArray()
    for (sc in shortcuts) {
      val obj = JSONObject()
      obj.put("id", sc.id)
      obj.put("title", sc.title)
      obj.put("amount", sc.amount)
      obj.put("category", sc.category)
      obj.put("type", sc.type)
      obj.put("account", sc.account)
      obj.put("iconEmoji", sc.iconEmoji)
      obj.put("orderIndex", sc.orderIndex)
      shortcutArray.put(obj)
    }
    root.put("shortcuts", shortcutArray)

    val jsonString = root.toString(2)
    val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())

    val isEncrypted = !passphrase.isNullOrBlank()
    val file = if (isEncrypted) {
      val encFile = File(exportDir, "budgetwise_encrypted_$timestamp.bwise")
      val encryptedBytes = encryptPayload(jsonString, passphrase!!)
      encFile.writeBytes(encryptedBytes)
      encFile
    } else {
      val plainFile = File(exportDir, "budgetwise_backup_$timestamp.json")
      plainFile.writeText(jsonString)
      plainFile
    }

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

  fun openGoogleDrive(context: Context) {
    try {
      val driveIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://drive.google.com/drive/my-drive")).apply {
        setPackage("com.google.android.apps.docs")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(driveIntent)
    } catch (_: Exception) {
      try {
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://drive.google.com/drive/my-drive")).apply {
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(webIntent)
      } catch (_: Exception) {}
    }
  }

  fun openGoogleDriveBackupsSearch(context: Context) {
    try {
      val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://drive.google.com/drive/search?q=BudgetWise")).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)
    } catch (_: Exception) {}
  }

  fun uploadBackupToGoogleDrive(context: Context, file: File, mimeType: String, title: String) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
      type = mimeType
      putExtra(Intent.EXTRA_STREAM, uri)
      putExtra(Intent.EXTRA_SUBJECT, title)
      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      setPackage("com.google.android.apps.docs")
    }
    try {
      sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      context.startActivity(sendIntent)
    } catch (_: Exception) {
      shareFile(context, file, mimeType, "Save to Google Drive: $title")
    }
  }

  // ==========================================
  // 3. Local Rolling Snapshot System
  // ==========================================

  private fun getSnapshotsDir(context: Context): File {
    return File(context.filesDir, "snapshots").apply { mkdirs() }
  }

  suspend fun createLocalSnapshot(
    context: Context,
    isAutomated: Boolean,
    passphrase: String? = null,
    maxRetained: Int = 7
  ): DatabaseSnapshotInfo = withContext(Dispatchers.IO) {
    val dir = getSnapshotsDir(context)
    val now = System.currentTimeMillis()
    val dateTag = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date(now))
    val autoTag = if (isAutomated) "auto" else "manual"
    val isEncrypted = !passphrase.isNullOrBlank()
    val ext = if (isEncrypted) "bwise" else "json"
    val targetFile = File(dir, "snapshot_${dateTag}_${autoTag}.$ext")

    val (_, jsonString) = exportAllDataToJson(context, passphrase = null)

    if (isEncrypted) {
      val encryptedBytes = encryptPayload(jsonString, passphrase!!)
      targetFile.writeBytes(encryptedBytes)
    } else {
      targetFile.writeText(jsonString)
    }

    // Read summary count
    val summary = parseBackupJson(jsonString)

    // Enforce retention policy for automated snapshots
    enforceSnapshotRetention(dir, maxRetained)

    DatabaseSnapshotInfo(
      file = targetFile,
      fileName = targetFile.name,
      timestamp = now,
      formattedDate = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(now)),
      sizeBytes = targetFile.length(),
      formattedSize = formatBytes(targetFile.length()),
      recordCount = summary.totalRecords,
      isAutomated = isAutomated,
      isEncrypted = isEncrypted
    )
  }

  private fun enforceSnapshotRetention(dir: File, maxRetained: Int) {
    val autoFiles = dir.listFiles { f -> f.isFile && f.name.contains("_auto.") } ?: return
    if (autoFiles.size > maxRetained) {
      val sorted = autoFiles.sortedBy { it.lastModified() }
      val toDelete = sorted.take(autoFiles.size - maxRetained)
      toDelete.forEach { it.delete() }
    }
  }

  suspend fun listSnapshots(context: Context): List<DatabaseSnapshotInfo> = withContext(Dispatchers.IO) {
    val dir = getSnapshotsDir(context)
    val files = dir.listFiles { f -> f.isFile && (f.name.endsWith(".json") || f.name.endsWith(".bwise")) } ?: return@withContext emptyList()

    files.sortedByDescending { it.lastModified() }.map { f ->
      val isEncrypted = f.name.endsWith(".bwise") || f.name.contains("encrypted")
      val isAutomated = f.name.contains("_auto.")
      val recordCount = if (!isEncrypted) {
        try {
          val text = f.readText()
          val obj = JSONObject(text)
          val counts = obj.optJSONObject("counts")
          if (counts != null) {
            counts.optInt("expenses") + counts.optInt("accounts") + counts.optInt("stocks") +
                counts.optInt("budgets") + counts.optInt("goals") + counts.optInt("subscriptions") + counts.optInt("loans")
          } else {
            0
          }
        } catch (_: Exception) {
          0
        }
      } else {
        0
      }

      DatabaseSnapshotInfo(
        file = f,
        fileName = f.name,
        timestamp = f.lastModified(),
        formattedDate = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(f.lastModified())),
        sizeBytes = f.length(),
        formattedSize = formatBytes(f.length()),
        recordCount = recordCount,
        isAutomated = isAutomated,
        isEncrypted = isEncrypted
      )
    }
  }

  fun deleteSnapshot(snapshotFile: File): Boolean {
    return snapshotFile.exists() && snapshotFile.delete()
  }

  // ==========================================
  // 4. Parse & Restore Engine
  // ==========================================

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
    val sipsCount = obj.optJSONArray("sips")?.length() ?: 0
    val mandatesCount = obj.optJSONArray("mandates")?.length() ?: 0
    val shortcutsCount = obj.optJSONArray("shortcuts")?.length() ?: 0

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
      sipsCount = sipsCount,
      mandatesCount = mandatesCount,
      shortcutsCount = shortcutsCount,
      isEncrypted = false,
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
      db.mutualFundSipDao().clearAll()
      db.conditionalMandateDao().clearAll()
      db.transactionShortcutDao().clearAll()
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

    // 8. Mutual Fund SIPs
    var importedSips = 0
    val sipArray = root.optJSONArray("sips")
    if (sipArray != null) {
      val sipList = mutableListOf<MutualFundSipEntity>()
      for (i in 0 until sipArray.length()) {
        val sp = sipArray.getJSONObject(i)
        sipList.add(
          MutualFundSipEntity(
            id = if (mode == ImportMode.REPLACE) sp.optLong("id", 0L) else 0L,
            schemeCode = sp.optString("schemeCode", ""),
            schemeName = sp.optString("schemeName", sp.optString("fundName", "SIP Fund")),
            installmentAmount = sp.optDouble("installmentAmount", 1000.0),
            frequency = sp.optString("frequency", "Monthly"),
            debitAccount = sp.optString("debitAccount", sp.optString("accountName", "Main Checking")),
            sipDayOfMonth = sp.optInt("sipDayOfMonth", sp.optInt("dayOfMonth", 5)),
            nextExecutionDate = sp.optLong("nextExecutionDate", System.currentTimeMillis() + 86400000L * 30),
            isActive = sp.optBoolean("isActive", true),
            totalInvested = sp.optDouble("totalInvested", 0.0),
            installmentsCompleted = sp.optInt("installmentsCompleted", 0),
            notes = sp.optString("notes", "")
          )
        )
      }
      db.mutualFundSipDao().insertSips(sipList)
      importedSips = sipList.size
    }

    // 9. Conditional Mandates
    var importedMandates = 0
    val mandateArray = root.optJSONArray("mandates")
    if (mandateArray != null) {
      for (i in 0 until mandateArray.length()) {
        val m = mandateArray.getJSONObject(i)
        db.conditionalMandateDao().insertMandate(
          ConditionalMandateEntity(
            id = if (mode == ImportMode.REPLACE) m.optLong("id", 0L) else 0L,
            title = m.optString("title", "Mandate"),
            sourceAccount = m.optString("sourceAccount", ""),
            targetAccount = m.optString("targetAccount", ""),
            conditionType = m.optString("conditionType", "BALANCE_BELOW"),
            thresholdAmount = m.optDouble("thresholdAmount", 1000.0),
            transferAmount = m.optDouble("transferAmount", 500.0),
            isEnabled = m.optBoolean("isEnabled", true),
            lastTriggeredAt = m.optLong("lastTriggeredAt", m.optLong("lastTriggered", 0L)),
            totalTriggeredCount = m.optInt("totalTriggeredCount", 0),
            notes = m.optString("notes", m.optString("description", ""))
          )
        )
        importedMandates++
      }
    }

    // 10. Quick Shortcuts
    var importedShortcuts = 0
    val shortcutArray = root.optJSONArray("shortcuts")
    if (shortcutArray != null) {
      val shortcutList = mutableListOf<TransactionShortcutEntity>()
      for (i in 0 until shortcutArray.length()) {
        val sc = shortcutArray.getJSONObject(i)
        shortcutList.add(
          TransactionShortcutEntity(
            id = if (mode == ImportMode.REPLACE) sc.optLong("id", 0L) else 0L,
            title = sc.optString("title", sc.optString("label", "Shortcut")),
            amount = sc.optDouble("amount", 50.0),
            category = sc.optString("category", "FOOD"),
            type = sc.optString("type", "EXPENSE"),
            account = sc.optString("account", "Main Checking"),
            iconEmoji = sc.optString("iconEmoji", "⚡"),
            orderIndex = sc.optInt("orderIndex", i)
          )
        )
      }
      db.transactionShortcutDao().insertShortcuts(shortcutList)
      importedShortcuts = shortcutList.size
    }

    ImportResultStats(
      importedExpenses = importedExpenses,
      importedAccounts = importedAccounts,
      importedBudgets = importedBudgets,
      importedGoals = importedGoals,
      importedSubscriptions = importedSubscriptions,
      importedLoans = importedLoans,
      importedStocks = importedStocks,
      importedSips = importedSips,
      importedMandates = importedMandates,
      importedShortcuts = importedShortcuts,
      isReplaceMode = mode == ImportMode.REPLACE
    )
  }

  // ==========================================
  // 5. Database Diagnostics & Maintenance
  // ==========================================

  suspend fun getDatabaseHealth(context: Context): DatabaseHealthInfo = withContext(Dispatchers.IO) {
    val db = AppDatabase.getDatabase(context)
    val dbFile = context.getDatabasePath("expense_tracker.db")

    var totalBytes = if (dbFile.exists()) dbFile.length() else 0L
    val walFile = File(dbFile.parentFile, "expense_tracker.db-wal")
    if (walFile.exists()) totalBytes += walFile.length()
    val shmFile = File(dbFile.parentFile, "expense_tracker.db-shm")
    if (shmFile.exists()) totalBytes += shmFile.length()

    val expenses = db.expenseDao().getAllExpensesList().size
    val accounts = db.accountDao().getAllAccountsList().size
    val budgets = db.budgetDao().getAllBudgetsList().size
    val goals = db.savingsGoalDao().getAllGoalsList().size
    val subs = db.subscriptionDao().getAllSubscriptionsList().size
    val loans = db.loanDao().getAllLoansList().size
    val stocks = db.stockDao().getAllStocksList().size
    val sips = db.mutualFundSipDao().getAllSipsList().size
    val mandates = db.conditionalMandateDao().getAllMandatesList().size
    val shortcuts = db.transactionShortcutDao().getAllShortcutsList().size

    val tableCounts = mapOf(
      "Transactions & Expenses" to expenses,
      "Accounts & Wallets" to accounts,
      "Monthly Budgets" to budgets,
      "Savings Goals" to goals,
      "Active Subscriptions" to subs,
      "Debts & Loans" to loans,
      "Stock Holdings" to stocks,
      "Mutual Fund SIPs" to sips,
      "Auto Mandate Rules" to mandates,
      "Quick Shortcuts" to shortcuts
    )

    val totalRecords = expenses + accounts + budgets + goals + subs + loans + stocks + sips + mandates + shortcuts

    var integrityStatus = "OK"
    try {
      val cursor = db.openHelper.readableDatabase.query("PRAGMA integrity_check")
      if (cursor.moveToFirst()) {
        val result = cursor.getString(0)
        integrityStatus = if (result.equals("ok", ignoreCase = true)) "OK (Verified)" else result
      }
      cursor.close()
    } catch (e: Exception) {
      integrityStatus = "Check Failed: ${e.localizedMessage}"
    }

    val snapshots = listSnapshots(context)
    val lastSnapshot = snapshots.maxByOrNull { it.timestamp }?.timestamp

    DatabaseHealthInfo(
      dbName = "expense_tracker.db",
      dbSizeBytes = totalBytes,
      formattedSize = formatBytes(totalBytes),
      version = 9,
      tableCounts = tableCounts,
      totalRecords = totalRecords,
      integrityStatus = integrityStatus,
      lastSnapshotTime = lastSnapshot
    )
  }

  suspend fun optimizeDatabase(context: Context): String = withContext(Dispatchers.IO) {
    val db = AppDatabase.getDatabase(context)
    return@withContext try {
      db.openHelper.writableDatabase.execSQL("PRAGMA wal_checkpoint(FULL)")
      db.openHelper.writableDatabase.execSQL("VACUUM")
      "Database compacted and WAL checkpoint completed successfully."
    } catch (e: Exception) {
      "Optimization error: ${e.localizedMessage}"
    }
  }

  private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
    return String.format(Locale.US, "%.1f %s", bytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
  }
}
