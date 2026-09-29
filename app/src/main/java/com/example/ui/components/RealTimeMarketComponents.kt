package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.LinearProgressIndicator
import com.example.data.api.FinnhubCompanyProfile
import com.example.data.api.FinnhubQuote
import com.example.data.api.AlphaVantageQuote
import com.example.data.api.AlphaVantageOverview
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.GroundedSource
import com.example.data.ai.RealTimeMarketIndex
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RealTimeMarketIndicesCard(
  indices: List<RealTimeMarketIndex>,
  isSyncing: Boolean,
  lastSyncTime: Long,
  statusMessage: String?,
  searchSources: List<GroundedSource>,
  searchQueries: List<String>,
  onSyncNow: () -> Unit,
  onViewSources: () -> Unit,
  modifier: Modifier = Modifier
) {
  val syncTimeStr = if (lastSyncTime > 0) {
    SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(lastSyncTime))
  } else {
    "Real-time ready"
  }

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    modifier = modifier
      .fillMaxWidth()
      .testTag("real_time_market_indices_card")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
    ) {
      // Header: Live Indicator, Title, Google Grounding Badge & Sync Button
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          // Green Pulsing Dot for Live Status
          Box(
            modifier = Modifier
              .size(8.dp)
              .clip(CircleShape)
              .background(if (isSyncing) Color(0xFFF39C12) else Color(0xFF2ECC71))
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "LIVE MARKET INDICES",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.8.sp
          )
          Spacer(modifier = Modifier.width(8.dp))

          // Google Grounding Pill
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = Color(0xFF4285F4).copy(alpha = 0.12f),
            border = BorderStroke(1.dp, Color(0xFF4285F4).copy(alpha = 0.3f))
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Public,
                contentDescription = null,
                tint = Color(0xFF1967D2),
                modifier = Modifier.size(10.dp)
              )
              Spacer(modifier = Modifier.width(3.dp))
              Text(
                text = "Google Search",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF1967D2),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
              )
            }
          }
        }

        // Sync Real-Time Data Button
        Button(
          onClick = onSyncNow,
          enabled = !isSyncing,
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
          ),
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
          modifier = Modifier
            .height(32.dp)
            .testTag("btn_sync_real_time_data")
        ) {
          if (isSyncing) {
            CircularProgressIndicator(
              modifier = Modifier.size(14.dp),
              color = MaterialTheme.colorScheme.onPrimary,
              strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Syncing...", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          } else {
            Icon(
              imageVector = Icons.Default.CloudSync,
              contentDescription = null,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Sync Live", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Horizontal Indices Ticker
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        items(indices) { idx ->
          MarketIndexChip(index = idx)
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Bottom Info & Grounding Sources Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (isSyncing) {
            "Grounded with Google Search via Gemini 3.5 Flash..."
          } else {
            "Last synced: $syncTimeStr • ${statusMessage ?: "All quotes current"}"
          },
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontSize = 11.sp,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.weight(1f)
        )

        if (searchSources.isNotEmpty() || searchQueries.isNotEmpty()) {
          TextButton(
            onClick = onViewSources,
            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
            modifier = Modifier
              .height(26.dp)
              .testTag("btn_view_grounding_sources")
          ) {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
              text = "Sources (${searchSources.size})",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.primary,
              fontSize = 10.sp,
              fontWeight = FontWeight.SemiBold
            )
          }
        }
      }
    }
  }
}

@Composable
private fun MarketIndexChip(index: RealTimeMarketIndex) {
  val changeColor = if (index.isPositive) Color(0xFF27AE60) else Color(0xFFE74C3C)
  val changeSign = if (index.changePercent >= 0) "+" else ""

  Surface(
    shape = RoundedCornerShape(10.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
    modifier = Modifier.testTag("index_chip_${index.name.replace(" ", "_")}")
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
      Text(
        text = index.name,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium
      )
      Spacer(modifier = Modifier.height(2.dp))
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = index.value,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp
        )
        Spacer(modifier = Modifier.width(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = if (index.isPositive) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
            contentDescription = null,
            tint = changeColor,
            modifier = Modifier.size(12.dp)
          )
          Spacer(modifier = Modifier.width(2.dp))
          Text(
            text = "$changeSign${String.format(Locale.US, "%.2f", index.changePercent)}%",
            style = MaterialTheme.typography.labelSmall,
            color = changeColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GoogleSearchSourcesDialog(
  searchQueries: List<String>,
  sources: List<GroundedSource>,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(Color(0xFF4285F4).copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Public,
            contentDescription = null,
            tint = Color(0xFF1967D2),
            modifier = Modifier.size(18.dp)
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text("Google Search Grounding", fontWeight = FontWeight.Bold, fontSize = 16.sp)
          Text(
            text = "Real-time verified web sources",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
          )
        }
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Search Queries Section
        if (searchQueries.isNotEmpty()) {
          Text(
            text = "WEB SEARCH QUERIES EXECUTED:",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 0.5.sp
          )
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            for (query in searchQueries) {
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(10.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = query,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp
                  )
                }
              }
            }
          }
        }

        // Sources Section
        Text(
          text = "GROUNDING SOURCES (${sources.size}):",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
          letterSpacing = 0.5.sp
        )

        if (sources.isEmpty()) {
          Text(
            text = "Live data synced using direct web and financial market queries.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        } else {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (src in sources.take(8)) {
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable {
                    try {
                      val intent = Intent(Intent.ACTION_VIEW, Uri.parse(src.uri))
                      context.startActivity(intent)
                    } catch (_: Exception) {}
                  }
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = src.title,
                      style = MaterialTheme.typography.bodySmall,
                      fontWeight = FontWeight.SemiBold,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                    Text(
                      text = src.uri,
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.primary,
                      fontSize = 10.sp,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                  }
                  Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = "Open link",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                  )
                }
              }
            }
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

/**
 * Displays Finnhub real-time market data including current price,
 * day range (high/low progress bar), open, and previous close.
 */
@Composable
fun FinnhubMarketMetricsCard(
  quote: FinnhubQuote,
  profile: FinnhubCompanyProfile?,
  onViewCompanyProfile: () -> Unit,
  modifier: Modifier = Modifier
) {
  val isPositive = quote.changePercent >= 0.0
  val changeColor = if (isPositive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error

  val low = quote.lowPrice
  val high = quote.highPrice
  val current = quote.currentPrice
  val progress = if (high > low && current in low..high) {
    ((current - low) / (high - low)).toFloat().coerceIn(0f, 1f)
  } else {
    0.5f
  }

  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.padding(2.dp)
          ) {
            Text(
              text = "FINNHUB LIVE",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onPrimaryContainer,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
          Text(
            text = quote.symbol,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
        }

        if (profile != null) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
            modifier = Modifier.clickable { onViewCompanyProfile() }
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              horizontalArrangement = Arrangement.spacedBy(4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Business,
                contentDescription = "Profile",
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(12.dp)
              )
              Text(
                text = "Profile",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
              )
            }
          }
        }
      }

      // Price and change display (Converted to INR)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
      ) {
        Column {
          Text(
            text = "₹${String.format(java.util.Locale.US, "%,.2f", quote.currentPrice)}",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(top = 2.dp)
          ) {
            Surface(
              shape = RoundedCornerShape(4.dp),
              color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
            ) {
              Text(
                text = "₹ INR Converted",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
              )
            }
            Text(
              text = "($${String.format(java.util.Locale.US, "%.2f", quote.rawPriceUsd)} USD • 1$ = ₹${String.format(java.util.Locale.US, "%.2f", quote.usdToInrRate)})",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = changeColor.copy(alpha = 0.12f)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = if (isPositive) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
              contentDescription = null,
              tint = changeColor,
              modifier = Modifier.size(16.dp)
            )
            Text(
              text = "${if (isPositive) "+" else ""}${String.format(java.util.Locale.US, "%,.2f", quote.changeAmount)} (${if (isPositive) "+" else ""}${String.format(java.util.Locale.US, "%.2f", quote.changePercent)}%)",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.SemiBold,
              color = changeColor
            )
          }
        }
      }

      // Day Range Bar (Low to High in INR)
      if (high > 0 && low > 0) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "Day Low: ₹${String.format(java.util.Locale.US, "%,.2f", low)} ($${String.format(java.util.Locale.US, "%.2f", quote.rawLowUsd)})",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = "Day High: ₹${String.format(java.util.Locale.US, "%,.2f", high)} ($${String.format(java.util.Locale.US, "%.2f", quote.rawHighUsd)})",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
              .fillMaxWidth()
              .height(6.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
          )
        }
      }

      // Open & Previous Close Row in INR
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column {
          Text(
            text = "Open",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = if (quote.openPrice > 0) "₹${String.format(java.util.Locale.US, "%,.2f", quote.openPrice)}" else "—",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
          )
        }

        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = "Prev Close",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = if (quote.previousClose > 0) "₹${String.format(java.util.Locale.US, "%,.2f", quote.previousClose)}" else "—",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
          )
        }
      }
    }
  }
}

/**
 * Dialog displaying detailed company profile info from Finnhub.
 */
@Composable
fun FinnhubCompanyProfileDialog(
  profile: FinnhubCompanyProfile,
  onDismiss: () -> Unit
) {
  val context = androidx.compose.ui.platform.LocalContext.current

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.Business,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary
        )
        Column {
          Text(
            text = profile.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "${profile.ticker} • ${profile.exchange.ifBlank { "US Exchange" }}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        if (profile.finnhubIndustry.isNotBlank()) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "Industry",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = profile.finnhubIndustry,
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.SemiBold
            )
          }
        }

        if (profile.country.isNotBlank()) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "Country",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = profile.country,
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.SemiBold
            )
          }
        }

        if (profile.marketCapitalization > 0) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "Market Cap",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            val capStr = if (profile.marketCapitalization >= 1000) {
              String.format(java.util.Locale.US, "$%.2f B", profile.marketCapitalization / 1000.0)
            } else {
              String.format(java.util.Locale.US, "$%.2f M", profile.marketCapitalization)
            }
            Text(
              text = capStr,
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.SemiBold
            )
          }
        }

        if (profile.ipo.isNotBlank()) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "IPO Date",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = profile.ipo,
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.Medium
            )
          }
        }

        if (profile.weburl.isNotBlank()) {
          Spacer(modifier = Modifier.height(4.dp))
          OutlinedButton(
            onClick = {
              try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(profile.weburl))
                context.startActivity(intent)
              } catch (_: Exception) {}
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.OpenInNew,
              contentDescription = null,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Visit Official Website", style = MaterialTheme.typography.labelMedium)
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

/**
 * Displays Alpha Vantage live market quote including current price,
 * day range (high/low progress bar), open, volume, and trading day.
 */
@Composable
fun AlphaVantageMarketMetricsCard(
  quote: AlphaVantageQuote,
  overview: AlphaVantageOverview?,
  onViewOverview: () -> Unit,
  modifier: Modifier = Modifier
) {
  val isPositive = quote.changePercent >= 0.0
  val changeColor = if (isPositive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error

  val low = quote.low
  val high = quote.high
  val current = quote.price
  val progress = if (high > low && current in low..high) {
    ((current - low) / (high - low)).toFloat().coerceIn(0f, 1f)
  } else {
    0.5f
  }

  val isIndianStock = quote.symbol.endsWith(".BSE") || quote.symbol.endsWith(".NSE")
  val currSymbol = if (isIndianStock) "₹" else "$"

  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = Color(0xFFE67E22).copy(alpha = 0.2f),
            modifier = Modifier.padding(2.dp)
          ) {
            Text(
              text = "ALPHA VANTAGE",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = Color(0xFFD35400),
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
          Text(
            text = quote.symbol,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
        }

        if (overview != null) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
            modifier = Modifier.clickable { onViewOverview() }
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              horizontalArrangement = Arrangement.spacedBy(4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Business,
                contentDescription = "Overview",
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(12.dp)
              )
              Text(
                text = "Overview",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
              )
            }
          }
        }
      }

      // Price and change display
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
      ) {
        Column {
          Text(
            text = "$currSymbol${String.format(java.util.Locale.US, "%.2f", quote.price)}",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "${if (isIndianStock) "INR" else "USD"} • Real-Time Quote (${quote.latestTradingDay.ifBlank { "Today" }})",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = changeColor.copy(alpha = 0.12f)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = if (isPositive) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
              contentDescription = null,
              tint = changeColor,
              modifier = Modifier.size(16.dp)
            )
            Text(
              text = "${if (isPositive) "+" else ""}${String.format(java.util.Locale.US, "%.2f", quote.change)} (${if (isPositive) "+" else ""}${String.format(java.util.Locale.US, "%.2f", quote.changePercent)}%)",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.SemiBold,
              color = changeColor
            )
          }
        }
      }

      // Day Range Bar (Low to High)
      if (high > 0 && low > 0) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "Low: $currSymbol${String.format(java.util.Locale.US, "%.2f", low)}",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = "High: $currSymbol${String.format(java.util.Locale.US, "%.2f", high)}",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
              .fillMaxWidth()
              .height(6.dp),
            color = Color(0xFFE67E22),
            trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
          )
        }
      }

      // Open, Previous Close & Volume
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column {
          Text(
            text = "Open",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = if (quote.open > 0) "$currSymbol${String.format(java.util.Locale.US, "%.2f", quote.open)}" else "—",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
          )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = "Volume",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = if (quote.volume > 0) "${quote.volume / 1000}K" else "—",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
          )
        }

        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = "Prev Close",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = if (quote.previousClose > 0) "$currSymbol${String.format(java.util.Locale.US, "%.2f", quote.previousClose)}" else "—",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
          )
        }
      }
    }
  }
}

/**
 * Dialog displaying detailed company overview from Alpha Vantage.
 */
@Composable
fun AlphaVantageOverviewDialog(
  overview: AlphaVantageOverview,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.Business,
          contentDescription = null,
          tint = Color(0xFFD35400)
        )
        Column {
          Text(
            text = overview.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "${overview.symbol} • ${overview.sector.ifBlank { "Equity" }}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        if (overview.industry.isNotBlank()) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "Industry",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = overview.industry,
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.SemiBold
            )
          }
        }

        if (overview.marketCap > 0) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "Market Cap",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            val capStr = if (overview.marketCap >= 1_000_000_000) {
              String.format(java.util.Locale.US, "$%.2f B", overview.marketCap / 1_000_000_000.0)
            } else {
              String.format(java.util.Locale.US, "$%.2f M", overview.marketCap / 1_000_000.0)
            }
            Text(
              text = capStr,
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.SemiBold
            )
          }
        }

        if (overview.peRatio > 0) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "P/E Ratio",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = String.format(java.util.Locale.US, "%.2f", overview.peRatio),
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.SemiBold
            )
          }
        }

        if (overview.week52High > 0 && overview.week52Low > 0) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "52-Week Range",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = "$${String.format(java.util.Locale.US, "%.2f", overview.week52Low)} - $${String.format(java.util.Locale.US, "%.2f", overview.week52High)}",
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.Medium
            )
          }
        }

        if (overview.description.isNotBlank()) {
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "About Company",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
          Text(
            text = overview.description.take(250) + if (overview.description.length > 250) "..." else "",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
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
