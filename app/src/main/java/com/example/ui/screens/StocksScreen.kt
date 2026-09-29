package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PriceChange
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material3.Checkbox
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.CircularProgressIndicator
import com.example.data.ai.GeminiPortfolioStockVerdict
import com.example.data.ai.GeminiStockRecommendation
import com.example.data.ai.StockRecommendationAction
import com.example.data.ai.GroundedSource
import com.example.data.ai.RealTimeMarketIndex
import com.example.data.api.DetailedIpoItem
import com.example.data.api.IpoStatus
import com.example.data.api.MutualFundCatalog
import com.example.data.api.MutualFundItem
import com.example.data.api.StockCatalogItem
import com.example.data.api.StockDatabaseCatalog
import com.example.data.model.AccountEntity
import com.example.data.model.MutualFundSipEntity
import com.example.ui.components.CreateMutualFundSipDialog
import com.example.ui.components.IpoSectionContent
import com.example.ui.components.RealTimeMarketIndicesCard
import com.example.ui.components.GoogleSearchSourcesDialog
import com.example.ui.components.FinnhubMarketMetricsCard
import com.example.ui.components.FinnhubCompanyProfileDialog
import com.example.ui.components.AlphaVantageMarketMetricsCard
import com.example.ui.components.AlphaVantageOverviewDialog
import java.text.SimpleDateFormat
import java.util.Date
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StockEntity
import com.example.ui.viewmodel.ExpenseViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StocksScreen(
  viewModel: ExpenseViewModel,
  modifier: Modifier = Modifier
) {
  val stocks by viewModel.filteredStocks.collectAsState()
  val allStocks by viewModel.allStocks.collectAsState()
  val totalInvested by viewModel.totalPortfolioInvested.collectAsState()
  val totalCurrentValue by viewModel.totalPortfolioCurrentValue.collectAsState()
  val totalPnl by viewModel.totalPortfolioPnl.collectAsState()
  val totalPnlPct by viewModel.totalPortfolioPnlPercentage.collectAsState()
  val currencySymbol by viewModel.currencySymbol.collectAsState()
  val allAccounts by viewModel.allAccounts.collectAsState()
  val allSips by viewModel.allSips.collectAsState()

  // Web API & Gemini Advisor State
  val isUpdatingStockPrices by viewModel.isUpdatingStockPrices.collectAsState()
  val lastSyncTime by viewModel.lastStockPriceSyncTime.collectAsState()
  val stockApiStatusMessage by viewModel.stockApiStatusMessage.collectAsState()
  val isAnalyzingStocksWithGemini by viewModel.isAnalyzingStocksWithGemini.collectAsState()
  val geminiStockVerdict by viewModel.geminiStockVerdict.collectAsState()
  val geminiStockRecommendations by viewModel.geminiStockRecommendations.collectAsState()

  // Real-Time Google Search Grounding & IPO States
  val allIpos by viewModel.allIpos.collectAsState()
  val isSyncingRealTimeData by viewModel.isSyncingRealTimeData.collectAsState()
  val realTimeMarketIndices by viewModel.realTimeMarketIndices.collectAsState()
  val realTimeSearchSources by viewModel.realTimeSearchSources.collectAsState()
  val realTimeSearchQueries by viewModel.realTimeSearchQueries.collectAsState()
  val lastRealTimeSyncTime by viewModel.lastRealTimeSyncTime.collectAsState()
  val realTimeSyncMessage by viewModel.realTimeSyncMessage.collectAsState()
  var showSearchSourcesDialog by remember { mutableStateOf(false) }

  // Finnhub Live Market State
  val finnhubKey by viewModel.finnhubApiKey.collectAsState()
  val activeFinnhubQuote by viewModel.activeFinnhubQuote.collectAsState()
  val activeFinnhubProfile by viewModel.activeFinnhubProfile.collectAsState()
  val isLoadingFinnhub by viewModel.isLoadingFinnhubDetails.collectAsState()
  var showCompanyProfileDialog by remember { mutableStateOf(false) }
  var showFinnhubQuickLookup by remember { mutableStateOf(false) }
  var finnhubLookupSymbol by remember { mutableStateOf("") }

  // Alpha Vantage Live Market State
  val alphaVantageKey by viewModel.alphaVantageApiKey.collectAsState()
  val activeAlphaVantageQuote by viewModel.activeAlphaVantageQuote.collectAsState()
  val activeAlphaVantageOverview by viewModel.activeAlphaVantageOverview.collectAsState()
  val isLoadingAlphaVantage by viewModel.isLoadingAlphaVantageDetails.collectAsState()
  var showAlphaVantageOverviewDialog by remember { mutableStateOf(false) }
  var showAlphaVantageQuickLookup by remember { mutableStateOf(false) }
  var alphaVantageLookupSymbol by remember { mutableStateOf("") }

  var selectedRecommendationDetail by remember { mutableStateOf<GeminiStockRecommendation?>(null) }
  var showFullGeminiVerdictDialog by remember { mutableStateOf(false) }

  var searchQuery by remember { mutableStateOf("") }
  var sortOption by remember { mutableStateOf("VALUE_DESC") } // VALUE_DESC, PNL_DESC, PNL_ASC, NAME_ASC

  // Investment Tabs: 0 = Stocks, 1 = Mutual Funds, 2 = IPOs
  var selectedInvestmentTab by remember { mutableStateOf(0) }
  var addInvestmentMode by remember { mutableStateOf("STOCK") }
  var prefilledNfo by remember { mutableStateOf<MutualFundItem?>(null) }
  var mfCategoryFilter by remember { mutableStateOf("ALL") }

  var showAddInvestmentDialog by remember { mutableStateOf(false) }
  var showCreateSipDialog by remember { mutableStateOf(false) }
  var stockToEditPrice by remember { mutableStateOf<StockEntity?>(null) }
  var stockToEdit by remember { mutableStateOf<StockEntity?>(null) }
  var stockToDelete by remember { mutableStateOf<StockEntity?>(null) }

  // Day's P&L calculation
  val totalDayPnl = remember(allStocks) {
    allStocks.sumOf { it.dayPnl }
  }
  val totalDayPnlPct = remember(allStocks, totalDayPnl) {
    val prevDayVal = allStocks.sumOf {
      val prevPrice = if (it.dailyChangePercent != 0.0) it.currentPrice / (1.0 + (it.dailyChangePercent / 100.0)) else it.currentPrice
      prevPrice * it.shares
    }
    if (prevDayVal > 0) (totalDayPnl / prevDayVal) * 100.0 else 0.0
  }

  // Holdings partitioned by Asset Type
  val equityHoldings = remember(stocks) {
    stocks.filter { it.assetType != "MUTUAL_FUND" }
  }
  val allEquityStocks = remember(allStocks) {
    allStocks.filter { it.assetType != "MUTUAL_FUND" }
  }

  val mfHoldings = remember(stocks, mfCategoryFilter) {
    stocks.filter { it.assetType == "MUTUAL_FUND" }.filter { item ->
      when (mfCategoryFilter.uppercase()) {
        "ALL" -> true
        "NFO" -> item.symbol.startsWith("NFO") || item.companyName.contains("NFO", ignoreCase = true) || item.notes.contains("NFO", ignoreCase = true)
        else -> item.notes.contains(mfCategoryFilter.replace("_", " "), ignoreCase = true) ||
                item.notes.contains(mfCategoryFilter, ignoreCase = true) ||
                item.companyName.contains(mfCategoryFilter.replace("_", " "), ignoreCase = true)
      }
    }
  }
  val allMfStocks = remember(allStocks) {
    allStocks.filter { it.assetType == "MUTUAL_FUND" }
  }

  Box(modifier = modifier.fillMaxSize()) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. Portfolio Overview Hero Card with Day's P&L & Total P&L
      item {
        PortfolioHeroCard(
          totalInvested = totalInvested,
          currentValue = totalCurrentValue,
          totalPnl = totalPnl,
          pnlPercentage = totalPnlPct,
          dayPnl = totalDayPnl,
          dayPnlPercentage = totalDayPnlPct,
          stockCount = allStocks.size,
          currencySymbol = currencySymbol
        )
      }

      // 1b. Real-Time Market Indices & Google Search Grounding Card
      item {
        RealTimeMarketIndicesCard(
          indices = realTimeMarketIndices,
          isSyncing = isSyncingRealTimeData,
          lastSyncTime = lastRealTimeSyncTime,
          statusMessage = realTimeSyncMessage,
          searchSources = realTimeSearchSources,
          searchQueries = realTimeSearchQueries,
          onSyncNow = { viewModel.syncWithRealTimeData(force = true) },
          onViewSources = { showSearchSourcesDialog = true }
        )
      }

      // 2. Investment Section Selector: STOCKS vs MUTUAL FUNDS vs IPOS
      item {
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
          modifier = Modifier.fillMaxWidth().testTag("investment_tabs")
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            val isStocksSelected = selectedInvestmentTab == 0
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = if (isStocksSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
              shadowElevation = if (isStocksSelected) 2.dp else 0.dp,
              modifier = Modifier
                .weight(1f)
                .clickable { selectedInvestmentTab = 0 }
                .testTag("tab_stocks")
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.ShowChart,
                  contentDescription = null,
                  tint = if (isStocksSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Stocks (${allEquityStocks.size})",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = if (isStocksSelected) FontWeight.Bold else FontWeight.Medium,
                  color = if (isStocksSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            val isMfSelected = selectedInvestmentTab == 1
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = if (isMfSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
              shadowElevation = if (isMfSelected) 2.dp else 0.dp,
              modifier = Modifier
                .weight(1f)
                .clickable { selectedInvestmentTab = 1 }
                .testTag("tab_mutual_funds")
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.Savings,
                  contentDescription = null,
                  tint = if (isMfSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Mutual Funds (${allMfStocks.size})",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = if (isMfSelected) FontWeight.Bold else FontWeight.Medium,
                  color = if (isMfSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            val isIpoSelected = selectedInvestmentTab == 2
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = if (isIpoSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
              shadowElevation = if (isIpoSelected) 2.dp else 0.dp,
              modifier = Modifier
                .weight(1f)
                .clickable { selectedInvestmentTab = 2 }
                .testTag("tab_ipos")
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.Bolt,
                  contentDescription = null,
                  tint = if (isIpoSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "IPOs (${allIpos.size})",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = if (isIpoSelected) FontWeight.Bold else FontWeight.Medium,
                  color = if (isIpoSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }
      }

      // ----------------------------------------------------
      // TAB 0: STOCKS (NSE & BSE Equities)
      // ----------------------------------------------------
      if (selectedInvestmentTab == 0) {
        // Web API Live Sync & 6-Hour Cycle Status Bar
        item {
          WebMarketSyncBar(
            isUpdating = isUpdatingStockPrices,
            lastSyncTime = lastSyncTime,
            statusMessage = stockApiStatusMessage,
            onSyncNow = { viewModel.syncWithRealTimeData(force = true) }
          )
        }

        // Gemini Stock Suggestions (BUY, HOLD, SELL)
        if (allEquityStocks.isNotEmpty()) {
          item {
            val activeVerdict = geminiStockVerdict ?: remember(allEquityStocks, currencySymbol) {
              com.example.data.ai.GeminiStockAdvisor().generateSmartRuleBasedVerdict(allEquityStocks, currencySymbol)
            }
            GeminiStockAdvisorBanner(
              verdict = activeVerdict,
              isAnalyzing = isAnalyzingStocksWithGemini,
              currencySymbol = currencySymbol,
              onRefresh = { viewModel.analyzeStockPortfolioWithGemini(force = true) },
              onViewFullVerdict = { showFullGeminiVerdictDialog = true }
            )
          }
        }

        // Search & Filter Bar
        item {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
              value = searchQuery,
              onValueChange = {
                searchQuery = it
                viewModel.stockSearchQuery.value = it
              },
              placeholder = { Text("Search stocks, symbol, or company...") },
              leadingIcon = {
                Icon(
                  imageVector = Icons.Default.Search,
                  contentDescription = "Search",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
              },
              trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                  IconButton(onClick = {
                    searchQuery = ""
                    viewModel.stockSearchQuery.value = ""
                  }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear search")
                  }
                }
              },
              singleLine = true,
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("stock_search_input")
            )

            // Filter Chips Row
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              FilterChip(
                selected = sortOption == "VALUE_DESC",
                onClick = {
                  sortOption = "VALUE_DESC"
                  viewModel.stockSortOption.value = "VALUE_DESC"
                },
                label = { Text("Highest Value") },
                colors = FilterChipDefaults.filterChipColors(),
                modifier = Modifier.testTag("sort_value_desc")
              )
              FilterChip(
                selected = sortOption == "PNL_DESC",
                onClick = {
                  sortOption = "PNL_DESC"
                  viewModel.stockSortOption.value = "PNL_DESC"
                },
                label = { Text("Top Gainers") },
                colors = FilterChipDefaults.filterChipColors(),
                modifier = Modifier.testTag("sort_pnl_desc")
              )
              FilterChip(
                selected = sortOption == "PNL_ASC",
                onClick = {
                  sortOption = "PNL_ASC"
                  viewModel.stockSortOption.value = "PNL_ASC"
                },
                label = { Text("Top Losers") },
                colors = FilterChipDefaults.filterChipColors(),
                modifier = Modifier.testTag("sort_pnl_asc")
              )
            }
          }
        }

        // Portfolio Allocation Bar (if stocks available)
        if (allEquityStocks.isNotEmpty() && totalCurrentValue > 0) {
          item {
            PortfolioAllocationSection(
              stocks = allEquityStocks,
              totalValue = allEquityStocks.sumOf { it.currentValue },
              currencySymbol = currencySymbol
            )
          }
        }

        // Active Finnhub Real-Time Quote & Day Range Card
        if (activeFinnhubQuote != null) {
          item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "Live Stock Details (Finnhub)",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
                TextButton(
                  onClick = { viewModel.activeFinnhubQuote.value = null }
                ) {
                  Text("Dismiss", style = MaterialTheme.typography.labelSmall)
                }
              }
              FinnhubMarketMetricsCard(
                quote = activeFinnhubQuote!!,
                profile = activeFinnhubProfile,
                onViewCompanyProfile = { showCompanyProfileDialog = true }
              )
            }
          }
        }

        // Active Alpha Vantage Real-Time Quote & Fundamentals Card
        if (activeAlphaVantageQuote != null) {
          item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "Live Stock Details (Alpha Vantage)",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFFD35400)
                )
                TextButton(
                  onClick = { viewModel.activeAlphaVantageQuote.value = null }
                ) {
                  Text("Dismiss", style = MaterialTheme.typography.labelSmall)
                }
              }
              AlphaVantageMarketMetricsCard(
                quote = activeAlphaVantageQuote!!,
                overview = activeAlphaVantageOverview,
                onViewOverview = { showAlphaVantageOverviewDialog = true }
              )
            }
          }
        }

        // Stock Holdings Count Header
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Stock Holdings (${equityHoldings.size})",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              val isFhConfigured = viewModel.finnhubApiService.isConfigured(finnhubKey)
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isFhConfigured) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.clickable { showFinnhubQuickLookup = true }
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.Public,
                    contentDescription = "Finnhub Live",
                    tint = if (isFhConfigured) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(12.dp)
                  )
                  Text(
                    text = if (isFhConfigured) "Finnhub" else "Finnhub",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isFhConfigured) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                  )
                }
              }

              val isAvConfigured = viewModel.alphaVantageApiService.isConfigured(alphaVantageKey)
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isAvConfigured) Color(0xFFE67E22).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.clickable { showAlphaVantageQuickLookup = true }
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = "Alpha Vantage",
                    tint = if (isAvConfigured) Color(0xFFD35400) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(12.dp)
                  )
                  Text(
                    text = "Alpha Vantage",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isAvConfigured) Color(0xFFD35400) else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                  )
                }
              }

              Text(
                text = "Live NSE/BSE",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
              )
            }
          }
        }

        // Stock Items List
        if (equityHoldings.isEmpty()) {
          item {
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
              colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
              ),
              shape = RoundedCornerShape(16.dp)
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
              ) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.ShowChart,
                  contentDescription = null,
                  modifier = Modifier.size(44.dp),
                  tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                  text = if (searchQuery.isNotEmpty()) "No matching stocks" else "No stocks added yet",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = "Add stocks from the NSE & BSE Database to track Day's P&L and 6-hour automatic quote updates.",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
              }
            }
          }
        } else {
          items(equityHoldings, key = { it.id }) { stock ->
            StockHoldingCard(
              stock = stock,
              currencySymbol = currencySymbol,
              recommendation = geminiStockRecommendations[stock.symbol.uppercase()],
              onViewRecommendation = { selectedRecommendationDetail = it },
              onUpdatePrice = { stockToEditPrice = stock },
              onEdit = { stockToEdit = stock },
              onDelete = { stockToDelete = stock },
              onViewFinnhub = {
                viewModel.fetchFinnhubDetailsForStock(stock.symbol)
              }
            )
          }
        }
      }

      // ----------------------------------------------------
      // TAB 1: MUTUAL FUNDS & NFOs (AMFI Directory & Live NAV)
      // ----------------------------------------------------
      if (selectedInvestmentTab == 1) {
        // AMFI Mutual Fund Sync Bar
        item {
          MutualFundSyncBar(
            isUpdating = isUpdatingStockPrices,
            lastSyncTime = lastSyncTime,
            statusMessage = stockApiStatusMessage,
            onSyncNow = { viewModel.syncWithRealTimeData(force = true) }
          )
        }

        // Active NFOs (New Fund Offers) Carousel
        item {
          ActiveNfoOffersSection(
            currencySymbol = currencySymbol,
            onSelectNfo = { nfo ->
              prefilledNfo = nfo
              addInvestmentMode = "MUTUAL_FUND"
              showAddInvestmentDialog = true
            }
          )
        }

        // Mutual Fund SIPs (Systematic Investment Plans)
        item {
          MutualFundSipSection(
            sips = allSips,
            currencySymbol = currencySymbol,
            onCreateSip = { showCreateSipDialog = true },
            onExecuteSip = { sip -> viewModel.executeSipInstallment(sip) },
            onToggleActive = { sip -> viewModel.toggleSipActive(sip) },
            onDeleteSip = { sip -> viewModel.deleteSip(sip) }
          )
        }

        // Search & Category Chips for Mutual Funds
        item {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
              value = searchQuery,
              onValueChange = {
                searchQuery = it
                viewModel.stockSearchQuery.value = it
              },
              placeholder = { Text("Search funds by name or AMC...") },
              leadingIcon = {
                Icon(
                  imageVector = Icons.Default.Search,
                  contentDescription = "Search",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
              },
              trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                  IconButton(onClick = {
                    searchQuery = ""
                    viewModel.stockSearchQuery.value = ""
                  }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear search")
                  }
                }
              },
              singleLine = true,
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("mf_search_input")
            )

            // Category filter chips
            LazyRow(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              val categories = listOf("ALL", "NFO", "FLEXI_CAP", "MID_CAP", "SMALL_CAP", "LARGE_CAP", "INDEX", "ELSS", "HYBRID", "DEBT", "SECTORAL")
              items(categories) { cat ->
                FilterChip(
                  selected = mfCategoryFilter == cat,
                  onClick = { mfCategoryFilter = cat },
                  label = { Text(if (cat == "NFO") "⭐ Active NFOs" else cat.replace("_", " ")) },
                  colors = FilterChipDefaults.filterChipColors()
                )
              }
            }
          }
        }

        // Mutual Fund Holdings Header (physical font 'edit' removed)
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Fund Holdings (${mfHoldings.size})",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "AMFI NAV 6hr Sync",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.SemiBold
            )
          }
        }

        // Mutual Fund Items List
        if (mfHoldings.isEmpty()) {
          item {
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
              colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
              ),
              shape = RoundedCornerShape(16.dp)
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Savings,
                  contentDescription = null,
                  modifier = Modifier.size(44.dp),
                  tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                  text = if (searchQuery.isNotEmpty()) "No matching mutual funds" else "No mutual funds added yet",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = "Add mutual funds or open NFOs from the Indian AMFI directory to track Day's P&L and daily NAV cycles.",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
              }
            }
          }
        } else {
          items(mfHoldings, key = { it.id }) { fund ->
            MutualFundHoldingCard(
              stock = fund,
              currencySymbol = currencySymbol,
              onUpdatePrice = { stockToEditPrice = fund },
              onEdit = { stockToEdit = fund },
              onDelete = { stockToDelete = fund }
            )
          }
        }
      }

      // ----------------------------------------------------
      // TAB 2: IPOs (Indian Primary Market, Live GMP & Application)
      // ----------------------------------------------------
      if (selectedInvestmentTab == 2) {
        item {
          IpoSectionContent(
            viewModel = viewModel,
            currencySymbol = currencySymbol,
            onApplyIpo = { ipo ->
              addInvestmentMode = "STOCK"
              showAddInvestmentDialog = true
            }
          )
        }
      }
    }

    // FAB Add Investment (Adapts dynamically to Stocks vs Mutual Funds vs IPOs tab)
    FloatingActionButton(
      onClick = {
        addInvestmentMode = if (selectedInvestmentTab == 1) "MUTUAL_FUND" else "STOCK"
        prefilledNfo = null
        showAddInvestmentDialog = true
      },
      containerColor = MaterialTheme.colorScheme.primary,
      contentColor = MaterialTheme.colorScheme.onPrimary,
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(20.dp)
        .testTag("fab_add_investment")
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(Icons.Default.Add, contentDescription = null)
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = when (selectedInvestmentTab) {
            1 -> "Add Mutual Fund"
            2 -> "Apply IPO / Stock"
            else -> "Add Stock"
          },
          fontWeight = FontWeight.SemiBold
        )
      }
    }
  }

  // Google Search Grounding Sources & Queries Dialog
  if (showSearchSourcesDialog) {
    GoogleSearchSourcesDialog(
      searchQueries = realTimeSearchQueries,
      sources = realTimeSearchSources,
      onDismiss = { showSearchSourcesDialog = false }
    )
  }

  // Finnhub Company Profile Dialog
  if (showCompanyProfileDialog && activeFinnhubProfile != null) {
    FinnhubCompanyProfileDialog(
      profile = activeFinnhubProfile!!,
      onDismiss = { showCompanyProfileDialog = false }
    )
  }

  // Finnhub Quick Lookup Dialog
  if (showFinnhubQuickLookup) {
    val isFhConfigured = viewModel.finnhubApiService.isConfigured(finnhubKey)
    AlertDialog(
      onDismissRequest = { showFinnhubQuickLookup = false },
      title = {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Public,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
          )
          Column {
            Text(
              text = "Finnhub Market Explorer",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = if (isFhConfigured) "Real-time quotes & day range" else "API key required for live data",
              style = MaterialTheme.typography.labelSmall,
              color = if (isFhConfigured) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
          }
        }
      },
      text = {
        Column(
          verticalArrangement = Arrangement.spacedBy(12.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = "Query real-time stock prices, daily high/low trading ranges, open, prev close, and company details via Finnhub API.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          OutlinedTextField(
            value = finnhubLookupSymbol,
            onValueChange = { finnhubLookupSymbol = it.uppercase() },
            label = { Text("Stock Symbol / Ticker") },
            placeholder = { Text("e.g. AAPL, NVDA, TSLA, MSFT") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )

          // Quick popular ticker chips
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
              text = "Popular US Equities:",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            val popularSymbols = listOf("AAPL", "NVDA", "TSLA", "MSFT", "GOOGL", "AMZN")
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              popularSymbols.take(3).forEach { sym ->
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = MaterialTheme.colorScheme.surfaceVariant,
                  modifier = Modifier
                    .weight(1f)
                    .clickable { finnhubLookupSymbol = sym }
                ) {
                  Text(
                    text = sym,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(vertical = 6.dp)
                  )
                }
              }
            }
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              popularSymbols.drop(3).forEach { sym ->
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = MaterialTheme.colorScheme.surfaceVariant,
                  modifier = Modifier
                    .weight(1f)
                    .clickable { finnhubLookupSymbol = sym }
                ) {
                  Text(
                    text = sym,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(vertical = 6.dp)
                  )
                }
              }
            }
          }

          if (isLoadingFinnhub) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
              Text("Fetching live market data from Finnhub...", style = MaterialTheme.typography.bodySmall)
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (finnhubLookupSymbol.isNotBlank()) {
              viewModel.fetchFinnhubDetailsForStock(finnhubLookupSymbol)
              showFinnhubQuickLookup = false
            }
          },
          enabled = finnhubLookupSymbol.isNotBlank() && !isLoadingFinnhub
        ) {
          Text("Fetch Live Data")
        }
      },
      dismissButton = {
        TextButton(onClick = { showFinnhubQuickLookup = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // Alpha Vantage Quick Lookup Dialog
  if (showAlphaVantageQuickLookup) {
    val isAvConfigured = viewModel.alphaVantageApiService.isConfigured(alphaVantageKey)
    AlertDialog(
      onDismissRequest = { showAlphaVantageQuickLookup = false },
      title = {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
            contentDescription = null,
            tint = Color(0xFFE67E22)
          )
          Column {
            Text(
              text = "Alpha Vantage Market Explorer",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = if (isAvConfigured) "Real-time BSE, NSE & global quotes active" else "API key required for live data",
              style = MaterialTheme.typography.labelSmall,
              color = if (isAvConfigured) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
          }
        }
      },
      text = {
        Column(
          verticalArrangement = Arrangement.spacedBy(12.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = "Query real-time stock prices, daily open/high/low/close, trading volume, and company fundamentals via Alpha Vantage API.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          OutlinedTextField(
            value = alphaVantageLookupSymbol,
            onValueChange = { alphaVantageLookupSymbol = it.uppercase() },
            label = { Text("Stock Symbol / Ticker") },
            placeholder = { Text("e.g. TATAMOTORS, RELIANCE, INFY, IBM, AAPL") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )

          // Quick popular ticker chips
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
              text = "Popular Indian & Global Equities:",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            val popularSymbols = listOf("TATAMOTORS", "RELIANCE", "INFY", "TCS", "AAPL", "MSFT")
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              popularSymbols.take(3).forEach { sym ->
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = MaterialTheme.colorScheme.surfaceVariant,
                  modifier = Modifier
                    .weight(1f)
                    .clickable { alphaVantageLookupSymbol = sym }
                ) {
                  Text(
                    text = sym,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(vertical = 6.dp)
                  )
                }
              }
            }
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              popularSymbols.drop(3).forEach { sym ->
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = MaterialTheme.colorScheme.surfaceVariant,
                  modifier = Modifier
                    .weight(1f)
                    .clickable { alphaVantageLookupSymbol = sym }
                ) {
                  Text(
                    text = sym,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(vertical = 6.dp)
                  )
                }
              }
            }
          }

          if (isLoadingAlphaVantage) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
              Text("Fetching live market quote from Alpha Vantage...", style = MaterialTheme.typography.bodySmall)
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (alphaVantageLookupSymbol.isNotBlank()) {
              viewModel.fetchAlphaVantageDetailsForStock(alphaVantageLookupSymbol)
              showAlphaVantageQuickLookup = false
            }
          },
          enabled = alphaVantageLookupSymbol.isNotBlank() && !isLoadingAlphaVantage
        ) {
          Text("Fetch Live Data")
        }
      },
      dismissButton = {
        TextButton(onClick = { showAlphaVantageQuickLookup = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // Add Investment Dialog
  if (showAddInvestmentDialog) {
    AddInvestmentDialog(
      currencySymbol = currencySymbol,
      initialMode = addInvestmentMode,
      prefilledNfo = prefilledNfo,
      availableAccounts = allAccounts,
      onDismiss = {
        showAddInvestmentDialog = false
        prefilledNfo = null
      },
      onFetchLivePrice = { sym, callback ->
        viewModel.fetchLiveQuoteForSymbol(sym) { quote ->
          callback(quote?.regularMarketPrice)
        }
      },
      onConfirm = { symbol, name, shares, buyPrice, currentPrice, notes, assetType, debitAccount, debitFromAccount ->
        viewModel.buyInvestment(
          symbol = symbol,
          companyName = name,
          shares = shares,
          buyPrice = buyPrice,
          currentPrice = currentPrice,
          debitAccount = debitAccount,
          debitFromAccount = debitFromAccount,
          currencySymbol = currencySymbol,
          notes = notes,
          assetType = assetType
        )
        showAddInvestmentDialog = false
        prefilledNfo = null
      }
    )
  }

  // Edit Stock or Mutual Fund Dialog
  stockToEdit?.let { stock ->
    AddInvestmentDialog(
      currencySymbol = currencySymbol,
      initialStock = stock,
      initialMode = stock.assetType,
      availableAccounts = allAccounts,
      onDismiss = { stockToEdit = null },
      onFetchLivePrice = { sym, callback ->
        viewModel.fetchLiveQuoteForSymbol(sym) { quote ->
          callback(quote?.regularMarketPrice)
        }
      },
      onConfirm = { symbol, name, shares, buyPrice, currentPrice, notes, assetType, debitAccount, _ ->
        viewModel.updateStock(
          stock.copy(
            symbol = symbol,
            companyName = name,
            shares = shares,
            avgBuyPrice = buyPrice,
            currentPrice = currentPrice,
            notes = notes,
            assetType = assetType,
            debitAccount = debitAccount
          )
        )
        stockToEdit = null
      }
    )
  }

  // Create Mutual Fund SIP Dialog
  if (showCreateSipDialog) {
    CreateMutualFundSipDialog(
      availableAccounts = allAccounts,
      currencySymbol = currencySymbol,
      onDismiss = { showCreateSipDialog = false },
      onConfirm = { schemeCode, schemeName, installmentAmount, frequency, debitAccount, sipDayOfMonth, notes ->
        viewModel.createMutualFundSip(
          schemeCode = schemeCode,
          schemeName = schemeName,
          installmentAmount = installmentAmount,
          frequency = frequency,
          debitAccount = debitAccount,
          sipDayOfMonth = sipDayOfMonth,
          notes = notes
        )
        showCreateSipDialog = false
      }
    )
  }

  // Gemini Stock Detail Dialog
  selectedRecommendationDetail?.let { rec ->
    GeminiStockDetailDialog(
      recommendation = rec,
      currencySymbol = currencySymbol,
      onDismiss = { selectedRecommendationDetail = null }
    )
  }

  // Gemini Portfolio Full Verdict Dialog
  if (showFullGeminiVerdictDialog && geminiStockVerdict != null) {
    GeminiPortfolioAnalysisDialog(
      verdict = geminiStockVerdict!!,
      currencySymbol = currencySymbol,
      onDismiss = { showFullGeminiVerdictDialog = false }
    )
  }

  // Quick Price Update Dialog
  stockToEditPrice?.let { stock ->
    QuickUpdatePriceDialog(
      stock = stock,
      currencySymbol = currencySymbol,
      onDismiss = { stockToEditPrice = null },
      onConfirm = { newPrice ->
        viewModel.updateStockPrice(stock.id, newPrice)
        stockToEditPrice = null
      }
    )
  }

  // Delete Confirm Dialog
  stockToDelete?.let { stock ->
    AlertDialog(
      onDismissRequest = { stockToDelete = null },
      title = { Text("Delete ${stock.symbol}?") },
      text = { Text("Are you sure you want to remove ${stock.companyName} from your portfolio?") },
      confirmButton = {
        Button(
          onClick = {
            viewModel.deleteStock(stock)
            stockToDelete = null
          },
          colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error
          )
        ) {
          Text("Delete")
        }
      },
      dismissButton = {
        TextButton(onClick = { stockToDelete = null }) {
          Text("Cancel")
        }
      }
    )
  }
}

// 1. Portfolio Hero Card
@Composable
private fun PortfolioHeroCard(
  totalInvested: Double,
  currentValue: Double,
  totalPnl: Double,
  pnlPercentage: Double,
  dayPnl: Double = 0.0,
  dayPnlPercentage: Double = 0.0,
  stockCount: Int,
  currencySymbol: String
) {
  val isProfit = totalPnl >= 0
  val pnlColor = if (isProfit) Color(0xFF2ECC71) else Color(0xFFE74C3C)
  val pnlContainer = if (isProfit) Color(0xFF2ECC71).copy(alpha = 0.15f) else Color(0xFFE74C3C).copy(alpha = 0.15f)

  val isDayProfit = dayPnl >= 0
  val dayPnlColor = if (isDayProfit) Color(0xFF2ECC71) else Color(0xFFE74C3C)
  val dayPnlContainer = if (isDayProfit) Color(0xFF2ECC71).copy(alpha = 0.12f) else Color(0xFFE74C3C).copy(alpha = 0.12f)

  ElevatedCard(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("portfolio_hero_card"),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.elevatedCardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(20.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Total Investment Value",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "$currencySymbol${formatStockAmount(currentValue)}",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        // Returns Badges (Total Returns & Day's Returns)
        Column(horizontalAlignment = Alignment.End) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = pnlContainer
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = if (isProfit) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                contentDescription = null,
                tint = pnlColor,
                modifier = Modifier.size(15.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Total: ${if (isProfit) "+" else ""}${String.format(Locale.US, "%.2f", pnlPercentage)}%",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = pnlColor
              )
            }
          }

          Spacer(modifier = Modifier.height(4.dp))

          Surface(
            shape = RoundedCornerShape(10.dp),
            color = dayPnlContainer
          ) {
            Text(
              text = "1D: ${if (isDayProfit) "+" else ""}$currencySymbol${formatStockAmount(kotlin.math.abs(dayPnl))} (${if (isDayProfit) "+" else ""}${String.format(Locale.US, "%.2f", dayPnlPercentage)}%)",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = dayPnlColor,
              fontSize = 10.sp,
              modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Secondary Stats Row: Buy Amount (Invested), Day's P&L, Total P&L, Assets Count
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
          .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        // Buy Amount (Invested)
        Column {
          Text(
            text = "Invested",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "$currencySymbol${formatStockAmount(totalInvested)}",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold
          )
        }

        // Day's P&L
        Column {
          Text(
            text = "1D P&L",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "${if (isDayProfit) "+" else "-"}$currencySymbol${formatStockAmount(kotlin.math.abs(dayPnl))}",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = dayPnlColor
          )
        }

        // Total Profit / Loss
        Column {
          Text(
            text = "Total P&L",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "${if (isProfit) "+" else "-"}$currencySymbol${formatStockAmount(kotlin.math.abs(totalPnl))}",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = pnlColor
          )
        }

        // Holdings Count
        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = "Holdings",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "$stockCount Assets",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold
          )
        }
      }
    }
  }
}

// 2. Portfolio Allocation Bar Section
@Composable
private fun PortfolioAllocationSection(
  stocks: List<StockEntity>,
  totalValue: Double,
  currencySymbol: String
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    )
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Text(
        text = "Portfolio Allocation",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold
      )
      Spacer(modifier = Modifier.height(10.dp))

      // Allocation bar with colored slices
      val colors = listOf(
        Color(0xFF3498DB),
        Color(0xFF9B59B6),
        Color(0xFF2ECC71),
        Color(0xFFE67E22),
        Color(0xFF1ABC9C),
        Color(0xFFE74C3C)
      )

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .height(10.dp)
          .clip(RoundedCornerShape(5.dp))
      ) {
        stocks.forEachIndexed { index, stock ->
          val fraction = if (totalValue > 0) (stock.currentValue / totalValue).toFloat() else 0f
          if (fraction > 0f) {
            Box(
              modifier = Modifier
                .weight(fraction.coerceAtLeast(0.01f))
                .height(10.dp)
                .background(colors[index % colors.size])
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Top allocations preview
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        stocks.take(4).forEachIndexed { index, stock ->
          val pct = if (totalValue > 0) ((stock.currentValue / totalValue) * 100).toInt() else 0
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(colors[index % colors.size])
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "${stock.symbol} $pct%",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }
  }
}

// 2. Web API Live Sync & 6-Hour Cycle Bar
@Composable
private fun WebMarketSyncBar(
  isUpdating: Boolean,
  lastSyncTime: Long,
  statusMessage: String?,
  onSyncNow: () -> Unit
) {
  val syncTimeText = if (lastSyncTime > 0) {
    SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(lastSyncTime))
  } else "Pending"

  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    ),
    modifier = Modifier.fillMaxWidth().testTag("web_market_sync_bar")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f)
      ) {
        Box(
          modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(Color(0xFF2ECC71).copy(alpha = 0.18f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.CloudSync,
            contentDescription = null,
            tint = Color(0xFF27AE60),
            modifier = Modifier.size(18.dp)
          )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "Real-Time Market Sync",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = Color(0xFF4285F4).copy(alpha = 0.12f),
              border = BorderStroke(1.dp, Color(0xFF4285F4).copy(alpha = 0.3f))
            ) {
              Text(
                text = "Google Grounded",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF1967D2),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }
          Text(
            text = if (isUpdating) "Grounding with Google Search in real-time..." else "Last sync: $syncTimeText • Live quotes current",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
          )
        }
      }

      Button(
        onClick = onSyncNow,
        enabled = !isUpdating,
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        modifier = Modifier.height(34.dp).testTag("btn_sync_stock_prices")
      ) {
        if (isUpdating) {
          CircularProgressIndicator(
            modifier = Modifier.size(14.dp),
            strokeWidth = 2.dp,
            color = MaterialTheme.colorScheme.onPrimary
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text("Syncing", fontSize = 11.sp)
        } else {
          Icon(
            imageVector = Icons.Default.CloudSync,
            contentDescription = null,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text("Sync Live", fontSize = 11.sp)
        }
      }
    }
  }
}

// 2b. Mutual Fund & AMFI Live Sync Bar (6-hour cycle + Daily NFO update)
@Composable
private fun MutualFundSyncBar(
  isUpdating: Boolean,
  lastSyncTime: Long,
  statusMessage: String?,
  onSyncNow: () -> Unit
) {
  val syncTimeText = if (lastSyncTime > 0) {
    SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(lastSyncTime))
  } else "Daily Active"

  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    ),
    modifier = Modifier.fillMaxWidth().testTag("mutual_fund_sync_bar")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f)
      ) {
        Box(
          modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(Color(0xFF8E44AD).copy(alpha = 0.18f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.CloudSync,
            contentDescription = null,
            tint = Color(0xFF8E44AD),
            modifier = Modifier.size(18.dp)
          )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "AMFI & MFAPI Directory",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = MaterialTheme.colorScheme.primaryContainer
            ) {
              Text(
                text = "6hr cycle • NFOs",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontSize = 10.sp,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }
          Text(
            text = if (isUpdating) "Updating mutual fund NAVs from AMFI..." else "Last sync: $syncTimeText • Real-time NAV",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
          )
        }
      }

      Button(
        onClick = onSyncNow,
        enabled = !isUpdating,
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        modifier = Modifier.height(34.dp).testTag("btn_sync_mf_prices")
      ) {
        if (isUpdating) {
          CircularProgressIndicator(
            modifier = Modifier.size(14.dp),
            strokeWidth = 2.dp,
            color = MaterialTheme.colorScheme.onPrimary
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text("Syncing", fontSize = 11.sp)
        } else {
          Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = null,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text("Sync", fontSize = 11.sp)
        }
      }
    }
  }
}

// 2c. Active NFO Offers Section (New Fund Offers from AMFI)
@Composable
private fun ActiveNfoOffersSection(
  currencySymbol: String,
  onSelectNfo: (MutualFundItem) -> Unit
) {
  val nfos = remember { MutualFundCatalog.NFO_CATALOG }

  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Star,
          contentDescription = null,
          tint = Color(0xFFF39C12),
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "Open NFO Offers (${nfos.size})",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold
        )
      }
      Text(
        text = "Base NAV $currencySymbol 10.00",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold
      )
    }

    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      items(nfos) { nfo ->
        ElevatedCard(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
          ),
          elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
          modifier = Modifier.width(260.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFFE67E22).copy(alpha = 0.15f)
              ) {
                Text(
                  text = "NFO • ${nfo.category.replace("_", " ")}",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFFE67E22),
                  fontSize = 10.sp,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
              Text(
                text = "Min $currencySymbol${nfo.minSipAmount.toInt()}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
              text = nfo.schemeName,
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Bold,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis
            )

            Text(
              text = nfo.amc,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = "Issue Price",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  fontSize = 10.sp
                )
                Text(
                  text = "$currencySymbol${String.format(Locale.US, "%.2f", nfo.nav)}",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
              }

              Button(
                onClick = { onSelectNfo(nfo) },
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(30.dp)
              ) {
                Text("+ Add", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }
    }
  }
}

// 3. Gemini Stock Suggestions Banner (Always Shown & Auto-Analyzed)
@Composable
private fun GeminiStockAdvisorBanner(
  verdict: GeminiPortfolioStockVerdict?,
  isAnalyzing: Boolean,
  currencySymbol: String,
  onRefresh: () -> Unit,
  onViewFullVerdict: () -> Unit
) {
  ElevatedCard(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.elevatedCardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
    modifier = Modifier.fillMaxWidth().testTag("gemini_stock_advisor_banner")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f)
        ) {
          Box(
            modifier = Modifier
              .size(34.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(
                androidx.compose.ui.graphics.Brush.linearGradient(
                  listOf(Color(0xFF673AB7), Color(0xFF2196F3))
                )
              ),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.AutoAwesome,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(18.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "Gemini Stock Suggestions",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "BUY • SELL • HOLD",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
              )
              if (verdict?.isGroundedWithGoogleSearch == true) {
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                  shape = RoundedCornerShape(4.dp),
                  color = Color(0xFF4285F4).copy(alpha = 0.12f)
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(
                      imageVector = Icons.Default.Public,
                      contentDescription = null,
                      tint = Color(0xFF1967D2),
                      modifier = Modifier.size(8.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                      text = "Search Grounded",
                      style = MaterialTheme.typography.labelSmall,
                      color = Color(0xFF1967D2),
                      fontSize = 9.sp,
                      fontWeight = FontWeight.Bold
                    )
                  }
                }
              }
            }
          }
        }

        // Live Auto-Analysis Status Pill & Quick Refresh
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          if (isAnalyzing) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                CircularProgressIndicator(
                  modifier = Modifier.size(10.dp),
                  strokeWidth = 2.dp,
                  color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Analyzing...",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.primary,
                  fontSize = 10.sp
                )
              }
            }
          } else {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color(0xFF673AB7).copy(alpha = 0.12f)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2ECC71))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Always Live",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF673AB7),
                  fontSize = 10.sp
                )
              }
            }
          }

          IconButton(
            onClick = onRefresh,
            modifier = Modifier.size(28.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = "Re-analyze stocks",
              modifier = Modifier.size(16.dp),
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      if (verdict != null) {
        val buyCount = verdict.buyCount
        val holdCount = verdict.holdCount
        val sellCount = verdict.sellCount

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .padding(horizontal = 10.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = verdict.overallStance,
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = verdict.portfolioSummary,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis
            )
          }

          Spacer(modifier = Modifier.width(8.dp))

          // Mini consensus badges
          Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            if (buyCount > 0) {
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFF2ECC71).copy(alpha = 0.18f)
              ) {
                Text(
                  text = "$buyCount BUY",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF2ECC71),
                  fontSize = 10.sp,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }
            if (holdCount > 0) {
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFFF39C12).copy(alpha = 0.18f)
              ) {
                Text(
                  text = "$holdCount HOLD",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFFF39C12),
                  fontSize = 10.sp,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }
            if (sellCount > 0) {
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFFE74C3C).copy(alpha = 0.18f)
              ) {
                Text(
                  text = "$sellCount SELL",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFFE74C3C),
                  fontSize = 10.sp,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End
        ) {
          TextButton(
            onClick = onViewFullVerdict,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
            modifier = Modifier.height(28.dp)
          ) {
            Text(
              text = "View Detailed Gemini Analysis →",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.SemiBold
            )
          }
        }
      } else {
        Text(
          text = "Gemini AI is analyzing your portfolio stocks in real-time. Buy, Hold, and Sell suggestions are updated automatically.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}

// 4. Stock Holding Card (Equity Holdings)
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun StockHoldingCard(
  stock: StockEntity,
  currencySymbol: String,
  recommendation: GeminiStockRecommendation? = null,
  onViewRecommendation: (GeminiStockRecommendation) -> Unit = {},
  onUpdatePrice: () -> Unit,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
  onViewFinnhub: () -> Unit = {}
) {
  val isProfit = stock.isProfit
  val pnlColor = if (isProfit) Color(0xFF2ECC71) else Color(0xFFE74C3C)
  val pnlBg = if (isProfit) Color(0xFF2ECC71).copy(alpha = 0.12f) else Color(0xFFE74C3C).copy(alpha = 0.12f)

  val dayPnl = stock.dayPnl
  val isDayProfit = dayPnl >= 0
  val dayPnlColor = if (isDayProfit) Color(0xFF2ECC71) else Color(0xFFE74C3C)
  val dayPnlBg = dayPnlColor.copy(alpha = 0.12f)

  ElevatedCard(
    modifier = Modifier
      .fillMaxWidth()
      .combinedClickable(
        onClick = { onEdit() },
        onDoubleClick = { onEdit() }
      )
      .testTag("stock_item_${stock.symbol}"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.elevatedCardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      // Top Row: Symbol, Company name, and quick actions
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f)
        ) {
          // Symbol Avatar
          Box(
            modifier = Modifier
              .size(42.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = stock.symbol.take(3),
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onPrimaryContainer
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column {
            Text(
              text = stock.symbol,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = stock.companyName,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }

        // Action Icons: Finnhub Live Metrics, Quick Update Price, Delete
        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = onViewFinnhub,
            modifier = Modifier.size(34.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Public,
              contentDescription = "Finnhub Live Quote & Range",
              tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
              modifier = Modifier.size(18.dp)
            )
          }
          IconButton(
            onClick = onUpdatePrice,
            modifier = Modifier.size(34.dp)
          ) {
            Icon(
              imageVector = Icons.Default.PriceChange,
              contentDescription = "Update Price",
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(18.dp)
            )
          }
          IconButton(
            onClick = onDelete,
            modifier = Modifier.size(34.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Delete,
              contentDescription = "Delete Stock",
              tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      // Gemini AI Suggestion Badge (BUY, SELL, HOLD)
      val effectiveRec = recommendation ?: remember(stock.id, stock.currentPrice, stock.avgBuyPrice, currencySymbol) {
        com.example.data.ai.GeminiStockAdvisor().generateFallbackRecommendation(stock, currencySymbol)
      }
      Spacer(modifier = Modifier.height(10.dp))
      val recAction = effectiveRec.action.name
      val recColor = when (effectiveRec.action) {
        StockRecommendationAction.BUY -> Color(0xFF2ECC71)
        StockRecommendationAction.SELL -> Color(0xFFE74C3C)
        else -> Color(0xFFF39C12)
      }
      val targetText = effectiveRec.targetPrice?.let {
        "Target: $currencySymbol${formatStockAmount(it)} →"
      } ?: "View Details →"

      Surface(
        shape = RoundedCornerShape(8.dp),
        color = recColor.copy(alpha = 0.12f),
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onViewRecommendation(effectiveRec) }
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.AutoAwesome,
              contentDescription = null,
              tint = recColor,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Gemini: $recAction (${effectiveRec.confidenceScore}% conf)",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = recColor
            )
          }
          Text(
            text = targetText,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = recColor,
            fontSize = 10.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Middle Details Grid: Shares, Avg Buy, Current Price
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(10.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
          .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column {
          Text(
            text = "Shares",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = if (stock.shares % 1.0 == 0.0) "${stock.shares.toInt()}" else String.format(Locale.US, "%.2f", stock.shares),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
          )
        }

        Column {
          Text(
            text = "Avg Buy Price",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "$currencySymbol${formatStockAmount(stock.avgBuyPrice)}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
          )
        }

        Column(horizontalAlignment = Alignment.End) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "Current Price",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (stock.dailyChangePercent != 0.0) {
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "${if (stock.dailyChangePercent > 0) "+" else ""}${String.format(Locale.US, "%.1f", stock.dailyChangePercent)}%",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (stock.dailyChangePercent >= 0) Color(0xFF2ECC71) else Color(0xFFE74C3C),
                fontSize = 10.sp
              )
            }
          }
          Text(
            text = "$currencySymbol${formatStockAmount(stock.currentPrice)}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Bottom Row: Total Invested vs Current Value, and P&L Badges
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "Invested: ",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = "$currencySymbol${formatStockAmount(stock.investedAmount)}",
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.Medium
            )
          }
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "Value: ",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = "$currencySymbol${formatStockAmount(stock.currentValue)}",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Bold
            )
          }
        }

        // Right side: Total P&L + Day's P&L Badges
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
          // Total Profit / Loss Badge
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = pnlBg
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = if (isProfit) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                contentDescription = null,
                tint = pnlColor,
                modifier = Modifier.size(12.dp)
              )
              Spacer(modifier = Modifier.width(3.dp))
              Text(
                text = "Total: ${if (isProfit) "+" else "-"}$currencySymbol${formatStockAmount(kotlin.math.abs(stock.totalPnl))} (${if (isProfit) "+" else ""}${String.format(Locale.US, "%.1f", stock.pnlPercentage)}%)",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = pnlColor
              )
            }
          }

          // Day's P&L Badge
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = dayPnlBg
          ) {
            Text(
              text = "1D: ${if (isDayProfit) "+" else ""}$currencySymbol${formatStockAmount(kotlin.math.abs(dayPnl))} (${if (isDayProfit) "+" else ""}${String.format(Locale.US, "%.2f", stock.dailyChangePercent)}%)",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = dayPnlColor,
              fontSize = 11.sp,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
          }
        }
      }

      if (stock.notes.isNotBlank()) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = stock.notes,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
    }
  }
}

// 5. Mutual Fund Holding Card (AMFI Schemas & NFOs)
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MutualFundHoldingCard(
  stock: StockEntity,
  currencySymbol: String,
  onUpdatePrice: () -> Unit,
  onEdit: () -> Unit,
  onDelete: () -> Unit
) {
  val isProfit = stock.isProfit
  val pnlColor = if (isProfit) Color(0xFF2ECC71) else Color(0xFFE74C3C)
  val pnlBg = if (isProfit) Color(0xFF2ECC71).copy(alpha = 0.12f) else Color(0xFFE74C3C).copy(alpha = 0.12f)

  val dayPnl = stock.dayPnl
  val isDayProfit = dayPnl >= 0
  val dayPnlColor = if (isDayProfit) Color(0xFF2ECC71) else Color(0xFFE74C3C)
  val dayPnlBg = dayPnlColor.copy(alpha = 0.12f)

  ElevatedCard(
    modifier = Modifier
      .fillMaxWidth()
      .combinedClickable(
        onClick = { onEdit() },
        onDoubleClick = { onEdit() }
      )
      .testTag("fund_item_${stock.symbol}"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.elevatedCardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      // Top Row: Avatar, Fund Name, Actions
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f)
        ) {
          Box(
            modifier = Modifier
              .size(42.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(Color(0xFF8E44AD).copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Savings,
              contentDescription = null,
              tint = Color(0xFF8E44AD),
              modifier = Modifier.size(22.dp)
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = stock.companyName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
              )
              if (stock.symbol.startsWith("NFO")) {
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                  shape = RoundedCornerShape(4.dp),
                  color = Color(0xFFE67E22).copy(alpha = 0.18f)
                ) {
                  Text(
                    text = "NFO",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFE67E22),
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                  )
                }
              }
            }
            Text(
              text = "Scheme Code: ${stock.symbol}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 11.sp
            )
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = onUpdatePrice,
            modifier = Modifier.size(34.dp)
          ) {
            Icon(
              imageVector = Icons.Default.PriceChange,
              contentDescription = "Update NAV",
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(18.dp)
            )
          }
          IconButton(
            onClick = onDelete,
            modifier = Modifier.size(34.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Delete,
              contentDescription = "Delete Fund",
              tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Middle Details: Units, Purchase NAV, Current NAV
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(10.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
          .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column {
          Text(
            text = "Units",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = String.format(Locale.US, "%.3f", stock.shares),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
          )
        }

        Column {
          Text(
            text = "Avg NAV",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "$currencySymbol${formatStockAmount(stock.avgBuyPrice)}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
          )
        }

        Column(horizontalAlignment = Alignment.End) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "Current NAV",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (stock.dailyChangePercent != 0.0) {
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "${if (stock.dailyChangePercent > 0) "+" else ""}${String.format(Locale.US, "%.1f", stock.dailyChangePercent)}%",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (stock.dailyChangePercent >= 0) Color(0xFF2ECC71) else Color(0xFFE74C3C),
                fontSize = 10.sp
              )
            }
          }
          Text(
            text = "$currencySymbol${formatStockAmount(stock.currentPrice)}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Bottom Row: Invested vs Current Value, Day's P&L and Total P&L
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "Invested: ",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = "$currencySymbol${formatStockAmount(stock.investedAmount)}",
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.Medium
            )
          }
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "Value: ",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = "$currencySymbol${formatStockAmount(stock.currentValue)}",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Bold
            )
          }
        }

        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
          // Total Profit / Loss Badge
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = pnlBg
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = if (isProfit) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                contentDescription = null,
                tint = pnlColor,
                modifier = Modifier.size(12.dp)
              )
              Spacer(modifier = Modifier.width(3.dp))
              Text(
                text = "Total: ${if (isProfit) "+" else "-"}$currencySymbol${formatStockAmount(kotlin.math.abs(stock.totalPnl))} (${if (isProfit) "+" else ""}${String.format(Locale.US, "%.1f", stock.pnlPercentage)}%)",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = pnlColor
              )
            }
          }

          // Day's P&L Badge
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = dayPnlBg
          ) {
            Text(
              text = "1D: ${if (isDayProfit) "+" else ""}$currencySymbol${formatStockAmount(kotlin.math.abs(dayPnl))} (${if (isDayProfit) "+" else ""}${String.format(Locale.US, "%.2f", stock.dailyChangePercent)}%)",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = dayPnlColor,
              fontSize = 11.sp,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
          }
        }
      }

      if (stock.notes.isNotBlank()) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = stock.notes,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
    }
  }
}

// 6. Add / Edit Investment Dialog with Stocks (NSE/BSE) & Mutual Funds (AMFI/NFO)
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddInvestmentDialog(
  currencySymbol: String,
  initialStock: StockEntity? = null,
  initialMode: String = "STOCK",
  prefilledNfo: MutualFundItem? = null,
  availableAccounts: List<AccountEntity> = emptyList(),
  onDismiss: () -> Unit,
  onFetchLivePrice: (String, (Double?) -> Unit) -> Unit = { _, _ -> },
  onConfirm: (
    symbol: String,
    name: String,
    shares: Double,
    buyPrice: Double,
    currentPrice: Double,
    notes: String,
    assetType: String,
    debitAccount: String,
    debitFromAccount: Boolean
  ) -> Unit
) {
  var activeAssetType by remember {
    mutableStateOf(initialStock?.assetType ?: (if (prefilledNfo != null) "MUTUAL_FUND" else initialMode))
  }

  var symbol by remember {
    mutableStateOf(initialStock?.symbol ?: prefilledNfo?.schemeCode ?: "")
  }
  var name by remember {
    mutableStateOf(initialStock?.companyName ?: prefilledNfo?.schemeName ?: "")
  }
  var sharesStr by remember {
    mutableStateOf(initialStock?.shares?.let { if (it % 1.0 == 0.0) "${it.toInt()}" else "$it" } ?: "")
  }
  var buyPriceStr by remember {
    mutableStateOf(initialStock?.avgBuyPrice?.let { "$it" } ?: prefilledNfo?.nav?.let { "$it" } ?: "")
  }
  var currentPriceStr by remember {
    mutableStateOf(initialStock?.currentPrice?.let { "$it" } ?: prefilledNfo?.nav?.let { "$it" } ?: "")
  }
  var notes by remember {
    mutableStateOf(initialStock?.notes ?: if (prefilledNfo != null) "NFO Application" else "")
  }
  var selectedDebitAccount by remember {
    mutableStateOf(initialStock?.debitAccount?.ifBlank { null } ?: availableAccounts.firstOrNull()?.name ?: "Main Checking")
  }
  var debitFromAccount by remember {
    mutableStateOf(initialStock == null)
  }
  var amountToInvestStr by remember { mutableStateOf("") }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  var isFetchingLiveQuote by remember { mutableStateOf(false) }

  // Stock catalog state
  var stockCatalogCategory by remember { mutableStateOf("ALL") }
  var stockCatalogQuery by remember { mutableStateOf("") }
  var showStockCatalogSection by remember { mutableStateOf(initialStock == null && activeAssetType == "STOCK") }

  // MF catalog state
  var mfCatalogCategory by remember { mutableStateOf("ALL") }
  var mfCatalogQuery by remember { mutableStateOf("") }
  var showMfCatalogSection by remember { mutableStateOf(initialStock == null && activeAssetType == "MUTUAL_FUND") }
  val mfApiService = remember { com.example.data.api.MutualFundApiService() }
  var isSearchingOnlineMf by remember { mutableStateOf(false) }
  var onlineMfResults by remember { mutableStateOf<List<MutualFundItem>>(emptyList()) }

  androidx.compose.runtime.LaunchedEffect(mfCatalogQuery) {
    val q = mfCatalogQuery.trim()
    if (q.length >= 3) {
      isSearchingOnlineMf = true
      kotlinx.coroutines.delay(450)
      try {
        val results = mfApiService.searchAllIndiaFunds(q)
        onlineMfResults = results
      } catch (_: Exception) {
        onlineMfResults = emptyList()
      } finally {
        isSearchingOnlineMf = false
      }
    } else {
      onlineMfResults = emptyList()
      isSearchingOnlineMf = false
    }
  }

  val stockCatalogResults = remember(stockCatalogQuery, stockCatalogCategory) {
    StockDatabaseCatalog.search(stockCatalogQuery, stockCatalogCategory).take(30)
  }

  val mfCatalogResults = remember(mfCatalogQuery, mfCatalogCategory, onlineMfResults) {
    if (onlineMfResults.isNotEmpty()) {
      onlineMfResults.take(30)
    } else {
      MutualFundCatalog.searchMutualFunds(mfCatalogQuery, mfCatalogCategory).take(30)
    }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Column {
        Text(
          text = if (initialStock == null) "Add Investment Holding" else "Edit ${initialStock.symbol}",
          fontWeight = FontWeight.Bold
        )

        if (initialStock == null) {
          Spacer(modifier = Modifier.height(10.dp))
          // Asset Type Selector Pill
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(3.dp),
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              val isStock = activeAssetType == "STOCK"
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isStock) MaterialTheme.colorScheme.surface else Color.Transparent,
                shadowElevation = if (isStock) 2.dp else 0.dp,
                modifier = Modifier
                  .weight(1f)
                  .clickable {
                    activeAssetType = "STOCK"
                    showStockCatalogSection = true
                  }
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                  horizontalArrangement = Arrangement.Center,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = Icons.AutoMirrored.Filled.ShowChart,
                    contentDescription = null,
                    tint = if (isStock) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "Stock",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isStock) FontWeight.Bold else FontWeight.Medium,
                    color = if (isStock) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }

              val isMf = activeAssetType == "MUTUAL_FUND"
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isMf) MaterialTheme.colorScheme.surface else Color.Transparent,
                shadowElevation = if (isMf) 2.dp else 0.dp,
                modifier = Modifier
                  .weight(1f)
                  .clickable {
                    activeAssetType = "MUTUAL_FUND"
                    showMfCatalogSection = true
                  }
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                  horizontalArrangement = Arrangement.Center,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = Icons.Default.Savings,
                    contentDescription = null,
                    tint = if (isMf) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "Mutual Fund / NFO",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isMf) FontWeight.Bold else FontWeight.Medium,
                    color = if (isMf) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }
          }
        }
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // ----------------------------------------------------
        // DIRECTORY CATALOG: STOCKS
        // ----------------------------------------------------
        if (initialStock == null && activeAssetType == "STOCK") {
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            ),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.CloudDownload,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "NSE & BSE Directory",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                  )
                }
                TextButton(
                  onClick = { showStockCatalogSection = !showStockCatalogSection },
                  contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                  modifier = Modifier.height(24.dp)
                ) {
                  Text(
                    text = if (showStockCatalogSection) "Hide" else "Browse",
                    style = MaterialTheme.typography.labelSmall
                  )
                }
              }

              AnimatedVisibility(visible = showStockCatalogSection) {
                Column(
                  modifier = Modifier.fillMaxWidth(),
                  verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Spacer(modifier = Modifier.height(2.dp))
                  // Search field
                  OutlinedTextField(
                    value = stockCatalogQuery,
                    onValueChange = { stockCatalogQuery = it },
                    placeholder = { Text("Search NSE/BSE stocks...", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                  )

                  stockCatalogResults.forEach { item ->
                    Surface(
                      shape = RoundedCornerShape(8.dp),
                      color = MaterialTheme.colorScheme.surface,
                      modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                          symbol = item.symbol
                          name = item.name
                          currentPriceStr = "${item.approximatePrice}"
                          if (buyPriceStr.isEmpty()) buyPriceStr = "${item.approximatePrice}"
                          isFetchingLiveQuote = true
                          onFetchLivePrice(item.symbol) { livePrice ->
                            isFetchingLiveQuote = false
                            if (livePrice != null && livePrice > 0) {
                              currentPriceStr = "$livePrice"
                            }
                          }
                        }
                    ) {
                      Row(
                        modifier = Modifier
                          .fillMaxWidth()
                          .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        Column(modifier = Modifier.weight(1f)) {
                          Text(
                            text = item.symbol,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                          )
                          Text(
                            text = "${item.name} • ${item.sector}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 11.sp
                          )
                        }
                        Text(
                          text = "Select",
                          style = MaterialTheme.typography.labelSmall,
                          color = MaterialTheme.colorScheme.primary,
                          fontWeight = FontWeight.Bold
                        )
                      }
                    }
                  }
                }
              }
            }
          }
        }

        // ----------------------------------------------------
        // DIRECTORY CATALOG: MUTUAL FUNDS & NFOs
        // ----------------------------------------------------
        if (initialStock == null && activeAssetType == "MUTUAL_FUND") {
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            ),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.Savings,
                    contentDescription = null,
                    tint = Color(0xFF8E44AD),
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "AMFI Directory & NFOs (${MutualFundCatalog.getAllMutualFunds().size}+)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                  )
                }
                TextButton(
                  onClick = { showMfCatalogSection = !showMfCatalogSection },
                  contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                  modifier = Modifier.height(24.dp)
                ) {
                  Text(
                    text = if (showMfCatalogSection) "Hide" else "Browse",
                    style = MaterialTheme.typography.labelSmall
                  )
                }
              }

              AnimatedVisibility(visible = showMfCatalogSection) {
                Column(
                  modifier = Modifier.fillMaxWidth(),
                  verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Spacer(modifier = Modifier.height(2.dp))
                  // Search field
                  OutlinedTextField(
                    value = mfCatalogQuery,
                    onValueChange = { mfCatalogQuery = it },
                    placeholder = { Text("Search all Indian funds (e.g. Parag Parikh, Quant, HDFC)...", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                  )

                  if (isSearchingOnlineMf) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                      CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp)
                      Spacer(modifier = Modifier.width(6.dp))
                      Text("Searching live across all Indian mutual funds...", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                    }
                  } else if (onlineMfResults.isNotEmpty()) {
                    Text("Found ${onlineMfResults.size} live schemes from AMFI", fontSize = 10.sp, color = Color(0xFF27AE60), modifier = Modifier.padding(start = 4.dp))
                  }

                  val mfBrowseCategories = listOf("ALL", "FLEXI_CAP", "MID_CAP", "SMALL_CAP", "LARGE_CAP", "INDEX", "SECTORAL", "ELSS", "HYBRID", "DEBT")
                  LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    items(mfBrowseCategories) { cat ->
                      FilterChip(
                        selected = mfCatalogCategory == cat,
                        onClick = { mfCatalogCategory = cat },
                        label = { Text(cat.replace("_", " "), fontSize = 10.sp) }
                      )
                    }
                  }

                  mfCatalogResults.forEach { item ->
                    Surface(
                      shape = RoundedCornerShape(8.dp),
                      color = MaterialTheme.colorScheme.surface,
                      modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                          symbol = item.schemeCode
                          name = item.schemeName
                          currentPriceStr = "${item.nav}"
                          if (buyPriceStr.isEmpty()) buyPriceStr = "${item.nav}"
                          notes = if (item.isNfo) "NFO Issue" else "${item.category.replace("_", " ")} SIP"
                        }
                    ) {
                      Row(
                        modifier = Modifier
                          .fillMaxWidth()
                          .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        Column(modifier = Modifier.weight(1f)) {
                          Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                              text = item.schemeName,
                              style = MaterialTheme.typography.labelMedium,
                              fontWeight = FontWeight.Bold,
                              maxLines = 1,
                              overflow = TextOverflow.Ellipsis
                            )
                            if (item.isNfo) {
                              Spacer(modifier = Modifier.width(4.dp))
                              Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFE67E22).copy(alpha = 0.2f)
                              ) {
                                Text(
                                  text = "NFO",
                                  style = MaterialTheme.typography.labelSmall,
                                  color = Color(0xFFE67E22),
                                  fontSize = 8.sp,
                                  fontWeight = FontWeight.Bold,
                                  modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                                )
                              }
                            }
                          }
                          Text(
                            text = "${item.amc} • NAV $currencySymbol${item.nav}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                          )
                        }
                        Text(
                          text = "Select",
                          style = MaterialTheme.typography.labelSmall,
                          color = Color(0xFF8E44AD),
                          fontWeight = FontWeight.Bold
                        )
                      }
                    }
                  }
                }
              }
            }
          }
        }

        // Symbol / Scheme Code
        OutlinedTextField(
          value = symbol,
          onValueChange = { symbol = it.uppercase() },
          label = { Text(if (activeAssetType == "MUTUAL_FUND") "Scheme Code / ID *" else "Stock Symbol *") },
          placeholder = { Text(if (activeAssetType == "MUTUAL_FUND") "e.g. 119551 or NFO-HDFC" else "e.g. RELIANCE, TCS") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("input_stock_symbol")
        )

        // Company / Scheme Name
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text(if (activeAssetType == "MUTUAL_FUND") "Scheme Name *" else "Company Name *") },
          placeholder = { Text(if (activeAssetType == "MUTUAL_FUND") "e.g. Parag Parikh Flexi Cap Fund" else "e.g. Reliance Industries") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("input_stock_name")
        )

        // Quick Units Auto-Calculator for Mutual Funds
        if (activeAssetType == "MUTUAL_FUND") {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedTextField(
              value = amountToInvestStr,
              onValueChange = { amt ->
                amountToInvestStr = amt
                val amount = amt.toDoubleOrNull()
                val nav = buyPriceStr.toDoubleOrNull() ?: currentPriceStr.toDoubleOrNull()
                if (amount != null && nav != null && nav > 0) {
                  sharesStr = String.format(Locale.US, "%.3f", amount / nav)
                }
              },
              label = { Text("Amount to Invest ($currencySymbol)") },
              placeholder = { Text("5000") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              singleLine = true,
              modifier = Modifier.weight(1f)
            )

            Button(
              onClick = {
                val amount = amountToInvestStr.toDoubleOrNull()
                val nav = buyPriceStr.toDoubleOrNull() ?: currentPriceStr.toDoubleOrNull()
                if (amount != null && nav != null && nav > 0) {
                  sharesStr = String.format(Locale.US, "%.3f", amount / nav)
                }
              },
              shape = RoundedCornerShape(10.dp),
              contentPadding = PaddingValues(horizontal = 8.dp),
              modifier = Modifier.height(48.dp)
            ) {
              Text("Calc Units", fontSize = 11.sp)
            }
          }
        }

        // Shares / Units and Buy Price / NAV
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = sharesStr,
            onValueChange = { sharesStr = it },
            label = { Text(if (activeAssetType == "MUTUAL_FUND") "Units *" else "Shares *") },
            placeholder = { Text(if (activeAssetType == "MUTUAL_FUND") "50.450" else "10") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.weight(1f).testTag("input_stock_shares")
          )

          OutlinedTextField(
            value = buyPriceStr,
            onValueChange = {
              buyPriceStr = it
              if (currentPriceStr.isEmpty()) currentPriceStr = it
            },
            label = { Text(if (activeAssetType == "MUTUAL_FUND") "Purchase NAV ($currencySymbol) *" else "Buy Price ($currencySymbol) *") },
            placeholder = { Text(if (activeAssetType == "MUTUAL_FUND") "10.00" else "2450.00") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.weight(1f).testTag("input_stock_buy_price")
          )
        }

        // Current Price / Current NAV
        OutlinedTextField(
          value = currentPriceStr,
          onValueChange = { currentPriceStr = it },
          label = { Text(if (activeAssetType == "MUTUAL_FUND") "Current NAV ($currencySymbol) *" else "Current Price ($currencySymbol) *") },
          placeholder = { Text(if (activeAssetType == "MUTUAL_FUND") "10.00" else "2890.00") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          trailingIcon = {
            if (activeAssetType == "STOCK" && symbol.isNotBlank()) {
              IconButton(
                onClick = {
                  isFetchingLiveQuote = true
                  onFetchLivePrice(symbol) { livePrice ->
                    isFetchingLiveQuote = false
                    if (livePrice != null && livePrice > 0) {
                      currentPriceStr = "$livePrice"
                    }
                  }
                },
                enabled = !isFetchingLiveQuote
              ) {
                if (isFetchingLiveQuote) {
                  CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                  Icon(
                    imageVector = Icons.Default.CloudDownload,
                    contentDescription = "Fetch Live Quote",
                    tint = MaterialTheme.colorScheme.primary
                  )
                }
              }
            }
          },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("input_stock_current_price")
        )

        // Notes
        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text("Notes (Optional)") },
          placeholder = { Text(if (activeAssetType == "MUTUAL_FUND") "e.g. Monthly SIP, Tax Saving ELSS" else "e.g. Bluechip, dividend") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        // Account Debit Selection & Transaction Integration Card
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
          ),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
          modifier = Modifier.fillMaxWidth().testTag("card_debit_account_selector")
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Checkbox(
                checked = debitFromAccount,
                onCheckedChange = { debitFromAccount = it },
                modifier = Modifier.size(24.dp).testTag("checkbox_debit_from_account")
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = "Debit money from account",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "Records transaction with buy price, units & debit account",
                  style = MaterialTheme.typography.bodySmall,
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            if (debitFromAccount) {
              Spacer(modifier = Modifier.height(10.dp))
              Text(
                text = "Select Account to Debit:",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.height(6.dp))

              FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                val accountsList = if (availableAccounts.isNotEmpty()) availableAccounts
                else listOf(AccountEntity(name = "Main Checking", type = "CHECKING", balance = 0.0))

                accountsList.forEach { acc ->
                  val isSelected = acc.name == selectedDebitAccount
                  Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) Color(0xFF1B664B) else MaterialTheme.colorScheme.surface,
                    contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                    border = BorderStroke(1.dp, if (isSelected) Color(0xFF1B664B) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.clickable { selectedDebitAccount = acc.name }
                  ) {
                    Row(
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Text(
                        text = acc.name,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                      )
                      Spacer(modifier = Modifier.width(4.dp))
                      Text(
                        text = "($currencySymbol${String.format(Locale.US, "%.0f", acc.balance)})",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = if (isSelected) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    }
                  }
                }
              }

              val totalCost = (sharesStr.toDoubleOrNull() ?: 0.0) * (buyPriceStr.toDoubleOrNull() ?: 0.0)
              if (totalCost > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = Color(0xFF1B664B).copy(alpha = 0.1f),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = "Total to Debit:",
                      style = MaterialTheme.typography.labelSmall,
                      color = Color(0xFF1B664B),
                      fontWeight = FontWeight.SemiBold
                    )
                    Text(
                      text = "-$currencySymbol${String.format(Locale.US, "%,.2f", totalCost)} from $selectedDebitAccount",
                      style = MaterialTheme.typography.labelSmall,
                      color = Color(0xFF1B664B),
                      fontWeight = FontWeight.Bold
                    )
                  }
                }
              }
            }
          }
        }

        errorMessage?.let {
          Text(
            text = it,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val s = symbol.trim()
          val n = name.trim()
          val shares = sharesStr.toDoubleOrNull()
          val buy = buyPriceStr.toDoubleOrNull()
          val curr = currentPriceStr.toDoubleOrNull()

          if (s.isBlank()) {
            errorMessage = if (activeAssetType == "MUTUAL_FUND") "Please enter scheme code / identifier." else "Please enter a stock symbol."
          } else if (n.isBlank()) {
            errorMessage = if (activeAssetType == "MUTUAL_FUND") "Please enter scheme name." else "Please enter company name."
          } else if (shares == null || shares <= 0) {
            errorMessage = if (activeAssetType == "MUTUAL_FUND") "Please enter valid units." else "Please enter valid number of shares."
          } else if (buy == null || buy <= 0) {
            errorMessage = if (activeAssetType == "MUTUAL_FUND") "Please enter valid purchase NAV." else "Please enter valid buy price."
          } else if (curr == null || curr <= 0) {
            errorMessage = if (activeAssetType == "MUTUAL_FUND") "Please enter valid current NAV." else "Please enter valid current price."
          } else {
            onConfirm(s, n, shares, buy, curr, notes, activeAssetType, selectedDebitAccount, debitFromAccount)
          }
        },
        modifier = Modifier.testTag("submit_investment_button")
      ) {
        Text(if (initialStock == null) "Add to Portfolio" else "Save Changes")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}

// 6. Gemini Stock Detail Dialog (Single Stock Verdict)
@Composable
private fun GeminiStockDetailDialog(
  recommendation: GeminiStockRecommendation,
  currencySymbol: String,
  onDismiss: () -> Unit
) {
  val actionColor = when (recommendation.action) {
    StockRecommendationAction.BUY -> Color(0xFF2ECC71)
    StockRecommendationAction.SELL -> Color(0xFFE74C3C)
    else -> Color(0xFFF39C12)
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = actionColor,
            modifier = Modifier.size(22.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "${recommendation.symbol} AI Verdict",
            fontWeight = FontWeight.Bold
          )
        }
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = actionColor.copy(alpha = 0.15f)
        ) {
          Text(
            text = recommendation.action.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = actionColor,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
          )
        }
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Targets & Metrics Grid
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            .padding(12.dp),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column {
            Text(
              text = "Target Price",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = recommendation.targetPrice?.let { "$currencySymbol${formatStockAmount(it)}" } ?: "N/A",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF2ECC71)
            )
          }

          Column {
            Text(
              text = "Stop Loss",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = recommendation.stopLossPrice?.let { "$currencySymbol${formatStockAmount(it)}" } ?: "N/A",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Bold,
              color = Color(0xFFE74C3C)
            )
          }

          Column(horizontalAlignment = Alignment.End) {
            Text(
              text = "Confidence",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = "${recommendation.confidenceScore}%",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
          }
        }

        // Risk Level Badge
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text(
            text = "Risk Level:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = when (recommendation.riskLevel.uppercase()) {
              "LOW" -> Color(0xFF2ECC71).copy(alpha = 0.15f)
              "HIGH" -> Color(0xFFE74C3C).copy(alpha = 0.15f)
              else -> Color(0xFFF39C12).copy(alpha = 0.15f)
            }
          ) {
            Text(
              text = recommendation.riskLevel.uppercase(),
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = when (recommendation.riskLevel.uppercase()) {
                "LOW" -> Color(0xFF2ECC71)
                "HIGH" -> Color(0xFFE74C3C)
                else -> Color(0xFFF39C12)
              },
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
          }
        }

        // Investment Thesis / Reasoning
        Column {
          Text(
            text = "Gemini Analysis & Thesis",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = recommendation.rationale,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        // Key Catalysts
        if (recommendation.keyCatalysts.isNotEmpty()) {
          Column {
            Text(
              text = "Key Market Catalysts",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = recommendation.keyCatalysts.joinToString("\n• ", prefix = "• "),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    },
    confirmButton = {
      Button(onClick = onDismiss) {
        Text("Done")
      }
    }
  )
}

// 7. Gemini Portfolio Analysis Dialog (All Stocks Consensus)
@Composable
private fun GeminiPortfolioAnalysisDialog(
  verdict: GeminiPortfolioStockVerdict,
  currencySymbol: String,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.AutoAwesome,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Gemini Portfolio Consensus", fontWeight = FontWeight.Bold)
      }
    },
    text = {
      LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        item {
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            ),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
            ) {
              Text(
                text = verdict.overallStance,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = verdict.portfolioSummary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }

        item {
          Text(
            text = "Stock Recommendations (${verdict.recommendations.size})",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
          )
        }

        items(verdict.recommendations) { rec ->
          val actionColor = when (rec.action) {
            StockRecommendationAction.BUY -> Color(0xFF2ECC71)
            StockRecommendationAction.SELL -> Color(0xFFE74C3C)
            else -> Color(0xFFF39C12)
          }

          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            ),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = rec.symbol,
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold
                )
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = actionColor.copy(alpha = 0.15f)
                ) {
                  Text(
                    text = "${rec.action.name} (${rec.confidenceScore}%)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = actionColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                  )
                }
              }

              Spacer(modifier = Modifier.height(4.dp))
              val targetStr = rec.targetPrice?.let { "$currencySymbol${formatStockAmount(it)}" } ?: "N/A"
              val stopLossStr = rec.stopLossPrice?.let { "$currencySymbol${formatStockAmount(it)}" } ?: "N/A"
              Text(
                text = "Target: $targetStr • Stop Loss: $stopLossStr • Risk: ${rec.riskLevel}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = rec.rationale,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }
      }
    },
    confirmButton = {
      Button(onClick = onDismiss) {
        Text("Close")
      }
    }
  )
}

// 5. Quick Update Price Dialog
@Composable
private fun QuickUpdatePriceDialog(
  stock: StockEntity,
  currencySymbol: String,
  onDismiss: () -> Unit,
  onConfirm: (Double) -> Unit
) {
  var priceStr by remember { mutableStateOf(stock.currentPrice.toString()) }
  var error by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Update Current Price") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = "Enter updated market price for ${stock.symbol} (${stock.companyName})",
          style = MaterialTheme.typography.bodyMedium
        )
        Text(
          text = "Avg Buy Price: $currencySymbol${formatStockAmount(stock.avgBuyPrice)} | Shares: ${stock.shares}",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
          value = priceStr,
          onValueChange = {
            priceStr = it
            error = false
          },
          label = { Text("Current Price ($currencySymbol)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          isError = error,
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("input_update_price")
        )
        if (error) {
          Text("Please enter a valid positive price", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val p = priceStr.toDoubleOrNull()
          if (p != null && p > 0) {
            onConfirm(p)
          } else {
            error = true
          }
        },
        modifier = Modifier.testTag("confirm_update_price")
      ) {
        Text("Update Price")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}

private fun formatStockAmount(amount: Double): String {
  return String.format(Locale.US, "%,.2f", amount)
}

// 7. Mutual Fund SIPs Section & SIP Item Card
@Composable
private fun MutualFundSipSection(
  sips: List<MutualFundSipEntity>,
  currencySymbol: String,
  onCreateSip: () -> Unit,
  onExecuteSip: (MutualFundSipEntity) -> Unit,
  onToggleActive: (MutualFundSipEntity) -> Unit,
  onDeleteSip: (MutualFundSipEntity) -> Unit
) {
  val activeCount = sips.count { it.isActive }
  val totalMonthlyCommitment = sips.filter { it.isActive }.sumOf {
    when (it.frequency.lowercase()) {
      "weekly" -> it.installmentAmount * 4.33
      "bi-weekly", "bi-weekly (2 weeks)" -> it.installmentAmount * 2.16
      "quarterly" -> it.installmentAmount / 3.0
      else -> it.installmentAmount
    }
  }

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    modifier = Modifier.fillMaxWidth().testTag("section_mutual_fund_sips")
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Surface(
            shape = CircleShape,
            color = Color(0xFF8E44AD).copy(alpha = 0.12f),
            modifier = Modifier.size(32.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Default.Savings,
                contentDescription = null,
                tint = Color(0xFF8E44AD),
                modifier = Modifier.size(18.dp)
              )
            }
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "Mutual Fund SIPs",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              Spacer(modifier = Modifier.width(6.dp))
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (activeCount > 0) Color(0xFF8E44AD).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
              ) {
                Text(
                  text = "$activeCount Active",
                  style = MaterialTheme.typography.labelSmall,
                  color = if (activeCount > 0) Color(0xFF8E44AD) else MaterialTheme.colorScheme.onSurfaceVariant,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }
            if (activeCount > 0) {
              Text(
                text = "Commitment: $currencySymbol${String.format(Locale.US, "%,.0f", totalMonthlyCommitment)}/mo",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
              )
            }
          }
        }

        Button(
          onClick = onCreateSip,
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E44AD)),
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
          modifier = Modifier.height(34.dp).testTag("button_open_create_sip")
        ) {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("New SIP", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      if (sips.isEmpty()) {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = "No Systematic Investment Plans active",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Set up recurring mutual fund SIPs with automated debits from your preferred account on your chosen execution day.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = androidx.compose.ui.text.style.TextAlign.Center,
              fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            androidx.compose.material3.OutlinedButton(
              onClick = onCreateSip,
              shape = RoundedCornerShape(8.dp)
            ) {
              Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Create First SIP")
            }
          }
        }
      } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          sips.forEach { sip ->
            SipItemCard(
              sip = sip,
              currencySymbol = currencySymbol,
              onExecuteNow = { onExecuteSip(sip) },
              onToggleActive = { onToggleActive(sip) },
              onDelete = { onDeleteSip(sip) }
            )
          }
        }
      }
    }
  }
}

@Composable
private fun SipItemCard(
  sip: MutualFundSipEntity,
  currencySymbol: String,
  onExecuteNow: () -> Unit,
  onToggleActive: () -> Unit,
  onDelete: () -> Unit
) {
  val nextDateStr = remember(sip.nextExecutionDate) {
    SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date(sip.nextExecutionDate))
  }

  Surface(
    shape = RoundedCornerShape(12.dp),
    color = if (sip.isActive) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
    border = BorderStroke(1.dp, if (sip.isActive) Color(0xFF8E44AD).copy(alpha = 0.3f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
    modifier = Modifier.fillMaxWidth().testTag("sip_card_${sip.id}")
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = sip.schemeName,
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
              shape = RoundedCornerShape(4.dp),
              color = if (sip.isActive) Color(0xFF2ECC71).copy(alpha = 0.15f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
            ) {
              Text(
                text = if (sip.isActive) "ACTIVE" else "PAUSED",
                style = MaterialTheme.typography.labelSmall,
                color = if (sip.isActive) Color(0xFF27AE60) else MaterialTheme.colorScheme.outline,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "Code: ${sip.schemeCode} • Day ${sip.sipDayOfMonth} of month",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
          )
        }

        // Installment badge
        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = "$currencySymbol${String.format(Locale.US, "%,.0f", sip.installmentAmount)}",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF8E44AD)
          )
          Text(
            text = "/ ${sip.frequency}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Meta chips
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.surface
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = sip.debitAccount,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
              )
            }
          }

          Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.surface
          ) {
            Text(
              text = "Next: $nextDateStr",
              style = MaterialTheme.typography.labelSmall,
              fontSize = 10.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }

        // Action icons
        Row(verticalAlignment = Alignment.CenterVertically) {
          // Quick Execute Now button
          IconButton(
            onClick = onExecuteNow,
            modifier = Modifier.size(28.dp).testTag("button_execute_sip_${sip.id}")
          ) {
            Icon(
              imageVector = Icons.Default.Bolt,
              contentDescription = "Execute Installment Now",
              tint = Color(0xFFF39C12),
              modifier = Modifier.size(18.dp)
            )
          }

          // Toggle pause/resume
          IconButton(
            onClick = onToggleActive,
            modifier = Modifier.size(28.dp)
          ) {
            Icon(
              imageVector = if (sip.isActive) Icons.Default.Pause else Icons.Default.PlayArrow,
              contentDescription = if (sip.isActive) "Pause SIP" else "Resume SIP",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(16.dp)
            )
          }

          // Delete
          IconButton(
            onClick = onDelete,
            modifier = Modifier.size(28.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Delete,
              contentDescription = "Delete SIP",
              tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }

      if (sip.installmentsCompleted > 0) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "✅ ${sip.installmentsCompleted} installment${if (sip.installmentsCompleted > 1) "s" else ""} completed ($currencySymbol${String.format(Locale.US, "%,.0f", sip.totalInvested)} invested)",
          style = MaterialTheme.typography.labelSmall,
          fontSize = 10.sp,
          color = Color(0xFF27AE60)
        )
      }
    }
  }
}
