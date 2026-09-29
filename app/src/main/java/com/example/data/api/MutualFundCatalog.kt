package com.example.data.api

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class MutualFundItem(
  val schemeCode: String,
  val schemeName: String,
  val amc: String,
  val category: String, // "FLEXI_CAP", "LARGE_CAP", "MID_CAP", "SMALL_CAP", "ELSS", "INDEX", "HYBRID", "DEBT", "NFO"
  val nav: Double,
  val changePercent: Double,
  val risk: String = "Very High",
  val minSipAmount: Double = 500.0,
  val isNfo: Boolean = false,
  val nfoCloseDate: String? = null,
  val description: String = ""
)

object MutualFundCatalog {

  private val dynamicNfos = mutableListOf<MutualFundItem>()
  private val dynamicImportedFunds = mutableListOf<MutualFundItem>()

  // 1. Active & Upcoming New Fund Offers (NFOs)
  val NFO_CATALOG: List<MutualFundItem> = listOf(
    MutualFundItem(
      schemeCode = "152180",
      schemeName = "Motilal Oswal Active Momentum Fund - Direct Plan - Growth",
      amc = "Motilal Oswal AMC",
      category = "THEMATIC",
      nav = 10.00,
      changePercent = 0.0,
      risk = "Very High",
      minSipAmount = 500.0,
      isNfo = true,
      nfoCloseDate = "Open for Subscription",
      description = "High-conviction active momentum equity strategy selecting leading outperforming stocks with strong price and earnings momentum."
    ),
    MutualFundItem(
      schemeCode = "NFO-HDFC-DEF",
      schemeName = "HDFC Defence Fund - Direct Plan - Growth",
      amc = "HDFC Asset Management",
      category = "NFO",
      nav = 10.00,
      changePercent = 0.0,
      risk = "Very High",
      minSipAmount = 500.0,
      isNfo = true,
      nfoCloseDate = "Open for Subscription",
      description = "Thematic equity fund investing in Indian defence and aerospace companies."
    ),
    MutualFundItem(
      schemeCode = "NFO-SBI-500",
      schemeName = "SBI Nifty 500 Index Fund - Direct Plan - Growth",
      amc = "SBI Mutual Fund",
      category = "NFO",
      nav = 10.00,
      changePercent = 0.0,
      risk = "Very High",
      minSipAmount = 500.0,
      isNfo = true,
      nfoCloseDate = "Open for Subscription",
      description = "Broad market passive index fund tracking total returns of Nifty 500."
    ),
    MutualFundItem(
      schemeCode = "NFO-NIP-MULTI",
      schemeName = "Nippon India Multi Asset Allocation Fund",
      amc = "Nippon Life India AM",
      category = "NFO",
      nav = 10.00,
      changePercent = 0.0,
      risk = "High",
      minSipAmount = 500.0,
      isNfo = true,
      nfoCloseDate = "Open for Subscription",
      description = "Dynamic multi-asset allocation investing across Equity, Debt, and Gold/Commodities."
    ),
    MutualFundItem(
      schemeCode = "NFO-ICICI-INNOV",
      schemeName = "ICICI Prudential Innovation Fund - Direct Growth",
      amc = "ICICI Prudential AMC",
      category = "NFO",
      nav = 10.00,
      changePercent = 0.0,
      risk = "Very High",
      minSipAmount = 1000.0,
      isNfo = true,
      nfoCloseDate = "Open for Subscription",
      description = "Theme focused on disruptive technologies, artificial intelligence, and R&D leaders."
    ),
    MutualFundItem(
      schemeCode = "NFO-TATA-INNOV",
      schemeName = "Tata India Innovation Fund - Direct Plan - Growth",
      amc = "Tata Asset Management",
      category = "NFO",
      nav = 10.00,
      changePercent = 0.0,
      risk = "Very High",
      minSipAmount = 500.0,
      isNfo = true,
      nfoCloseDate = "Open for Subscription",
      description = "Capital appreciation via equities positioned at the forefront of digital transformation."
    ),
    MutualFundItem(
      schemeCode = "NFO-QUANT-COMM",
      schemeName = "Quant Commodities Fund - Direct Plan - Growth",
      amc = "Quant Mutual Fund",
      category = "NFO",
      nav = 10.00,
      changePercent = 0.0,
      risk = "Very High",
      minSipAmount = 1000.0,
      isNfo = true,
      nfoCloseDate = "Open for Subscription",
      description = "Dynamic quantitative fund capitalizing on global commodity and energy supercycles."
    ),
    MutualFundItem(
      schemeCode = "NFO-MIRAE-EV",
      schemeName = "Mirae Asset EV and New Age Mobility ETF FoF",
      amc = "Mirae Asset Investment",
      category = "NFO",
      nav = 10.00,
      changePercent = 0.0,
      risk = "Very High",
      minSipAmount = 500.0,
      isNfo = true,
      nfoCloseDate = "Open for Subscription",
      description = "Exposure to electric vehicles, lithium battery producers, and green transportation."
    ),
    MutualFundItem(
      schemeCode = "NFO-MO-DIGI",
      schemeName = "Motilal Oswal Digital India Fund - Direct Growth",
      amc = "Motilal Oswal AMC",
      category = "NFO",
      nav = 10.00,
      changePercent = 0.0,
      risk = "Very High",
      minSipAmount = 500.0,
      isNfo = true,
      nfoCloseDate = "Open for Subscription",
      description = "Investing in technology software, e-commerce, fintech, and semiconductor ecosystems."
    ),
    MutualFundItem(
      schemeCode = "NFO-KOTAK-SPEC",
      schemeName = "Kotak Special Opportunities Fund - Direct Growth",
      amc = "Kotak Mahindra AMC",
      category = "NFO",
      nav = 10.00,
      changePercent = 0.0,
      risk = "Very High",
      minSipAmount = 500.0,
      isNfo = true,
      nfoCloseDate = "Open for Subscription",
      description = "Bottom-up equity fund investing in special situations, corporate restructuring, and spin-offs."
    ),
    MutualFundItem(
      schemeCode = "NFO-AXIS-MFG",
      schemeName = "Axis India Manufacturing Fund - Direct Growth",
      amc = "Axis Asset Management",
      category = "NFO",
      nav = 10.00,
      changePercent = 0.0,
      risk = "Very High",
      minSipAmount = 500.0,
      isNfo = true,
      nfoCloseDate = "Open for Subscription",
      description = "Capitalizing on Make-in-India, PLI schemes, industrial capex, and export revival."
    ),
    MutualFundItem(
      schemeCode = "NFO-BANDHAN-CYCLE",
      schemeName = "Bandhan Business Cycle Fund - Direct Growth",
      amc = "Bandhan AMC",
      category = "NFO",
      nav = 10.00,
      changePercent = 0.0,
      risk = "Very High",
      minSipAmount = 500.0,
      isNfo = true,
      nfoCloseDate = "Open for Subscription",
      description = "Top-down macro strategy identifying economic recovery and expansion cycles."
    ),
    MutualFundItem(
      schemeCode = "NFO-GROWW-TOTAL",
      schemeName = "Groww Nifty Total Market Index Fund",
      amc = "Groww Mutual Fund",
      category = "NFO",
      nav = 10.00,
      changePercent = 0.0,
      risk = "Very High",
      minSipAmount = 100.0,
      isNfo = true,
      nfoCloseDate = "Open for Subscription",
      description = "Low-cost passive tracking of 750 Indian listed equities across all market capitalizations."
    ),
    MutualFundItem(
      schemeCode = "NFO-DSP-HEALTH",
      schemeName = "DSP Healthcare Fund - Direct Plan - Growth",
      amc = "DSP Mutual Fund",
      category = "NFO",
      nav = 10.00,
      changePercent = 0.0,
      risk = "Very High",
      minSipAmount = 500.0,
      isNfo = true,
      nfoCloseDate = "Open for Subscription",
      description = "Focused on pharma formulations, hospitals, medical diagnostic labs, and CDMO firms."
    ),
    MutualFundItem(
      schemeCode = "NFO-WHITEOAK-LM",
      schemeName = "WhiteOak Capital Large & Mid Cap Fund",
      amc = "WhiteOak Capital AMC",
      category = "NFO",
      nav = 10.00,
      changePercent = 0.0,
      risk = "Very High",
      minSipAmount = 500.0,
      isNfo = true,
      nfoCloseDate = "Open for Subscription",
      description = "Balanced high-quality franchise approach investing in large and mid-sized leaders."
    ),
    MutualFundItem(
      schemeCode = "NFO-EDEL-TECH",
      schemeName = "Edelweiss Technology Fund - Direct Growth",
      amc = "Edelweiss AMC",
      category = "NFO",
      nav = 10.00,
      changePercent = 0.0,
      risk = "Very High",
      minSipAmount = 500.0,
      isNfo = true,
      nfoCloseDate = "Open for Subscription",
      description = "Invests across domestic IT service giants and global tech pioneers."
    )
  )

  // 2. Comprehensive Direct Growth Mutual Funds
  val DIRECT_MF_CATALOG: List<MutualFundItem> = listOf(
    // --- Flexi Cap & Multi Cap ---
    MutualFundItem("122639", "Parag Parikh Flexi Cap Fund - Direct Plan - Growth", "PPFAS Mutual Fund", "FLEXI_CAP", 78.42, 0.64, "Very High", 1000.0),
    MutualFundItem("120503", "HDFC Flexi Cap Fund - Direct Plan - Growth", "HDFC Asset Management", "FLEXI_CAP", 1980.50, 0.72, "Very High", 500.0),
    MutualFundItem("125354", "Quant Active Fund - Direct Plan - Growth", "Quant Mutual Fund", "FLEXI_CAP", 685.20, 1.15, "Very High", 1000.0),
    MutualFundItem("118989", "Kotak Flexicap Fund - Direct Plan - Growth", "Kotak Mahindra AMC", "FLEXI_CAP", 94.60, 0.58, "Very High", 500.0),
    MutualFundItem("120716", "UTI Flexi Cap Fund - Direct Plan - Growth", "UTI Mutual Fund", "FLEXI_CAP", 312.40, 0.49, "Very High", 500.0),
    MutualFundItem("119598", "Canara Robeco Flexi Cap Fund - Direct Plan - Growth", "Canara Robeco", "FLEXI_CAP", 320.10, 0.61, "Very High", 500.0),
    MutualFundItem("120828", "SBI Flexicap Fund - Direct Plan - Growth", "SBI Mutual Fund", "FLEXI_CAP", 114.80, 0.52, "Very High", 500.0),
    MutualFundItem("120365", "ICICI Prudential Flexicap Fund - Direct Growth", "ICICI Prudential AMC", "FLEXI_CAP", 18.25, 0.55, "Very High", 500.0),
    MutualFundItem("119793", "Axis Flexi Cap Fund - Direct Plan - Growth", "Axis Asset Management", "FLEXI_CAP", 28.90, 0.45, "Very High", 500.0),
    MutualFundItem("145328", "Nippon India Multi Cap Fund - Direct Plan - Growth", "Nippon Life India AM", "FLEXI_CAP", 342.10, 0.85, "Very High", 500.0),
    MutualFundItem("127041", "Motilal Oswal Flexi Cap Fund - Direct Plan - Growth", "Motilal Oswal AMC", "FLEXI_CAP", 56.40, 0.78, "Very High", 500.0),
    MutualFundItem("152180", "Motilal Oswal Active Momentum Fund - Direct Plan - Growth", "Motilal Oswal AMC", "THEMATIC", 10.00, 0.0, "Very High", 500.0, description = "Active momentum strategy investing in stocks exhibiting strong relative price strength and positive momentum."),
    MutualFundItem("127042", "Motilal Oswal Midcap Fund - Direct Plan - Growth", "Motilal Oswal AMC", "MID_CAP", 94.20, 0.85, "Very High", 500.0),
    MutualFundItem("151120", "Motilal Oswal Small Cap Fund - Direct Plan - Growth", "Motilal Oswal AMC", "SMALL_CAP", 18.50, 0.95, "Very High", 500.0),
    MutualFundItem("118775", "Franklin India Flexi Cap Fund - Direct Plan - Growth", "Franklin Templeton", "FLEXI_CAP", 1540.20, 0.74, "Very High", 500.0),
    MutualFundItem("119224", "DSP Flexi Cap Fund - Direct Plan - Growth", "DSP Mutual Fund", "FLEXI_CAP", 104.50, 0.62, "Very High", 500.0),
    MutualFundItem("148651", "Edelweiss Flexi Cap Fund - Direct Plan - Growth", "Edelweiss AMC", "FLEXI_CAP", 38.60, 0.65, "Very High", 500.0),
    MutualFundItem("125360", "Tata Flexi Cap Fund - Direct Plan - Growth", "Tata Asset Management", "FLEXI_CAP", 22.80, 0.70, "Very High", 500.0),
    MutualFundItem("147945", "Bandhan Multi Cap Fund - Direct Plan - Growth", "Bandhan AMC", "FLEXI_CAP", 21.30, 0.82, "Very High", 500.0),
    MutualFundItem("149205", "Mahindra Manulife Multi Cap Fund - Direct Growth", "Mahindra Manulife", "FLEXI_CAP", 34.20, 0.88, "Very High", 500.0),
    MutualFundItem("150341", "WhiteOak Capital Flexi Cap Fund - Direct Growth", "WhiteOak Capital AMC", "FLEXI_CAP", 18.40, 0.92, "Very High", 500.0),
    MutualFundItem("146142", "Baroda BNP Paribas Multi Cap Fund - Direct Growth", "Baroda BNP Paribas", "FLEXI_CAP", 245.60, 0.76, "Very High", 500.0),
    MutualFundItem("118992", "Sundaram Multi Cap Fund - Direct Plan - Growth", "Sundaram Mutual Fund", "FLEXI_CAP", 312.40, 0.80, "Very High", 500.0),

    // --- Small Cap Funds ---
    MutualFundItem("125497", "Quant Small Cap Fund - Direct Plan - Growth", "Quant Mutual Fund", "SMALL_CAP", 262.15, 1.28, "Very High", 1000.0),
    MutualFundItem("118778", "Nippon India Small Cap Fund - Direct Plan - Growth", "Nippon Life India AM", "SMALL_CAP", 172.80, 1.12, "Very High", 500.0),
    MutualFundItem("120505", "HDFC Small Cap Fund - Direct Plan - Growth", "HDFC Asset Management", "SMALL_CAP", 145.60, 0.95, "Very High", 500.0),
    MutualFundItem("125354", "Tata Small Cap Fund - Direct Plan - Growth", "Tata Asset Management", "SMALL_CAP", 42.10, 1.05, "Very High", 500.0),
    MutualFundItem("120847", "SBI Small Cap Fund - Direct Plan - Growth", "SBI Mutual Fund", "SMALL_CAP", 168.40, 0.82, "Very High", 500.0),
    MutualFundItem("120377", "Axis Small Cap Fund - Direct Plan - Growth", "Axis Asset Management", "SMALL_CAP", 104.20, 0.88, "Very High", 500.0),
    MutualFundItem("145881", "Kotak Small Cap Fund - Direct Plan - Growth", "Kotak Mahindra AMC", "SMALL_CAP", 258.10, 0.91, "Very High", 500.0),
    MutualFundItem("147944", "Bandhan Small Cap Fund - Direct Plan - Growth", "Bandhan AMC", "SMALL_CAP", 38.50, 1.10, "Very High", 500.0),
    MutualFundItem("146141", "Franklin India Smaller Companies Fund - Direct - Growth", "Franklin Templeton", "SMALL_CAP", 162.30, 1.02, "Very High", 500.0),
    MutualFundItem("149204", "Mahindra Manulife Small Cap Fund - Direct Growth", "Mahindra Manulife", "SMALL_CAP", 24.80, 1.15, "Very High", 500.0),
    MutualFundItem("147412", "Invesco India Smallcap Fund - Direct Plan - Growth", "Invesco Mutual Fund", "SMALL_CAP", 39.40, 0.94, "Very High", 500.0),
    MutualFundItem("147253", "Canara Robeco Small Cap Fund - Direct Plan - Growth", "Canara Robeco", "SMALL_CAP", 41.20, 0.98, "Very High", 500.0),
    MutualFundItem("119226", "DSP Small Cap Fund - Direct Plan - Growth", "DSP Mutual Fund", "SMALL_CAP", 178.60, 0.96, "Very High", 500.0),
    MutualFundItem("148652", "Union Small Cap Fund - Direct Plan - Growth", "Union Mutual Fund", "SMALL_CAP", 48.20, 1.04, "Very High", 500.0),
    MutualFundItem("145332", "HSBC Small Cap Fund - Direct Plan - Growth", "HSBC Mutual Fund", "SMALL_CAP", 84.50, 0.95, "Very High", 500.0),
    MutualFundItem("151201", "Motilal Oswal Small Cap Fund - Direct Plan - Growth", "Motilal Oswal AMC", "SMALL_CAP", 14.80, 1.12, "Very High", 500.0),
    MutualFundItem("148653", "Edelweiss Small Cap Fund - Direct Plan - Growth", "Edelweiss AMC", "SMALL_CAP", 42.10, 1.08, "Very High", 500.0),
    MutualFundItem("149801", "ITI Small Cap Fund - Direct Plan - Growth", "ITI Mutual Fund", "SMALL_CAP", 28.60, 1.20, "Very High", 500.0),
    MutualFundItem("118993", "Sundaram Small Cap Fund - Direct Plan - Growth", "Sundaram Mutual Fund", "SMALL_CAP", 245.10, 1.05, "Very High", 500.0),
    MutualFundItem("150342", "PGIM India Small Cap Fund - Direct Plan - Growth", "PGIM India MF", "SMALL_CAP", 32.40, 1.10, "Very High", 500.0),

    // --- Mid Cap Funds ---
    MutualFundItem("120504", "HDFC Mid-Cap Opportunities Fund - Direct Plan - Growth", "HDFC Asset Management", "MID_CAP", 184.50, 0.78, "Very High", 500.0),
    MutualFundItem("127042", "Motilal Oswal Midcap Fund - Direct Plan - Growth", "Motilal Oswal AMC", "MID_CAP", 118.20, 0.94, "Very High", 500.0),
    MutualFundItem("118989", "Kotak Emerging Equity Fund - Direct Plan - Growth", "Kotak Mahindra AMC", "MID_CAP", 124.60, 0.85, "Very High", 500.0),
    MutualFundItem("125357", "Quant Mid Cap Fund - Direct Plan - Growth", "Quant Mutual Fund", "MID_CAP", 235.40, 1.18, "Very High", 1000.0),
    MutualFundItem("120843", "SBI Magnum Midcap Fund - Direct Plan - Growth", "SBI Mutual Fund", "MID_CAP", 228.90, 0.72, "Very High", 500.0),
    MutualFundItem("118776", "Nippon India Growth Fund - Direct Plan - Growth", "Nippon Life India AM", "MID_CAP", 412.50, 0.81, "Very High", 500.0),
    MutualFundItem("119794", "Axis Midcap Fund - Direct Plan - Growth", "Axis Asset Management", "MID_CAP", 112.30, 0.65, "Very High", 500.0),
    MutualFundItem("120718", "UTI Mid Cap Fund - Direct Plan - Growth", "UTI Mutual Fund", "MID_CAP", 310.20, 0.74, "Very High", 500.0),
    MutualFundItem("120364", "ICICI Prudential MidCap Fund - Direct Growth", "ICICI Prudential AMC", "MID_CAP", 258.40, 0.79, "Very High", 500.0),
    MutualFundItem("120485", "Mirae Asset Midcap Fund - Direct Plan - Growth", "Mirae Asset Investment", "MID_CAP", 34.60, 0.82, "Very High", 500.0),
    MutualFundItem("119225", "DSP Midcap Fund - Direct Plan - Growth", "DSP Mutual Fund", "MID_CAP", 132.80, 0.68, "Very High", 500.0),
    MutualFundItem("119597", "Canara Robeco Emerging Equities Fund - Direct Growth", "Canara Robeco", "MID_CAP", 245.80, 0.68, "Very High", 500.0),
    MutualFundItem("125361", "Tata Mid Cap Growth Fund - Direct Plan - Growth", "Tata Asset Management", "MID_CAP", 384.20, 0.75, "Very High", 500.0),
    MutualFundItem("148654", "Edelweiss Mid Cap Fund - Direct Plan - Growth", "Edelweiss AMC", "MID_CAP", 98.40, 0.80, "Very High", 500.0),
    MutualFundItem("118994", "Sundaram Mid Cap Fund - Direct Plan - Growth", "Sundaram Mutual Fund", "MID_CAP", 1210.50, 0.84, "Very High", 500.0),
    MutualFundItem("146143", "Baroda BNP Paribas Mid Cap Fund - Direct Growth", "Baroda BNP Paribas", "MID_CAP", 112.30, 0.82, "Very High", 500.0),
    MutualFundItem("149206", "Mahindra Manulife Mid Cap Fund - Direct Growth", "Mahindra Manulife", "MID_CAP", 31.80, 0.90, "Very High", 500.0),
    MutualFundItem("118777", "Franklin India Prima Fund (Mid Cap) - Direct Growth", "Franklin Templeton", "MID_CAP", 2450.80, 0.78, "Very High", 500.0),
    MutualFundItem("145333", "HSBC Mid Cap Fund - Direct Plan - Growth", "HSBC Mutual Fund", "MID_CAP", 132.40, 0.79, "Very High", 500.0),
    MutualFundItem("147413", "Invesco India Mid Cap Fund - Direct Plan - Growth", "Invesco Mutual Fund", "MID_CAP", 158.20, 0.76, "Very High", 500.0),
    MutualFundItem("147946", "Bandhan Midcap Fund - Direct Plan - Growth", "Bandhan AMC", "MID_CAP", 22.40, 0.85, "Very High", 500.0),
    MutualFundItem("148655", "Union Midcap Fund - Direct Plan - Growth", "Union Mutual Fund", "MID_CAP", 46.50, 0.86, "Very High", 500.0),

    // --- Large Cap & Large & Mid Cap ---
    MutualFundItem("120363", "ICICI Prudential Bluechip Fund - Direct Growth", "ICICI Prudential AMC", "LARGE_CAP", 112.40, 0.42, "Very High", 500.0),
    MutualFundItem("120826", "SBI Bluechip Fund - Direct Plan - Growth", "SBI Mutual Fund", "LARGE_CAP", 96.75, 0.38, "Very High", 500.0),
    MutualFundItem("120502", "HDFC Top 100 Fund - Direct Plan - Growth", "HDFC Asset Management", "LARGE_CAP", 1120.30, 0.44, "Very High", 500.0),
    MutualFundItem("118774", "Nippon India Large Cap Fund - Direct Plan - Growth", "Nippon Life India AM", "LARGE_CAP", 88.20, 0.48, "Very High", 500.0),
    MutualFundItem("119792", "Axis Bluechip Fund - Direct Plan - Growth", "Axis Asset Management", "LARGE_CAP", 58.90, 0.40, "Very High", 500.0),
    MutualFundItem("120484", "Mirae Asset Large Cap Fund - Direct Plan - Growth", "Mirae Asset Investment", "LARGE_CAP", 125.40, 0.46, "Very High", 500.0),
    MutualFundItem("119596", "Canara Robeco Bluechip Equity Fund - Direct Growth", "Canara Robeco", "LARGE_CAP", 62.80, 0.45, "Very High", 500.0),
    MutualFundItem("118988", "Kotak Bluechip Fund - Direct Plan - Growth", "Kotak Mahindra AMC", "LARGE_CAP", 520.10, 0.42, "Very High", 500.0),
    MutualFundItem("125353", "Quant Large Cap Fund - Direct Plan - Growth", "Quant Mutual Fund", "LARGE_CAP", 14.80, 0.65, "Very High", 1000.0),
    MutualFundItem("120715", "UTI Mastershare Unit Scheme - Direct Plan - Growth", "UTI Mutual Fund", "LARGE_CAP", 270.30, 0.41, "Very High", 500.0),
    MutualFundItem("146144", "Baroda BNP Paribas Large Cap Fund - Direct Growth", "Baroda BNP Paribas", "LARGE_CAP", 260.40, 0.48, "Very High", 500.0),
    MutualFundItem("119227", "DSP Top 100 Equity Fund - Direct Plan - Growth", "DSP Mutual Fund", "LARGE_CAP", 420.50, 0.45, "Very High", 500.0),
    MutualFundItem("148656", "Edelweiss Large Cap Fund - Direct Plan - Growth", "Edelweiss AMC", "LARGE_CAP", 84.20, 0.50, "Very High", 500.0),
    MutualFundItem("125362", "Tata Large Cap Fund - Direct Plan - Growth", "Tata Asset Management", "LARGE_CAP", 485.60, 0.46, "Very High", 500.0),
    MutualFundItem("118779", "Franklin India Bluechip Fund - Direct Plan - Growth", "Franklin Templeton", "LARGE_CAP", 950.40, 0.49, "Very High", 500.0),
    MutualFundItem("147414", "Invesco India Largecap Fund - Direct Plan - Growth", "Invesco Mutual Fund", "LARGE_CAP", 68.20, 0.44, "Very High", 500.0),
    MutualFundItem("118995", "Sundaram Large Cap Fund - Direct Plan - Growth", "Sundaram Mutual Fund", "LARGE_CAP", 74.80, 0.48, "Very High", 500.0),
    MutualFundItem("145334", "HSBC Large Cap Fund - Direct Plan - Growth", "HSBC Mutual Fund", "LARGE_CAP", 460.10, 0.45, "Very High", 500.0),
    MutualFundItem("120487", "Mirae Asset Large & Midcap Fund - Direct Growth", "Mirae Asset Investment", "LARGE_CAP", 154.20, 0.55, "Very High", 500.0),
    MutualFundItem("118996", "Kotak Equity Opportunities Fund (Large & Mid) - Direct", "Kotak Mahindra AMC", "LARGE_CAP", 320.50, 0.52, "Very High", 500.0),
    MutualFundItem("120844", "SBI Large & Midcap Fund - Direct Plan - Growth", "SBI Mutual Fund", "LARGE_CAP", 580.40, 0.54, "Very High", 500.0),
    MutualFundItem("120507", "HDFC Large and Mid Cap Fund - Direct Plan - Growth", "HDFC Asset Management", "LARGE_CAP", 312.80, 0.58, "Very High", 500.0),

    // --- Index Funds & ETFs ---
    MutualFundItem("120716", "UTI Nifty 50 Index Fund - Direct Plan - Growth", "UTI Mutual Fund", "INDEX", 175.20, 0.48, "Very High", 500.0),
    MutualFundItem("145328", "HDFC Index Fund - Nifty 50 Plan - Direct Growth", "HDFC Asset Management", "INDEX", 232.40, 0.48, "Very High", 500.0),
    MutualFundItem("148654", "Navi Nifty 50 Index Fund - Direct Plan - Growth", "Navi Mutual Fund", "INDEX", 18.90, 0.48, "Very High", 100.0),
    MutualFundItem("120829", "SBI Nifty Index Fund - Direct Plan - Growth", "SBI Mutual Fund", "INDEX", 215.60, 0.48, "Very High", 500.0),
    MutualFundItem("120368", "ICICI Prudential Nifty 50 Index Fund - Direct Growth", "ICICI Prudential AMC", "INDEX", 262.10, 0.48, "Very High", 500.0),
    MutualFundItem("147943", "Bandhan Nifty 50 Index Fund - Direct Growth", "Bandhan AMC", "INDEX", 48.20, 0.48, "Very High", 500.0),
    MutualFundItem("120717", "UTI Nifty Next 50 Index Fund - Direct Plan - Growth", "UTI Mutual Fund", "INDEX", 74.80, 0.85, "Very High", 500.0),
    MutualFundItem("145329", "Motilal Oswal Nasdaq 100 FoF - Direct Plan - Growth", "Motilal Oswal AMC", "INDEX", 38.40, 1.25, "Very High", 500.0),
    MutualFundItem("150343", "Nippon India Nifty 500 Momentum 50 Index Fund", "Nippon Life India AM", "INDEX", 24.60, 0.65, "Very High", 500.0),
    MutualFundItem("148657", "DSP Nifty 50 Equal Weight Index Fund - Direct Growth", "DSP Mutual Fund", "INDEX", 22.80, 0.52, "Very High", 500.0),
    MutualFundItem("151202", "Motilal Oswal Nifty Microcap 250 Index Fund - Direct", "Motilal Oswal AMC", "INDEX", 32.40, 0.95, "Very High", 500.0),
    MutualFundItem("149802", "Tata Nifty Midcap 150 Index Fund - Direct Plan - Growth", "Tata Asset Management", "INDEX", 28.50, 0.55, "Very High", 500.0),
    MutualFundItem("145882", "Kotak Nifty Bank Index Fund - Direct Plan - Growth", "Kotak Mahindra AMC", "INDEX", 18.20, 0.42, "Very High", 500.0),
    MutualFundItem("120830", "SBI S&P BSE Sensex Index Fund - Direct Plan - Growth", "SBI Mutual Fund", "INDEX", 280.40, 0.45, "Very High", 500.0),
    MutualFundItem("145335", "HDFC Nifty Next 50 Index Fund - Direct Plan - Growth", "HDFC Asset Management", "INDEX", 62.40, 0.80, "Very High", 500.0),
    MutualFundItem("120369", "ICICI Prudential Nifty Next 50 Index Fund - Direct Growth", "ICICI Prudential AMC", "INDEX", 64.10, 0.82, "Very High", 500.0),
    MutualFundItem("152101", "Groww Nifty 50 Index Fund - Direct Plan - Growth", "Groww Mutual Fund", "INDEX", 14.20, 0.48, "Very High", 100.0),
    MutualFundItem("152102", "Zerodha Nifty LargeMidcap 250 Index Fund - Direct", "Zerodha Fund House", "INDEX", 13.50, 0.55, "Very High", 100.0),
    MutualFundItem("147947", "Bandhan Nifty IT Index Fund - Direct Plan - Growth", "Bandhan AMC", "INDEX", 16.80, 1.15, "Very High", 500.0),
    MutualFundItem("148658", "Navi Nifty Bank Index Fund - Direct Plan - Growth", "Navi Mutual Fund", "INDEX", 15.60, 0.40, "Very High", 100.0),

    // --- Sectoral & Thematic ---
    MutualFundItem("125358", "Tata Digital India Fund - Direct Plan - Growth", "Tata Asset Management", "EQUITY", 52.40, 1.45, "Very High", 500.0),
    MutualFundItem("120371", "ICICI Prudential Technology Fund - Direct Growth", "ICICI Prudential AMC", "EQUITY", 210.80, 1.38, "Very High", 500.0),
    MutualFundItem("120838", "SBI Technology Opportunities Fund - Direct Growth", "SBI Mutual Fund", "EQUITY", 225.40, 1.42, "Very High", 500.0),
    MutualFundItem("120372", "ICICI Prudential Pharma Healthcare Fund - Direct", "ICICI Prudential AMC", "EQUITY", 42.80, 0.65, "Very High", 500.0),
    MutualFundItem("120839", "SBI Healthcare Opportunities Fund - Direct Growth", "SBI Mutual Fund", "EQUITY", 412.30, 0.72, "Very High", 500.0),
    MutualFundItem("120508", "HDFC Defence Fund - Direct Plan - Growth", "HDFC Asset Management", "EQUITY", 18.90, 1.82, "Very High", 500.0),
    MutualFundItem("125359", "Quant Infrastructure Fund - Direct Plan - Growth", "Quant Mutual Fund", "EQUITY", 48.60, 1.22, "Very High", 1000.0),
    MutualFundItem("120836", "SBI PSU Fund - Direct Plan - Growth", "SBI Mutual Fund", "EQUITY", 32.50, 0.95, "Very High", 500.0),
    MutualFundItem("120370", "ICICI Prudential Bharat 22 FOF - Direct Growth", "ICICI Prudential AMC", "EQUITY", 35.80, 0.88, "Very High", 500.0),
    MutualFundItem("120845", "SBI Contra Fund - Direct Plan - Growth", "SBI Mutual Fund", "EQUITY", 395.40, 0.91, "Very High", 500.0),
    MutualFundItem("118780", "Nippon India Power & Infra Fund - Direct Plan - Growth", "Nippon Life India AM", "EQUITY", 320.40, 1.15, "Very High", 500.0),
    MutualFundItem("146145", "Aditya Birla Sun Life Manufacturing Equity Fund - Direct", "Aditya Birla Sun Life", "EQUITY", 42.60, 1.05, "Very High", 500.0),
    MutualFundItem("119228", "DSP Healthcare Fund - Direct Plan - Growth", "DSP Mutual Fund", "EQUITY", 14.80, 0.68, "Very High", 500.0),
    MutualFundItem("148659", "Edelweiss US Technology Equity FOF - Direct Growth", "Edelweiss AMC", "EQUITY", 38.20, 1.35, "Very High", 500.0),
    MutualFundItem("145883", "Kotak Pioneer Fund - Direct Plan - Growth", "Kotak Mahindra AMC", "EQUITY", 24.50, 0.85, "Very High", 500.0),
    MutualFundItem("118781", "Franklin India Technology Fund - Direct Plan - Growth", "Franklin Templeton", "EQUITY", 520.10, 1.35, "Very High", 500.0),
    MutualFundItem("147415", "Invesco India Financial Services Fund - Direct Growth", "Invesco Mutual Fund", "EQUITY", 118.40, 0.78, "Very High", 500.0),
    MutualFundItem("150344", "Mirae Asset Artificial Intelligence ETF FoF - Direct", "Mirae Asset Investment", "EQUITY", 16.50, 1.45, "Very High", 500.0),
    MutualFundItem("151203", "Motilal Oswal Defence Index Fund - Direct Plan - Growth", "Motilal Oswal AMC", "EQUITY", 14.20, 1.75, "Very High", 500.0),
    MutualFundItem("152103", "SBI Automotive Opportunities Fund - Direct Plan - Growth", "SBI Mutual Fund", "EQUITY", 12.80, 0.95, "Very High", 500.0),

    // --- ELSS Tax Saver ---
    MutualFundItem("147942", "Parag Parikh Tax Saver Fund - Direct Plan - Growth", "PPFAS Mutual Fund", "ELSS", 34.80, 0.62, "Very High", 500.0),
    MutualFundItem("120486", "Mirae Asset ELSS Tax Saver Fund - Direct Growth", "Mirae Asset Investment", "ELSS", 46.50, 0.58, "Very High", 500.0),
    MutualFundItem("125355", "Quant ELSS Tax Saver Fund - Direct Plan - Growth", "Quant Mutual Fund", "ELSS", 425.80, 1.18, "Very High", 500.0),
    MutualFundItem("119795", "Axis ELSS Tax Saver Fund - Direct Plan - Growth", "Axis Asset Management", "ELSS", 98.40, 0.44, "Very High", 500.0),
    MutualFundItem("120834", "SBI Long Term Equity Fund (ELSS) - Direct Growth", "SBI Mutual Fund", "ELSS", 412.50, 0.75, "Very High", 500.0),
    MutualFundItem("145330", "Bandhan ELSS Tax Saver Fund - Direct Plan - Growth", "Bandhan AMC", "ELSS", 152.40, 0.68, "Very High", 500.0),
    MutualFundItem("118990", "Kotak ELSS Tax Saver Fund - Direct Plan - Growth", "Kotak Mahindra AMC", "ELSS", 112.50, 0.52, "Very High", 500.0),
    MutualFundItem("120509", "HDFC ELSS Tax Saver Fund - Direct Plan - Growth", "HDFC Asset Management", "ELSS", 1250.40, 0.65, "Very High", 500.0),
    MutualFundItem("119229", "DSP ELSS Tax Saver Fund - Direct Plan - Growth", "DSP Mutual Fund", "ELSS", 118.50, 0.62, "Very High", 500.0),
    MutualFundItem("119599", "Canara Robeco ELSS Tax Saver - Direct Plan - Growth", "Canara Robeco", "ELSS", 185.40, 0.60, "Very High", 500.0),
    MutualFundItem("125363", "Tata ELSS Tax Saver Fund - Direct Plan - Growth", "Tata Asset Management", "ELSS", 165.20, 0.66, "Very High", 500.0),
    MutualFundItem("120373", "ICICI Prudential Long Term Equity Fund (ELSS) - Direct", "ICICI Prudential AMC", "ELSS", 940.60, 0.55, "Very High", 500.0),
    MutualFundItem("118782", "Franklin India Taxshield - Direct Plan - Growth", "Franklin Templeton", "ELSS", 1420.10, 0.70, "Very High", 500.0),
    MutualFundItem("127043", "Motilal Oswal ELSS Tax Saver Fund - Direct Growth", "Motilal Oswal AMC", "ELSS", 52.80, 0.68, "Very High", 500.0),
    MutualFundItem("118783", "Nippon India ELSS Tax Saver Fund - Direct Plan - Growth", "Nippon Life India AM", "ELSS", 114.20, 0.69, "Very High", 500.0),

    // --- Hybrid & Balanced Advantage ---
    MutualFundItem("120367", "ICICI Prudential Balanced Advantage Fund - Direct", "ICICI Prudential AMC", "HYBRID", 72.10, 0.32, "High", 500.0),
    MutualFundItem("120506", "HDFC Balanced Advantage Fund - Direct Plan - Growth", "HDFC Asset Management", "HYBRID", 498.20, 0.41, "High", 500.0),
    MutualFundItem("118991", "Kotak Balanced Advantage Fund - Direct Plan - Growth", "Kotak Mahindra AMC", "HYBRID", 21.40, 0.28, "High", 500.0),
    MutualFundItem("120840", "SBI Equity Hybrid Fund - Direct Plan - Growth", "SBI Mutual Fund", "HYBRID", 298.50, 0.35, "Very High", 500.0),
    MutualFundItem("145331", "Canara Robeco Equity Hybrid Fund - Direct Growth", "Canara Robeco", "HYBRID", 342.10, 0.39, "Very High", 500.0),
    MutualFundItem("125356", "Quant Multi Asset Fund - Direct Plan - Growth", "Quant Mutual Fund", "HYBRID", 142.80, 0.75, "Very High", 1000.0),
    MutualFundItem("148660", "Edelweiss Balanced Advantage Fund - Direct Growth", "Edelweiss AMC", "HYBRID", 48.50, 0.35, "High", 500.0),
    MutualFundItem("120488", "Mirae Asset Hybrid Equity Fund - Direct Plan - Growth", "Mirae Asset Investment", "HYBRID", 38.60, 0.38, "Very High", 500.0),
    MutualFundItem("119230", "DSP Equity & Bond Fund - Direct Plan - Growth", "DSP Mutual Fund", "HYBRID", 345.20, 0.36, "Very High", 500.0),
    MutualFundItem("125364", "Tata Balanced Advantage Fund - Direct Plan - Growth", "Tata Asset Management", "HYBRID", 21.80, 0.30, "High", 500.0),
    MutualFundItem("150345", "Nippon India Multi Asset Fund - Direct Plan - Growth", "Nippon Life India AM", "HYBRID", 22.40, 0.42, "Very High", 500.0),
    MutualFundItem("120374", "ICICI Prudential Multi-Asset Fund - Direct Growth", "ICICI Prudential AMC", "HYBRID", 720.50, 0.48, "Very High", 500.0),
    MutualFundItem("120510", "HDFC Multi-Asset Fund - Direct Plan - Growth", "HDFC Asset Management", "HYBRID", 74.20, 0.45, "Very High", 500.0),
    MutualFundItem("119796", "Axis Balanced Advantage Fund - Direct Plan - Growth", "Axis Asset Management", "HYBRID", 18.90, 0.34, "High", 500.0),

    // --- Debt & Liquid Funds ---
    MutualFundItem("120831", "SBI Liquid Fund - Direct Plan - Growth", "SBI Mutual Fund", "DEBT", 3850.20, 0.02, "Low", 500.0),
    MutualFundItem("120501", "HDFC Liquid Fund - Direct Plan - Growth", "HDFC Asset Management", "DEBT", 4920.10, 0.02, "Low", 500.0),
    MutualFundItem("120361", "ICICI Prudential Liquid Fund - Direct Growth", "ICICI Prudential AMC", "DEBT", 375.40, 0.02, "Low", 500.0),
    MutualFundItem("118770", "Nippon India Liquid Fund - Direct Plan - Growth", "Nippon Life India AM", "DEBT", 5840.10, 0.02, "Low", 500.0),
    MutualFundItem("118985", "Kotak Liquid Fund - Direct Plan - Growth", "Kotak Mahindra AMC", "DEBT", 4820.60, 0.02, "Low", 500.0),
    MutualFundItem("120832", "SBI Overnight Fund - Direct Plan - Growth", "SBI Mutual Fund", "DEBT", 3720.50, 0.01, "Low", 500.0),
    MutualFundItem("146146", "Aditya Birla Sun Life Liquid Fund - Direct Growth", "Aditya Birla Sun Life", "DEBT", 412.50, 0.02, "Low", 500.0),
    MutualFundItem("119797", "Axis Liquid Fund - Direct Plan - Growth", "Axis Asset Management", "DEBT", 2750.40, 0.02, "Low", 500.0),
    MutualFundItem("120719", "UTI Liquid Cash Plan - Direct Plan - Growth", "UTI Mutual Fund", "DEBT", 4210.80, 0.02, "Low", 500.0),
    MutualFundItem("119231", "DSP Liquidity Fund - Direct Plan - Growth", "DSP Mutual Fund", "DEBT", 3650.10, 0.02, "Low", 500.0),
    MutualFundItem("120375", "ICICI Prudential Corporate Bond Fund - Direct Growth", "ICICI Prudential AMC", "DEBT", 28.50, 0.03, "Low to Moderate", 500.0),
    MutualFundItem("120511", "HDFC Short Term Debt Fund - Direct Plan - Growth", "HDFC Asset Management", "DEBT", 31.40, 0.03, "Low to Moderate", 500.0),
    MutualFundItem("118997", "Kotak Banking and PSU Debt Fund - Direct Growth", "Kotak Mahindra AMC", "DEBT", 64.20, 0.03, "Low to Moderate", 500.0),
    MutualFundItem("120833", "SBI Banking & PSU Fund - Direct Plan - Growth", "SBI Mutual Fund", "DEBT", 3420.50, 0.03, "Low to Moderate", 500.0),

    // --- Sectoral & Thematic Funds (India High Growth Themes) ---
    MutualFundItem("120376", "ICICI Prudential Technology Fund - Direct Plan - Growth", "ICICI Prudential AMC", "SECTORAL", 215.40, 1.15, "Very High", 500.0),
    MutualFundItem("135781", "Tata Digital India Fund - Direct Plan - Growth", "Tata Asset Management", "SECTORAL", 52.80, 1.25, "Very High", 500.0),
    MutualFundItem("119798", "Axis Technology Fund - Direct Plan - Growth", "Axis Asset Management", "SECTORAL", 17.50, 0.95, "Very High", 500.0),
    MutualFundItem("120377", "ICICI Prudential Pharma Healthcare and Diagnostics Fund", "ICICI Prudential AMC", "SECTORAL", 42.10, 0.65, "Very High", 500.0),
    MutualFundItem("118771", "Nippon India Pharma Fund - Direct Plan - Growth", "Nippon Life India AM", "SECTORAL", 520.40, 0.72, "Very High", 500.0),
    MutualFundItem("120834", "SBI Healthcare Opportunities Fund - Direct Plan - Growth", "SBI Mutual Fund", "SECTORAL", 380.20, 0.68, "Very High", 500.0),
    MutualFundItem("118772", "Nippon India Power & Infra Fund - Direct Growth", "Nippon Life India AM", "SECTORAL", 360.80, 1.10, "Very High", 500.0),
    MutualFundItem("120512", "HDFC Infrastructure Fund - Direct Plan - Growth", "HDFC Asset Management", "SECTORAL", 54.20, 0.98, "Very High", 500.0),
    MutualFundItem("146147", "Aditya Birla Sun Life Manufacturing Equity Fund - Direct", "Aditya Birla Sun Life", "SECTORAL", 34.50, 0.85, "Very High", 500.0),
    MutualFundItem("125357", "Quant Infrastructure Fund - Direct Plan - Growth", "Quant Mutual Fund", "SECTORAL", 58.40, 1.30, "Very High", 1000.0),
    MutualFundItem("120835", "SBI PSU Fund - Direct Plan - Growth", "SBI Mutual Fund", "SECTORAL", 36.80, 0.92, "Very High", 500.0),
    MutualFundItem("135782", "Tata India Pharma & Healthcare Fund - Direct Growth", "Tata Asset Management", "SECTORAL", 38.60, 0.74, "Very High", 500.0),

    // --- All Remaining Indian AMCs Directory ---
    // Sundaram Mutual Fund
    MutualFundItem("119045", "Sundaram Large Cap Fund - Direct Plan - Growth", "Sundaram Mutual Fund", "LARGE_CAP", 78.40, 0.35, "Very High", 500.0),
    MutualFundItem("119046", "Sundaram Mid Cap Fund - Direct Plan - Growth", "Sundaram Mutual Fund", "MID_CAP", 1250.20, 0.58, "Very High", 500.0),
    MutualFundItem("145220", "Sundaram Small Cap Fund - Direct Plan - Growth", "Sundaram Mutual Fund", "SMALL_CAP", 320.10, 0.82, "Very High", 500.0),
    MutualFundItem("119047", "Sundaram Flexi Cap Fund - Direct Plan - Growth", "Sundaram Mutual Fund", "FLEXI_CAP", 112.50, 0.44, "Very High", 500.0),

    // Invesco India Mutual Fund
    MutualFundItem("120280", "Invesco India Contra Fund - Direct Plan - Growth", "Invesco Mutual Fund", "FLEXI_CAP", 132.40, 0.52, "Very High", 500.0),
    MutualFundItem("120281", "Invesco India Mid Cap Fund - Direct Plan - Growth", "Invesco Mutual Fund", "MID_CAP", 168.90, 0.65, "Very High", 500.0),
    MutualFundItem("145300", "Invesco India Smallcap Fund - Direct Plan - Growth", "Invesco Mutual Fund", "SMALL_CAP", 42.50, 0.78, "Very High", 500.0),
    MutualFundItem("120282", "Invesco India Largecap Fund - Direct Plan - Growth", "Invesco Mutual Fund", "LARGE_CAP", 74.20, 0.38, "Very High", 500.0),

    // Franklin Templeton Mutual Fund
    MutualFundItem("120220", "Franklin India Flexi Cap Fund - Direct Plan - Growth", "Franklin Templeton", "FLEXI_CAP", 1680.50, 0.55, "Very High", 500.0),
    MutualFundItem("120221", "Franklin India Prima Fund (Mid Cap) - Direct Growth", "Franklin Templeton", "MID_CAP", 2450.30, 0.62, "Very High", 500.0),
    MutualFundItem("120222", "Franklin India Smaller Companies Fund - Direct Growth", "Franklin Templeton", "SMALL_CAP", 175.40, 0.75, "Very High", 500.0),
    MutualFundItem("120223", "Franklin India Bluechip Fund - Direct Plan - Growth", "Franklin Templeton", "LARGE_CAP", 980.20, 0.36, "Very High", 500.0),
    MutualFundItem("120224", "Franklin India Technology Fund - Direct Plan - Growth", "Franklin Templeton", "SECTORAL", 490.60, 1.05, "Very High", 500.0),

    // WhiteOak Capital Mutual Fund
    MutualFundItem("149880", "WhiteOak Capital Flexi Cap Fund - Direct Plan - Growth", "WhiteOak Capital AMC", "FLEXI_CAP", 21.40, 0.48, "Very High", 500.0),
    MutualFundItem("149881", "WhiteOak Capital Mid Cap Fund - Direct Plan - Growth", "WhiteOak Capital AMC", "MID_CAP", 22.80, 0.62, "Very High", 500.0),
    MutualFundItem("149882", "WhiteOak Capital Large Cap Fund - Direct Plan - Growth", "WhiteOak Capital AMC", "LARGE_CAP", 16.50, 0.35, "Very High", 500.0),

    // Groww Mutual Fund
    MutualFundItem("151210", "Groww Nifty Total Market Index Fund - Direct Growth", "Groww Mutual Fund", "INDEX", 14.80, 0.42, "Very High", 100.0),
    MutualFundItem("151211", "Groww Large Cap Fund - Direct Plan - Growth", "Groww Mutual Fund", "LARGE_CAP", 12.60, 0.35, "Very High", 100.0),
    MutualFundItem("151212", "Groww Nifty Non-Cyclical Consumer Index Fund - Direct", "Groww Mutual Fund", "INDEX", 13.50, 0.38, "Very High", 100.0),

    // Navi Mutual Fund
    MutualFundItem("148920", "Navi Nifty 50 Index Fund - Direct Plan - Growth", "Navi Mutual Fund", "INDEX", 16.20, 0.32, "Very High", 10.0),
    MutualFundItem("148921", "Navi US Total Stock Market Fund of Fund - Direct", "Navi Mutual Fund", "INDEX", 15.40, 0.55, "Very High", 10.0),
    MutualFundItem("148922", "Navi Nifty Next 50 Index Fund - Direct Plan - Growth", "Navi Mutual Fund", "INDEX", 17.80, 0.45, "Very High", 10.0),
    MutualFundItem("148923", "Navi Nifty Midcap 150 Index Fund - Direct Growth", "Navi Mutual Fund", "INDEX", 19.50, 0.60, "Very High", 10.0),

    // Zerodha Fund House
    MutualFundItem("151440", "Zerodha Nifty LargeMidcap 250 Index Fund - Direct Growth", "Zerodha Fund House", "INDEX", 13.90, 0.45, "Very High", 100.0),
    MutualFundItem("151441", "Zerodha ELSS Tax Saver Nifty LargeMidcap 250 Index", "Zerodha Fund House", "ELSS", 13.80, 0.45, "Very High", 500.0),
    MutualFundItem("151442", "Zerodha Gold ETF Fund of Fund - Direct Growth", "Zerodha Fund House", "HYBRID", 11.80, 0.20, "Moderate", 100.0),

    // Mahindra Manulife Mutual Fund
    MutualFundItem("145110", "Mahindra Manulife Multi Cap Fund - Direct Plan - Growth", "Mahindra Manulife AMC", "FLEXI_CAP", 36.40, 0.52, "Very High", 500.0),
    MutualFundItem("145111", "Mahindra Manulife Small Cap Fund - Direct Plan - Growth", "Mahindra Manulife AMC", "SMALL_CAP", 24.50, 0.76, "Very High", 500.0),
    MutualFundItem("145112", "Mahindra Manulife Mid Cap Fund - Direct Plan - Growth", "Mahindra Manulife AMC", "MID_CAP", 32.10, 0.65, "Very High", 500.0),

    // Bank of India Mutual Fund
    MutualFundItem("145920", "Bank of India Small Cap Fund - Direct Plan - Growth", "Bank of India Mutual Fund", "SMALL_CAP", 52.60, 0.85, "Very High", 500.0),
    MutualFundItem("145921", "Bank of India Flexi Cap Fund - Direct Plan - Growth", "Bank of India Mutual Fund", "FLEXI_CAP", 41.20, 0.50, "Very High", 500.0),
    MutualFundItem("145922", "Bank of India Manufacturing & Infra Fund - Direct", "Bank of India Mutual Fund", "SECTORAL", 46.80, 0.95, "Very High", 500.0),

    // Baroda BNP Paribas Mutual Fund
    MutualFundItem("145830", "Baroda BNP Paribas Large & Mid Cap Fund - Direct Growth", "Baroda BNP Paribas AMC", "LARGE_CAP", 38.50, 0.48, "Very High", 500.0),
    MutualFundItem("145831", "Baroda BNP Paribas Multi Cap Fund - Direct Plan - Growth", "Baroda BNP Paribas AMC", "FLEXI_CAP", 28.60, 0.54, "Very High", 500.0),
    MutualFundItem("145832", "Baroda BNP Paribas Small Cap Fund - Direct Plan - Growth", "Baroda BNP Paribas AMC", "SMALL_CAP", 18.20, 0.72, "Very High", 500.0),

    // Union Mutual Fund
    MutualFundItem("145610", "Union Flexi Cap Fund - Direct Plan - Growth", "Union Mutual Fund", "FLEXI_CAP", 58.20, 0.49, "Very High", 500.0),
    MutualFundItem("145611", "Union Small Cap Fund - Direct Plan - Growth", "Union Mutual Fund", "SMALL_CAP", 48.70, 0.80, "Very High", 500.0),
    MutualFundItem("145612", "Union Midcap Fund - Direct Plan - Growth", "Union Mutual Fund", "MID_CAP", 42.10, 0.62, "Very High", 500.0),

    // LIC Mutual Fund
    MutualFundItem("120610", "LIC MF Large Cap Fund - Direct Plan - Growth", "LIC Mutual Fund", "LARGE_CAP", 62.40, 0.35, "Very High", 500.0),
    MutualFundItem("120611", "LIC MF Flexi Cap Fund - Direct Plan - Growth", "LIC Mutual Fund", "FLEXI_CAP", 88.50, 0.48, "Very High", 500.0),
    MutualFundItem("120612", "LIC MF Infrastructure Fund - Direct Plan - Growth", "LIC Mutual Fund", "SECTORAL", 41.60, 0.88, "Very High", 500.0),

    // JM Financial Mutual Fund
    MutualFundItem("120780", "JM Flexicap Fund - Direct Plan - Growth", "JM Financial Mutual Fund", "FLEXI_CAP", 128.50, 0.58, "Very High", 500.0),
    MutualFundItem("120781", "JM Value Fund - Direct Plan - Growth", "JM Financial Mutual Fund", "FLEXI_CAP", 92.40, 0.65, "Very High", 500.0),
    MutualFundItem("120782", "JM Midcap Fund - Direct Plan - Growth", "JM Financial Mutual Fund", "MID_CAP", 19.80, 0.72, "Very High", 500.0),

    // Samco Mutual Fund
    MutualFundItem("149610", "Samco Flexi Cap Fund - Direct Plan - Growth", "Samco Mutual Fund", "FLEXI_CAP", 14.80, 0.45, "Very High", 500.0),
    MutualFundItem("149611", "Samco ELSS Tax Saver Fund - Direct Plan - Growth", "Samco Mutual Fund", "ELSS", 15.20, 0.45, "Very High", 500.0),

    // Quantum Mutual Fund
    MutualFundItem("120910", "Quantum Long Term Equity Value Fund - Direct Growth", "Quantum Mutual Fund", "LARGE_CAP", 112.50, 0.42, "Very High", 500.0),
    MutualFundItem("120911", "Quantum Liquid Fund - Direct Plan - Growth", "Quantum Mutual Fund", "DEBT", 38.60, 0.02, "Low", 500.0),

    // ITI Mutual Fund
    MutualFundItem("148110", "ITI Small Cap Fund - Direct Plan - Growth", "ITI Mutual Fund", "SMALL_CAP", 28.40, 0.78, "Very High", 500.0),
    MutualFundItem("148111", "ITI Mid Cap Fund - Direct Plan - Growth", "ITI Mutual Fund", "MID_CAP", 24.60, 0.64, "Very High", 500.0),
    MutualFundItem("148112", "ITI Flexi Cap Fund - Direct Plan - Growth", "ITI Mutual Fund", "FLEXI_CAP", 19.20, 0.50, "Very High", 500.0),

    // Taurus Mutual Fund
    MutualFundItem("120890", "Taurus Discovery (Midcap) Fund - Direct Plan - Growth", "Taurus Mutual Fund", "MID_CAP", 124.50, 0.60, "Very High", 500.0),
    MutualFundItem("120891", "Taurus Infrastructure Fund - Direct Plan - Growth", "Taurus Mutual Fund", "SECTORAL", 64.20, 0.85, "Very High", 500.0),

    // Trust Mutual Fund
    MutualFundItem("149210", "TrustMF Banking & PSU Debt Fund - Direct Plan - Growth", "Trust Mutual Fund", "DEBT", 1250.40, 0.03, "Low to Moderate", 500.0),
    MutualFundItem("149211", "TrustMF Flexi Cap Fund - Direct Plan - Growth", "Trust Mutual Fund", "FLEXI_CAP", 14.20, 0.45, "Very High", 500.0),

    // Shriram Mutual Fund
    MutualFundItem("145710", "Shriram Flexi Cap Fund - Direct Plan - Growth", "Shriram Mutual Fund", "FLEXI_CAP", 28.60, 0.48, "Very High", 500.0),
    MutualFundItem("145711", "Shriram Multi Asset Allocation Fund - Direct Growth", "Shriram Mutual Fund", "HYBRID", 16.40, 0.35, "High", 500.0),

    // 360 ONE Mutual Fund
    MutualFundItem("145450", "360 ONE Focused Equity Fund - Direct Plan - Growth", "360 ONE Mutual Fund", "FLEXI_CAP", 44.80, 0.52, "Very High", 1000.0),
    MutualFundItem("145451", "360 ONE Quant Fund - Direct Plan - Growth", "360 ONE Mutual Fund", "FLEXI_CAP", 22.40, 0.60, "Very High", 1000.0),

    // Helios Mutual Fund
    MutualFundItem("151510", "Helios Flexi Cap Fund - Direct Plan - Growth", "Helios Mutual Fund", "FLEXI_CAP", 14.50, 0.45, "Very High", 500.0),
    MutualFundItem("151511", "Helios Balanced Advantage Fund - Direct Growth", "Helios Mutual Fund", "HYBRID", 12.80, 0.32, "High", 500.0),

    // Bajaj Finserv Mutual Fund
    MutualFundItem("151320", "Bajaj Finserv Flexi Cap Fund - Direct Plan - Growth", "Bajaj Finserv AMC", "FLEXI_CAP", 15.20, 0.48, "Very High", 500.0),
    MutualFundItem("151321", "Bajaj Finserv Large and Mid Cap Fund - Direct Growth", "Bajaj Finserv AMC", "LARGE_CAP", 14.60, 0.52, "Very High", 500.0),
    MutualFundItem("151322", "Bajaj Finserv Nifty 50 Index Fund - Direct Plan - Growth", "Bajaj Finserv AMC", "INDEX", 13.40, 0.32, "Very High", 100.0)
  )

  val catalogVersion = kotlinx.coroutines.flow.MutableStateFlow(0)

  fun getAllMutualFunds(): List<MutualFundItem> {
    return NFO_CATALOG + dynamicNfos + dynamicImportedFunds + DIRECT_MF_CATALOG
  }

  fun getNfos(): List<MutualFundItem> {
    return NFO_CATALOG + dynamicNfos.filter { it.isNfo }
  }

  fun addDynamicNfos(newNfos: List<MutualFundItem>) {
    var added = false
    for (item in newNfos) {
      if (dynamicNfos.none { it.schemeCode.equals(item.schemeCode, ignoreCase = true) }) {
        dynamicNfos.add(item)
        added = true
      }
    }
    if (added) {
      catalogVersion.value++
    }
  }

  fun addDynamicFunds(newFunds: List<MutualFundItem>) {
    var added = false
    for (item in newFunds) {
      if (dynamicImportedFunds.none { it.schemeCode.equals(item.schemeCode, ignoreCase = true) } &&
          DIRECT_MF_CATALOG.none { it.schemeCode.equals(item.schemeCode, ignoreCase = true) } &&
          NFO_CATALOG.none { it.schemeCode.equals(item.schemeCode, ignoreCase = true) }) {
        dynamicImportedFunds.add(item)
        added = true
      }
    }
    if (added) {
      catalogVersion.value++
    }
  }

  fun addFundToDirectory(item: MutualFundItem) {
    dynamicImportedFunds.removeAll { it.schemeCode.equals(item.schemeCode, ignoreCase = true) }
    dynamicImportedFunds.add(item)
    catalogVersion.value++
  }

  fun searchMutualFunds(query: String, filterCategory: String = "ALL"): List<MutualFundItem> {
    val q = query.trim().lowercase()
    val all = getAllMutualFunds()

    return all.filter { item ->
      val matchesCategory = when (filterCategory.uppercase()) {
        "ALL" -> true
        "NFO" -> item.isNfo
        "EQUITY" -> item.category in listOf("FLEXI_CAP", "LARGE_CAP", "MID_CAP", "SMALL_CAP", "EQUITY")
        "FLEXI_CAP" -> item.category == "FLEXI_CAP"
        "LARGE_CAP" -> item.category == "LARGE_CAP"
        "MID_CAP" -> item.category == "MID_CAP"
        "SMALL_CAP" -> item.category == "SMALL_CAP"
        "ELSS" -> item.category == "ELSS"
        "INDEX" -> item.category == "INDEX"
        "HYBRID" -> item.category == "HYBRID"
        "DEBT" -> item.category == "DEBT"
        else -> item.category.contains(filterCategory, ignoreCase = true)
      }

      val matchesQuery = if (q.isBlank()) true else {
        item.schemeName.lowercase().contains(q) ||
          item.amc.lowercase().contains(q) ||
          item.schemeCode.lowercase().contains(q) ||
          item.category.lowercase().contains(q)
      }

      matchesCategory && matchesQuery
    }
  }

  fun getFallbackNav(codeOrName: String): Double? {
    val clean = codeOrName.trim().lowercase()
    val match = getAllMutualFunds().firstOrNull {
      it.schemeCode.lowercase() == clean ||
        it.schemeName.lowercase().contains(clean) ||
        clean.contains(it.schemeCode.lowercase())
    }
    return match?.nav
  }
}
