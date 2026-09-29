package com.example.data.api

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FinnhubApiServiceTest {

  private val service = FinnhubApiService()

  @Test
  fun testNormalizeSymbol() {
    assertEquals("AAPL", service.normalizeSymbol("aapl"))
    assertEquals("AAPL", service.normalizeSymbol("AAPL.US"))
    assertEquals("NVDA", service.normalizeSymbol("nvda.us"))
    assertEquals("MSFT", service.normalizeSymbol(" MSFT "))
    assertEquals("TCS.NS", service.normalizeSymbol("TCS.NS"))
  }

  @Test
  fun testQuoteJsonParsing() {
    val sampleJson = """
      {
        "c": 224.23,
        "d": 3.45,
        "dp": 1.56,
        "h": 226.00,
        "l": 221.50,
        "o": 222.10,
        "pc": 220.78,
        "t": 1727400000
      }
    """.trimIndent()

    val json = JSONObject(sampleJson)
    val quote = FinnhubQuote(
      symbol = "AAPL",
      currentPrice = json.getDouble("c"),
      changeAmount = json.getDouble("d"),
      changePercent = json.getDouble("dp"),
      highPrice = json.getDouble("h"),
      lowPrice = json.getDouble("l"),
      openPrice = json.getDouble("o"),
      previousClose = json.getDouble("pc"),
      timestamp = json.getLong("t") * 1000L
    )

    assertEquals("AAPL", quote.symbol)
    assertEquals(224.23, quote.currentPrice, 0.001)
    assertEquals(3.45, quote.changeAmount, 0.001)
    assertEquals(1.56, quote.changePercent, 0.001)
    assertEquals(226.00, quote.highPrice, 0.001)
    assertEquals(221.50, quote.lowPrice, 0.001)
    assertEquals(222.10, quote.openPrice, 0.001)
    assertEquals(220.78, quote.previousClose, 0.001)
  }

  @Test
  fun testCompanyProfileJsonParsing() {
    val sampleJson = """
      {
        "country": "US",
        "currency": "USD",
        "exchange": "NASDAQ",
        "finnhubIndustry": "Technology",
        "ipo": "1980-12-12",
        "logo": "https://static2.finnhub.io/logo.png",
        "marketCapitalization": 3450000.5,
        "name": "Apple Inc",
        "ticker": "AAPL",
        "weburl": "https://www.apple.com/"
      }
    """.trimIndent()

    val json = JSONObject(sampleJson)
    val profile = FinnhubCompanyProfile(
      ticker = json.getString("ticker"),
      name = json.getString("name"),
      country = json.optString("country"),
      currency = json.optString("currency"),
      exchange = json.optString("exchange"),
      finnhubIndustry = json.optString("finnhubIndustry"),
      ipo = json.optString("ipo"),
      logo = json.optString("logo"),
      marketCapitalization = json.optDouble("marketCapitalization"),
      weburl = json.optString("weburl")
    )

    assertEquals("AAPL", profile.ticker)
    assertEquals("Apple Inc", profile.name)
    assertEquals("Technology", profile.finnhubIndustry)
    assertEquals("NASDAQ", profile.exchange)
    assertEquals("https://www.apple.com/", profile.weburl)
    assertEquals(3450000.5, profile.marketCapitalization, 0.1)
  }

  @Test
  fun testApiKeyPrecedence() {
    val customKey = "custom_test_token_123"
    assertEquals("custom_test_token_123", service.resolveApiKey(customKey))

    // Blank or placeholder key should fallback or return empty if no BuildConfig key
    val emptyResult = service.resolveApiKey("")
    assertNotNull(emptyResult)
  }
}
