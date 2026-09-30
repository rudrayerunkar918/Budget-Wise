package com.example.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap

data class StockQuote(
  val symbol: String,
  val regularMarketPrice: Double,
  val previousClose: Double,
  val changeAmount: Double,
  val changePercent: Double,
  val currency: String = "INR",
  val timestamp: Long = System.currentTimeMillis(),
  val exchange: String = "",
  val displayName: String? = null,
  val isRealTime: Boolean = true,
  val originalCurrency: String = "INR",
  val originalPrice: Double = regularMarketPrice,
  val exchangeRateToInr: Double = 1.0,
  val dataSource: String = "Live"
)

class StockMarketApiService {

  private val client: OkHttpClient get() = AppHttpClient.client

  // Cache live USD to INR rate in memory with timestamp to avoid excessive API hits while keeping rates fresh
  private var cachedUsdInrRate: Double = 85.50
  private var lastUsdInrFetchTime: Long = 0L
  private val FOREX_CACHE_VALIDITY_MS = 60_000L // 1 minute fresh cache

  /**
   * Fetch live USD to INR forex exchange rate.
   * Prioritizes Alpha Vantage, then Finnhub, then Yahoo Finance, with resilient fallback.
   */
  suspend fun fetchLiveUsdInrRate(
    alphaVantageKey: String = "",
    finnhubKey: String = "",
    forceRefresh: Boolean = false
  ): Double = withContext(Dispatchers.IO) {
    val now = System.currentTimeMillis()
    if (!forceRefresh && (now - lastUsdInrFetchTime) < FOREX_CACHE_VALIDITY_MS && cachedUsdInrRate > 0) {
      return@withContext cachedUsdInrRate
    }

    // 1. Try Alpha Vantage Real-time Currency Exchange Rate
    if (alphaVantageKey.isNotBlank()) {
      try {
        val avRate = queryAlphaVantageForex(alphaVantageKey)
        if (avRate != null && avRate > 50.0) {
          cachedUsdInrRate = avRate
          lastUsdInrFetchTime = now
          return@withContext avRate
        }
      } catch (_: Exception) {}
    }

    // 2. Try Finnhub Forex Rates
    if (finnhubKey.isNotBlank()) {
      try {
        val fhRate = queryFinnhubForex(finnhubKey)
        if (fhRate != null && fhRate > 50.0) {
          cachedUsdInrRate = fhRate
          lastUsdInrFetchTime = now
          return@withContext fhRate
        }
      } catch (_: Exception) {}
    }

    // 3. Fallback: Query Yahoo Finance USDINR=X
    try {
      val yfRate = queryYahooForex()
      if (yfRate != null && yfRate > 50.0) {
        cachedUsdInrRate = yfRate
        lastUsdInrFetchTime = now
        return@withContext yfRate
      }
    } catch (_: Exception) {}

    // 4. Return cached or reliable default
    cachedUsdInrRate
  }

  /**
   * Validate Finnhub API Key by pinging a test quote.
   */
  suspend fun testFinnhubApiKey(key: String): Result<String> = withContext(Dispatchers.IO) {
    if (key.isBlank()) return@withContext Result.failure(IllegalArgumentException("Finnhub API key is empty"))
    try {
      val url = "https://finnhub.io/api/v1/quote?symbol=AAPL&token=${key.trim()}"
      val request = Request.Builder().url(url).build()
      client.newCall(request).execute().use { response ->
        if (response.code == 401 || response.code == 403) {
          return@withContext Result.failure(Exception("Invalid API key or unauthorized (HTTP ${response.code})"))
        }
        if (!response.isSuccessful) {
          return@withContext Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
        }
        val body = response.body?.string() ?: ""
        val json = JSONObject(body)
        if (json.has("error")) {
          return@withContext Result.failure(Exception(json.optString("error", "API Error")))
        }
        val price = json.optDouble("c", 0.0)
        if (price > 0.0) {
          Result.success("Connected! Live AAPL price: $$price USD")
        } else {
          Result.failure(Exception("Valid key, but received empty quote response from Finnhub"))
        }
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Validate Alpha Vantage API Key by testing USD/INR forex endpoint.
   */
  suspend fun testAlphaVantageApiKey(key: String): Result<String> = withContext(Dispatchers.IO) {
    if (key.isBlank()) return@withContext Result.failure(IllegalArgumentException("Alpha Vantage API key is empty"))
    try {
      val url = "https://www.alphavantage.co/query?function=CURRENCY_EXCHANGE_RATE&from_currency=USD&to_currency=INR&apikey=${key.trim()}"
      val request = Request.Builder().url(url).build()
      client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
          return@withContext Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
        }
        val body = response.body?.string() ?: ""
        val json = JSONObject(body)
        if (json.has("Error Message")) {
          return@withContext Result.failure(Exception("Invalid API key: ${json.getString("Error Message")}"))
        }
        if (json.has("Note")) {
          return@withContext Result.failure(Exception("Rate limit reached: ${json.getString("Note")}"))
        }
        val rateObj = json.optJSONObject("Realtime Currency Exchange Rate")
        if (rateObj != null) {
          val rate = rateObj.optDouble("5. Exchange Rate", 0.0)
          if (rate > 0.0) {
            Result.success("Connected! Live USD/INR Rate: ₹${String.format("%.2f", rate)}")
          } else {
            Result.failure(Exception("Rate not found in response"))
          }
        } else {
          Result.failure(Exception("Unexpected response format from Alpha Vantage"))
        }
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Fetch current stock quote from Web API for a given symbol.
   * All foreign stocks are converted to INR in real time.
   */
  suspend fun fetchStockQuote(
    rawSymbol: String,
    finnhubKey: String = "",
    alphaVantageKey: String = ""
  ): Result<StockQuote> = withContext(Dispatchers.IO) {
    val symbol = rawSymbol.trim().uppercase()
    if (symbol.isBlank()) {
      return@withContext Result.failure(IllegalArgumentException("Stock symbol cannot be empty"))
    }

    val liveUsdInrRate = fetchLiveUsdInrRate(alphaVantageKey, finnhubKey)

    // 1. If Finnhub key is present, try Finnhub first
    if (finnhubKey.isNotBlank()) {
      try {
        val fhQuote = queryFinnhubQuote(symbol, finnhubKey, liveUsdInrRate)
        if (fhQuote != null) {
          return@withContext Result.success(fhQuote)
        }
      } catch (_: Exception) {}
    }

    // 2. If Alpha Vantage key is present, try Alpha Vantage
    if (alphaVantageKey.isNotBlank()) {
      try {
        val avQuote = queryAlphaVantageQuote(symbol, alphaVantageKey, liveUsdInrRate)
        if (avQuote != null) {
          return@withContext Result.success(avQuote)
        }
      } catch (_: Exception) {}
    }

    // 3. Fallback to Yahoo Finance web quote
    val candidateSymbols = if (!symbol.contains(".") && !symbol.contains("-") && !symbol.startsWith("^")) {
      listOf("$symbol.NS", symbol)
    } else {
      listOf(symbol)
    }

    var lastException: Exception? = null
    for (cand in candidateSymbols) {
      try {
        val quote = queryYahooFinance(cand, liveUsdInrRate)
        if (quote != null) {
          return@withContext Result.success(quote)
        }
      } catch (e: Exception) {
        lastException = e
      }
    }

    // 4. Catalog Fallback (convert to INR if foreign)
    val fallback = StockDatabaseCatalog.getFallbackQuote(symbol)
    if (fallback != null) {
      val inrFallback = if (fallback.currency == "USD") {
        convertToInr(fallback, liveUsdInrRate, "Catalog Fallback")
      } else {
        fallback
      }
      return@withContext Result.success(inrFallback)
    }

    // 5. Mutual Fund Directory Check (always in INR)
    val mfMatch = MutualFundCatalog.getAllMutualFunds().firstOrNull {
      it.schemeCode.equals(symbol, ignoreCase = true) ||
        it.schemeName.contains(symbol, ignoreCase = true) ||
        symbol.contains(it.schemeCode, ignoreCase = true)
    }
    if (mfMatch != null) {
      val prevClose = if (mfMatch.changePercent != 0.0) {
        mfMatch.nav / (1.0 + (mfMatch.changePercent / 100.0))
      } else {
        mfMatch.nav
      }
      return@withContext Result.success(
        StockQuote(
          symbol = mfMatch.schemeCode,
          regularMarketPrice = mfMatch.nav,
          previousClose = prevClose,
          changeAmount = mfMatch.nav - prevClose,
          changePercent = mfMatch.changePercent,
          currency = "INR",
          timestamp = System.currentTimeMillis(),
          exchange = if (mfMatch.isNfo) "NFO" else "AMFI",
          displayName = mfMatch.schemeName,
          isRealTime = true,
          originalCurrency = "INR",
          originalPrice = mfMatch.nav,
          exchangeRateToInr = 1.0,
          dataSource = "AMFI / NFO"
        )
      )
    }

    Result.failure(lastException ?: Exception("Unable to fetch stock quote for $symbol"))
  }

  private fun queryFinnhubQuote(symbol: String, key: String, liveUsdInrRate: Double): StockQuote? {
    val cleanSym = symbol.trim().uppercase()
    val url = "https://finnhub.io/api/v1/quote?symbol=$cleanSym&token=${key.trim()}"
    val request = Request.Builder().url(url).build()

    client.newCall(request).execute().use { response ->
      if (!response.isSuccessful) return null
      val body = response.body?.string() ?: return null
      val json = JSONObject(body)
      val currentPrice = json.optDouble("c", 0.0)
      if (currentPrice <= 0.0) return null

      val previousClose = json.optDouble("pc", currentPrice)
      val changeAmount = json.optDouble("d", currentPrice - previousClose)
      val changePercent = json.optDouble("dp", if (previousClose > 0) (changeAmount / previousClose) * 100.0 else 0.0)
      val timestamp = json.optLong("t", System.currentTimeMillis() / 1000) * 1000

      val isIndianStock = cleanSym.endsWith(".NS") || cleanSym.endsWith(".BO") || cleanSym.endsWith(".BSE") || cleanSym.endsWith(".NSE")
      val currency = if (isIndianStock) "INR" else "USD"

      val quote = StockQuote(
        symbol = cleanSym,
        regularMarketPrice = currentPrice,
        previousClose = previousClose,
        changeAmount = changeAmount,
        changePercent = changePercent,
        currency = currency,
        timestamp = timestamp,
        exchange = if (isIndianStock) "NSE/BSE" else "US/Global",
        displayName = cleanSym,
        isRealTime = true,
        originalCurrency = currency,
        originalPrice = currentPrice,
        exchangeRateToInr = if (isIndianStock) 1.0 else liveUsdInrRate,
        dataSource = "Finnhub"
      )

      return if (currency != "INR") convertToInr(quote, liveUsdInrRate, "Finnhub") else quote
    }
  }

  private fun queryAlphaVantageQuote(symbol: String, key: String, liveUsdInrRate: Double): StockQuote? {
    val cleanSym = symbol.trim().uppercase()
    val url = "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=$cleanSym&apikey=${key.trim()}"
    val request = Request.Builder().url(url).build()

    client.newCall(request).execute().use { response ->
      if (!response.isSuccessful) return null
      val body = response.body?.string() ?: return null
      val json = JSONObject(body)
      val gq = json.optJSONObject("Global Quote") ?: return null

      val price = gq.optDouble("05. price", 0.0)
      if (price <= 0.0) return null

      val prevClose = gq.optDouble("08. previous close", price)
      val change = gq.optDouble("09. change", price - prevClose)
      val changePercentRaw = gq.optString("10. change percent", "0%").replace("%", "").trim()
      val changePercent = changePercentRaw.toDoubleOrNull() ?: 0.0

      val isIndianStock = cleanSym.endsWith(".BSE") || cleanSym.endsWith(".NSE") || cleanSym.endsWith(".NS")
      val currency = if (isIndianStock) "INR" else "USD"

      val quote = StockQuote(
        symbol = cleanSym,
        regularMarketPrice = price,
        previousClose = prevClose,
        changeAmount = change,
        changePercent = changePercent,
        currency = currency,
        timestamp = System.currentTimeMillis(),
        exchange = if (isIndianStock) "NSE/BSE" else "US/Global",
        displayName = cleanSym,
        isRealTime = true,
        originalCurrency = currency,
        originalPrice = price,
        exchangeRateToInr = if (isIndianStock) 1.0 else liveUsdInrRate,
        dataSource = "Alpha Vantage"
      )

      return if (currency != "INR") convertToInr(quote, liveUsdInrRate, "Alpha Vantage") else quote
    }
  }

  private fun queryYahooFinance(symbol: String, liveUsdInrRate: Double): StockQuote? {
    val url = "https://query1.finance.yahoo.com/v8/finance/chart/$symbol?interval=1d&range=1d"
    val request = Request.Builder()
      .url(url)
      .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36")
      .header("Accept", "application/json")
      .build()

    client.newCall(request).execute().use { response ->
      if (!response.isSuccessful) return null
      val responseBody = response.body?.string() ?: return null

      val json = JSONObject(responseBody)
      val chart = json.optJSONObject("chart") ?: return null
      val results = chart.optJSONArray("result") ?: return null
      if (results.length() == 0) return null

      val resultObj = results.getJSONObject(0)
      val meta = resultObj.optJSONObject("meta") ?: return null

      val regularMarketPrice = meta.optDouble("regularMarketPrice", 0.0)
      if (regularMarketPrice <= 0.0) return null

      val previousClose = meta.optDouble("chartPreviousClose", meta.optDouble("previousClose", regularMarketPrice))
      val changeAmount = regularMarketPrice - previousClose
      val changePercent = if (previousClose > 0) (changeAmount / previousClose) * 100.0 else 0.0
      val currency = meta.optString("currency", "INR")
      val exchangeName = meta.optString("exchangeName", "NSE")
      val shortName = meta.optString("shortName", symbol)

      val rawQuote = StockQuote(
        symbol = symbol,
        regularMarketPrice = regularMarketPrice,
        previousClose = previousClose,
        changeAmount = changeAmount,
        changePercent = changePercent,
        currency = currency,
        timestamp = System.currentTimeMillis(),
        exchange = exchangeName,
        displayName = shortName,
        isRealTime = true,
        originalCurrency = currency,
        originalPrice = regularMarketPrice,
        exchangeRateToInr = if (currency == "INR") 1.0 else liveUsdInrRate,
        dataSource = "Web Market"
      )

      return if (currency != "INR") convertToInr(rawQuote, liveUsdInrRate, "Web Market") else rawQuote
    }
  }

  private fun convertToInr(quote: StockQuote, rate: Double, source: String): StockQuote {
    val convertedPrice = quote.regularMarketPrice * rate
    val convertedPrevClose = quote.previousClose * rate
    val convertedChange = quote.changeAmount * rate
    return quote.copy(
      regularMarketPrice = convertedPrice,
      previousClose = convertedPrevClose,
      changeAmount = convertedChange,
      currency = "INR", // Primary currency is ALWAYS INR as requested
      originalCurrency = quote.currency,
      originalPrice = quote.regularMarketPrice,
      exchangeRateToInr = rate,
      dataSource = source
    )
  }

  private fun queryAlphaVantageForex(key: String): Double? {
    val url = "https://www.alphavantage.co/query?function=CURRENCY_EXCHANGE_RATE&from_currency=USD&to_currency=INR&apikey=${key.trim()}"
    val request = Request.Builder().url(url).build()
    client.newCall(request).execute().use { response ->
      if (!response.isSuccessful) return null
      val json = JSONObject(response.body?.string() ?: return null)
      val rateObj = json.optJSONObject("Realtime Currency Exchange Rate") ?: return null
      val rate = rateObj.optDouble("5. Exchange Rate", 0.0)
      return if (rate > 0.0) rate else null
    }
  }

  private fun queryFinnhubForex(key: String): Double? {
    val url = "https://finnhub.io/api/v1/forex/rates?base=USD&token=${key.trim()}"
    val request = Request.Builder().url(url).build()
    client.newCall(request).execute().use { response ->
      if (!response.isSuccessful) return null
      val json = JSONObject(response.body?.string() ?: return null)
      val quoteObj = json.optJSONObject("quote") ?: return null
      val rate = quoteObj.optDouble("INR", 0.0)
      return if (rate > 0.0) rate else null
    }
  }

  private fun queryYahooForex(): Double? {
    val url = "https://query1.finance.yahoo.com/v8/finance/chart/USDINR=X?interval=1d&range=1d"
    val request = Request.Builder()
      .url(url)
      .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36")
      .build()
    client.newCall(request).execute().use { response ->
      if (!response.isSuccessful) return null
      val json = JSONObject(response.body?.string() ?: return null)
      val result = json.optJSONObject("chart")?.optJSONArray("result")?.optJSONObject(0) ?: return null
      val meta = result.optJSONObject("meta") ?: return null
      val price = meta.optDouble("regularMarketPrice", 0.0)
      return if (price > 0.0) price else null
    }
  }

  /**
   * Concurrently fetch quotes for multiple symbols with throttling and API keys.
   */
  suspend fun fetchBatchQuotes(
    symbols: List<String>,
    finnhubKey: String = "",
    alphaVantageKey: String = ""
  ): Map<String, StockQuote> = coroutineScope {
    symbols.distinct().map { sym ->
      async(Dispatchers.IO) {
        val result = fetchStockQuote(sym, finnhubKey, alphaVantageKey)
        sym to result.getOrNull()
      }
    }.awaitAll()
      .mapNotNull { (sym, quote) -> if (quote != null) sym to quote else null }
      .toMap()
  }
}
