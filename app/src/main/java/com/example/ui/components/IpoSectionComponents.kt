package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.DetailedIpoItem
import com.example.data.api.IpoStatus
import com.example.ui.viewmodel.ExpenseViewModel

@Composable
fun IpoSectionContent(
  viewModel: ExpenseViewModel,
  currencySymbol: String,
  onApplyIpo: (DetailedIpoItem) -> Unit,
  modifier: Modifier = Modifier
) {
  val ipos by viewModel.allIpos.collectAsState()
  val isSyncing by viewModel.isSyncingIpos.collectAsState()
  val syncMessage by viewModel.ipoSyncMessage.collectAsState()
  val lastSyncDate by viewModel.lastIpoSyncDate.collectAsState()
  val totalDirectoryCount by viewModel.totalDirectoryCount.collectAsState()

  var selectedFilter by remember { mutableStateOf("ALL") } // ALL, OPEN, UPCOMING, LISTED
  var searchQuery by remember { mutableStateOf("") }
  var selectedIpoForGeminiModal by remember { mutableStateOf<DetailedIpoItem?>(null) }

  val filteredIpos = remember(ipos, selectedFilter, searchQuery) {
    val q = searchQuery.trim().lowercase()
    ipos.filter { item ->
      val matchesFilter = when (selectedFilter) {
        "OPEN" -> item.status == IpoStatus.OPEN
        "UPCOMING" -> item.status == IpoStatus.UPCOMING
        "LISTED" -> item.isListed || item.status == IpoStatus.LISTED
        else -> true
      }
      val matchesSearch = q.isEmpty() ||
        item.companyName.lowercase().contains(q) ||
        item.symbol.lowercase().contains(q) ||
        item.sector.lowercase().contains(q)

      matchesFilter && matchesSearch
    }
  }

  val openCount = remember(ipos) { ipos.count { it.status == IpoStatus.OPEN } }
  val upcomingCount = remember(ipos) { ipos.count { it.status == IpoStatus.UPCOMING } }
  val listedCount = remember(ipos) { ipos.count { it.isListed || it.status == IpoStatus.LISTED } }

  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // 1. Hero Card: Daily IPO & Directory Sync
    IpoHeroCard(
      totalIpos = ipos.size,
      openCount = openCount,
      upcomingCount = upcomingCount,
      listedCount = listedCount,
      totalDirectoryCount = totalDirectoryCount,
      lastSyncDate = lastSyncDate,
      isSyncing = isSyncing,
      syncMessage = syncMessage,
      onSyncDaily = { viewModel.syncStockDirectoryNow() }
    )

    // 2. Search & Filter Bar
    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      modifier = Modifier
        .fillMaxWidth()
        .testTag("ipo_search_field"),
      placeholder = { Text("Search IPOs by company name, sector or ticker...", fontSize = 13.sp) },
      leadingIcon = {
        Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
      },
      trailingIcon = {
        if (searchQuery.isNotEmpty()) {
          IconButton(onClick = { searchQuery = "" }) {
            Icon(Icons.Default.Close, contentDescription = "Clear")
          }
        }
      },
      shape = RoundedCornerShape(16.dp),
      singleLine = true,
      colors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surface,
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
      )
    )

    // Filter Chips
    val filterScrollState = rememberScrollState()
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(filterScrollState),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      FilterChip(
        selected = selectedFilter == "ALL",
        onClick = { selectedFilter = "ALL" },
        label = { Text("All IPOs (${ipos.size})") },
        colors = FilterChipDefaults.filterChipColors(
          selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
          selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        modifier = Modifier.testTag("filter_all_ipos")
      )
      FilterChip(
        selected = selectedFilter == "OPEN",
        onClick = { selectedFilter = "OPEN" },
        label = { Text("🟢 Open for Application ($openCount)") },
        colors = FilterChipDefaults.filterChipColors(
          selectedContainerColor = Color(0xFFE8F5E9),
          selectedLabelColor = Color(0xFF1B5E20)
        ),
        modifier = Modifier.testTag("filter_open_ipos")
      )
      FilterChip(
        selected = selectedFilter == "UPCOMING",
        onClick = { selectedFilter = "UPCOMING" },
        label = { Text("⏳ Upcoming ($upcomingCount)") },
        colors = FilterChipDefaults.filterChipColors(
          selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
          selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        modifier = Modifier.testTag("filter_upcoming_ipos")
      )
      FilterChip(
        selected = selectedFilter == "LISTED",
        onClick = { selectedFilter = "LISTED" },
        label = { Text("🏆 Listed on Exchange ($listedCount)") },
        colors = FilterChipDefaults.filterChipColors(
          selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
          selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer
        ),
        modifier = Modifier.testTag("filter_listed_ipos")
      )
    }

    // 3. List of IPO Cards
    if (filteredIpos.isEmpty()) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(16.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(36.dp)
          )
          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = "No IPOs match your filter",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Try clearing the search query or changing filter tabs.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    } else {
      for (ipo in filteredIpos) {
        DetailedIpoCard(
          ipo = ipo,
          currencySymbol = currencySymbol,
          onApply = { onApplyIpo(ipo) },
          onViewGeminiAnalysis = { selectedIpoForGeminiModal = ipo },
          onMarkAsListed = {
            viewModel.markIpoAsListed(ipo.symbol, ipo.issuePrice)
          }
        )
      }
    }
  }

  // Gemini AI Analysis Modal Dialog
  selectedIpoForGeminiModal?.let { ipo ->
    IpoGeminiAnalysisDialog(
      ipo = ipo,
      currencySymbol = currencySymbol,
      onDismiss = { selectedIpoForGeminiModal = null },
      onApply = {
        selectedIpoForGeminiModal = null
        onApplyIpo(ipo)
      },
      onMarkAsListed = {
        viewModel.markIpoAsListed(ipo.symbol, ipo.issuePrice)
        selectedIpoForGeminiModal = null
      }
    )
  }
}

@Composable
private fun IpoHeroCard(
  totalIpos: Int,
  openCount: Int,
  upcomingCount: Int,
  listedCount: Int,
  totalDirectoryCount: Int,
  lastSyncDate: String,
  isSyncing: Boolean,
  syncMessage: String?,
  onSyncDaily: () -> Unit
) {
  val infiniteTransition = rememberInfiniteTransition(label = "ipo_sync_spin")
  val rotation by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(1000, easing = LinearEasing)
    ),
    label = "rotation"
  )

  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
    modifier = Modifier.fillMaxWidth().testTag("ipo_hero_card")
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          Brush.horizontalGradient(
            listOf(
              Color(0xFF673AB7).copy(alpha = 0.15f),
              Color(0xFF2196F3).copy(alpha = 0.10f),
              MaterialTheme.colorScheme.surface
            )
          )
        )
        .padding(16.dp)
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Title & Sync Button
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(
                  Brush.linearGradient(
                    listOf(Color(0xFF7E57C2), Color(0xFF29B6F6))
                  )
                ),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "IPO & New Listings Hub",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "NSE & BSE Mainboard • Updated Daily",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          // Daily Sync Button
          Button(
            onClick = onSyncDaily,
            enabled = !isSyncing,
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            modifier = Modifier.testTag("btn_sync_daily_ipos")
          ) {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = "Sync",
              modifier = Modifier
                .size(16.dp)
                .then(if (isSyncing) Modifier.rotate(rotation) else Modifier)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (isSyncing) "Updating..." else "Sync Daily",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }

        // Stats Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          StatChip(label = "Open Now", count = "$openCount", color = Color(0xFF2E7D32), modifier = Modifier.weight(1f))
          StatChip(label = "Upcoming", count = "$upcomingCount", color = Color(0xFF1565C0), modifier = Modifier.weight(1f))
          StatChip(label = "Listed", count = "$listedCount", color = Color(0xFF6A1B9A), modifier = Modifier.weight(1f))
        }

        // Status & Directory Count info
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "📅 Last Synced: $lastSyncDate",
              style = MaterialTheme.typography.labelSmall,
              fontSize = 10.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = "📚 $totalDirectoryCount Stocks in Directory",
              style = MaterialTheme.typography.labelSmall,
              fontSize = 10.sp,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.primary
            )
          }
        }

        syncMessage?.let { msg ->
          Text(
            text = msg,
            style = MaterialTheme.typography.bodySmall,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.primary
          )
        }
      }
    }
  }
}

@Composable
private fun StatChip(
  label: String,
  count: String,
  color: Color,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(10.dp),
    color = color.copy(alpha = 0.12f),
    border = BorderStroke(1.dp, color.copy(alpha = 0.3f)),
    modifier = modifier
  ) {
    Column(
      modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = count,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = color
      )
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        fontSize = 10.sp,
        color = color
      )
    }
  }
}

@Composable
private fun DetailedIpoCard(
  ipo: DetailedIpoItem,
  currencySymbol: String,
  onApply: () -> Unit,
  onViewGeminiAnalysis: () -> Unit,
  onMarkAsListed: () -> Unit
) {
  val isListed = ipo.isListed || ipo.status == IpoStatus.LISTED
  val isOpen = ipo.status == IpoStatus.OPEN
  val isUpcoming = ipo.status == IpoStatus.UPCOMING

  val statusBg = when {
    isOpen -> Color(0xFFE8F5E9)
    isListed -> Color(0xFFEDE7F6)
    else -> Color(0xFFE3F2FD)
  }
  val statusColor = when {
    isOpen -> Color(0xFF2E7D32)
    isListed -> Color(0xFF512DA8)
    else -> Color(0xFF1565C0)
  }
  val statusLabel = when {
    isOpen -> "🟢 Open for Application"
    isListed -> "🏆 Listed on NSE"
    else -> "⏳ Upcoming IPO"
  }

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    border = BorderStroke(
      1.dp,
      if (isOpen) Color(0xFF81C784).copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    ),
    modifier = Modifier.fillMaxWidth().testTag("ipo_card_${ipo.symbol}")
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // 1. Header: Name, Symbol & Status Badge
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = ipo.companyName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Spacer(modifier = Modifier.height(2.dp))
          Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
              shape = RoundedCornerShape(4.dp),
              color = MaterialTheme.colorScheme.surfaceVariant,
              modifier = Modifier.padding(end = 6.dp)
            ) {
              Text(
                text = ipo.symbol,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
            Text(
              text = ipo.sector,
              style = MaterialTheme.typography.bodySmall,
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }

        // Status Chip
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = statusBg,
          border = BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
        ) {
          Text(
            text = statusLabel,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = statusColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      // 2. Metrics Grid: Dates, Price, Lot, Issue Size
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Row(modifier = Modifier.fillMaxWidth()) {
            IpoDetailCell(
              label = "Application Dates",
              value = "${ipo.openDate} – ${ipo.closeDate}",
              icon = Icons.Default.DateRange,
              modifier = Modifier.weight(1f)
            )
            IpoDetailCell(
              label = "Listing Date",
              value = ipo.listingDate,
              icon = Icons.Default.CalendarToday,
              modifier = Modifier.weight(1f)
            )
          }
          Row(modifier = Modifier.fillMaxWidth()) {
            IpoDetailCell(
              label = "Price Band / Lot",
              value = "${ipo.priceBand} (${ipo.lotSize} sh)",
              icon = Icons.AutoMirrored.Filled.TrendingUp,
              modifier = Modifier.weight(1f)
            )
            IpoDetailCell(
              label = "Issue Size",
              value = ipo.issueSize,
              icon = Icons.Default.Info,
              modifier = Modifier.weight(1f)
            )
          }
        }
      }

      // 3. Gemini AI Analysis Container
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF3E5F5).copy(alpha = 0.6f),
        border = BorderStroke(1.dp, Color(0xFFCE93D8).copy(alpha = 0.7f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color(0xFF7B1FA2),
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Gemini AI Verdict: ${ipo.geminiRecommendation}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4A148C)
              )
            }

            Surface(
              shape = RoundedCornerShape(6.dp),
              color = Color(0xFF7B1FA2),
              modifier = Modifier.padding(start = 4.dp)
            ) {
              Text(
                text = "${ipo.geminiConfidence}% Conf",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }

          Text(
            text = ipo.geminiAnalysisThesis,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
          )

          TextButton(
            onClick = onViewGeminiAnalysis,
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.height(28.dp).testTag("btn_view_gemini_thesis_${ipo.symbol}")
          ) {
            Text(
              text = "Read In-Depth Gemini Analysis →",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF7B1FA2)
            )
          }
        }
      }

      // 4. Action Buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (!isListed) {
          OutlinedButton(
            onClick = onMarkAsListed,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.weight(1f).testTag("btn_mark_listed_${ipo.symbol}")
          ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Mark as Listed", fontSize = 11.sp)
          }

          Button(
            onClick = onApply,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.weight(1f).testTag("btn_apply_ipo_${ipo.symbol}")
          ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Apply / Record", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        } else {
          OutlinedButton(
            onClick = onMarkAsListed,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.weight(1f).testTag("btn_sync_directory_${ipo.symbol}")
          ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("In Stock Directory", fontSize = 11.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.SemiBold)
          }

          Button(
            onClick = onApply,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.weight(1f).testTag("btn_trade_ipo_${ipo.symbol}")
          ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Add to Portfolio", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

@Composable
private fun IpoDetailCell(
  label: String,
  value: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  modifier: Modifier = Modifier
) {
  Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.primary,
      modifier = Modifier.size(14.dp)
    )
    Spacer(modifier = Modifier.width(6.dp))
    Column {
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        fontSize = 10.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Text(
        text = value,
        style = MaterialTheme.typography.bodySmall,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

@Composable
fun IpoGeminiAnalysisDialog(
  ipo: DetailedIpoItem,
  currencySymbol: String,
  onDismiss: () -> Unit,
  onApply: () -> Unit,
  onMarkAsListed: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(
              Brush.linearGradient(listOf(Color(0xFF8E44AD), Color(0xFF3498DB)))
            ),
          contentAlignment = Alignment.Center
        ) {
          Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(ipo.companyName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
          Text("${ipo.symbol} • ${ipo.sector}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
    },
    text = {
      LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Verdict Card
        item {
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
          ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "Recommendation",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "${ipo.geminiConfidence}% Confidence",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.primary,
                  fontWeight = FontWeight.Bold
                )
              }
              Text(
                text = ipo.geminiRecommendation,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
            }
          }
        }

        // Comprehensive Thesis
        item {
          Text(
            text = "Gemini Investment Thesis",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = ipo.geminiAnalysisThesis,
            style = MaterialTheme.typography.bodySmall,
            lineHeight = 18.sp
          )
        }

        // Key Pros
        if (ipo.geminiKeyPros.isNotEmpty()) {
          item {
            Text(
              text = "Key Growth Catalysts & Strengths",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF2E7D32)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
              for (pro in ipo.geminiKeyPros) {
                Row(verticalAlignment = Alignment.Top) {
                  Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(pro, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
                }
              }
            }
          }
        }

        // Key Risks
        if (ipo.geminiKeyRisks.isNotEmpty()) {
          item {
            Text(
              text = "Key Risks & Valuation Considerations",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(4.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
              for (risk in ipo.geminiKeyRisks) {
                Row(verticalAlignment = Alignment.Top) {
                  Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(risk, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
                }
              }
            }
          }
        }

        // Issue Details Summary
        item {
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Price Band:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(ipo.priceBand, fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Lot Size:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${ipo.lotSize} shares", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Listing Date:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(ipo.listingDate, fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("GMP / Premium:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(ipo.subscriptionGmp, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Button(onClick = onApply) {
        Text("Record / Apply")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Close")
      }
    }
  )
}
