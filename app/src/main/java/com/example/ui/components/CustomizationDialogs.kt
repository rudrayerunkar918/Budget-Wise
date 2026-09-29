package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.preferences.NavTabDestination
import com.example.data.preferences.ThemeMode

@Composable
fun ThemeSelectionDialog(
  currentMode: ThemeMode,
  onSelectMode: (ThemeMode) -> Unit,
  onDismiss: () -> Unit
) {
  val options = listOf(
    Triple(ThemeMode.SYSTEM, "System Default", Icons.Default.SettingsBrightness),
    Triple(ThemeMode.LIGHT, "Light Mode", Icons.Default.LightMode),
    Triple(ThemeMode.DARK, "Dark Mode", Icons.Default.DarkMode)
  )

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "Theme & Appearance",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Text(
          text = "Choose how BudgetWise looks on your device:",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(6.dp))

        options.forEach { (mode, label, icon) ->
          val isSelected = currentMode == mode
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .clickable {
                onSelectMode(mode)
                onDismiss()
              }
              .testTag("theme_option_${mode.name.lowercase()}")
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
              )
              Spacer(modifier = Modifier.width(14.dp))
              Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
              )
              RadioButton(
                selected = isSelected,
                onClick = {
                  onSelectMode(mode)
                  onDismiss()
                }
              )
            }
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text("Done")
      }
    }
  )
}

@Composable
fun NavBarCustomizationDialog(
  currentTabs: List<NavTabDestination>,
  onSaveTabs: (List<NavTabDestination>) -> Unit,
  onResetToDefault: () -> Unit,
  onDismiss: () -> Unit
) {
  var workingList by remember(currentTabs) { mutableStateOf(currentTabs.toMutableList()) }

  val allPossibleTabs = listOf(
    NavTabDestination.HOME,
    NavTabDestination.TRANSACTIONS,
    NavTabDestination.BUDGETS,
    NavTabDestination.LOANS,
    NavTabDestination.RECURRING,
    NavTabDestination.GOALS,
    NavTabDestination.ANALYTICS,
    NavTabDestination.MORE
  )

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text("Customize Navigation Bar", fontWeight = FontWeight.Bold)
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Text(
          text = "Select 3 to 5 tabs to display on the bottom navigation bar. Use arrows to reorder them.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          itemsIndexed(allPossibleTabs) { _, tab ->
            val isIncluded = workingList.contains(tab)
            val indexInWorking = workingList.indexOf(tab)

            Surface(
              shape = RoundedCornerShape(10.dp),
              color = if (isIncluded) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface,
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable {
                  val updated = workingList.toMutableList()
                  if (isIncluded) {
                    if (updated.size > 3) {
                      updated.remove(tab)
                      workingList = updated
                    }
                  } else {
                    if (updated.size < 5) {
                      updated.add(tab)
                      workingList = updated
                    }
                  }
                }
                .testTag("nav_bar_item_${tab.id}")
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Checkbox(
                  checked = isIncluded,
                  onCheckedChange = { checked ->
                    val updated = workingList.toMutableList()
                    if (checked) {
                      if (updated.size < 5 && !updated.contains(tab)) {
                        updated.add(tab)
                        workingList = updated
                      }
                    } else {
                      if (updated.size > 3) {
                        updated.remove(tab)
                        workingList = updated
                      }
                    }
                  }
                )

                Icon(
                  imageVector = getNavTabIcon(tab),
                  contentDescription = tab.label,
                  modifier = Modifier.size(20.dp),
                  tint = if (isIncluded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                  text = tab.label,
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = if (isIncluded) FontWeight.SemiBold else FontWeight.Normal,
                  modifier = Modifier.weight(1f)
                )

                if (isIncluded) {
                  Row {
                    IconButton(
                      onClick = {
                        if (indexInWorking > 0) {
                          val updated = workingList.toMutableList()
                          val item = updated.removeAt(indexInWorking)
                          updated.add(indexInWorking - 1, item)
                          workingList = updated
                        }
                      },
                      enabled = indexInWorking > 0,
                      modifier = Modifier.size(28.dp)
                    ) {
                      Icon(
                        Icons.Default.KeyboardArrowUp,
                        contentDescription = "Move tab up",
                        modifier = Modifier.size(16.dp)
                      )
                    }

                    IconButton(
                      onClick = {
                        if (indexInWorking < workingList.lastIndex) {
                          val updated = workingList.toMutableList()
                          val item = updated.removeAt(indexInWorking)
                          updated.add(indexInWorking + 1, item)
                          workingList = updated
                        }
                      },
                      enabled = indexInWorking < workingList.lastIndex,
                      modifier = Modifier.size(28.dp)
                    ) {
                      Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = "Move tab down",
                        modifier = Modifier.size(16.dp)
                      )
                    }
                  }
                }
              }
            }
          }
        }
      }
    },
    confirmButton = {
      TextButton(
        onClick = {
          onSaveTabs(workingList)
          onDismiss()
        },
        modifier = Modifier.testTag("btn_save_nav_tabs")
      ) {
        Text("Save")
      }
    },
    dismissButton = {
      Row {
        TextButton(
          onClick = {
            onResetToDefault()
            onDismiss()
          }
        ) {
          Text("Reset")
        }
        TextButton(onClick = onDismiss) {
          Text("Cancel")
        }
      }
    }
  )
}

@Composable
fun CurrencySelectionDialog(
  currentSymbol: String,
  onSelectCurrency: (String) -> Unit,
  onDismiss: () -> Unit
) {
  val currencies = listOf(
    Pair("₹", "INR - Indian Rupee"),
    Pair("$", "USD - US Dollar"),
    Pair("€", "EUR - Euro"),
    Pair("£", "GBP - British Pound"),
    Pair("¥", "JPY - Japanese Yen"),
    Pair("C$", "CAD - Canadian Dollar"),
    Pair("A$", "AUD - Australian Dollar"),
    Pair("CHF", "CHF - Swiss Franc")
  )

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Currency Symbol", fontWeight = FontWeight.Bold) },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        currencies.forEach { (sym, desc) ->
          val isSelected = currentSymbol == sym
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .clickable {
                onSelectCurrency(sym)
                onDismiss()
              }
              .testTag("currency_option_$sym")
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = sym,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.width(40.dp)
              )
              Text(
                text = desc,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
              )
              if (isSelected) {
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = "Selected",
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(20.dp)
                )
              }
            }
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text("Close")
      }
    }
  )
}

fun getNavTabIcon(destination: NavTabDestination): ImageVector = when (destination) {
  NavTabDestination.HOME -> Icons.Default.Home
  NavTabDestination.TRANSACTIONS -> Icons.AutoMirrored.Filled.ReceiptLong
  NavTabDestination.BUDGETS -> Icons.Default.PieChart
  NavTabDestination.STOCKS -> Icons.AutoMirrored.Filled.ShowChart
  NavTabDestination.LOANS -> Icons.Default.Handshake
  NavTabDestination.RECURRING -> Icons.Default.Repeat
  NavTabDestination.GOALS -> Icons.Default.EmojiEvents
  NavTabDestination.ANALYTICS -> Icons.AutoMirrored.Filled.TrendingUp
  NavTabDestination.NET_WORTH -> Icons.Default.AccountBalance
  NavTabDestination.MORE -> Icons.Default.MoreHoriz
}
