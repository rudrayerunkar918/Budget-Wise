package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExpenseCategory
import com.example.ui.viewmodel.CategorySpendSummary
import com.example.ui.viewmodel.DailySpend
import java.util.Locale
import kotlin.math.atan2

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryDonutChart(
  categorySummaries: List<CategorySpendSummary>,
  totalSpent: Double,
  modifier: Modifier = Modifier,
  currencySymbol: String = "₹",
  onCategoryClick: ((ExpenseCategory) -> Unit)? = null
) {
  var selectedCategory by remember { mutableStateOf<ExpenseCategory?>(null) }
  val animationProgress = remember { Animatable(0f) }

  LaunchedEffect(categorySummaries) {
    animationProgress.snapTo(0f)
    animationProgress.animateTo(
      targetValue = 1f,
      animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
    )
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("category_donut_chart_card"),
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(20.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Spending Breakdown",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "By category this month",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        if (selectedCategory != null) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.clickable { selectedCategory = null }
          ) {
            Text(
              text = "Reset selection",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSecondaryContainer,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      if (categorySummaries.isEmpty() || totalSpent <= 0.0) {
        // Empty State
        Box(
          modifier = Modifier
            .size(200.dp)
            .padding(16.dp),
          contentAlignment = Alignment.Center
        ) {
          Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
              color = Color.LightGray.copy(alpha = 0.3f),
              style = Stroke(width = 24.dp.toPx())
            )
          }
          Text(
            text = "No expenses recorded yet",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
          )
        }
      } else {
        // Donut Chart Canvas with interactive tap
        Box(
          modifier = Modifier
            .size(220.dp)
            .padding(8.dp),
          contentAlignment = Alignment.Center
        ) {
          val activeCat = selectedCategory
          Canvas(
            modifier = Modifier
              .fillMaxSize()
              .pointerInput(categorySummaries) {
                detectTapGestures { offset ->
                  val center = Offset(size.width / 2f, size.height / 2f)
                  val dx = offset.x - center.x
                  val dy = offset.y - center.y
                  var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                  if (angle < 0) angle += 360f

                  // Donut starts at -90 degrees (top)
                  val shiftedAngle = (angle + 90f) % 360f

                  var accumulated = 0f
                  for (item in categorySummaries) {
                    val sweep = (item.percentageOfTotal * 360f)
                    if (shiftedAngle in accumulated..(accumulated + sweep)) {
                      selectedCategory = if (selectedCategory == item.category) null else item.category
                      onCategoryClick?.invoke(item.category)
                      break
                    }
                    accumulated += sweep
                  }
                }
              }
          ) {
            val strokeWidthDefault = 26.dp.toPx()
            val strokeWidthHighlighted = 34.dp.toPx()
            val diameter = size.minDimension - strokeWidthHighlighted
            val topLeft = Offset(
              (size.width - diameter) / 2f,
              (size.height - diameter) / 2f
            )
            val chartSize = Size(diameter, diameter)

            var startAngle = -90f
            val spacingDegrees = if (categorySummaries.size > 1) 2.5f else 0f

            categorySummaries.forEach { item ->
              val rawSweep = item.percentageOfTotal * 360f
              val sweep = (rawSweep - spacingDegrees).coerceAtLeast(0.5f) * animationProgress.value
              val isSelected = activeCat == null || activeCat == item.category
              val strokeWidth = if (activeCat == item.category) strokeWidthHighlighted else strokeWidthDefault
              val color = if (isSelected) item.category.color else item.category.color.copy(alpha = 0.25f)

              drawArc(
                color = color,
                startAngle = startAngle + (spacingDegrees / 2f),
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = chartSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
              )
              startAngle += rawSweep
            }
          }

          // Center Text Metric
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 24.dp)
          ) {
            if (activeCat != null) {
              val item = categorySummaries.firstOrNull { it.category == activeCat }
              val catSpent = item?.spent ?: 0.0
              val catPct = ((item?.percentageOfTotal ?: 0f) * 100).toInt()
              Text(
                text = activeCat.displayName,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Text(
                text = "$currencySymbol${formatAmount(catSpent)}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "$catPct% of total",
                style = MaterialTheme.typography.labelSmall,
                color = activeCat.color,
                fontWeight = FontWeight.SemiBold
              )
            } else {
              Text(
                text = "Total Spent",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "$currencySymbol${formatAmount(totalSpent)}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "${categorySummaries.size} categories",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Category Legend Chips
        FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          categorySummaries.forEach { item ->
            val isSelected = selectedCategory == item.category
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (isSelected) {
                item.category.color.copy(alpha = 0.2f)
              } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
              },
              modifier = Modifier
                .clickable {
                  selectedCategory = if (isSelected) null else item.category
                  onCategoryClick?.invoke(item.category)
                }
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(item.category.color)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = item.category.displayName,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "$currencySymbol${formatAmount(item.spent)}",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun SpendingTrendBarChart(
  dailySpends: List<DailySpend>,
  modifier: Modifier = Modifier,
  currencySymbol: String = "₹"
) {
  val maxAmount = remember(dailySpends) {
    (dailySpends.maxOfOrNull { it.amount } ?: 100.0).coerceAtLeast(50.0)
  }
  val averageDaily = remember(dailySpends) {
    if (dailySpends.isNotEmpty()) dailySpends.sumOf { it.amount } / dailySpends.size else 0.0
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("spending_trend_bar_chart_card"),
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
            text = "Daily Spending Trend",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "Last 7 days activity",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Avg: $currencySymbol${formatAmount(averageDaily)}/day",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.SemiBold
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Bar Chart Bars
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .height(160.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
      ) {
        dailySpends.forEach { day ->
          val fraction = if (maxAmount > 0) (day.amount / maxAmount).toFloat().coerceIn(0.04f, 1f) else 0.04f
          val animatedHeightFraction by animateFloatAsState(
            targetValue = fraction,
            animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
            label = "bar_height"
          )

          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
              .weight(1f)
              .fillMaxHeight(),
            verticalArrangement = Arrangement.Bottom
          ) {
            if (day.amount > 0) {
              Text(
                text = "$currencySymbol${day.amount.toInt()}",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = if (day.isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1
              )
              Spacer(modifier = Modifier.height(4.dp))
            }

            Box(
              modifier = Modifier
                .width(22.dp)
                .fillMaxHeight(animatedHeightFraction)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
                .background(
                  if (day.isToday) {
                    Brush.verticalGradient(
                      listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
                      )
                    )
                  } else {
                    Brush.verticalGradient(
                      listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.surfaceVariant
                      )
                    )
                  }
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
              text = day.dayLabel,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal,
              color = if (day.isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }
  }
}

@Composable
fun BudgetProgressCard(
  title: String,
  spent: Double,
  limit: Double,
  category: ExpenseCategory?,
  onEditClick: () -> Unit,
  modifier: Modifier = Modifier,
  currencySymbol: String = "₹"
) {
  val fraction = if (limit > 0) (spent / limit).toFloat() else 0f
  val progressAnimated by animateFloatAsState(
    targetValue = fraction.coerceIn(0f, 1f),
    animationSpec = tween(durationMillis = 600),
    label = "budget_progress"
  )
  val isOverBudget = spent > limit
  val isWarning = fraction >= 0.8f && !isOverBudget
  val percentInt = (fraction * 100).toInt()

  val statusColor = when {
    isOverBudget -> MaterialTheme.colorScheme.error
    isWarning -> Color(0xFFE67E22)
    else -> MaterialTheme.colorScheme.primary
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("budget_progress_card_${title.lowercase().replace(" ", "_")}"),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
        Row(verticalAlignment = Alignment.CenterVertically) {
          if (category != null) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(category.color.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = category.icon,
                contentDescription = category.displayName,
                tint = category.color,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
          }
          Column {
            Text(
              text = title,
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = if (isOverBudget) {
                "Exceeded by $currencySymbol${formatAmount(spent - limit)}"
              } else {
                "$currencySymbol${formatAmount(limit - spent)} remaining"
              },
              style = MaterialTheme.typography.bodySmall,
              color = if (isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        // Status Badge
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = statusColor.copy(alpha = 0.15f),
          modifier = Modifier.clickable { onEditClick() }
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            if (isOverBudget) {
              Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Over Budget",
                tint = statusColor,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
              text = "$percentInt%",
              style = MaterialTheme.typography.labelSmall,
              color = statusColor,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Progress Bar
      LinearProgressIndicator(
        progress = { progressAnimated },
        modifier = Modifier
          .fillMaxWidth()
          .height(8.dp)
          .clip(RoundedCornerShape(4.dp)),
        color = statusColor,
        trackColor = MaterialTheme.colorScheme.surfaceVariant
      )

      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = "Spent: $currencySymbol${formatAmount(spent)}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
          text = "Limit: $currencySymbol${formatAmount(limit)}",
          style = MaterialTheme.typography.bodySmall,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface
        )
      }
    }
  }
}

@Composable
fun IncomeExpenseComparisonCard(
  income: Double,
  expense: Double,
  net: Double,
  modifier: Modifier = Modifier,
  currencySymbol: String = "₹"
) {
  val totalFlow = (income + expense).coerceAtLeast(1.0)
  val incomeRatio = (income / totalFlow).toFloat().coerceIn(0f, 1f)
  val expenseRatio = (expense / totalFlow).toFloat().coerceIn(0f, 1f)
  val savingsRate = if (income > 0) (((income - expense) / income) * 100).toInt().coerceIn(0, 100) else 0

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("income_expense_comparison_card"),
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
            text = "Monthly Cash Flow",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "Income vs Expenses",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color(0xFF1B664B).copy(alpha = 0.15f)
        ) {
          Text(
            text = "Save Rate: $savingsRate%",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1B664B),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Ratio visual bar
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .height(12.dp)
          .clip(RoundedCornerShape(6.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant)
      ) {
        if (incomeRatio > 0f) {
          Box(
            modifier = Modifier
              .weight(incomeRatio)
              .fillMaxHeight()
              .background(Color(0xFF00B894))
          )
        }
        if (expenseRatio > 0f) {
          Box(
            modifier = Modifier
              .weight(expenseRatio)
              .fillMaxHeight()
              .background(Color(0xFFFF6B6B))
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(Color(0xFF00B894))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Total Income",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "+$currencySymbol${formatAmount(income)}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00B894)
          )
        }

        Column(horizontalAlignment = Alignment.End) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(Color(0xFFFF6B6B))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Total Expenses",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "-$currencySymbol${formatAmount(expense)}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFF6B6B)
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Net Saved",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = (if (net >= 0) "+$currencySymbol" else "-$currencySymbol") + formatAmount(kotlin.math.abs(net)),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = if (net >= 0) Color(0xFF00B894) else Color(0xFFFF6B6B)
          )
        }
      }
    }
  }
}

private fun formatAmount(amount: Double): String {
  return String.format(Locale.US, "%,.2f", amount)
}
