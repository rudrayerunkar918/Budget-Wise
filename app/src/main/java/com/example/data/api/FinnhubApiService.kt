package com.example.data.api

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap

const val USD_TO_INR_RATE = 86.80

data class FinnhubQuote(
  val symbol: String,
  val currentPrice: Double, // Always converted to INR (₹)
  val changeAmount: Double, // Converted to INR (₹)
  val changePercent: Double,
  val highPrice: Double, // Converted to INR (₹)
  val lowPrice: Double, // Converted to INR (₹)
  val openPrice: Double, // Converted to INR (₹)
  val previousClose: Double, // Converted to INR (₹)
  val rawPriceUsd: Double = 0.0,
  val rawChangeUsd: Double = 0.0,
  val rawHighUsd: Double = 0.0,
  val rawLowUsd: Double = 0.0,
  val rawOpenUsd: Double = 0.0,
  val rawPrevCloseUsd: Double = 0.0,
  val usdToInrRate: Double = USD_TO_INR_RATE,
  val currency: String = "INR",
  val timestamp: Long = System.currentTimeMillis()
)

data class FinnhubCompanyProfile(
  val ticker: String,
  val name: String,
  val country: String = "",
  val currency: String = "USD",
  val exchange: String = "",
  val finnhubIndustry: String = "",
  val ipo: String = "",
  val logo: String = "",
  val marketCapitalization: Double = 0.0,
  val weburl: String = ""
)

/**
 * Service to interact with the Finnhub Stock API for real-time quotes,
 * company profiles, and daily range data.
 *
 * Free Tier: https://finnhub.io/
 */
class FinnhubApiService {

  private val client: OkHttpClient get() = AppHttpClient.client

  // Cache company profiles in memory to conserve rate limits
  private val profileCache = ConcurrentHashMap<String, FinnhubCompanyProfile>()

  /**
   * Resolves the active API key with priority:
   * 1. Explicitly passed in-app key (from UserPreferencesManager)
   * 2. BuildConfig key injected from Secrets panel via Secrets Gradle Plugin
   */
  fun resolveApiKey(customKey: String? = null): String {
    if (!customKey.isNullOrBlank() && customKey != "YOUR_FINNHUB_API_KEY") {
      return customKey.trim()
    }
    return try {
      val buildKey = BuildConfig.FINNHUB_API_KEY
      if (buildKey.isNotBlank() && buildKey != "YOUR_FINNHUB_API_KEY") {
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
   * Normalizes ticker symbol for Finnhub (e.g. "AAPL.US" -> "AAPL", "TCS.NS" -> "TCS.NS")
   */
  fun normalizeSymbol(raw: String): String {
    var sym = raw.trim().uppercase()
    if (sym.endsWith(".US")) {
      sym = sym.removeSuffix(".US")
    }
    return sym
  }

  /**
   * Fetch real-time stock quote from Finnhub:
   * GET https://finnhub.io/api/v1/quote?symbol={symbol}&token={token}
   */
  suspend fun fetchQuote(
    rawSymbol: String,
    customApiKey: String? = null
  ): Result<FinnhubQuote> = withContext(Dispatchers.IO) {
    val symbol = normalizeSymbol(rawSymbol)
    if (symbol.isBlank()) {
      return@withContext Result.failure(IllegalArgumentException("Stock symbol cannot be empty"))
    }

    val token = resolveApiKey(customApiKey)
    if (token.isBlank()) {
      return@withContext Result.failure(
        IllegalStateException("Finnhub API key is not configured. Add it in AI Studio Secrets panel or Settings.")
      )
    }

    try {
      val url = "https://finnhub.io/api/v1/quote?symbol=$symbol&token=$token"
      val request = Request.Builder()
        .url(url)
        .addHeader("Accept", "application/json")
        .build()

      val (responseBody, isSuccessful, code) = client.newCall(request).execute().use { resp ->
        Triple(resp.body?.string(), resp.isSuccessful, resp.code)
      }

      if (!isSuccessful || responseBody.isNullOrBlank()) {
        return@withContext Result.failure(
          Exception("Finnhub API request failed with HTTP $code")
        )
      }

      val json = JSONObject(responseBody)
      val currentUsd = json.optDouble("c", 0.0)
      val changeUsd = json.optDouble("d", 0.0)
      val dp = json.optDouble("dp", 0.0)
      val highUsd = json.optDouble("h", 0.0)
      val lowUsd = json.optDouble("l", 0.0)
      val openUsd = json.optDouble("o", 0.0)
      val prevCloseUsd = json.optDouble("pc", 0.0)
      val rawTime = json.optLong("t", 0L)
      val timeMillis = if (rawTime > 0L) rawTime * 1000L else System.currentTimeMillis()

      if (currentUsd <= 0.0 && prevCloseUsd <= 0.0) {
        return@withContext Result.failure(
          Exception("No market quote returned for symbol '$symbol'")
        )
      }

      // Convert USD prices to INR (₹)
      val rate = USD_TO_INR_RATE
      val currentInr = currentUsd * rate
      val changeInr = changeUsd * rate
      val highInr = if (highUsd > 0.0) highUsd * rate else currentInr
      val lowInr = if (lowUsd > 0.0) lowUsd * rate else currentInr
      val openInr = if (openUsd > 0.0) openUsd * rate else currentInr
      val prevCloseInr = if (prevCloseUsd > 0.0) prevCloseUsd * rate else currentInr

      Result.success(
        FinnhubQuote(
          symbol = symbol,
          currentPrice = currentInr,
          changeAmount = changeInr,
          changePercent = dp,
          highPrice = highInr,
          lowPrice = lowInr,
          openPrice = openInr,
          previousClose = prevCloseInr,
          rawPriceUsd = currentUsd,
          rawChangeUsd = changeUsd,
          rawHighUsd = highUsd,
          rawLowUsd = lowUsd,
          rawOpenUsd = openUsd,
          rawPrevCloseUsd = prevCloseUsd,
          usdToInrRate = rate,
          currency = "INR",
          timestamp = timeMillis
        )
      )
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Fetch company profile (name, market cap, industry, logo, weburl):
   * GET https://finnhub.io/api/v1/stock/profile2?symbol={symbol}&token={token}
   */
  suspend fun fetchCompanyProfile(
    rawSymbol: String,
    customApiKey: String? = null
  ): Result<FinnhubCompanyProfile> = withContext(Dispatchers.IO) {
    val symbol = normalizeSymbol(rawSymbol)
    profileCache[symbol]?.let { return@withContext Result.success(it) }

    val token = resolveApiKey(customApiKey)
    if (token.isBlank()) {
      return@withContext Result.failure(IllegalStateException("Finnhub API key is not configured"))
    }

    try {
      val url = "https://finnhub.io/api/v1/stock/profile2?symbol=$symbol&token=$token"
      val request = Request.Builder()
        .url(url)
        .addHeader("Accept", "application/json")
        .build()

      val (responseBody, isSuccessful, code) = client.newCall(request).execute().use { resp ->
        Triple(resp.body?.string(), resp.isSuccessful, resp.code)
      }

      if (!isSuccessful || responseBody.isNullOrBlank()) {
        return@withContext Result.failure(Exception("HTTP $code from Finnhub profile"))
      }

      val json = JSONObject(responseBody)
      val name = json.optString("name", symbol)
      val ticker = json.optString("ticker", symbol)
      if (name.isBlank() && ticker.isBlank()) {
        return@withContext Result.failure(Exception("No profile data found for $symbol"))
      }

      val profile = FinnhubCompanyProfile(
        ticker = ticker.ifBlank { symbol },
        name = name.ifBlank { symbol },
        country = json.optString("country", ""),
        currency = json.optString("currency", "USD"),
        exchange = json.optString("exchange", ""),
        finnhubIndustry = json.optString("finnhubIndustry", ""),
        ipo = json.optString("ipo", ""),
        logo = json.optString("logo", ""),
        marketCapitalization = json.optDouble("marketCapitalization", 0.0),
        weburl = json.optString("weburl", "")
      )
      profileCache[symbol] = profile
      Result.success(profile)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Concurrently fetch quotes for multiple symbols with Finnhub.
   */
  suspend fun fetchBatchQuotes(
    symbols: List<String>,
    customApiKey: String? = null
  ): Map<String, FinnhubQuote> = coroutineScope {
    val cleanSymbols = symbols.map { normalizeSymbol(it) }.distinct()
    val deferredList = cleanSymbols.map { sym ->
      async(Dispatchers.IO) {
        val res = fetchQuote(sym, customApiKey)
        if (res.isSuccess) {
          sym to res.getOrNull()
        } else {
          null
        }
      }
    }
    deferredList.awaitAll().filterNotNull().mapNotNull { (sym, quote) ->
      if (quote != null) sym to quote else null
    }.toMap()
  }

  /**
   * Validate the given API key by pinging the quote endpoint for AAPL
   */
  suspend fun validateApiKey(testKey: String): Result<Boolean> = withContext(Dispatchers.IO) {
    if (testKey.isBlank()) {
      return@withContext Result.failure(IllegalArgumentException("Key cannot be empty"))
    }
    try {
      val url = "https://finnhub.io/api/v1/quote?symbol=AAPL&token=${testKey.trim()}"
      val request = Request.Builder().url(url).build()
      val (responseBody, isSuccessful, code) = client.newCall(request).execute().use { resp ->
        Triple(resp.body?.string(), resp.isSuccessful, resp.code)
      }

      if (code == 401 || code == 403) {
        return@withContext Result.failure(Exception("Invalid API key (HTTP $code)"))
      }
      if (!isSuccessful || responseBody.isNullOrBlank()) {
        return@withContext Result.failure(Exception("Validation failed with HTTP $code"))
      }

      val json = JSONObject(responseBody)
      val current = json.optDouble("c", 0.0)
      if (current > 0.0) {
        Result.success(true)
      } else {
        Result.failure(Exception("Key did not return valid market data"))
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
