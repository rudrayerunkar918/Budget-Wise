package com.example.data.ai

import com.example.BuildConfig
import com.example.data.model.BudgetEntity
import com.example.data.model.ExpenseEntity
import com.example.data.model.LoanEntity
import com.example.data.model.SavingsGoalEntity
import com.example.data.model.SubscriptionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class GeminiInsight(
  val title: String,
  val category: String,
  val description: String,
  val potentialSavings: String? = null,
  val actionTag: String? = null
)

data class SpendingAnalysisResult(
  val summaryHeadline: String,
  val overallHealthRating: String, // e.g. "Good", "Moderate", "Needs Attention"
  val insights: List<GeminiInsight>,
  val topSpendingAlert: String,
  val timestamp: Long = System.currentTimeMillis(),
  val isFallback: Boolean = false,
  val rawResponse: String = ""
)

class GeminiSpendingAnalyzer {

  private val client: OkHttpClient get() = com.example.data.api.AppHttpClient.client

  private val modelName = "gemini-3.5-flash"

  suspend fun analyzeSpendingHabits(
    expenses: List<ExpenseEntity>,
    subscriptions: List<SubscriptionEntity>,
    budgets: List<BudgetEntity>,
    goals: List<SavingsGoalEntity>,
    loans: List<LoanEntity>,
    currencySymbol: String
  ): SpendingAnalysisResult = withContext(Dispatchers.IO) {
    val apiKey = try {
      BuildConfig.GEMINI_API_KEY
    } catch (_: Exception) {
      ""
    }

    // Build comprehensive spending context
    val totalExpense = expenses.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    val totalIncome = expenses.filter { it.type == "INCOME" }.sumOf { it.amount }
    val categoryTotals = expenses.filter { it.type == "EXPENSE" }
      .groupBy { it.category }
      .mapValues { entry -> entry.value.sumOf { it.amount } }
      .toList()
      .sortedByDescending { it.second }

    val monthlySubscriptionsTotal = subscriptions.sumOf { it.amount }
    val activeLoansBorrowed = loans.filter { it.type == "LOAN" && !it.isSettled }.sumOf { it.remainingAmount }
    val activeLoansLent = loans.filter { it.type == "LENT" && !it.isSettled }.sumOf { it.remainingAmount }

    if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY") {
      return@withContext generateLocalRuleBasedInsights(
        totalExpense = totalExpense,
        totalIncome = totalIncome,
        categoryTotals = categoryTotals,
        monthlySubscriptions = monthlySubscriptionsTotal,
        goals = goals,
        currencySymbol = currencySymbol,
        reason = "API Key not configured in Secrets"
      )
    }

    val prompt = buildPrompt(
      totalExpense = totalExpense,
      totalIncome = totalIncome,
      categoryTotals = categoryTotals,
      subscriptions = subscriptions,
      budgets = budgets,
      goals = goals,
      loansBorrowed = activeLoansBorrowed,
      loansLent = activeLoansLent,
      currencySymbol = currencySymbol
    )

    try {
      val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"

      val requestJson = JSONObject().apply {
        put("contents", JSONArray().apply {
          put(JSONObject().apply {
            put("parts", JSONArray().apply {
              put(JSONObject().apply {
                put("text", prompt)
              })
            })
          })
        })
        put("generationConfig", JSONObject().apply {
          put("temperature", 0.3)
          put("topP", 0.9)
          put("topK", 40)
        })
      }

      val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
      val httpRequest = Request.Builder()
        .url(url)
        .post(requestBody)
        .build()

      val (responseString, isSuccessful, responseCode) = client.newCall(httpRequest).execute().use { response ->
        Triple(response.body?.string() ?: "", response.isSuccessful, response.code)
      }

      if (!isSuccessful) {
        return@withContext generateLocalRuleBasedInsights(
          totalExpense = totalExpense,
          totalIncome = totalIncome,
          categoryTotals = categoryTotals,
          monthlySubscriptions = monthlySubscriptionsTotal,
          goals = goals,
          currencySymbol = currencySymbol,
          reason = "Gemini API HTTP $responseCode: using offline analyzer"
        )
      }

      val parsed = parseGeminiResponse(responseString, currencySymbol)
      if (parsed != null && parsed.insights.isNotEmpty()) {
        parsed
      } else {
        generateLocalRuleBasedInsights(
          totalExpense = totalExpense,
          totalIncome = totalIncome,
          categoryTotals = categoryTotals,
          monthlySubscriptions = monthlySubscriptionsTotal,
          goals = goals,
          currencySymbol = currencySymbol,
          reason = "Parsing fallback"
        )
      }
    } catch (e: Exception) {
      generateLocalRuleBasedInsights(
        totalExpense = totalExpense,
        totalIncome = totalIncome,
        categoryTotals = categoryTotals,
        monthlySubscriptions = monthlySubscriptionsTotal,
        goals = goals,
        currencySymbol = currencySymbol,
        reason = "Network/Quota fallback: ${e.localizedMessage ?: "Connection error"}"
      )
    }
  }

  private fun buildPrompt(
    totalExpense: Double,
    totalIncome: Double,
    categoryTotals: List<Pair<String, Double>>,
    subscriptions: List<SubscriptionEntity>,
    budgets: List<BudgetEntity>,
    goals: List<SavingsGoalEntity>,
    loansBorrowed: Double,
    loansLent: Double,
    currencySymbol: String
  ): String {
    val categoryListStr = categoryTotals.joinToString("\n") { (cat, amount) ->
      "- $cat: $currencySymbol${String.format("%.2f", amount)}"
    }
    val subsListStr = subscriptions.joinToString("\n") { sub ->
      "- ${sub.title}: $currencySymbol${String.format("%.2f", sub.amount)} / ${sub.billingCycle}"
    }
    val goalsListStr = goals.joinToString("\n") { g ->
      "- ${g.title}: $currencySymbol${String.format("%.2f", g.currentAmount)} of $currencySymbol${String.format("%.2f", g.targetAmount)} (${g.progressPercent}%)"
    }

    return """
You are a friendly, expert personal financial advisor and spending analyst.
Analyze the user's spending data and provide high-value, practical money-saving tips and insights.

DATA:
- Currency: $currencySymbol
- Total Monthly Income: $currencySymbol${String.format("%.2f", totalIncome)}
- Total Monthly Expenses: $currencySymbol${String.format("%.2f", totalExpense)}
- Active Debts/Loans: Borrowed = $currencySymbol${String.format("%.2f", loansBorrowed)}, Lent Out = $currencySymbol${String.format("%.2f", loansLent)}

EXPENSE BY CATEGORIES:
$categoryListStr

ACTIVE RECURRING SUBSCRIPTIONS:
$subsListStr

SAVINGS GOALS:
$goalsListStr

OUTPUT INSTRUCTIONS:
Return a valid JSON object strictly matching this schema (do NOT wrap with markdown ticks if possible, or use standard ```json block):
{
  "summaryHeadline": "Concise 1-sentence assessment of their spending and saving balance",
  "overallHealthRating": "Excellent" | "Good" | "Moderate" | "Needs Attention",
  "topSpendingAlert": "1-sentence highlight of their biggest spending area or leak",
  "insights": [
    {
      "title": "Short punchy insight title",
      "category": "Food" | "Subscriptions" | "Shopping" | "Bills" | "Savings" | "General",
      "description": "Specific, actionable tip tailored directly to their numbers",
      "potentialSavings": "Estimated savings per month, e.g. '${currencySymbol}50/mo' or null",
      "actionTag": "Quick action, e.g. 'Audit Subs' or 'Meal Prep' or 'Set Cap'"
    }
  ]
}
Provide at least 3 to 5 realistic, high-impact tips. Ensure calculations and numbers reference their real amounts.
    """.trimIndent()
  }

  private fun parseGeminiResponse(rawJson: String, currencySymbol: String): SpendingAnalysisResult? {
    try {
      val root = JSONObject(rawJson)
      val candidates = root.optJSONArray("candidates") ?: return null
      val firstCandidate = candidates.optJSONObject(0) ?: return null
      val content = firstCandidate.optJSONObject("content") ?: return null
      val parts = content.optJSONArray("parts") ?: return null
      val text = parts.optJSONObject(0)?.optString("text") ?: return null

      // Clean up markdown code fences if present
      val cleanJson = text
        .trim()
        .removePrefix("```json")
        .removePrefix("```")
        .removeSuffix("```")
        .trim()

      val jsonObject = JSONObject(cleanJson)
      val headline = jsonObject.optString("summaryHeadline", "Spending Analysis Complete")
      val health = jsonObject.optString("overallHealthRating", "Moderate")
      val alert = jsonObject.optString("topSpendingAlert", "Track daily expenses closely to avoid budget overruns.")

      val insightsArray = jsonObject.optJSONArray("insights") ?: JSONArray()
      val insightList = mutableListOf<GeminiInsight>()

      for (i in 0 until insightsArray.length()) {
        val obj = insightsArray.optJSONObject(i) ?: continue
        insightList.add(
          GeminiInsight(
            title = obj.optString("title", "Smart Tip"),
            category = obj.optString("category", "General"),
            description = obj.optString("description", ""),
            potentialSavings = obj.optString("potentialSavings").takeIf { it.isNotBlank() },
            actionTag = obj.optString("actionTag").takeIf { it.isNotBlank() }
          )
        )
      }

      return SpendingAnalysisResult(
        summaryHeadline = headline,
        overallHealthRating = health,
        insights = insightList,
        topSpendingAlert = alert,
        isFallback = false,
        rawResponse = text
      )
    } catch (_: Exception) {
      return null
    }
  }

  private fun generateLocalRuleBasedInsights(
    totalExpense: Double,
    totalIncome: Double,
    categoryTotals: List<Pair<String, Double>>,
    monthlySubscriptions: Double,
    goals: List<SavingsGoalEntity>,
    currencySymbol: String,
    reason: String
  ): SpendingAnalysisResult {
    val insights = mutableListOf<GeminiInsight>()

    val topCat = categoryTotals.firstOrNull()
    if (topCat != null && totalExpense > 0) {
      val topCatPct = ((topCat.second / totalExpense) * 100).toInt()
      insights.add(
        GeminiInsight(
          title = "High Spending on ${topCat.first}",
          category = topCat.first,
          description = "${topCat.first} accounts for $topCatPct% of total spending ($currencySymbol${String.format("%.2f", topCat.second)}). Trimming 15% here would yield significant monthly savings.",
          potentialSavings = "$currencySymbol${String.format("%.0f", topCat.second * 0.15)}/mo",
          actionTag = "Set Category Budget"
        )
      )
    }

    if (monthlySubscriptions > 0) {
      insights.add(
        GeminiInsight(
          title = "Recurring Subscriptions Audit",
          category = "Subscriptions",
          description = "You are spending $currencySymbol${String.format("%.2f", monthlySubscriptions)} monthly on recurring services. Review inactive memberships and rotate streaming services.",
          potentialSavings = "$currencySymbol${String.format("%.0f", monthlySubscriptions * 0.3)}/mo",
          actionTag = "Audit Services"
        )
      )
    }

    val savingsRate = if (totalIncome > 0) ((totalIncome - totalExpense) / totalIncome * 100).toInt() else 0
    if (savingsRate >= 20) {
      insights.add(
        GeminiInsight(
          title = "Healthy Savings Velocity ($savingsRate%)",
          category = "Savings",
          description = "You are saving $savingsRate% of your income! Direct excess surplus toward your high-priority goals or index funds to beat inflation.",
          potentialSavings = "+5% extra",
          actionTag = "Boost Goals"
        )
      )
    } else {
      insights.add(
        GeminiInsight(
          title = "Target 20% Net Savings Margin",
          category = "Savings",
          description = "Current savings margin is below the recommended 20% benchmark. Applying the 50/30/20 rule (50% needs, 30% wants, 20% savings) will build your emergency safety net faster.",
          potentialSavings = "$currencySymbol${String.format("%.0f", (totalIncome * 0.20).coerceAtLeast(100.0))}/mo",
          actionTag = "50/30/20 Rule"
        )
      )
    }

    val activeGoals = goals.filter { !it.isCompleted }
    if (activeGoals.isNotEmpty()) {
      val nextGoal = activeGoals.minByOrNull { it.targetAmount - it.currentAmount }
      if (nextGoal != null) {
        val remaining = nextGoal.targetAmount - nextGoal.currentAmount
        insights.add(
          GeminiInsight(
            title = "Fast-track \"${nextGoal.title}\"",
            category = "Goals",
            description = "You only need $currencySymbol${String.format("%.2f", remaining)} more to complete \"${nextGoal.title}\". A single small spending cut this week would reach 100% completion!",
            potentialSavings = "Goal completion",
            actionTag = "Reach Milestone"
          )
        )
      }
    }

    val health = when {
      savingsRate >= 30 -> "Excellent"
      savingsRate >= 15 -> "Good"
      savingsRate >= 0 -> "Moderate"
      else -> "Needs Attention"
    }

    val topAlert = if (topCat != null) {
      "${topCat.first} is your largest expense line at $currencySymbol${String.format("%.2f", topCat.second)}."
    } else {
      "Maintain consistent expense tracking to reveal spending trends."
    }

    return SpendingAnalysisResult(
      summaryHeadline = if (savingsRate >= 0) "Cash flow is positive with $savingsRate% saved this month." else "Monthly expenses currently exceed recorded income.",
      overallHealthRating = health,
      insights = insights,
      topSpendingAlert = topAlert,
      isFallback = true,
      rawResponse = "Local financial heuristic engine ($reason)"
    )
  }
}
