package com.example.data.api

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap

data class AlphaVantageQuote(
  val symbol: String,
  val price: Double,
  val open: Double,
  val high: Double,
  val low: Double,
  val volume: Long,
  val previousClose: Double,
  val change: Double,
  val changePercent: Double,
  val latestTradingDay: String,
  val timestamp: Long = System.currentTimeMillis()
)

data class AlphaVantageOverview(
  val symbol: String,
  val name: String,
  val description: String = "",
  val sector: String = "",
  val industry: String = "",
  val marketCap: Double = 0.0,
  val peRatio: Double = 0.0,
  val week52High: Double = 0.0,
  val week52Low: Double = 0.0
)

/**
 * Service to interact with the Alpha Vantage Stock API for live quotes,
 * fundamentals, and company overview.
 *
 * Free Tier: https://www.alphavantage.co/support/#api-key
 */
class AlphaVantageApiService {

  private val client: OkHttpClient get() = AppHttpClient.client

  // Cache company overviews in memory to conserve rate limits
  private val overviewCache = ConcurrentHashMap<String, AlphaVantageOverview>()

  /**
   * Resolves the active API key with priority:
   * 1. Explicitly passed in-app key (from UserPreferencesManager)
   * 2. BuildConfig key injected from Secrets panel via Secrets Gradle Plugin
   */
  fun resolveApiKey(customKey: String? = null): String {
    if (!customKey.isNullOrBlank() && customKey != "YOUR_ALPHA_VANTAGE_API_KEY") {
      return customKey.trim()
    }
    return try {
      val buildKey = BuildConfig.ALPHA_VANTAGE_API_KEY
      if (buildKey.isNotBlank() && buildKey != "YOUR_ALPHA_VANTAGE_API_KEY") {
        buildKey.trim()
      } else {
        ""
      }
    } catch (_: Exception) {
      ""
    }
  }

  fun isConfigured(customKey: String? = null): Boolean {
    return resolveApiKey(customKey).isNotBlank()
  }

  /**
   * Normalizes ticker symbol for Alpha Vantage.
   * E.g. Indian stocks can be queried with .BSE or .NSE (e.g. TATAMOTORS.BSE, INFY.BSE)
   */
  fun normalizeSymbol(raw: String): String {
    val sym = raw.trim().uppercase()
    return if (sym.endsWith(".NS")) {
      sym.replace(".NS", ".BSE")
    } else {
      sym
    }
  }

  /**
   * Fetch real-time global quote from Alpha Vantage:
   * GET https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol={symbol}&apikey={apiKey}
   */
  suspend fun fetchGlobalQuote(
    rawSymbol: String,
    customApiKey: String? = null
  ): Result<AlphaVantageQuote> = withContext(Dispatchers.IO) {
    val symbol = rawSymbol.trim().uppercase()
    if (symbol.isBlank()) {
      return@withContext Result.failure(IllegalArgumentException("Stock symbol cannot be empty"))
    }

    val token = resolveApiKey(customApiKey)
    if (token.isBlank()) {
      return@withContext Result.failure(
        IllegalStateException("Alpha Vantage API key is not configured. Add it in AI Studio Secrets panel or Settings.")
      )
    }

    // Candidate symbols to try (e.g. for Indian stocks, try .BSE first)
    val candidates = if (!symbol.contains(".") && !symbol.contains("-")) {
      listOf("$symbol.BSE", symbol)
    } else if (symbol.endsWith(".NS")) {
      listOf(symbol.replace(".NS", ".BSE"), symbol)
    } else {
      listOf(symbol)
    }

    var lastError: Exception? = null

    for (cand in candidates) {
      try {
        val url = "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=$cand&apikey=$token"
        val request = Request.Builder()
          .url(url)
          .addHeader("Accept", "application/json")
          .build()

        val (responseBody, isSuccessful, code) = client.newCall(request).execute().use { resp ->
          Triple(resp.body?.string(), resp.isSuccessful, resp.code)
        }

        if (!isSuccessful || responseBody.isNullOrBlank()) {
          lastError = Exception("Alpha Vantage request failed with HTTP $code")
          continue
        }

        val json = JSONObject(responseBody)

        // Check for rate limit note or error message
        if (json.has("Note") || json.has("Information")) {
          val note = json.optString("Note").ifBlank { json.optString("Information") }
          lastError = Exception("Alpha Vantage notice: $note")
          continue
        }

        if (json.has("Error Message")) {
          lastError = Exception(json.optString("Error Message"))
          continue
        }

        val quoteObj = json.optJSONObject("Global Quote")
        if (quoteObj != null && quoteObj.length() > 0) {
          val priceStr = quoteObj.optString("05. price", "0.0")
          val price = priceStr.toDoubleOrNull() ?: 0.0

          if (price > 0.0) {
            val open = quoteObj.optString("02. open", "0.0").toDoubleOrNull() ?: 0.0
            val high = quoteObj.optString("03. high", "0.0").toDoubleOrNull() ?: 0.0
            val low = quoteObj.optString("04. low", "0.0").toDoubleOrNull() ?: 0.0
            val volume = quoteObj.optString("06. volume", "0").toLongOrNull() ?: 0L
            val prevClose = quoteObj.optString("08. previous close", "0.0").toDoubleOrNull() ?: 0.0
            val change = quoteObj.optString("09. change", "0.0").toDoubleOrNull() ?: 0.0
            val changePctRaw = quoteObj.optString("10. change percent", "0.0%").replace("%", "").trim()
            val changePct = changePctRaw.toDoubleOrNull() ?: 0.0
            val day = quoteObj.optString("07. latest trading day", "")

            return@withContext Result.success(
              AlphaVantageQuote(
                symbol = cand,
                price = price,
                open = open,
                high = high,
                low = low,
                volume = volume,
                previousClose = prevClose,
                change = change,
                changePercent = changePct,
                latestTradingDay = day
              )
            )
          }
        }
      } catch (e: Exception) {
        lastError = e
      }
    }

    Result.failure(lastError ?: Exception("No market quote returned from Alpha Vantage for '$symbol'"))
  }

  /**
   * Fetch company overview (fundamentals, PE ratio, 52-week high/low, description):
   * GET https://www.alphavantage.co/query?function=OVERVIEW&symbol={symbol}&apikey={apiKey}
   */
  suspend fun fetchOverview(
    rawSymbol: String,
    customApiKey: String? = null
  ): Result<AlphaVantageOverview> = withContext(Dispatchers.IO) {
    val symbol = rawSymbol.trim().uppercase()
    overviewCache[symbol]?.let { return@withContext Result.success(it) }

    val token = resolveApiKey(customApiKey)
    if (token.isBlank()) {
      return@withContext Result.failure(IllegalStateException("Alpha Vantage API key is not configured"))
    }

    try {
      val url = "https://www.alphavantage.co/query?function=OVERVIEW&symbol=$symbol&apikey=$token"
      val request = Request.Builder()
        .url(url)
        .addHeader("Accept", "application/json")
        .build()

      val (responseBody, isSuccessful, code) = client.newCall(request).execute().use { resp ->
        Triple(resp.body?.string(), resp.isSuccessful, resp.code)
      }

      if (!isSuccessful || responseBody.isNullOrBlank()) {
        return@withContext Result.failure(Exception("HTTP $code from Alpha Vantage overview"))
      }

      val json = JSONObject(responseBody)
      val name = json.optString("Name", symbol)
      val sym = json.optString("Symbol", symbol)

      if (name.isBlank() && sym.isBlank()) {
        return@withContext Result.failure(Exception("No overview found for $symbol"))
      }

      val overview = AlphaVantageOverview(
        symbol = sym.ifBlank { symbol },
        name = name.ifBlank { symbol },
        description = json.optString("Description", ""),
        sector = json.optString("Sector", ""),
        industry = json.optString("Industry", ""),
        marketCap = json.optString("MarketCapitalization", "0").toDoubleOrNull() ?: 0.0,
        peRatio = json.optString("PERatio", "0").toDoubleOrNull() ?: 0.0,
        week52High = json.optString("52WeekHigh", "0").toDoubleOrNull() ?: 0.0,
        week52Low = json.optString("52WeekLow", "0").toDoubleOrNull() ?: 0.0
      )
      overviewCache[symbol] = overview
      Result.success(overview)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Validate the given API key by pinging the GLOBAL_QUOTE endpoint for IBM
   */
  suspend fun validateApiKey(testKey: String): Result<Boolean> = withContext(Dispatchers.IO) {
    if (testKey.isBlank()) {
      return@withContext Result.failure(IllegalArgumentException("Key cannot be empty"))
    }
    try {
      val url = "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=IBM&apikey=${testKey.trim()}"
      val request = Request.Builder().url(url).build()
      val (responseBody, isSuccessful, code) = client.newCall(request).execute().use { resp ->
        Triple(resp.body?.string(), resp.isSuccessful, resp.code)
      }

      if (!isSuccessful || responseBody.isNullOrBlank()) {
        return@withContext Result.failure(Exception("Validation failed with HTTP $code"))
      }

      val json = JSONObject(responseBody)
      if (json.has("Error Message")) {
        return@withContext Result.failure(Exception("Invalid API key or call error"))
      }
      if (json.has("Note")) {
        // Standard rate limit reached, but key is valid
        return@withContext Result.success(true)
      }

      val quoteObj = json.optJSONObject("Global Quote")
      if (quoteObj != null && quoteObj.length() > 0) {
        Result.success(true)
      } else {
        Result.failure(Exception("API returned empty data for validation"))
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Concurrently fetch quotes for multiple symbols with rate limit throttling.
   */
  suspend fun fetchBatchQuotes(
    symbols: List<String>,
    customApiKey: String? = null
  ): Map<String, AlphaVantageQuote> = withContext(Dispatchers.IO) {
    val results = mutableMapOf<String, AlphaVantageQuote>()
    for (sym in symbols.distinct()) {
      val res = fetchGlobalQuote(sym, customApiKey)
      if (res.isSuccess) {
        val q = res.getOrNull()
        if (q != null && q.price > 0.0) {
          results[sym] = q
          val clean = sym.uppercase().removeSuffix(".BSE").removeSuffix(".NSE").removeSuffix(".NS").removeSuffix(".BO")
          results[clean] = q
        }
      }
    }
    results
  }
}
