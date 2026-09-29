package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.ViewWeek
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.preferences.HomeSectionItem
import com.example.data.preferences.HomeSectionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditHomeScreen(
  sections: List<HomeSectionItem>,
  userName: String,
  greeting: String,
  onToggleSection: (HomeSectionType) -> Unit,
  onMoveUp: (HomeSectionType) -> Unit,
  onMoveDown: (HomeSectionType) -> Unit,
  onResetToDefault: () -> Unit,
  onSaveUserProfile: (String, String) -> Unit,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  var editingSectionForCustomization by remember { mutableStateOf<HomeSectionType?>(null) }
  var tempUserName by remember(userName) { mutableStateOf(userName) }
  var tempGreeting by remember(greeting) { mutableStateOf(greeting) }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        title = { Text("Edit Home", fontWeight = FontWeight.SemiBold) },
        navigationIcon = {
          IconButton(
            onClick = onBack,
            modifier = Modifier.testTag("btn_edit_home_back")
          ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          IconButton(
            onClick = onResetToDefault,
            modifier = Modifier.testTag("btn_reset_home_sections")
          ) {
            Icon(Icons.Default.RestartAlt, contentDescription = "Reset Sections")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(horizontal = 16.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // Header Informational Card (matching Screenshots 4 & 5)
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
          ),
          modifier = Modifier.fillMaxWidth().testTag("edit_home_info_card")
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = "Edit Home",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Reorder and enable homepage sections. Tap each section for extra customization.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              lineHeight = 18.sp,
              textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
          }
        }
      }

      // List of customizable sections
      itemsIndexed(sections, key = { _, item -> item.type.name }) { index, item ->
        EditSectionItemRow(
          item = item,
          index = index,
          totalCount = sections.size,
          onToggle = { onToggleSection(item.type) },
          onMoveUp = { onMoveUp(item.type) },
          onMoveDown = { onMoveDown(item.type) },
          onCustomize = { editingSectionForCustomization = item.type }
        )
      }

      item {
        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }

  // Section Customization Dialog (e.g. for Banner greeting / name)
  if (editingSectionForCustomization != null) {
    val section = editingSectionForCustomization!!
    AlertDialog(
      onDismissRequest = { editingSectionForCustomization = null },
      title = {
        Text(text = "Customize ${section.defaultTitle}")
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text(
            text = section.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          if (section == HomeSectionType.HOMEPAGE_BANNER) {
            OutlinedTextField(
              value = tempGreeting,
              onValueChange = { tempGreeting = it },
              label = { Text("Greeting Text") },
              modifier = Modifier.fillMaxWidth().testTag("input_edit_greeting")
            )
            OutlinedTextField(
              value = tempUserName,
              onValueChange = { tempUserName = it },
              label = { Text("User Name") },
              modifier = Modifier.fillMaxWidth().testTag("input_edit_user_name")
            )
          } else {
            Text(
              text = "Visibility is currently: ${if (sections.firstOrNull { it.type == section }?.isEnabled == true) "Enabled" else "Hidden"}",
              style = MaterialTheme.typography.bodySmall
            )
          }
        }
      },
      confirmButton = {
        TextButton(
          onClick = {
            if (section == HomeSectionType.HOMEPAGE_BANNER) {
              onSaveUserProfile(tempUserName, tempGreeting)
            }
            editingSectionForCustomization = null
          },
          modifier = Modifier.testTag("btn_save_section_customization")
        ) {
          Text("Done")
        }
      },
      dismissButton = {
        TextButton(onClick = { editingSectionForCustomization = null }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
private fun EditSectionItemRow(
  item: HomeSectionItem,
  index: Int,
  totalCount: Int,
  onToggle: () -> Unit,
  onMoveUp: () -> Unit,
  onMoveDown: () -> Unit,
  onCustomize: () -> Unit
) {
  val icon = getSectionIcon(item.type)

  Surface(
    shape = RoundedCornerShape(12.dp),
    color = if (item.isEnabled) {
      MaterialTheme.colorScheme.surface
    } else {
      MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
    },
    tonalElevation = if (item.isEnabled) 2.dp else 0.dp,
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .clickable { onCustomize() }
      .testTag("edit_section_item_${item.type.id}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Left Icon
      Icon(
        imageVector = icon,
        contentDescription = item.type.defaultTitle,
        tint = if (item.isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.size(24.dp)
      )

      Spacer(modifier = Modifier.width(14.dp))

      // Title
      Text(
        text = item.type.defaultTitle,
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = if (item.isEnabled) FontWeight.SemiBold else FontWeight.Normal,
        color = if (item.isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        modifier = Modifier.weight(1f)
      )

      // Three dots options
      IconButton(
        onClick = onCustomize,
        modifier = Modifier.size(36.dp)
      ) {
        Icon(
          imageVector = Icons.Default.MoreVert,
          contentDescription = "Options",
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(20.dp)
        )
      }

      Spacer(modifier = Modifier.width(4.dp))

      // Toggle Button (Matching BudgetWise minus in circle / plus in circle)
      Box(
        modifier = Modifier
          .size(30.dp)
          .clip(CircleShape)
          .background(
            if (item.isEnabled) Color(0xFFD9534F) else Color(0xFF2ECC71)
          )
          .clickable { onToggle() }
          .testTag("toggle_section_${item.type.id}"),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = if (item.isEnabled) Icons.Default.Remove else Icons.Default.Add,
          contentDescription = if (item.isEnabled) "Disable section" else "Enable section",
          tint = Color.White,
          modifier = Modifier.size(18.dp)
        )
      }

      Spacer(modifier = Modifier.width(8.dp))

      // Reorder buttons (Move Up / Move Down)
      Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        if (index > 0) {
          IconButton(
            onClick = onMoveUp,
            modifier = Modifier.size(20.dp).testTag("btn_move_up_${item.type.id}")
          ) {
            Icon(
              imageVector = Icons.Default.KeyboardArrowUp,
              contentDescription = "Move Up",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(18.dp)
            )
          }
        } else {
          Spacer(modifier = Modifier.height(20.dp))
        }

        if (index < totalCount - 1) {
          IconButton(
            onClick = onMoveDown,
            modifier = Modifier.size(20.dp).testTag("btn_move_down_${item.type.id}")
          ) {
            Icon(
              imageVector = Icons.Default.KeyboardArrowDown,
              contentDescription = "Move Down",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(18.dp)
            )
          }
        } else {
          Spacer(modifier = Modifier.height(20.dp))
        }
      }

      Spacer(modifier = Modifier.width(4.dp))

      // Drag Handle icon (visual affordance)
      Icon(
        imageVector = Icons.Default.DragHandle,
        contentDescription = "Reorder handle",
        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier.size(20.dp)
      )
    }
  }
}

private fun getSectionIcon(type: HomeSectionType): ImageVector = when (type) {
  HomeSectionType.HOMEPAGE_BANNER -> Icons.Default.CreditCard
  HomeSectionType.ACCOUNTS -> Icons.Default.AccountBalance
  HomeSectionType.ACCOUNTS_LIST -> Icons.AutoMirrored.Filled.FormatListBulleted
  HomeSectionType.BUDGETS -> Icons.Default.PieChart
  HomeSectionType.GOALS -> Icons.Default.EmojiEvents
  HomeSectionType.INCOME_EXPENSES -> Icons.Default.SwapVert
  HomeSectionType.NET_WORTH -> Icons.AutoMirrored.Filled.TrendingUp
  HomeSectionType.OVERDUE_UPCOMING -> Icons.Default.CalendarMonth
  HomeSectionType.PIE_CHART -> Icons.Default.PieChart
  HomeSectionType.SPENDING_GRAPH -> Icons.AutoMirrored.Filled.ShowChart
  HomeSectionType.LOANS -> Icons.Default.Handshake
  HomeSectionType.STOCKS -> Icons.AutoMirrored.Filled.ShowChart
  HomeSectionType.STACKED_BAR -> Icons.Default.ViewWeek
  HomeSectionType.PINNED_TRANSACTIONS -> Icons.Default.PushPin
}
