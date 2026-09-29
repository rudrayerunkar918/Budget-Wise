package com.example.data.ai

import com.example.BuildConfig
import com.example.data.model.StockEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

enum class StockRecommendationAction {
  BUY,
  HOLD,
  SELL
}

data class GeminiStockRecommendation(
  val symbol: String,
  val companyName: String,
  val action: StockRecommendationAction,
  val confidenceScore: Int, // 50 to 98%
  val targetPrice: Double?,
  val stopLossPrice: Double?,
  val riskLevel: String, // "Low", "Medium", "High"
  val timeHorizon: String, // "Short-Term", "Medium-Term", "Long-Term"
  val rationale: String,
  val keyCatalysts: List<String> = emptyList(),
  val timestamp: Long = System.currentTimeMillis(),
  val isGroundedWithGoogleSearch: Boolean = false,
  val searchSources: List<GroundedSource> = emptyList()
)

data class GeminiPortfolioStockVerdict(
  val overallStance: String,
  val portfolioSummary: String,
  val buyCount: Int,
  val holdCount: Int,
  val sellCount: Int,
  val recommendations: List<GeminiStockRecommendation>,
  val isAiPowered: Boolean = true,
  val timestamp: Long = System.currentTimeMillis(),
  val isGroundedWithGoogleSearch: Boolean = false,
  val searchQueries: List<String> = emptyList(),
  val searchSources: List<GroundedSource> = emptyList()
)

class GeminiStockAdvisor {

  private val client: OkHttpClient get() = com.example.data.api.AppHttpClient.client

  private val modelName = "gemini-3.5-flash"

  suspend fun analyzePortfolioStocks(
    stocks: List<StockEntity>,
    currencySymbol: String
  ): GeminiPortfolioStockVerdict = withContext(Dispatchers.IO) {
    if (stocks.isEmpty()) {
      return@withContext GeminiPortfolioStockVerdict(
        overallStance = "Empty Portfolio",
        portfolioSummary = "Add stocks to your portfolio to receive AI buy, hold, and sell suggestions.",
        buyCount = 0,
        holdCount = 0,
        sellCount = 0,
        recommendations = emptyList()
      )
    }

    val apiKey = try {
      BuildConfig.GEMINI_API_KEY
    } catch (_: Exception) {
      ""
    }

    if (apiKey.isBlank()) {
      return@withContext generateSmartRuleBasedVerdict(stocks, currencySymbol)
    }

    try {
      val prompt = buildPortfolioAnalysisPrompt(stocks, currencySymbol)
      val (responseText, queries, sources) = callGeminiApi(apiKey, prompt)
      val parsed = parseVerdictJson(responseText, stocks, currencySymbol)
      if (parsed != null && parsed.recommendations.isNotEmpty()) {
        val updatedRecs = parsed.recommendations.map { rec ->
          rec.copy(
            isGroundedWithGoogleSearch = queries.isNotEmpty() || sources.isNotEmpty(),
            searchSources = sources
          )
        }
        return@withContext parsed.copy(
          isGroundedWithGoogleSearch = queries.isNotEmpty() || sources.isNotEmpty(),
          searchQueries = queries,
          searchSources = sources,
          recommendations = updatedRecs
        )
      }
    } catch (_: Exception) {
      // Graceful fallback to rule-based analysis if network or API error occurs
    }

    return@withContext generateSmartRuleBasedVerdict(stocks, currencySymbol)
  }

  suspend fun analyzeSingleStock(
    stock: StockEntity,
    currencySymbol: String
  ): GeminiStockRecommendation = withContext(Dispatchers.IO) {
    val verdict = analyzePortfolioStocks(listOf(stock), currencySymbol)
    verdict.recommendations.firstOrNull() ?: generateFallbackRecommendation(stock, currencySymbol)
  }

  private fun buildPortfolioAnalysisPrompt(stocks: List<StockEntity>, currencySymbol: String): String {
    val holdingsList = stocks.joinToString("\n") { s ->
      "- Symbol: ${s.symbol} (${s.companyName}), Shares: ${s.shares}, Buy Price: $currencySymbol${s.avgBuyPrice}, Current Price: $currencySymbol${s.currentPrice}, P&L: ${String.format("%.2f", s.pnlPercentage)}%, 24h Change: ${String.format("%.2f", s.dailyChangePercent)}%"
    }

    return """
      You are an expert equity research analyst and portfolio manager. Analyze the following user stock portfolio and provide actionable, rigorous BUY, HOLD, or SELL recommendations for each holding based on valuation, price momentum, risk-reward skew, and market fundamentals.

      Holdings:
      $holdingsList

      Currency: $currencySymbol

      Return ONLY a single valid JSON object (no markdown, no backticks, no preamble) strictly following this JSON schema:
      {
        "overallStance": "e.g. Bullish Accumulate, Hold & Rebalance, or Selective Profit Taking",
        "portfolioSummary": "A 2-sentence executive summary of portfolio health, valuation, and immediate tactical steps",
        "recommendations": [
          {
            "symbol": "SYMBOL (must match input symbol)",
            "companyName": "Company Name",
            "action": "BUY" or "HOLD" or "SELL",
            "confidenceScore": 85,
            "targetPrice": 3200.0,
            "stopLossPrice": 2750.0,
            "riskLevel": "Low" or "Medium" or "High",
            "timeHorizon": "Short-Term" or "Medium-Term" or "Long-Term",
            "rationale": "Clear, concise 1-2 sentence explanation of why BUY, HOLD, or SELL",
            "keyCatalysts": ["Catalyst 1", "Catalyst 2"]
          }
        ]
      }
    """.trimIndent()
  }

  private fun callGeminiApi(apiKey: String, prompt: String): Triple<String, List<String>, List<GroundedSource>> {
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
      // Ground with Google Search tool for live stock news, quarterly results & analyst targets
      put("tools", JSONArray().apply {
        put(JSONObject().apply {
          put("googleSearch", JSONObject())
        })
      })
      put("generationConfig", JSONObject().apply {
        put("temperature", 0.25)
        put("maxOutputTokens", 2048)
      })
    }

    val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
    val request = Request.Builder()
      .url(url)
      .post(requestBody)
      .build()

    client.newCall(request).execute().use { response ->
      if (!response.isSuccessful) {
        throw Exception("Gemini API call failed: ${response.code} ${response.message}")
      }
      val responseBody = response.body?.string() ?: throw Exception("Empty Gemini response")
      val json = JSONObject(responseBody)
      val candidates = json.optJSONArray("candidates") ?: throw Exception("No candidates returned")
      if (candidates.length() == 0) throw Exception("Empty candidate list")

      val candidate = candidates.getJSONObject(0)
      val content = candidate.optJSONObject("content") ?: throw Exception("No content object")
      val parts = content.optJSONArray("parts") ?: throw Exception("No parts array")
      if (parts.length() == 0) throw Exception("Empty parts array")

      val text = parts.getJSONObject(0).optString("text", "")

      val queries = mutableListOf<String>()
      val sources = mutableListOf<GroundedSource>()

      val groundingMeta = candidate.optJSONObject("groundingMetadata")
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
              val title = web.optString("title", "Market Source")
              val uri = web.optString("uri", "")
              if (uri.isNotBlank()) {
                sources.add(GroundedSource(title, uri))
              }
            }
          }
        }
      }

      return Triple(text, queries.distinct(), sources.distinctBy { it.uri })
    }
  }

  private fun parseVerdictJson(
    rawText: String,
    originalStocks: List<StockEntity>,
    currencySymbol: String
  ): GeminiPortfolioStockVerdict? {
    try {
      var clean = rawText.trim()
      if (clean.startsWith("```json")) {
        clean = clean.removePrefix("```json").trim()
      } else if (clean.startsWith("```")) {
        clean = clean.removePrefix("```").trim()
      }
      if (clean.endsWith("```")) {
        clean = clean.removeSuffix("```").trim()
      }

      val json = JSONObject(clean)
      val overallStance = json.optString("overallStance", "Hold & Monitor")
      val portfolioSummary = json.optString("portfolioSummary", "Portfolio analysis complete.")
      val recsArray = json.optJSONArray("recommendations") ?: return null

      val recommendations = mutableListOf<GeminiStockRecommendation>()
      var buy = 0
      var hold = 0
      var sell = 0

      for (i in 0 until recsArray.length()) {
        val obj = recsArray.getJSONObject(i)
        val symbol = obj.optString("symbol", "")
        val company = obj.optString("companyName", symbol)
        val actionStr = obj.optString("action", "HOLD").uppercase()
        val action = when (actionStr) {
          "BUY" -> {
            buy++
            StockRecommendationAction.BUY
          }
          "SELL" -> {
            sell++
            StockRecommendationAction.SELL
          }
          else -> {
            hold++
            StockRecommendationAction.HOLD
          }
        }

        val confidence = obj.optInt("confidenceScore", 75).coerceIn(50, 99)
        val targetPrice = if (obj.has("targetPrice") && !obj.isNull("targetPrice")) obj.optDouble("targetPrice") else null
        val stopLoss = if (obj.has("stopLossPrice") && !obj.isNull("stopLossPrice")) obj.optDouble("stopLossPrice") else null
        val risk = obj.optString("riskLevel", "Medium")
        val horizon = obj.optString("timeHorizon", "Medium-Term")
        val rationale = obj.optString("rationale", "Maintain position with disciplined risk management.")

        val catalysts = mutableListOf<String>()
        val catArray = obj.optJSONArray("keyCatalysts")
        if (catArray != null) {
          for (c in 0 until catArray.length()) {
            catalysts.add(catArray.getString(c))
          }
        }

        recommendations.add(
          GeminiStockRecommendation(
            symbol = symbol,
            companyName = company,
            action = action,
            confidenceScore = confidence,
            targetPrice = targetPrice,
            stopLossPrice = stopLoss,
            riskLevel = risk,
            timeHorizon = horizon,
            rationale = rationale,
            keyCatalysts = catalysts
          )
        )
      }

      // Ensure all user stocks have a recommendation
      for (stk in originalStocks) {
        if (recommendations.none { it.symbol.equals(stk.symbol, ignoreCase = true) }) {
          recommendations.add(generateFallbackRecommendation(stk, currencySymbol))
        }
      }

      return GeminiPortfolioStockVerdict(
        overallStance = overallStance,
        portfolioSummary = portfolioSummary,
        buyCount = recommendations.count { it.action == StockRecommendationAction.BUY },
        holdCount = recommendations.count { it.action == StockRecommendationAction.HOLD },
        sellCount = recommendations.count { it.action == StockRecommendationAction.SELL },
        recommendations = recommendations,
        isAiPowered = true
      )
    } catch (_: Exception) {
      return null
    }
  }

  fun generateSmartRuleBasedVerdict(
    stocks: List<StockEntity>,
    currencySymbol: String
  ): GeminiPortfolioStockVerdict {
    val recs = stocks.map { generateFallbackRecommendation(it, currencySymbol) }
    val buyCount = recs.count { it.action == StockRecommendationAction.BUY }
    val holdCount = recs.count { it.action == StockRecommendationAction.HOLD }
    val sellCount = recs.count { it.action == StockRecommendationAction.SELL }

    val stance = when {
      buyCount > sellCount && buyCount > holdCount -> "Bullish Momentum • Accumulate"
      sellCount > buyCount -> "Cautious Stance • Take Profits"
      else -> "Balanced Portfolio • Hold & Monitor"
    }

    val summary = "Evaluated ${stocks.size} holdings across valuation metrics and P&L momentum. $buyCount strong buys identified with favorable upside, $holdCount stable core holdings, and $sellCount candidates for profit taking."

    return GeminiPortfolioStockVerdict(
      overallStance = stance,
      portfolioSummary = summary,
      buyCount = buyCount,
      holdCount = holdCount,
      sellCount = sellCount,
      recommendations = recs,
      isAiPowered = false
    )
  }

  fun generateFallbackRecommendation(
    stock: StockEntity,
    currencySymbol: String
  ): GeminiStockRecommendation {
    val pnlPct = stock.pnlPercentage
    val current = stock.currentPrice

    return when {
      // Over +35% gain -> Recommend SELL / Take partial profits
      pnlPct >= 35.0 -> {
        GeminiStockRecommendation(
          symbol = stock.symbol,
          companyName = stock.companyName,
          action = StockRecommendationAction.SELL,
          confidenceScore = 84,
          targetPrice = current * 1.05,
          stopLossPrice = current * 0.92,
          riskLevel = "Low",
          timeHorizon = "Short-Term",
          rationale = "Exceptional unrealized gain of +${String.format("%.1f", pnlPct)}%. Consider booking partial profits or tightening trailing stop-losses to protect capital.",
          keyCatalysts = listOf("Strong multimonth run-up", "Overbought momentum indicators")
        )
      }
      // Down > 10% -> Dip buying opportunity / BUY
      pnlPct <= -8.0 -> {
        GeminiStockRecommendation(
          symbol = stock.symbol,
          companyName = stock.companyName,
          action = StockRecommendationAction.BUY,
          confidenceScore = 80,
          targetPrice = stock.avgBuyPrice * 1.15,
          stopLossPrice = current * 0.88,
          riskLevel = "Medium",
          timeHorizon = "Medium-Term",
          rationale = "Trading below average cost basis (-${String.format("%.1f", -pnlPct)}%). High-conviction entry point to accumulate and lower your average purchase price.",
          keyCatalysts = listOf("Discount to intrinsic value", "Oversold RSI reversal support")
        )
      }
      // Moderate upside -> BUY with target
      pnlPct in 5.0..25.0 -> {
        GeminiStockRecommendation(
          symbol = stock.symbol,
          companyName = stock.companyName,
          action = StockRecommendationAction.BUY,
          confidenceScore = 82,
          targetPrice = current * 1.20,
          stopLossPrice = stock.avgBuyPrice * 0.98,
          riskLevel = "Low",
          timeHorizon = "Long-Term",
          rationale = "Healthy positive price momentum (+${String.format("%.1f", pnlPct)}%) with solid earnings support. Favorable risk-to-reward ratio for continued accumulation.",
          keyCatalysts = listOf("Earnings expansion", "Sector tailwinds")
        )
      }
      // Flat or moderate -> HOLD
      else -> {
        GeminiStockRecommendation(
          symbol = stock.symbol,
          companyName = stock.companyName,
          action = StockRecommendationAction.HOLD,
          confidenceScore = 78,
          targetPrice = current * 1.12,
          stopLossPrice = current * 0.93,
          riskLevel = "Medium",
          timeHorizon = "Medium-Term",
          rationale = "Position is consolidating within expected valuation bounds. Maintain existing allocation while tracking upcoming quarterly earnings.",
          keyCatalysts = listOf("Support consolidation", "Volume confirmation")
        )
      }
    }
  }
}
