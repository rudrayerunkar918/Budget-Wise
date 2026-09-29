package com.example.data.api

enum class IpoStatus {
  UPCOMING,
  OPEN,
  ALLOTMENT_FINALIZED,
  LISTED
}

data class DetailedIpoItem(
  val symbol: String, // e.g. "ATHER.NS"
  val displaySymbol: String, // e.g. "ATHER"
  val companyName: String, // e.g. "Ather Energy Limited"
  val sector: String, // e.g. "Electric Two-Wheelers & Smart EV"
  val openDate: String, // e.g. "14 Oct 2025"
  val closeDate: String, // e.g. "17 Oct 2025"
  val listingDate: String, // e.g. "24 Oct 2025"
  val priceBand: String, // e.g. "₹350 - ₹380"
  val issuePrice: Double, // e.g. 380.0
  val lotSize: Int, // e.g. 39
  val issueSize: String, // e.g. "₹3,100 Cr"
  val status: IpoStatus,
  val isListed: Boolean = false,
  val exchange: String = "NSE",
  val geminiRecommendation: String, // "SUBSCRIBE for Listing Gains", "SUBSCRIBE for Long Term", "NEUTRAL", "AVOID"
  val geminiConfidence: Int, // 85%
  val geminiAnalysisThesis: String,
  val geminiKeyPros: List<String> = emptyList(),
  val geminiKeyRisks: List<String> = emptyList(),
  val subscriptionGmp: String = "N/A", // Grey Market Premium
  val lastUpdatedDate: String = "Today"
)
