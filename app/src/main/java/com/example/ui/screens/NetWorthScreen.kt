package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountEntity
import com.example.data.model.ExpenseEntity
import com.example.data.model.LoanEntity
import com.example.data.model.StockEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class NetWorthPoint(
  val timestamp: Long,
  val dateLabel: String,
  val value: Double
)

@Composable
fun NetWorthScreen(
  netWorth: Double,
  accounts: List<AccountEntity>,
  expenses: List<ExpenseEntity>,
  stocks: List<StockEntity>,
  loans: List<LoanEntity>,
  totalLent: Double,
  totalBorrowed: Double,
  income: Double = 0.0,
  expense: Double = 0.0,
  currencySymbol: String = "₹",
  onAddAccount: () -> Unit = {},
  onAddStock: () -> Unit = {},
  onNavigateToLoans: () -> Unit = {},
  onAccountClick: (AccountEntity) -> Unit = {},
  modifier: Modifier = Modifier
) {
  var selectedTimeframe by remember { mutableStateOf("1M") } // 1W, 1M, 3M, 6M, 1Y, ALL
  var showAccountsSection by remember { mutableStateOf(true) }
  var showInvestmentsSection by remember { mutableStateOf(true) }
  var showLiabilitiesSection by remember { mutableStateOf(true) }

  // Calculations
  val totalLiquidCash = remember(accounts) {
    accounts.filter { it.balance > 0 }.sumOf { it.balance }
  }
  val totalStocksValue = remember(stocks) {
    stocks.filter { it.assetType.uppercase() == "STOCK" }.sumOf { it.shares * it.currentPrice }
  }
  val totalMfValue = remember(stocks) {
    stocks.filter { it.assetType.uppercase() == "MUTUAL_FUND" }.sumOf { it.shares * it.currentPrice }
  }
  val totalInvestments = totalStocksValue + totalMfValue
  val totalReceivables = totalLent
  val totalLiabilities = totalBorrowed

  // Historical data points for interactive graph
  val chartPoints = remember(selectedTimeframe, netWorth, expenses, accounts, stocks) {
    generateHistoricalNetWorthPoints(
      currentNetWorth = netWorth,
      expenses = expenses,
      timeframe = selectedTimeframe
    )
  }

  val startValue = chartPoints.firstOrNull()?.value ?: netWorth
  val endValue = chartPoints.lastOrNull()?.value ?: netWorth
  val deltaValue = endValue - startValue
  val deltaPercent = if (startValue > 0) (deltaValue / startValue) * 100.0 else 0.0
  val isPositiveGrowth = deltaValue >= 0

  val minVal = (chartPoints.minOfOrNull { it.value } ?: 0.0).coerceAtLeast(0.0)
  val maxVal = (chartPoints.maxOfOrNull { it.value } ?: (netWorth * 1.1)).coerceAtLeast(minVal + 100.0)

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("screen_net_worth"),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. HERO NET WORTH SUMMARY CARD
    item {
      Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("net_worth_hero_card")
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
            Row(verticalAlignment = Alignment.CenterVertically) {
              Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                modifier = Modifier.size(40.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = Icons.Default.AccountBalance,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                  )
                }
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "Total Net Worth",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = "All accounts, investments & loans",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  fontSize = 11.sp
                )
              }
            }

            // Period Delta Badge
            Surface(
              shape = RoundedCornerShape(20.dp),
              color = if (isPositiveGrowth) Color(0xFF2ECC71).copy(alpha = 0.15f) else Color(0xFFFF5B5B).copy(alpha = 0.15f),
              border = BorderStroke(
                1.dp,
                if (isPositiveGrowth) Color(0xFF2ECC71).copy(alpha = 0.4f) else Color(0xFFFF5B5B).copy(alpha = 0.4f)
              )
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = if (isPositiveGrowth) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                  contentDescription = null,
                  tint = if (isPositiveGrowth) Color(0xFF27AE60) else Color(0xFFE74C3C),
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "${if (isPositiveGrowth) "+" else ""}${String.format(Locale.US, "%.1f", deltaPercent)}%",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = if (isPositiveGrowth) Color(0xFF27AE60) else Color(0xFFE74C3C)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Big Main Net Worth Amount
          Text(
            text = "$currencySymbol${formatAmount(netWorth)}",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.testTag("net_worth_total_amount")
          )

          Spacer(modifier = Modifier.height(16.dp))

          // 4-Quadrant Mini Stats (Liquid vs Invested vs Lent vs Borrowed)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            MiniAssetStatCard(
              title = "Liquid Cash",
              amount = totalLiquidCash,
              currencySymbol = currencySymbol,
              color = Color(0xFF1B664B),
              modifier = Modifier.weight(1f)
            )
            MiniAssetStatCard(
              title = "Investments",
              amount = totalInvestments,
              currencySymbol = currencySymbol,
              color = Color(0xFF2980B9),
              modifier = Modifier.weight(1f)
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            MiniAssetStatCard(
              title = "Money Lent",
              amount = totalReceivables,
              currencySymbol = currencySymbol,
              color = Color(0xFFD35400),
              modifier = Modifier.weight(1f)
            )
            MiniAssetStatCard(
              title = "Liabilities",
              amount = totalLiabilities,
              currencySymbol = currencySymbol,
              color = Color(0xFFC0392B),
              modifier = Modifier.weight(1f)
            )
          }
        }
      }
    }

    // 2. INTERACTIVE NET WORTH GRAPH CARD
    item {
      Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("net_worth_graph_card")
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Net Worth Progression",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "Historical valuation curve",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            Surface(
              shape = RoundedCornerShape(10.dp),
              color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            ) {
              Text(
                text = "${if (isPositiveGrowth) "+" else ""}$currencySymbol${formatAmount(deltaValue)}",
                style = MaterialTheme.typography.labelSmall,
                color = if (isPositiveGrowth) Color(0xFF27AE60) else Color(0xFFE74C3C),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Timeframe Selector Chips
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            listOf("1W", "1M", "3M", "6M", "1Y", "ALL").forEach { tf ->
              val isSelected = selectedTimeframe == tf
              FilterChip(
                selected = isSelected,
                onClick = { selectedTimeframe = tf },
                label = {
                  Text(
                    text = tf,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                  )
                },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = MaterialTheme.colorScheme.primary,
                  selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                ),
                border = BorderStroke(
                  1.dp,
                  if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                  .weight(1f)
                  .height(34.dp)
                  .testTag("chip_net_worth_tf_$tf")
              )
            }
          }

          Spacer(modifier = Modifier.height(18.dp))

          // Interactive Canvas Line Chart
          InteractiveNetWorthChart(
            points = chartPoints,
            currencySymbol = currencySymbol,
            minVal = minVal,
            maxVal = maxVal,
            isPositiveGrowth = isPositiveGrowth,
            modifier = Modifier
              .fillMaxWidth()
              .height(200.dp)
          )

          Spacer(modifier = Modifier.height(12.dp))

          // Min / Max Footnotes
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "Low: $currencySymbol${formatAmount(minVal)}",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 10.sp
            )
            Text(
              text = "High: $currencySymbol${formatAmount(maxVal)}",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 10.sp
            )
          }
        }
      }
    }

    // 3. ASSET ALLOCATION BREAKDOWN BAR
    item {
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth().testTag("net_worth_asset_allocation_card")
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Text(
            text = "Asset Allocation",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )

          val totalAssets = (totalLiquidCash + totalInvestments + totalReceivables).coerceAtLeast(1.0)
          val cashFraction = (totalLiquidCash / totalAssets).toFloat()
          val stocksFraction = (totalStocksValue / totalAssets).toFloat()
          val mfFraction = (totalMfValue / totalAssets).toFloat()
          val lentFraction = (totalReceivables / totalAssets).toFloat()

          // Multi-color Stacked Progress Bar
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .height(14.dp)
              .clip(RoundedCornerShape(8.dp))
          ) {
            if (cashFraction > 0) {
              Box(
                modifier = Modifier
                  .weight(cashFraction.coerceAtLeast(0.01f))
                  .background(Color(0xFF1B664B))
              )
            }
            if (stocksFraction > 0) {
              Box(
                modifier = Modifier
                  .weight(stocksFraction.coerceAtLeast(0.01f))
                  .background(Color(0xFF2980B9))
              )
            }
            if (mfFraction > 0) {
              Box(
                modifier = Modifier
                  .weight(mfFraction.coerceAtLeast(0.01f))
                  .background(Color(0xFF8E44AD))
              )
            }
            if (lentFraction > 0) {
              Box(
                modifier = Modifier
                  .weight(lentFraction.coerceAtLeast(0.01f))
                  .background(Color(0xFFE67E22))
              )
            }
          }

          // Legend
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            AssetLegendItem(label = "Cash/Bank", percentage = (cashFraction * 100).toInt(), color = Color(0xFF1B664B))
            AssetLegendItem(label = "Stocks", percentage = (stocksFraction * 100).toInt(), color = Color(0xFF2980B9))
            AssetLegendItem(label = "Mutual Funds", percentage = (mfFraction * 100).toInt(), color = Color(0xFF8E44AD))
            AssetLegendItem(label = "Lent", percentage = (lentFraction * 100).toInt(), color = Color(0xFFE67E22))
          }
        }
      }
    }

    // 4. ACCOUNTS ACCORDION
    item {
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth().testTag("net_worth_accounts_accordion")
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { showAccountsSection = !showAccountsSection },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.AccountBalanceWallet,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Bank Accounts (${accounts.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "$currencySymbol${formatAmount(totalLiquidCash)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
              )
              Spacer(modifier = Modifier.width(4.dp))
              Icon(
                imageVector = if (showAccountsSection) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          AnimatedVisibility(visible = showAccountsSection) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              if (accounts.isEmpty()) {
                Text(
                  text = "No bank accounts added yet.",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              } else {
                accounts.forEach { acc ->
                  Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier
                      .fillMaxWidth()
                      .clickable { onAccountClick(acc) }
                  ) {
                    Row(
                      modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Column {
                        Text(
                          text = acc.name,
                          style = MaterialTheme.typography.bodyMedium,
                          fontWeight = FontWeight.SemiBold,
                          color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                          text = acc.type.replace("_", " "),
                          style = MaterialTheme.typography.labelSmall,
                          color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                      }
                      Text(
                        text = "$currencySymbol${formatAmount(acc.balance)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (acc.balance >= 0) MaterialTheme.colorScheme.onSurface else Color(0xFFFF5B5B)
                      )
                    }
                  }
                }
              }

              OutlinedButton(
                onClick = onAddAccount,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().testTag("btn_net_worth_add_account")
              ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add New Bank Account")
              }
            }
          }
        }
      }
    }

    // 5. INVESTMENTS (STOCKS & MUTUAL FUNDS) ACCORDION
    item {
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth().testTag("net_worth_investments_accordion")
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { showInvestmentsSection = !showInvestmentsSection },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ShowChart,
                contentDescription = null,
                tint = Color(0xFF2980B9),
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Investments (${stocks.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "$currencySymbol${formatAmount(totalInvestments)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2980B9)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Icon(
                imageVector = if (showInvestmentsSection) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          AnimatedVisibility(visible = showInvestmentsSection) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              if (stocks.isEmpty()) {
                Text(
                  text = "No stock or mutual fund holdings tracked yet.",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              } else {
                stocks.take(6).forEach { stk ->
                  val isMf = stk.assetType.uppercase() == "MUTUAL_FUND"
                  val curVal = stk.shares * stk.currentPrice
                  Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Row(
                      modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                          shape = CircleShape,
                          color = if (isMf) Color(0xFF8E44AD).copy(alpha = 0.15f) else Color(0xFF2980B9).copy(alpha = 0.15f),
                          modifier = Modifier.size(32.dp)
                        ) {
                          Box(contentAlignment = Alignment.Center) {
                            Icon(
                              imageVector = if (isMf) Icons.Default.Savings else Icons.AutoMirrored.Filled.ShowChart,
                              contentDescription = null,
                              tint = if (isMf) Color(0xFF8E44AD) else Color(0xFF2980B9),
                              modifier = Modifier.size(16.dp)
                            )
                          }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                          Text(
                            text = stk.symbol,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                          )
                          Text(
                            text = if (isMf) "Mutual Fund • ${stk.shares} units" else "${stk.companyName} • ${stk.shares.toInt()} shares",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                          )
                        }
                      }
                      Text(
                        text = "$currencySymbol${formatAmount(curVal)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                      )
                    }
                  }
                }
              }

              Button(
                onClick = onAddStock,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2980B9)),
                modifier = Modifier.fillMaxWidth().testTag("btn_net_worth_open_invest")
              ) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Manage Portfolio & Add Holdings")
              }
            }
          }
        }
      }
    }

    // 6. LIABILITIES & RECEIVABLES ACCORDION
    item {
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth().testTag("net_worth_liabilities_accordion")
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { showLiabilitiesSection = !showLiabilitiesSection },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Handshake,
                contentDescription = null,
                tint = Color(0xFFC0392B),
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Debts & Receivables",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "Net: $currencySymbol${formatAmount(totalReceivables - totalLiabilities)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (totalReceivables >= totalLiabilities) Color(0xFF27AE60) else Color(0xFFC0392B)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Icon(
                imageVector = if (showLiabilitiesSection) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          AnimatedVisibility(visible = showLiabilitiesSection) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column {
                    Text(
                      text = "Money Lent (Assets)",
                      style = MaterialTheme.typography.bodyMedium,
                      fontWeight = FontWeight.SemiBold,
                      color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                      text = "Receivables owed to you",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                  Text(
                    text = "$currencySymbol${formatAmount(totalReceivables)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF27AE60)
                  )
                }
              }

              Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column {
                    Text(
                      text = "Money Borrowed (Liabilities)",
                      style = MaterialTheme.typography.bodyMedium,
                      fontWeight = FontWeight.SemiBold,
                      color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                      text = "Loans and debts to repay",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                  Text(
                    text = "$currencySymbol${formatAmount(totalLiabilities)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFC0392B)
                  )
                }
              }

              OutlinedButton(
                onClick = onNavigateToLoans,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().testTag("btn_net_worth_open_loans")
              ) {
                Icon(Icons.Default.Handshake, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Manage Loans & Debts")
              }
            }
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
private fun MiniAssetStatCard(
  title: String,
  amount: Double,
  currencySymbol: String,
  color: Color,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
    modifier = modifier
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 11.sp
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "$currencySymbol${formatAmount(amount)}",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

@Composable
private fun AssetLegendItem(
  label: String,
  percentage: Int,
  color: Color
) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Box(
      modifier = Modifier
        .size(8.dp)
        .clip(CircleShape)
        .background(color)
    )
    Spacer(modifier = Modifier.width(4.dp))
    Text(
      text = "$label ($percentage%)",
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      fontSize = 10.sp
    )
  }
}

@Composable
private fun InteractiveNetWorthChart(
  points: List<NetWorthPoint>,
  currencySymbol: String,
  minVal: Double,
  maxVal: Double,
  isPositiveGrowth: Boolean,
  modifier: Modifier = Modifier
) {
  var selectedIndex by remember(points) { mutableStateOf<Int?>(null) }
  val primaryColor = MaterialTheme.colorScheme.primary
  val onSurfaceColor = MaterialTheme.colorScheme.onSurface
  val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
  val outlineColor = MaterialTheme.colorScheme.outlineVariant

  val range = (maxVal - minVal).coerceAtLeast(1.0)

  Column(modifier = modifier) {
    // Tooltip Banner when inspecting point
    val activeIndex = selectedIndex
    if (activeIndex != null && activeIndex in points.indices) {
      val pt = points[activeIndex]
      Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier
          .align(Alignment.CenterHorizontally)
          .padding(bottom = 6.dp)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "${pt.dateLabel}: ",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
          )
          Text(
            text = "$currencySymbol${formatAmount(pt.value)}",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
          )
        }
      }
    } else {
      Spacer(modifier = Modifier.height(26.dp))
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
      val w = constraints.maxWidth.toFloat()
      val h = constraints.maxHeight.toFloat()

      Canvas(
        modifier = Modifier
          .fillMaxSize()
          .pointerInput(points) {
            detectTapGestures { offset ->
              if (points.isNotEmpty()) {
                val stepX = w / (points.size - 1).coerceAtLeast(1)
                val idx = (offset.x / stepX).toInt().coerceIn(0, points.lastIndex)
                selectedIndex = idx
              }
            }
          }
          .pointerInput(points) {
            detectDragGestures(
              onDragStart = { offset ->
                if (points.isNotEmpty()) {
                  val stepX = w / (points.size - 1).coerceAtLeast(1)
                  val idx = (offset.x / stepX).toInt().coerceIn(0, points.lastIndex)
                  selectedIndex = idx
                }
              },
              onDrag = { change, _ ->
                change.consume()
                if (points.isNotEmpty()) {
                  val stepX = w / (points.size - 1).coerceAtLeast(1)
                  val idx = (change.position.x / stepX).toInt().coerceIn(0, points.lastIndex)
                  selectedIndex = idx
                }
              }
            )
          }
      ) {
        if (points.size < 2) return@Canvas

        val paddingBottom = 20.dp.toPx()
        val graphHeight = h - paddingBottom
        val stepX = w / (points.size - 1)

        // Draw 3 horizontal guideline dashes
        val dashedEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        for (i in 0..2) {
          val y = graphHeight * (i / 2f)
          drawLine(
            color = outlineColor.copy(alpha = 0.4f),
            start = Offset(0f, y),
            end = Offset(w, y),
            strokeWidth = 1.dp.toPx(),
            pathEffect = dashedEffect
          )
        }

        // Build Bezier Smooth Path
        val strokePath = Path()
        val fillPath = Path()

        val coords = points.mapIndexed { index, point ->
          val x = index * stepX
          val norm = ((point.value - minVal) / range).toFloat().coerceIn(0f, 1f)
          val y = graphHeight - (norm * (graphHeight - 12.dp.toPx())) - 6.dp.toPx()
          Offset(x, y)
        }

        strokePath.moveTo(coords[0].x, coords[0].y)
        fillPath.moveTo(coords[0].x, graphHeight)
        fillPath.lineTo(coords[0].x, coords[0].y)

        for (i in 0 until coords.size - 1) {
          val p0 = coords[i]
          val p1 = coords[i + 1]
          val midX = (p0.x + p1.x) / 2f
          strokePath.cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
          fillPath.cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
        }

        fillPath.lineTo(coords.last().x, graphHeight)
        fillPath.close()

        // Draw Area Fill Gradient
        val fillColor = if (isPositiveGrowth) Color(0xFF2ECC71) else Color(0xFF1B664B)
        drawPath(
          path = fillPath,
          brush = Brush.verticalGradient(
            listOf(
              fillColor.copy(alpha = 0.35f),
              fillColor.copy(alpha = 0.05f),
              Color.Transparent
            ),
            startY = 0f,
            endY = graphHeight
          )
        )

        // Draw Stroke Line
        drawPath(
          path = strokePath,
          color = fillColor,
          style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw End Point Dot
        val lastCoord = coords.last()
        drawCircle(
          color = fillColor,
          radius = 5.dp.toPx(),
          center = lastCoord
        )
        drawCircle(
          color = Color.White,
          radius = 2.5.dp.toPx(),
          center = lastCoord
        )

        // If inspecting a specific index, draw guideline & highlight circle
        val inspectIdx = selectedIndex
        if (inspectIdx != null && inspectIdx in coords.indices) {
          val c = coords[inspectIdx]
          drawLine(
            color = primaryColor,
            start = Offset(c.x, 0f),
            end = Offset(c.x, graphHeight),
            strokeWidth = 1.5.dp.toPx(),
            pathEffect = dashedEffect
          )
          drawCircle(
            color = primaryColor,
            radius = 7.dp.toPx(),
            center = c
          )
          drawCircle(
            color = Color.White,
            radius = 3.5.dp.toPx(),
            center = c
          )
        }
      }
    }
  }
}

/**
 * Computes realistic, smooth historical net worth points across the selected timeframe.
 */
private fun generateHistoricalNetWorthPoints(
  currentNetWorth: Double,
  expenses: List<ExpenseEntity>,
  timeframe: String
): List<NetWorthPoint> {
  val calendar = Calendar.getInstance()
  val now = calendar.timeInMillis

  val daysCount = when (timeframe) {
    "1W" -> 7
    "1M" -> 30
    "3M" -> 90
    "6M" -> 180
    "1Y" -> 365
    "ALL" -> 730
    else -> 30
  }

  val sampleSteps = when {
    daysCount <= 7 -> 7
    daysCount <= 30 -> 15
    daysCount <= 90 -> 20
    else -> 25
  }

  val stepDays = (daysCount / sampleSteps).coerceAtLeast(1)
  val points = mutableListOf<NetWorthPoint>()

  val dateFormat = SimpleDateFormat("dd MMM", Locale.US)

  // Map expenses to days
  val dayNetMap = mutableMapOf<Int, Double>()
  expenses.forEach { exp ->
    val diffDays = ((now - exp.timestamp) / (24 * 60 * 60 * 1000L)).toInt()
    if (diffDays in 0..daysCount) {
      val impact = when (exp.type.uppercase()) {
        "INCOME" -> exp.amount
        "EXPENSE" -> -exp.amount
        else -> 0.0
      }
      dayNetMap[diffDays] = (dayNetMap[diffDays] ?: 0.0) + impact
    }
  }

  // Work backwards from today
  var rollingNetWorth = currentNetWorth
  val backwardsList = mutableListOf<Pair<Long, Double>>()

  for (step in 0 until sampleSteps) {
    val dayOffset = step * stepDays
    val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -dayOffset) }
    backwardsList.add(cal.timeInMillis to rollingNetWorth)

    // Compute net change in this interval
    var intervalDelta = 0.0
    for (d in dayOffset until (dayOffset + stepDays)) {
      intervalDelta += (dayNetMap[d] ?: 0.0)
    }
    // Previous net worth before this interval's cashflow
    rollingNetWorth -= intervalDelta
  }

  // Reverse so it's chronological from oldest to newest
  backwardsList.reverse()

  backwardsList.forEach { (time, value) ->
    points.add(
      NetWorthPoint(
        timestamp = time,
        dateLabel = dateFormat.format(Date(time)),
        value = value.coerceAtLeast(0.0)
      )
    )
  }

  // Ensure last point is exactly today with current net worth
  if (points.isNotEmpty()) {
    points[points.lastIndex] = NetWorthPoint(
      timestamp = now,
      dateLabel = "Today",
      value = currentNetWorth
    )
  }

  return points
}

private fun formatAmount(amount: Double): String {
  return String.format(Locale.US, "%,.2f", amount)
}
