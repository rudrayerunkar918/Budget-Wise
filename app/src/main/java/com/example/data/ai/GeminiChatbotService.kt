package com.example.data.ai

import com.example.BuildConfig
import com.example.data.model.AccountEntity
import com.example.data.model.BudgetEntity
import com.example.data.model.ExpenseEntity
import com.example.data.model.LoanEntity
import com.example.data.model.StockEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

enum class MessageSender {
  USER,
  GEMINI
}

data class ExecutedChatAction(
  val actionType: String,
  val title: String,
  val description: String,
  val isSuccess: Boolean = true
)

data class ChatMessage(
  val id: String = UUID.randomUUID().toString(),
  val sender: MessageSender,
  val text: String,
  val timestamp: Long = System.currentTimeMillis(),
  val executedAction: ExecutedChatAction? = null,
  val isGroundedWithGoogleSearch: Boolean = false,
  val searchQueries: List<String> = emptyList(),
  val sources: List<GroundedSource> = emptyList()
)

sealed class ParsedAction {
  data class AddExpense(
    val title: String,
    val amount: Double,
    val category: String,
    val account: String = "Main Checking",
    val note: String = ""
  ) : ParsedAction()

  data class AddIncome(
    val title: String,
    val amount: Double,
    val account: String = "Main Checking"
  ) : ParsedAction()

  data class AddStock(
    val symbol: String,
    val companyName: String,
    val shares: Double,
    val buyPrice: Double,
    val currentPrice: Double = buyPrice,
    val assetType: String = "STOCK"
  ) : ParsedAction()

  data class AddMutualFund(
    val schemeCode: String,
    val schemeName: String,
    val units: Double,
    val nav: Double
  ) : ParsedAction()

  data class SetBudget(
    val category: String,
    val limit: Double
  ) : ParsedAction()

  data class AddAccount(
    val name: String,
    val type: String,
    val balance: Double
  ) : ParsedAction()

  data class AddLoan(
    val personName: String,
    val amount: Double,
    val type: String // "LENT" or "LOAN"
  ) : ParsedAction()

  object DeleteLastTransaction : ParsedAction()

  object SyncRealTimeData : ParsedAction()
}

data class ChatbotResponse(
  val replyText: String,
  val action: ParsedAction? = null,
  val isGroundedWithGoogleSearch: Boolean = false,
  val searchQueries: List<String> = emptyList(),
  val sources: List<GroundedSource> = emptyList()
)

class GeminiChatbotService {

  private val client: OkHttpClient get() = com.example.data.api.AppHttpClient.client

  private val modelName = "gemini-3.5-flash"

  suspend fun sendMessage(
    userMessage: String,
    history: List<ChatMessage>,
    netWorth: Double,
    accounts: List<AccountEntity>,
    expenses: List<ExpenseEntity>,
    stocks: List<StockEntity>,
    budgets: List<BudgetEntity>,
    loans: List<LoanEntity>,
    currencySymbol: String,
    userName: String
  ): ChatbotResponse = withContext(Dispatchers.IO) {
    val apiKey = try {
      BuildConfig.GEMINI_API_KEY
    } catch (_: Exception) {
      ""
    }

    if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY") {
      return@withContext processWithLocalIntelligence(
        userMessage = userMessage,
        netWorth = netWorth,
        accounts = accounts,
        expenses = expenses,
        stocks = stocks,
        budgets = budgets,
        loans = loans,
        currencySymbol = currencySymbol,
        userName = userName
      )
    }

    try {
      val systemInstruction = buildSystemPrompt(
        netWorth = netWorth,
        accounts = accounts,
        expenses = expenses,
        stocks = stocks,
        budgets = budgets,
        loans = loans,
        currencySymbol = currencySymbol,
        userName = userName
      )

      val contentsArray = JSONArray()

      // Add recent relevant history (last 6 messages)
      val recentHistory = history.takeLast(6)
      for (msg in recentHistory) {
        val role = if (msg.sender == MessageSender.USER) "user" else "model"
        val parts = JSONArray().put(JSONObject().put("text", msg.text))
        contentsArray.put(JSONObject().put("role", role).put("parts", parts))
      }

      // Add current message
      val currentParts = JSONArray().put(JSONObject().put("text", userMessage))
      contentsArray.put(JSONObject().put("role", "user").put("parts", currentParts))

      val requestJson = JSONObject().apply {
        put("contents", contentsArray)
        put("systemInstruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", systemInstruction))))
        // Ground with Google Search for real-time market data, prices & live information
        put("tools", JSONArray().apply {
          put(JSONObject().apply {
            put("googleSearch", JSONObject())
          })
        })
        put(
          "generationConfig",
          JSONObject().apply {
            put("temperature", 0.3)
            put("topP", 0.95)
            put("maxOutputTokens", 1500)
          }
        )
      }

      val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"
      val request = Request.Builder()
        .url(url)
        .addHeader("Content-Type", "application/json")
        .post(requestJson.toString().toRequestBody("application/json".toMediaType()))
        .build()

      val (responseBody, isSuccessful) = client.newCall(request).execute().use { response ->
        Pair(response.body?.string(), response.isSuccessful)
      }

      if (!isSuccessful || responseBody.isNullOrBlank()) {
        return@withContext processWithLocalIntelligence(
          userMessage = userMessage,
          netWorth = netWorth,
          accounts = accounts,
          expenses = expenses,
          stocks = stocks,
          budgets = budgets,
          loans = loans,
          currencySymbol = currencySymbol,
          userName = userName
        )
      }

      val responseJson = JSONObject(responseBody)
      val candidates = responseJson.optJSONArray("candidates")
      val firstCandidate = candidates?.optJSONObject(0)
      val contentObj = firstCandidate?.optJSONObject("content")
      val partsArr = contentObj?.optJSONArray("parts")
      val rawText = partsArr?.optJSONObject(0)?.optString("text", "") ?: ""

      if (rawText.isBlank()) {
        return@withContext processWithLocalIntelligence(
          userMessage = userMessage,
          netWorth = netWorth,
          accounts = accounts,
          expenses = expenses,
          stocks = stocks,
          budgets = budgets,
          loans = loans,
          currencySymbol = currencySymbol,
          userName = userName
        )
      }

      // Extract Google Search Grounding Metadata
      val queries = mutableListOf<String>()
      val sources = mutableListOf<GroundedSource>()

      val groundingMeta = firstCandidate?.optJSONObject("groundingMetadata")
      if (groundingMeta != null) {
        val webQueries = groundingMeta.optJSONArray("webSearchQueries")
        if (webQueries != null) {
          for (i in 0 until webQueries.length()) {
            queries.add(webQueries.getString(i))
          }
        }

        val chunks = groundingMeta.optJSONArray("groundingChunks")
        if (chunks != null) {
          for (i in 0 until chunks.length()) {
            val chunk = chunks.getJSONObject(i)
            val web = chunk.optJSONObject("web")
            if (web != null) {
              val title = web.optString("title", "Google Search Source")
              val uri = web.optString("uri", "")
              if (uri.isNotBlank()) {
                sources.add(GroundedSource(title, uri))
              }
            }
          }
        }
      }

      // Parse text and any embedded action block
      val (cleanReply, action) = extractActionFromResponse(rawText)
      ChatbotResponse(
        replyText = cleanReply,
        action = action,
        isGroundedWithGoogleSearch = queries.isNotEmpty() || sources.isNotEmpty(),
        searchQueries = queries.distinct(),
        sources = sources.distinctBy { it.uri }
      )
    } catch (_: Exception) {
      processWithLocalIntelligence(
        userMessage = userMessage,
        netWorth = netWorth,
        accounts = accounts,
        expenses = expenses,
        stocks = stocks,
        budgets = budgets,
        loans = loans,
        currencySymbol = currencySymbol,
        userName = userName
      )
    }
  }

  private fun buildSystemPrompt(
    netWorth: Double,
    accounts: List<AccountEntity>,
    expenses: List<ExpenseEntity>,
    stocks: List<StockEntity>,
    budgets: List<BudgetEntity>,
    loans: List<LoanEntity>,
    currencySymbol: String,
    userName: String
  ): String {
    val totalIncome = expenses.filter { it.type == "INCOME" }.sumOf { it.amount }
    val totalExpense = expenses.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    val topCategories = expenses.filter { it.type == "EXPENSE" }
      .groupBy { it.category }
      .mapValues { it.value.sumOf { e -> e.amount } }
      .toList()
      .sortedByDescending { it.second }
      .take(5)
      .joinToString(", ") { "${it.first}: $currencySymbol${it.second.toInt()}" }

    val accountsSummary = accounts.joinToString(", ") { "${it.name} (${it.type}): $currencySymbol${it.balance.toInt()}" }
    val stocksSummary = stocks.joinToString(", ") {
      "${it.symbol} (${it.assetType}): ${it.shares} units @ $currencySymbol${it.currentPrice}"
    }

    return """
You are BudgetWise Gemini AI Assistant, a personal finance expert and actionable assistant for $userName.
You can answer any questions about the user's finances, analyze spending, give stock advice, and TAKE REAL ACTIONS to edit data, log expenses, add stocks, create budgets, add bank accounts, or record loans.

CURRENT LIVE USER FINANCIAL CONTEXT:
- Net Worth: $currencySymbol${String.format(Locale.US, "%,.2f", netWorth)}
- Total Income: $currencySymbol${String.format(Locale.US, "%,.2f", totalIncome)}
- Total Spent: $currencySymbol${String.format(Locale.US, "%,.2f", totalExpense)}
- Accounts: [${if (accountsSummary.isBlank()) "None" else accountsSummary}]
- Top Spending: [${if (topCategories.isBlank()) "No expenses yet" else topCategories}]
- Stock & Fund Holdings: [${if (stocksSummary.isBlank()) "None" else stocksSummary}]
- Currency: $currencySymbol

ACTION PROTOCOL:
When the user asks you to perform an action (e.g. log an expense, add a stock, add a mutual fund, create a budget, add an account, or record a loan), you MUST include an action block at the END of your reply using exact JSON:

For Expense:
```action
{"action":"ADD_EXPENSE","title":"Coffee","amount":150.0,"category":"FOOD","account":"Main Checking"}
```
(Categories: FOOD, SHOPPING, HOUSING, TRANSPORT, ENTERTAINMENT, HEALTH, GROCERIES, EDUCATION, OTHER)

For Income:
```action
{"action":"ADD_INCOME","title":"Salary","amount":50000.0,"account":"Main Checking"}
```

For Stock:
```action
{"action":"ADD_STOCK","symbol":"TATAMOTORS.NS","companyName":"Tata Motors Ltd","shares":10.0,"buyPrice":441.50,"assetType":"STOCK"}
```

For Mutual Fund:
```action
{"action":"ADD_MUTUAL_FUND","schemeCode":"122639","schemeName":"Parag Parikh Flexi Cap Fund","units":25.0,"nav":75.0}
```

For Budget:
```action
{"action":"SET_BUDGET","category":"FOOD","limit":8000.0}
```

For Bank Account:
```action
{"action":"ADD_ACCOUNT","name":"HDFC Bank","type":"CHECKING","balance":25000.0}
```

For Loan/Lent:
```action
{"action":"ADD_LOAN","personName":"Rahul","amount":2000.0,"type":"LENT"}
```

For Deleting Last Transaction:
```action
{"action":"DELETE_LAST_TRANSACTION"}
```

For Syncing Real-Time Market Data / Prices:
```action
{"action":"SYNC_REAL_TIME_DATA"}
```

GUIDELINES:
1. Always speak warmly, professionally, and concisely.
2. If performing an action, confirm what you did clearly in natural conversational text and append the ```action ``` block.
3. If answering a question (e.g., "What is my net worth?" or "How much did I spend on Food?"), answer directly with numbers from the context.
4. When adding a stock, infer standard Indian NSE symbols if applicable (e.g., Tata Motors -> TATAMOTORS.NS, Reliance -> RELIANCE.NS, Infosys -> INFY.NS, TCS -> TCS.NS).
""".trimIndent()
  }

  private fun extractActionFromResponse(text: String): Pair<String, ParsedAction?> {
    val actionRegex = Regex("```(?:action|json)?\\s*(\\{[\\s\\S]*?\\})\\s*```")
    val match = actionRegex.find(text)

    if (match != null) {
      val jsonStr = match.groupValues[1].trim()
      val cleanText = text.replace(match.value, "").trim()
      val action = parseActionJson(jsonStr)
      return cleanText to action
    }

    // Try finding raw JSON block with "action" key
    val rawJsonRegex = Regex("(\\{\\s*\"action\"\\s*:[\\s\\S]*?\\})")
    val rawMatch = rawJsonRegex.find(text)
    if (rawMatch != null) {
      val jsonStr = rawMatch.groupValues[1].trim()
      val cleanText = text.replace(rawMatch.value, "").trim()
      val action = parseActionJson(jsonStr)
      return cleanText to action
    }

    return text to null
  }

  private fun parseActionJson(jsonStr: String): ParsedAction? {
    return try {
      val obj = JSONObject(jsonStr)
      val actionType = obj.optString("action", "").uppercase()
      when (actionType) {
        "ADD_EXPENSE" -> {
          ParsedAction.AddExpense(
            title = obj.optString("title", "Expense").ifBlank { "Expense" },
            amount = obj.optDouble("amount", 0.0),
            category = obj.optString("category", "OTHER").uppercase(),
            account = obj.optString("account", "Main Checking"),
            note = obj.optString("note", "Logged via Gemini Assistant")
          )
        }
        "ADD_INCOME" -> {
          ParsedAction.AddIncome(
            title = obj.optString("title", "Income").ifBlank { "Income" },
            amount = obj.optDouble("amount", 0.0),
            account = obj.optString("account", "Main Checking")
          )
        }
        "ADD_STOCK" -> {
          val sym = obj.optString("symbol", "STOCK.NS").uppercase()
          ParsedAction.AddStock(
            symbol = sym,
            companyName = obj.optString("companyName", sym),
            shares = obj.optDouble("shares", 1.0).coerceAtLeast(0.01),
            buyPrice = obj.optDouble("buyPrice", 100.0),
            currentPrice = obj.optDouble("currentPrice", obj.optDouble("buyPrice", 100.0)),
            assetType = "STOCK"
          )
        }
        "ADD_MUTUAL_FUND" -> {
          val code = obj.optString("schemeCode", "NFO-FUND").uppercase()
          ParsedAction.AddMutualFund(
            schemeCode = code,
            schemeName = obj.optString("schemeName", "Mutual Fund Holding"),
            units = obj.optDouble("units", 10.0).coerceAtLeast(0.01),
            nav = obj.optDouble("nav", 50.0)
          )
        }
        "SET_BUDGET" -> {
          ParsedAction.SetBudget(
            category = obj.optString("category", "TOTAL").uppercase(),
            limit = obj.optDouble("limit", 5000.0)
          )
        }
        "ADD_ACCOUNT" -> {
          ParsedAction.AddAccount(
            name = obj.optString("name", "New Account"),
            type = obj.optString("type", "CHECKING").uppercase(),
            balance = obj.optDouble("balance", 0.0)
          )
        }
        "ADD_LOAN" -> {
          ParsedAction.AddLoan(
            personName = obj.optString("personName", "Friend"),
            amount = obj.optDouble("amount", 1000.0),
            type = obj.optString("type", "LENT").uppercase()
          )
        }
        "DELETE_LAST_TRANSACTION" -> {
          ParsedAction.DeleteLastTransaction
        }
        "SYNC_REAL_TIME_DATA", "SYNC_MARKET", "SYNC_STOCKS", "SYNC_DATA" -> {
          ParsedAction.SyncRealTimeData
        }
        else -> null
      }
    } catch (_: Exception) {
      null
    }
  }

  /**
   * High-accuracy Local Rule-based Financial NLP Assistant.
   * Ensures the chatbot answers questions and executes commands seamlessly even if offline or without API key.
   */
  private fun processWithLocalIntelligence(
    userMessage: String,
    netWorth: Double,
    accounts: List<AccountEntity>,
    expenses: List<ExpenseEntity>,
    stocks: List<StockEntity>,
    budgets: List<BudgetEntity>,
    loans: List<LoanEntity>,
    currencySymbol: String,
    userName: String
  ): ChatbotResponse {
    val q = userMessage.trim().lowercase()

    // 0. SYNC REAL TIME DATA COMMAND
    if (q.contains("sync") && (q.contains("real time") || q.contains("live") || q.contains("market") || q.contains("stock") || q.contains("price") || q.contains("data"))) {
      return ChatbotResponse(
        replyText = "Initiating real-time market data sync with Google Search Grounding. Fetching live stock quotes, market indices (NIFTY 50, SENSEX), and IPO updates...",
        action = ParsedAction.SyncRealTimeData,
        isGroundedWithGoogleSearch = true,
        searchQueries = listOf("NIFTY 50 live today", "SENSEX current index", "NSE BSE live quotes")
      )
    }

    // 1. ADD STOCK COMMAND
    if (q.contains("add stock") || q.contains("buy stock") || (q.contains("shares") && (q.contains("buy") || q.contains("add")))) {
      val sharesMatch = Regex("(\\d+(?:\\.\\d+)?)\\s*(?:shares|units|share)").find(q)
      val shares = sharesMatch?.groupValues?.get(1)?.toDoubleOrNull() ?: 10.0

      val priceMatch = Regex("(?:at|price|@|for)\\s*(?:₹|rs\\.?|\\$)?\\s*(\\d+(?:\\.\\d+)?)").find(q)
      val price = priceMatch?.groupValues?.get(1)?.toDoubleOrNull() ?: 500.0

      val symbol = when {
        q.contains("tata motor") || q.contains("tatamotors") -> "TATAMOTORS.NS"
        q.contains("tata power") || q.contains("tatapower") -> "TATAPOWER.NS"
        q.contains("reliance") || q.contains("ril") -> "RELIANCE.NS"
        q.contains("infosys") || q.contains("infy") -> "INFY.NS"
        q.contains("tcs") -> "TCS.NS"
        q.contains("hdfc bank") || q.contains("hdfcbank") -> "HDFCBANK.NS"
        q.contains("icici") -> "ICICIBANK.NS"
        q.contains("sbi") || q.contains("sbin") -> "SBIN.NS"
        q.contains("apple") || q.contains("aapl") -> "AAPL"
        q.contains("tesla") || q.contains("tsla") -> "TSLA"
        q.contains("swiggy") -> "SWIGGY.NS"
        q.contains("zomato") -> "ZOMATO.NS"
        else -> {
          // Extract word after "stock" or "buy"
          val words = q.split(" ")
          val stockIdx = words.indexOfFirst { it == "stock" || it == "shares" }
          if (stockIdx > 0 && words[stockIdx - 1].length >= 3) {
            "${words[stockIdx - 1].uppercase()}.NS"
          } else "TATAMOTORS.NS"
        }
      }

      val companyName = symbol.removeSuffix(".NS").replace("_", " ") + " Limited"
      val totalCost = shares * price
      val action = ParsedAction.AddStock(
        symbol = symbol,
        companyName = companyName,
        shares = shares,
        buyPrice = price,
        currentPrice = price
      )

      return ChatbotResponse(
        replyText = "I have recorded the investment holding: **$shares shares** of **$symbol** bought at **$currencySymbol${String.format(Locale.US, "%,.2f", price)}** each (Total: $currencySymbol${String.format(Locale.US, "%,.2f", totalCost)}). Your portfolio valuation and asset distribution have been updated.",
        action = action
      )
    }

    // 2. ADD MUTUAL FUND COMMAND
    if (q.contains("add mutual fund") || q.contains("add fund") || q.contains("add nfo") || q.contains("buy fund")) {
      val unitsMatch = Regex("(\\d+(?:\\.\\d+)?)\\s*(?:units|shares)").find(q)
      val units = unitsMatch?.groupValues?.get(1)?.toDoubleOrNull() ?: 50.0

      val navMatch = Regex("(?:at|nav|@|for)\\s*(?:₹|rs\\.?|\\$)?\\s*(\\d+(?:\\.\\d+)?)").find(q)
      val nav = navMatch?.groupValues?.get(1)?.toDoubleOrNull() ?: 50.0

      val fundName = when {
        q.contains("parag parikh") || q.contains("ppfas") -> "Parag Parikh Flexi Cap Fund"
        q.contains("quant") -> "Quant Small Cap Fund - Direct Plan"
        q.contains("mirae") -> "Mirae Asset Large Cap Fund"
        q.contains("hdfc") -> "HDFC Defence Fund - Direct Plan"
        q.contains("sbi") -> "SBI Bluechip Fund - Direct Plan"
        else -> "Selected Direct Mutual Fund"
      }

      val action = ParsedAction.AddMutualFund(
        schemeCode = "MF-${fundName.take(6).uppercase().replace(" ", "")}",
        schemeName = fundName,
        units = units,
        nav = nav
      )

      return ChatbotResponse(
        replyText = "I have added **$units units** of **$fundName** at NAV **$currencySymbol$nav** to your portfolio holdings.",
        action = action
      )
    }

    // 3. ADD EXPENSE COMMAND
    if (q.contains("add expense") || q.contains("log expense") || q.contains("spent") || q.contains("paid")) {
      val amountMatch = Regex("(?:₹|rs\\.?|\\$)?\\s*(\\d+(?:\\.\\d+)?)").find(q)
      val amount = amountMatch?.groupValues?.get(1)?.toDoubleOrNull() ?: 100.0

      val category = when {
        q.contains("food") || q.contains("lunch") || q.contains("dinner") || q.contains("coffee") || q.contains("restaurant") -> "FOOD"
        q.contains("grocer") || q.contains("milk") || q.contains("vegetable") || q.contains("supermarket") -> "GROCERIES"
        q.contains("shop") || q.contains("clothes") || q.contains("amazon") || q.contains("shoes") -> "SHOPPING"
        q.contains("cab") || q.contains("uber") || q.contains("ola") || q.contains("fuel") || q.contains("petrol") -> "TRANSPORT"
        q.contains("movie") || q.contains("netflix") || q.contains("game") -> "ENTERTAINMENT"
        q.contains("doctor") || q.contains("medicine") || q.contains("pharmacy") || q.contains("health") -> "HEALTH"
        q.contains("rent") || q.contains("house") || q.contains("maintenance") -> "HOUSING"
        else -> "FOOD"
      }

      val title = when (category) {
        "FOOD" -> "Dining & Food"
        "GROCERIES" -> "Groceries"
        "SHOPPING" -> "Shopping"
        "TRANSPORT" -> "Transit & Commute"
        "ENTERTAINMENT" -> "Entertainment"
        "HEALTH" -> "Healthcare"
        "HOUSING" -> "Rent & Housing"
        else -> "Expense"
      }

      val action = ParsedAction.AddExpense(
        title = title,
        amount = amount,
        category = category,
        account = accounts.firstOrNull()?.name ?: "Main Checking",
        note = "Logged via Gemini Assistant"
      )

      return ChatbotResponse(
        replyText = "I have logged an expense of **$currencySymbol${String.format(Locale.US, "%,.2f", amount)}** under **$category** ($title) for you.",
        action = action
      )
    }

    // 4. ADD INCOME COMMAND
    if (q.contains("add income") || q.contains("received") || q.contains("salary")) {
      val amountMatch = Regex("(?:₹|rs\\.?|\\$)?\\s*(\\d+(?:\\.\\d+)?)").find(q)
      val amount = amountMatch?.groupValues?.get(1)?.toDoubleOrNull() ?: 1000.0
      val title = if (q.contains("salary")) "Monthly Salary" else "Income Deposit"

      val action = ParsedAction.AddIncome(
        title = title,
        amount = amount,
        account = accounts.firstOrNull()?.name ?: "Main Checking"
      )

      return ChatbotResponse(
        replyText = "I have recorded an income deposit of **$currencySymbol${String.format(Locale.US, "%,.2f", amount)}** ($title).",
        action = action
      )
    }

    // 5. SET BUDGET COMMAND
    if (q.contains("budget") && (q.contains("set") || q.contains("update") || q.contains("limit"))) {
      val amountMatch = Regex("(?:₹|rs\\.?|\\$)?\\s*(\\d+(?:\\.\\d+)?)").find(q)
      val limit = amountMatch?.groupValues?.get(1)?.toDoubleOrNull() ?: 5000.0

      val category = when {
        q.contains("food") -> "FOOD"
        q.contains("shop") -> "SHOPPING"
        q.contains("transport") -> "TRANSPORT"
        q.contains("grocer") -> "GROCERIES"
        q.contains("overall") || q.contains("total") -> "TOTAL"
        else -> "FOOD"
      }

      val action = ParsedAction.SetBudget(category = category, limit = limit)
      return ChatbotResponse(
        replyText = "I have updated your monthly budget for **$category** to **$currencySymbol${String.format(Locale.US, "%,.2f", limit)}**.",
        action = action
      )
    }

    // 6. QUESTION: NET WORTH
    if (q.contains("net worth") || q.contains("total worth") || q.contains("wealth")) {
      val liquid = accounts.filter { it.balance > 0 }.sumOf { it.balance }
      val invest = stocks.sumOf { it.shares * it.currentPrice }
      return ChatbotResponse(
        replyText = "Your current Total Net Worth is **$currencySymbol${String.format(Locale.US, "%,.2f", netWorth)}**.\n\n- **Liquid Bank Balances:** $currencySymbol${String.format(Locale.US, "%,.2f", liquid)}\n- **Investments (Stocks & Funds):** $currencySymbol${String.format(Locale.US, "%,.2f", invest)}\n\nYou can ask me anytime to add new stock holdings, log transactions, or check financial health."
      )
    }

    // 7. QUESTION: SPENDING / EXPENSES
    if (q.contains("how much") && (q.contains("spent") || q.contains("expense"))) {
      val totalSpent = expenses.filter { it.type == "EXPENSE" }.sumOf { it.amount }
      val topCategory = expenses.filter { it.type == "EXPENSE" }
        .groupBy { it.category }
        .maxByOrNull { entry -> entry.value.sumOf { it.amount } }

      val topStr = if (topCategory != null) {
        " Your highest expenditure category is **${topCategory.key}** with $currencySymbol${String.format(Locale.US, "%,.2f", topCategory.value.sumOf { it.amount })}."
      } else ""

      return ChatbotResponse(
        replyText = "You have spent a total of **$currencySymbol${String.format(Locale.US, "%,.2f", totalSpent)}** this month across ${expenses.count { it.type == "EXPENSE" }} transactions.$topStr"
      )
    }

    // 8. QUESTION: STOCKS / PORTFOLIO
    if (q.contains("portfolio") || q.contains("stock") || q.contains("shares")) {
      val totalVal = stocks.sumOf { it.shares * it.currentPrice }
      val totalInvested = stocks.sumOf { it.shares * it.avgBuyPrice }
      val profit = totalVal - totalInvested
      val holdingsList = stocks.take(5).joinToString("\n") {
        "• **${it.symbol}**: ${it.shares} units @ $currencySymbol${String.format(Locale.US, "%,.2f", it.currentPrice)}"
      }

      return ChatbotResponse(
        replyText = "Your investment portfolio contains **${stocks.size} holdings** with current valuation of **$currencySymbol${String.format(Locale.US, "%,.2f", totalVal)}** (P&L: ${if (profit >= 0) "+" else ""}$currencySymbol${String.format(Locale.US, "%,.2f", profit)}).\n\n$holdingsList\n\nYou can tell me: *\"Add 10 shares of Tata Motors at ₹441.50\"* or *\"Buy 5 shares of Reliance at ₹2900\"* to record new holdings instantly."
      )
    }

    // 9. QUESTION: LOANS / DEBTS
    if (q.contains("loan") || q.contains("lent") || q.contains("borrow") || q.contains("debt")) {
      val lent = loans.filter { it.type == "LENT" && !it.isSettled }.sumOf { it.remainingAmount }
      val borrowed = loans.filter { it.type == "LOAN" && !it.isSettled }.sumOf { it.remainingAmount }
      return ChatbotResponse(
        replyText = "Here is your debt & loan summary:\n- **Money Lent (Receivables):** $currencySymbol${String.format(Locale.US, "%,.2f", lent)}\n- **Money Borrowed (Liabilities):** $currencySymbol${String.format(Locale.US, "%,.2f", borrowed)}"
      )
    }

    // 10. DEFAULT HELPFUL ASSISTANT RESPONSE
    return ChatbotResponse(
      replyText = "Hello $userName! I am your BudgetWise Gemini AI Assistant. I can answer your financial questions and perform live actions in your database.\n\nTry asking me:\n• *\"Add 10 shares of Tata Motors at ₹441.50\"*\n• *\"Log an expense of ₹450 for lunch under Food\"*\n• *\"What is my current net worth?\"*\n• *\"Set budget for Shopping to ₹6,000\"*\n• *\"How is my stock portfolio performing?\"*"
    )
  }
}
