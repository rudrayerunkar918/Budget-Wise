package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DataArray
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale
import java.util.UUID

enum class ArchitectureTab(val label: String, val icon: ImageVector) {
  OVERVIEW("Overview", Icons.Default.Layers),
  POSTGRESQL("PostgreSQL (Ledger)", Icons.Default.AccountBalance),
  REDIS("Redis (Cache)", Icons.Default.FlashOn),
  TIMESCALEDB("TimescaleDB (EOD)", Icons.Default.Timeline)
}

data class SimulatedLedgerEntry(
  val id: String = UUID.randomUUID().toString().take(8),
  val accountName: String,
  val accountType: String,
  val amount: Double,
  val description: String,
  val partition: String = "ledger_entries_2026_09"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatabaseArchitectureScreen(
  onBackClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  BackHandler { onBackClick() }

  val context = LocalContext.current
  var selectedTab by remember { mutableStateOf(ArchitectureTab.OVERVIEW) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "Trading Database Architecture",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Text(
              text = "PostgreSQL • Redis • TimescaleDB",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.SemiBold
            )
          }
        },
        navigationIcon = {
          IconButton(
            onClick = onBackClick,
            modifier = Modifier.testTag("btn_back_database_architecture")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back"
            )
          }
        },
        actions = {
          IconButton(
            onClick = {
              copyToClipboard(
                context,
                "Trading Database Stack Summary",
                """
                Financial Trading Database Architecture:
                1. PostgreSQL: ACID User Accounts, Watchlists, Monthly Partitioned Double-Entry Ledger.
                2. Redis: Proactive Write-Through Cache with Token-Bucket Rate Limiter (<5ms latency).
                3. TimescaleDB: Compressed EOD Candlesticks & User Net-Worth Hypertables (90% compression).
                """.trimIndent()
              )
              Toast.makeText(context, "Architecture summary copied!", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.testTag("btn_copy_db_summary")
          ) {
            Icon(
              imageVector = Icons.Default.ContentCopy,
              contentDescription = "Copy Summary"
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    },
    modifier = modifier.fillMaxSize()
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      ScrollableTabRow(
        selectedTabIndex = selectedTab.ordinal,
        edgePadding = 16.dp,
        containerColor = MaterialTheme.colorScheme.surface,
        indicator = { tabPositions ->
          TabRowDefaults.SecondaryIndicator(
            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
            color = MaterialTheme.colorScheme.primary
          )
        }
      ) {
        ArchitectureTab.entries.forEach { tab ->
          Tab(
            selected = selectedTab == tab,
            onClick = { selectedTab = tab },
            text = {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(
                  imageVector = tab.icon,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp)
                )
                Text(tab.label, fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal)
              }
            },
            modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
          )
        }
      }

      when (selectedTab) {
        ArchitectureTab.OVERVIEW -> OverviewTabContent(context)
        ArchitectureTab.POSTGRESQL -> PostgresTabContent(context)
        ArchitectureTab.REDIS -> RedisTabContent(context)
        ArchitectureTab.TIMESCALEDB -> TimescaleTabContent(context)
      }
    }
  }
}

// ----------------------------------------------------------------------------
// TAB 1: OVERVIEW & TOPOLOGY
// ----------------------------------------------------------------------------

@Composable
private fun OverviewTabContent(context: Context) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth().testTag("card_arch_hero")
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Shield,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary
            )
            Text(
              text = "Production Trading Architecture",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
          }
          Text(
            text = "Designed specifically for financial institutions and retail brokerage apps. Decouples ACID financial book-keeping from high-frequency market data streaming and historical technical analysis.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    item {
      Text(
        text = "SYSTEM TOPOLOGY & LATENCY TARGETS",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
      )
    }

    item {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ArchitectureTierCard(
          title = "Redis 7.2 (Write-Through Market Cache)",
          role = "Sub-millisecond quote cache & API rate limit defense",
          latency = "< 3 ms",
          latencyColor = Color(0xFF10B981),
          specs = listOf(
            "Proactive worker ingestion (Alpha Vantage / Finnhub)",
            "Sliding-window token bucket in atomic Lua",
            "Zero upstream calls from client thread requests"
          ),
          icon = Icons.Default.FlashOn,
          accentColor = Color(0xFFE11D48)
        )

        ArchitectureTierCard(
          title = "PostgreSQL 16 (Transactional Core)",
          role = "Users, watchlists, orders & double-entry ledger",
          latency = "< 18 ms",
          latencyColor = Color(0xFF3B82F6),
          specs = listOf(
            "Double-entry bookkeeping (sum of amounts == 0)",
            "Monthly range partitions on created_at",
            "Cryptographic entry hashing & audit integrity views"
          ),
          icon = Icons.Default.AccountBalance,
          accentColor = Color(0xFF2563EB)
        )

        ArchitectureTierCard(
          title = "TimescaleDB (Time-Series Engine)",
          role = "Historical EOD candles & portfolio net-worth snapshots",
          latency = "< 35 ms",
          latencyColor = Color(0xFF8B5CF6),
          specs = listOf(
            "Hypertables with 1-month & 3-month chunk intervals",
            "Native columnar compression (90%+ disk savings)",
            "Continuous aggregates for weekly/monthly candlestick rollups"
          ),
          icon = Icons.Default.Timeline,
          accentColor = Color(0xFF9333EA)
        )
      }
    }

    item {
      Text(
        text = "DATA FLOW PIPELINE",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
      )
    }

    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          DataFlowStep(
            number = "1",
            title = "Clients Request Live Quotes",
            desc = "Client queries watchlist. Requests hit Redis Hash keys (quote:NSE:SYMBOL) with sub-3ms response. Never triggers external API."
          )
          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
          DataFlowStep(
            number = "2",
            title = "Proactive Background Polling",
            desc = "Worker pools check Redis active_symbols set, acquire rate tokens, fetch provider quotes, and write-through to Redis."
          )
          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
          DataFlowStep(
            number = "3",
            title = "Order Submission & Double-Entry Ledger",
            desc = "PostgreSQL inserts trade into orders and atomically records Debit & Credit pairs into the monthly partitioned ledger."
          )
          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
          DataFlowStep(
            number = "4",
            title = "EOD Closing & Compression",
            desc = "Post-market batch worker writes official closing bars to TimescaleDB stock_eod_candles. Background policy compresses chunks older than 30 days."
          )
        }
      }
    }
  }
}

// ----------------------------------------------------------------------------
// TAB 2: POSTGRESQL & DOUBLE-ENTRY LEDGER SIMULATOR
// ----------------------------------------------------------------------------

@Composable
private fun PostgresTabContent(context: Context) {
  val ledgerEntries = remember {
    mutableStateListOf(
      SimulatedLedgerEntry(accountName = "Cash Wallet", accountType = "CASH_WALLET", amount = 100000.0, description = "Initial Bank Deposit", partition = "ledger_entries_2026_09"),
      SimulatedLedgerEntry(accountName = "Bank Clearing", accountType = "CLEARING_ESCROW", amount = -100000.0, description = "ACH Inbound Transfer", partition = "ledger_entries_2026_09")
    )
  }

  var selectedSymbol by remember { mutableStateOf("TATAMOTORS") }
  var sharePrice by remember { mutableDoubleStateOf(441.50) }
  var quantity by remember { mutableIntStateOf(10) }
  val brokerageFee = 20.00

  val totalCost = (quantity * sharePrice) + brokerageFee
  val cashBalance = ledgerEntries.filter { it.accountType == "CASH_WALLET" }.sumOf { it.amount }
  val totalImbalance = ledgerEntries.sumOf { it.amount }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth().testTag("card_postgres_ledger_sim")
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(Icons.Default.AccountBalance, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(
              text = "Double-Entry Ledger Simulator",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
          }

          Text(
            text = "In banking and trading systems, balances are NEVER modified with UPDATE statements. Instead, balanced Debit (+) and Credit (-) entries are appended to partitioned ledger tables.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          HorizontalDivider()

          // Quick stock selector
          Text("Select Instrument to Trade:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
          Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            listOf(
              "TATAMOTORS" to 441.50,
              "RELIANCE" to 1380.00,
              "INFY" to 1495.00,
              "TCS" to 3120.00,
              "AAPL" to 228.50
            ).forEach { (sym, prc) ->
              FilterChip(
                selected = selectedSymbol == sym,
                onClick = {
                  selectedSymbol = sym
                  sharePrice = prc
                },
                label = { Text("$sym (₹$prc)") },
                modifier = Modifier.testTag("chip_sim_$sym")
              )
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text("Quantity: $quantity shares", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
              Text("Order Value: ₹${formatCurrency(quantity * sharePrice)} + ₹20 Fee", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              OutlinedButton(
                onClick = { if (quantity > 1) quantity -= 1 },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
              ) { Text("-1") }
              Button(
                onClick = { quantity += 5 },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
              ) { Text("+5") }
            }
          }

          Button(
            onClick = {
              if (cashBalance >= totalCost) {
                // Execute double-entry trade
                val txId = UUID.randomUUID().toString().take(6)
                ledgerEntries.add(
                  0,
                  SimulatedLedgerEntry(
                    id = txId,
                    accountName = "Cash Wallet",
                    accountType = "CASH_WALLET",
                    amount = -totalCost,
                    description = "Buy $quantity $selectedSymbol @ ₹$sharePrice + fee",
                    partition = "ledger_entries_2026_09"
                  )
                )
                ledgerEntries.add(
                  0,
                  SimulatedLedgerEntry(
                    id = txId,
                    accountName = "Equity Asset ($selectedSymbol)",
                    accountType = "EQUITY_HOLDING",
                    amount = (quantity * sharePrice),
                    description = "Position added: $quantity shares",
                    partition = "ledger_entries_2026_09"
                  )
                )
                ledgerEntries.add(
                  0,
                  SimulatedLedgerEntry(
                    id = txId,
                    accountName = "Brokerage & Exchange",
                    accountType = "FEE_EXPENSE",
                    amount = brokerageFee,
                    description = "Regulatory fee for $selectedSymbol order",
                    partition = "ledger_entries_2026_09"
                  )
                )
                Toast.makeText(context, "Appended 3 balanced ledger records to ledger_entries_2026_09", Toast.LENGTH_SHORT).show()
              } else {
                Toast.makeText(context, "Insufficient cash in ledger wallet!", Toast.LENGTH_SHORT).show()
              }
            },
            enabled = cashBalance >= totalCost,
            modifier = Modifier.fillMaxWidth().testTag("btn_execute_sim_trade"),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
          ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            Text("Simulate Trade Execution (Postgres Partition)")
          }
        }
      }
    }

    // Ledger Status Header & Mathematical Proof
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text("LEDGER AUDIT: ZERO-SUM BALANCE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
          Text("Available Cash: ₹${formatCurrency(cashBalance)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
        }

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = if (Math.abs(totalImbalance) < 0.01) Color(0xFF10B981).copy(alpha = 0.15f) else MaterialTheme.colorScheme.errorContainer,
          border = BorderStroke(1.dp, if (Math.abs(totalImbalance) < 0.01) Color(0xFF10B981) else MaterialTheme.colorScheme.error)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Icon(
              imageVector = if (Math.abs(totalImbalance) < 0.01) Icons.Default.CheckCircle else Icons.Default.Security,
              contentDescription = null,
              tint = if (Math.abs(totalImbalance) < 0.01) Color(0xFF10B981) else MaterialTheme.colorScheme.error,
              modifier = Modifier.size(14.dp)
            )
            Text(
              text = if (Math.abs(totalImbalance) < 0.01) "Sum = ₹0.00 (Balanced)" else "Imbalanced!",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = if (Math.abs(totalImbalance) < 0.01) Color(0xFF10B981) else MaterialTheme.colorScheme.error
            )
          }
        }
      }
    }

    // Live entries list
    items(ledgerEntries) { entry ->
      Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              Text(entry.accountName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
              Surface(
                shape = RoundedCornerShape(4.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.padding(2.dp)
              ) {
                Text(
                  text = entry.partition,
                  style = MaterialTheme.typography.labelSmall,
                  fontSize = 9.sp,
                  modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
              }
            }
            Text(entry.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }

          Text(
            text = (if (entry.amount >= 0) "+₹" else "-₹") + formatCurrency(Math.abs(entry.amount)),
            fontWeight = FontWeight.Bold,
            color = if (entry.amount >= 0) Color(0xFF10B981) else Color(0xFFE11D48),
            style = MaterialTheme.typography.bodyMedium
          )
        }
      }
    }

    // Copy SQL Button
    item {
      OutlinedButton(
        onClick = {
          copyToClipboard(
            context,
            "PostgreSQL Schema",
            """
            -- PostgreSQL Monthly Partitioned Ledger Schema
            CREATE TABLE ledger_entries (
                id UUID NOT NULL DEFAULT gen_random_uuid(),
                transaction_id UUID NOT NULL,
                account_id UUID NOT NULL REFERENCES accounts(id),
                order_id UUID REFERENCES orders(id),
                amount NUMERIC(18, 4) NOT NULL,
                currency VARCHAR(10) NOT NULL DEFAULT 'INR',
                description VARCHAR(255) NOT NULL,
                created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                PRIMARY KEY (id, created_at)
            ) PARTITION BY RANGE (created_at);
            """.trimIndent()
          )
          Toast.makeText(context, "PostgreSQL DDL copied to clipboard!", Toast.LENGTH_SHORT).show()
        },
        modifier = Modifier.fillMaxWidth().testTag("btn_copy_postgres_sql")
      ) {
        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text("Copy Full PostgreSQL DDL (01_postgresql_schema.sql)")
      }
    }
  }
}

// ----------------------------------------------------------------------------
// TAB 3: REDIS WRITE-THROUGH CACHE & RATE LIMITS
// ----------------------------------------------------------------------------

@Composable
private fun RedisTabContent(context: Context) {
  var remainingTokens by remember { mutableIntStateOf(71) }
  var totalCallsSaved by remember { mutableIntStateOf(1420) }
  var cacheHits by remember { mutableIntStateOf(99) }

  LaunchedEffect(Unit) {
    while (true) {
      delay(2500)
      cacheHits += 3
      totalCallsSaved += 3
    }
  }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth().testTag("card_redis_overview")
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(Icons.Default.FlashOn, contentDescription = null, tint = Color(0xFFE11D48))
            Text(
              text = "Redis Write-Through Market Cache",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
          }

          Text(
            text = "External financial APIs enforce strict rate limits (e.g. 5-75 calls/min). Under standard cache-aside, concurrent user traffic triggers 429 quota exhaustion. With write-through caching, background workers handle all upstream polling, and users read 100% from Redis.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          HorizontalDivider()

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
          ) {
            MetricStat(label = "Cache Hit Ratio", value = "99.8%", color = Color(0xFF10B981))
            MetricStat(label = "Client Latency", value = "1.8 ms", color = Color(0xFF3B82F6))
            MetricStat(label = "API Calls Saved", value = "$totalCallsSaved", color = Color(0xFF8B5CF6))
          }
        }
      }
    }

    item {
      Text(
        text = "ACTIVE RATE LIMITER STATUS (SLIDING WINDOW LUA)",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
      )
    }

    item {
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("Alpha Vantage Worker Quota", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
            Text("$remainingTokens / 75 RPM remaining", fontWeight = FontWeight.Bold, color = Color(0xFF10B981), style = MaterialTheme.typography.bodySmall)
          }

          LinearProgressIndicator(
            progress = { remainingTokens / 75f },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = Color(0xFF10B981),
            trackColor = MaterialTheme.colorScheme.surfaceVariant
          )

          Text(
            text = "Enforced via atomic Redis ZREMRANGEBYSCORE + ZADD rolling 60-second window. Prevents cluster-wide rate limit breaches.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    item {
      Text(
        text = "ACTIVE REDIS HASH ENTRIES (quote:exchange:symbol)",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
      )
    }

    item {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        RedisQuoteHashCard("quote:NSE:TATAMOTORS", "441.50", "+0.82%", "14.2M", "172s remaining")
        RedisQuoteHashCard("quote:NSE:RELIANCE", "1380.00", "+0.45%", "8.9M", "154s remaining")
        RedisQuoteHashCard("quote:NSE:INFY", "1495.00", "-0.32%", "5.1M", "168s remaining")
      }
    }

    item {
      OutlinedButton(
        onClick = {
          copyToClipboard(
            context,
            "Redis Rate Limit Lua Script",
            """
            -- Redis Sliding Window Rate Limiter Lua Script
            local key = KEYS[1]
            local max_calls = tonumber(ARGV[1])
            local window = tonumber(ARGV[2])
            local now = tonumber(ARGV[3])
            local clear_before = now - (window * 1000)

            redis.call('ZREMRANGEBYSCORE', key, '-inf', clear_before)
            local current_requests = redis.call('ZCARD', key)

            if current_requests < max_calls then
                redis.call('ZADD', key, now, now .. '-' .. math.random(1000, 9999))
                redis.call('EXPIRE', key, window + 1)
                return 1
            else
                return 0
            end
            """.trimIndent()
          )
          Toast.makeText(context, "Redis strategy & Lua script copied!", Toast.LENGTH_SHORT).show()
        },
        modifier = Modifier.fillMaxWidth().testTag("btn_copy_redis_lua")
      ) {
        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text("Copy Redis Lua Script & Worker Blueprint")
      }
    }
  }
}

// ----------------------------------------------------------------------------
// TAB 4: TIMESCALEDB HYPERTABLES & COMPRESSION
// ----------------------------------------------------------------------------

@Composable
private fun TimescaleTabContent(context: Context) {
  var isCompressed by remember { mutableStateOf(true) }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth().testTag("card_timescale_overview")
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(Icons.Default.Timeline, contentDescription = null, tint = Color(0xFF9333EA))
            Text(
              text = "TimescaleDB Hypertables & Compression",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
          }

          Text(
            text = "TimescaleDB partitions high-volume historical market data into automated time chunks (Hypertables). Chunks older than 30 days are automatically converted into columnar compressed format, saving ~90% disk space while speeding up multi-year chart queries.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          HorizontalDivider()

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
          ) {
            MetricStat(label = "Hypertables", value = "2 Tables", color = Color(0xFF9333EA))
            MetricStat(label = "Compression", value = "90.2%", color = Color(0xFF10B981))
            MetricStat(label = "Candle Resolution", value = "1D EOD", color = Color(0xFF3B82F6))
          }
        }
      }
    }

    item {
      Text(
        text = "HYPERTABLE PARTITION & COMPRESSION INSPECTOR",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
      )
    }

    item {
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text("Hypertable: stock_eod_candles (1-Month Chunks)", fontWeight = FontWeight.Bold)

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text("Raw Size: 1,240 MB (10.5M candles)", style = MaterialTheme.typography.bodySmall)
              Text(
                text = if (isCompressed) "Compressed Size: 121 MB (90.2% savings)" else "Compression: Disabled (Raw Rows)",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = if (isCompressed) Color(0xFF10B981) else Color(0xFFE11D48)
              )
            }

            OutlinedButton(
              onClick = { isCompressed = !isCompressed },
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(if (isCompressed) "Simulate Uncompressed" else "Apply Compression")
            }
          }

          LinearProgressIndicator(
            progress = { if (isCompressed) 0.098f else 1.0f },
            modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
            color = if (isCompressed) Color(0xFF10B981) else Color(0xFFE11D48),
            trackColor = MaterialTheme.colorScheme.surfaceVariant
          )

          Text(
            text = "Policy: add_compression_policy('stock_eod_candles', INTERVAL '30 days') with segmentby = 'symbol, exchange'.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    item {
      Text(
        text = "HYPERTABLES & CONTINUOUS AGGREGATES SCHEMA",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
      )
    }

    item {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        HypertableRow(
          name = "stock_eod_candles",
          type = "Hypertable (1-month chunks)",
          columns = "time, symbol, exchange, open, high, low, close, volume, adj_close",
          compression = "Active (30 days)"
        )
        HypertableRow(
          name = "user_networth_snapshots",
          type = "Hypertable (3-month chunks)",
          columns = "time, user_id, cash_balance, invested_equity, total_net_worth",
          compression = "Active (60 days)"
        )
        HypertableRow(
          name = "stock_weekly_candles",
          type = "Continuous Aggregate View",
          columns = "time_bucket('1 week', time), first(open), max(high), min(low), last(close)",
          compression = "Materialized"
        )
      }
    }

    item {
      OutlinedButton(
        onClick = {
          copyToClipboard(
            context,
            "TimescaleDB DDL",
            """
            -- TimescaleDB Hypertables & Compression DDL
            CREATE TABLE stock_eod_candles (
                time TIMESTAMPTZ NOT NULL,
                symbol VARCHAR(32) NOT NULL,
                exchange VARCHAR(16) NOT NULL DEFAULT 'NSE',
                open NUMERIC(14, 4) NOT NULL,
                high NUMERIC(14, 4) NOT NULL,
                low NUMERIC(14, 4) NOT NULL,
                close NUMERIC(14, 4) NOT NULL,
                volume BIGINT NOT NULL,
                adjusted_close NUMERIC(14, 4),
                PRIMARY KEY (time, symbol, exchange)
            );

            SELECT create_hypertable('stock_eod_candles', by_range('time', INTERVAL '1 month'));

            ALTER TABLE stock_eod_candles SET (
                timescaledb.compress,
                timescaledb.compress_segmentby = 'symbol, exchange',
                timescaledb.compress_orderby = 'time DESC'
            );

            SELECT add_compression_policy('stock_eod_candles', INTERVAL '30 days');
            """.trimIndent()
          )
          Toast.makeText(context, "TimescaleDB DDL copied to clipboard!", Toast.LENGTH_SHORT).show()
        },
        modifier = Modifier.fillMaxWidth().testTag("btn_copy_timescale_sql")
      ) {
        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text("Copy Full TimescaleDB DDL (03_timescaledb_hypertables.sql)")
      }
    }
  }
}

// ----------------------------------------------------------------------------
// REUSABLE HELPER COMPONENTS
// ----------------------------------------------------------------------------

@Composable
private fun ArchitectureTierCard(
  title: String,
  role: String,
  latency: String,
  latencyColor: Color,
  specs: List<String>,
  icon: ImageVector,
  accentColor: Color
) {
  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Box(
            modifier = Modifier.size(32.dp).background(accentColor.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
          }
          Column {
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            Text(role, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }

        Surface(
          shape = RoundedCornerShape(6.dp),
          color = latencyColor.copy(alpha = 0.12f),
          border = BorderStroke(1.dp, latencyColor)
        ) {
          Text(
            text = latency,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = latencyColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

      specs.forEach { spec ->
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(14.dp))
          Text(spec, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
        }
      }
    }
  }
}

@Composable
private fun DataFlowStep(number: String, title: String, desc: String) {
  Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
    Box(
      modifier = Modifier.size(24.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Text(number, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
      Text(title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
      Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
  }
}

@Composable
private fun MetricStat(label: String, value: String, color: Color) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = color)
    Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
  }
}

@Composable
private fun RedisQuoteHashCard(key: String, price: String, change: String, vol: String, ttl: String) {
  Card(
    shape = RoundedCornerShape(8.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier.padding(10.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(key, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Text("Vol: $vol • TTL: $ttl", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }

      Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("₹$price", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
        Surface(
          shape = RoundedCornerShape(4.dp),
          color = if (change.startsWith("+")) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFE11D48).copy(alpha = 0.15f)
        ) {
          Text(
            text = change,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = if (change.startsWith("+")) Color(0xFF10B981) else Color(0xFFE11D48),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
          )
        }
      }
    }
  }
}

@Composable
private fun HypertableRow(name: String, type: String, columns: String, compression: String) {
  Card(
    shape = RoundedCornerShape(8.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(name, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Surface(
          shape = RoundedCornerShape(4.dp),
          color = Color(0xFF9333EA).copy(alpha = 0.12f),
          border = BorderStroke(1.dp, Color(0xFF9333EA))
        ) {
          Text(
            text = compression,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF9333EA),
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
          )
        }
      }
      Text(type, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
      Text(columns, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
    }
  }
}

private fun copyToClipboard(context: Context, label: String, text: String) {
  val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
  val clip = ClipData.newPlainText(label, text)
  clipboard.setPrimaryClip(clip)
}

private fun formatCurrency(amount: Double): String {
  val format = NumberFormat.getNumberInstance(Locale("en", "IN"))
  format.minimumFractionDigits = 2
  format.maximumFractionDigits = 2
  return format.format(amount)
}
