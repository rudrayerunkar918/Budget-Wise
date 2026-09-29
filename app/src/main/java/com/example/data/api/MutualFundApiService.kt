package com.example.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class MutualFundNavQuote(
  val schemeCode: String,
  val schemeName: String,
  val nav: Double,
  val previousNav: Double,
  val changeAmount: Double,
  val changePercent: Double,
  val date: String,
  val isRealTime: Boolean = true
)

class MutualFundApiService {

  private val client: OkHttpClient get() = AppHttpClient.client

  /**
   * Fetches latest NAV for Indian mutual funds from official/AMFI API (api.mfapi.in).
   * Supports scheme codes (e.g. 122639) or fallback catalog matching.
   */
  suspend fun fetchNav(schemeCodeOrSymbol: String): Result<MutualFundNavQuote> = withContext(Dispatchers.IO) {
    val clean = schemeCodeOrSymbol.trim()
    if (clean.isBlank()) {
      return@withContext Result.failure(IllegalArgumentException("Scheme code cannot be blank"))
    }

    // Check if it's an NFO or custom symbol
    if (clean.startsWith("NFO", ignoreCase = true)) {
      val fallback = MutualFundCatalog.getFallbackNav(clean) ?: 10.00
      return@withContext Result.success(
        MutualFundNavQuote(
          schemeCode = clean,
          schemeName = clean,
          nav = fallback,
          previousNav = 10.00,
          changeAmount = 0.0,
          changePercent = 0.0,
          date = "NFO Issue Price (₹10.00)",
          isRealTime = false
        )
      )
    }

    // Extract numeric scheme code if present
    val numericCode = clean.filter { it.isDigit() }
    val targetCode = if (numericCode.isNotBlank()) numericCode else {
      // Find code from catalog
      MutualFundCatalog.getAllMutualFunds().firstOrNull {
        it.schemeCode.equals(clean, ignoreCase = true) ||
          it.schemeName.contains(clean, ignoreCase = true)
      }?.schemeCode ?: clean
    }

    if (targetCode.all { it.isDigit() } && targetCode.isNotBlank()) {
      try {
        val url = "https://api.mfapi.in/mf/$targetCode"
        val request = Request.Builder()
          .url(url)
          .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) BudgetWise/2.0")
          .header("Accept", "application/json")
          .build()

        client.newCall(request).execute().use { response ->
          if (response.isSuccessful) {
            val body = response.body?.string()
            if (!body.isNullOrBlank()) {
              val json = JSONObject(body)
              val meta = json.optJSONObject("meta")
              val schemeName = meta?.optString("scheme_name", clean) ?: clean
              val dataArray = json.optJSONArray("data")
              if (dataArray != null && dataArray.length() > 0) {
                val latest = dataArray.getJSONObject(0)
                val nav = latest.optDouble("nav", 0.0)
                val date = latest.optString("date", "")
                var prevNav = nav
                if (dataArray.length() > 1) {
                  prevNav = dataArray.getJSONObject(1).optDouble("nav", nav)
                }
                val changeAmount = nav - prevNav
                val changePercent = if (prevNav > 0) (changeAmount / prevNav) * 100.0 else 0.0

                if (nav > 0) {
                  return@withContext Result.success(
                    MutualFundNavQuote(
                      schemeCode = targetCode,
                      schemeName = schemeName,
                      nav = nav,
                      previousNav = prevNav,
                      changeAmount = changeAmount,
                      changePercent = changePercent,
                      date = date,
                      isRealTime = true
                    )
                  )
                }
              }
            }
          }
        }
      } catch (_: Exception) {
        // Fallback below
      }
    }

    // Fallback from built-in directory
    val catalogMatch = MutualFundCatalog.getAllMutualFunds().firstOrNull {
      it.schemeCode.equals(clean, ignoreCase = true) ||
        it.schemeName.contains(clean, ignoreCase = true)
    }

    if (catalogMatch != null) {
      return@withContext Result.success(
        MutualFundNavQuote(
          schemeCode = catalogMatch.schemeCode,
          schemeName = catalogMatch.schemeName,
          nav = catalogMatch.nav,
          previousNav = catalogMatch.nav / (1.0 + (catalogMatch.changePercent / 100.0)),
          changeAmount = catalogMatch.nav * (catalogMatch.changePercent / 100.0),
          changePercent = catalogMatch.changePercent,
          date = "Directory NAV",
          isRealTime = false
        )
      )
    }

    Result.failure(Exception("Unable to fetch Mutual Fund NAV for $clean"))
  }

  /**
   * Searches the entire live AMFI registry across all 10,000+ mutual fund schemes in India.
   * Caches results into MutualFundCatalog dynamically for offline and catalog lookups.
   */
  suspend fun searchAllIndiaFunds(query: String): List<MutualFundItem> = withContext(Dispatchers.IO) {
    val q = query.trim()
    if (q.isBlank()) return@withContext emptyList()

    try {
      val encoded = java.net.URLEncoder.encode(q, "UTF-8")
      val url = "https://api.mfapi.in/mf/search?q=$encoded"
      val request = Request.Builder()
        .url(url)
        .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) BudgetWise/2.0")
        .header("Accept", "application/json")
        .build()

      client.newCall(request).execute().use { response ->
        if (response.isSuccessful) {
          val body = response.body?.string()
          if (!body.isNullOrBlank()) {
            val jsonArray = org.json.JSONArray(body)
            val results = mutableListOf<MutualFundItem>()
            val maxCount = minOf(jsonArray.length(), 25)
            for (i in 0 until maxCount) {
              val obj = jsonArray.getJSONObject(i)
              val code = obj.optString("schemeCode", "")
              val name = obj.optString("schemeName", "")
              if (code.isNotBlank() && name.isNotBlank()) {
                val amc = inferAmcFromName(name)
                val category = inferCategoryFromName(name)
                results.add(
                  MutualFundItem(
                    schemeCode = code,
                    schemeName = name,
                    amc = amc,
                    category = category,
                    nav = 50.0,
                    changePercent = 0.0,
                    risk = if (category in listOf("SMALL_CAP", "MID_CAP", "SECTORAL")) "Very High" else "High",
                    minSipAmount = 500.0,
                    isNfo = false,
                    description = "Official AMFI Scheme #$code under $amc ($category)"
                  )
                )
              }
            }
            if (results.isNotEmpty()) {
              MutualFundCatalog.addDynamicFunds(results)
              return@withContext results
            }
          }
        }
      }
    } catch (_: Exception) {
      // Fall back to local directory
    }

    MutualFundCatalog.searchMutualFunds(q, "ALL")
  }

  private fun inferAmcFromName(name: String): String {
    val lower = name.lowercase()
    return when {
      lower.contains("sbi") -> "SBI Mutual Fund"
      lower.contains("hdfc") -> "HDFC Asset Management"
      lower.contains("icici") -> "ICICI Prudential AMC"
      lower.contains("nippon") -> "Nippon Life India AM"
      lower.contains("kotak") -> "Kotak Mahindra AMC"
      lower.contains("axis") -> "Axis Mutual Fund"
      lower.contains("aditya birla") || lower.contains("absl") -> "Aditya Birla Sun Life AMC"
      lower.contains("uti") -> "UTI Mutual Fund"
      lower.contains("mirae") -> "Mirae Asset Mutual Fund"
      lower.contains("tata") -> "Tata Asset Management"
      lower.contains("dsp") -> "DSP Investment Managers"
      lower.contains("motilal") -> "Motilal Oswal AMC"
      lower.contains("quant") -> "Quant Mutual Fund"
      lower.contains("bandhan") -> "Bandhan Mutual Fund"
      lower.contains("canara") -> "Canara Robeco AMC"
      lower.contains("edelweiss") -> "Edelweiss Mutual Fund"
      lower.contains("sundaram") -> "Sundaram Mutual Fund"
      lower.contains("invesco") -> "Invesco Mutual Fund"
      lower.contains("franklin") -> "Franklin Templeton"
      lower.contains("parag parikh") || lower.contains("ppfas") -> "PPFAS Mutual Fund"
      lower.contains("whiteoak") -> "WhiteOak Capital AMC"
      lower.contains("groww") -> "Groww Mutual Fund"
      lower.contains("navi") -> "Navi Mutual Fund"
      lower.contains("zerodha") -> "Zerodha Fund House"
      lower.contains("mahindra") -> "Mahindra Manulife AMC"
      lower.contains("pgim") -> "PGIM India Mutual Fund"
      lower.contains("bank of india") || lower.contains("boi") -> "Bank of India Mutual Fund"
      lower.contains("baroda") -> "Baroda BNP Paribas AMC"
      lower.contains("union") -> "Union Mutual Fund"
      lower.contains("lic") -> "LIC Mutual Fund"
      lower.contains("jm") -> "JM Financial Mutual Fund"
      lower.contains("samco") -> "Samco Mutual Fund"
      lower.contains("quantum") -> "Quantum Mutual Fund"
      lower.contains("taurus") -> "Taurus Mutual Fund"
      lower.contains("iti") -> "ITI Mutual Fund"
      lower.contains("trust") -> "Trust Mutual Fund"
      lower.contains("shriram") -> "Shriram Mutual Fund"
      lower.contains("360 one") -> "360 ONE Mutual Fund"
      lower.contains("helios") -> "Helios Mutual Fund"
      lower.contains("bajaj") -> "Bajaj Finserv AMC"
      else -> name.split(" ").take(2).joinToString(" ") + " AMC"
    }
  }

  private fun inferCategoryFromName(name: String): String {
    val lower = name.lowercase()
    return when {
      lower.contains("small cap") || lower.contains("smallcap") -> "SMALL_CAP"
      lower.contains("mid cap") || lower.contains("midcap") -> "MID_CAP"
      lower.contains("large cap") || lower.contains("bluechip") || lower.contains("top 100") -> "LARGE_CAP"
      lower.contains("flexi cap") || lower.contains("flexicap") || lower.contains("multi cap") || lower.contains("multicap") -> "FLEXI_CAP"
      lower.contains("index") || lower.contains("nifty") || lower.contains("sensex") || lower.contains("nasdaq") || lower.contains("etf") -> "INDEX"
      lower.contains("elss") || lower.contains("tax saver") || lower.contains("tax saving") -> "ELSS"
      lower.contains("hybrid") || lower.contains("balanced advantage") || lower.contains("arbitrage") || lower.contains("multi asset") -> "HYBRID"
      lower.contains("liquid") || lower.contains("debt") || lower.contains("bond") || lower.contains("gilt") || lower.contains("money market") || lower.contains("overnight") -> "DEBT"
      lower.contains("pharma") || lower.contains("health") || lower.contains("tech") || lower.contains("infra") || lower.contains("defence") || lower.contains("manufacturing") || lower.contains("auto") || lower.contains("banking") || lower.contains("momentum") || lower.contains("thematic") -> "SECTORAL"
      else -> "EQUITY"
    }
  }
}
