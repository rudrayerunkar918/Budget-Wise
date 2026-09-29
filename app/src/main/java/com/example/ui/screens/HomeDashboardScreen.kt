package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountEntity
import com.example.data.model.BudgetEntity
import com.example.data.model.ExpenseEntity
import com.example.data.model.LoanEntity
import com.example.data.model.SavingsGoalEntity
import com.example.data.model.SubscriptionEntity
import com.example.data.model.ExpenseCategory
import com.example.data.model.ExpenseLogEntity
import com.example.data.preferences.HomeSectionItem
import com.example.data.preferences.HomeSectionType
import com.example.data.preferences.NavTabDestination
import com.example.ui.components.CategoryDonutChart
import com.example.ui.components.DailyExpenseLogSectionCard
import com.example.ui.viewmodel.CategorySpendSummary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun HomeDashboardScreen(
  sections: List<HomeSectionItem>,
  userName: String,
  greeting: String,
  currencySymbol: String,
  netWorth: Double,
  accounts: List<AccountEntity>,
  expenses: List<ExpenseEntity>,
  budgets: List<BudgetEntity>,
  goals: List<SavingsGoalEntity>,
  subscriptions: List<SubscriptionEntity>,
  loans: List<LoanEntity>,
  totalLent: Double,
  totalBorrowed: Double,
  totalIncomeThisMonth: Double,
  totalSpentThisMonth: Double,
  categorySummaries: List<CategorySpendSummary>,
  overallBudget: Double?,
  dailyExpenseLogs: List<ExpenseLogEntity> = emptyList(),
  todayDailyTotal: Double = 0.0,
  onNavigateToTab: (NavTabDestination) -> Unit,
  onOpenEditHomeScreen: () -> Unit,
  onAddAccount: () -> Unit,
  onAccountClick: (AccountEntity) -> Unit = {},
  onExpenseClick: (ExpenseEntity) -> Unit = {},
  onOpenGeminiAssistant: () -> Unit = {},
  onOpenAddExpenseLog: () -> Unit = {},
  onDeleteExpenseLog: (ExpenseLogEntity) -> Unit = {},
  modifier: Modifier = Modifier
) {
  val enabledSections = sections.filter { it.isEnabled }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 10.dp, bottom = 80.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    for (item in enabledSections) {
      item(key = item.type.name) {
        when (item.type) {
          HomeSectionType.HOMEPAGE_BANNER -> {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
              HomepageBannerSection(
                greeting = greeting,
                userName = userName,
                onOpenEditHome = onOpenEditHomeScreen,
                onOpenGeminiAssistant = onOpenGeminiAssistant
              )
              DailyExpenseLogSectionCard(
                dailyLogs = dailyExpenseLogs,
                todayTotal = todayDailyTotal,
                currencySymbol = currencySymbol,
                onOpenAddLog = onOpenAddExpenseLog,
                onDeleteLog = onDeleteExpenseLog
              )
            }
          }
          HomeSectionType.ACCOUNTS -> {
            AccountsCarouselSection(
              accounts = accounts,
              expenses = expenses,
              currencySymbol = currencySymbol,
              onAddAccount = onAddAccount,
              onAccountClick = onAccountClick
            )
          }
          HomeSectionType.ACCOUNTS_LIST -> {
            AccountsListSection(
              accounts = accounts,
              currencySymbol = currencySymbol,
              onAccountClick = onAccountClick
            )
          }
          HomeSectionType.BUDGETS -> {
            BudgetsHomeSection(
              budgets = budgets,
              overallBudget = overallBudget,
              totalSpent = totalSpentThisMonth,
              currencySymbol = currencySymbol,
              onNavigateToBudgets = { onNavigateToTab(NavTabDestination.BUDGETS) }
            )
          }
          HomeSectionType.GOALS -> {
            GoalsHomeSection(
              goals = goals,
              currencySymbol = currencySymbol,
              onNavigateToGoals = { onNavigateToTab(NavTabDestination.GOALS) }
            )
          }
          HomeSectionType.INCOME_EXPENSES -> {
            IncomeExpensesHomeSection(
              income = totalIncomeThisMonth,
              expense = totalSpentThisMonth,
              expensesList = expenses,
              currencySymbol = currencySymbol
            )
          }
          HomeSectionType.NET_WORTH -> {
            NetWorthHomeSection(
              netWorth = netWorth,
              income = totalIncomeThisMonth,
              expense = totalSpentThisMonth,
              currencySymbol = currencySymbol,
              onOpenNetWorthTab = { onNavigateToTab(NavTabDestination.NET_WORTH) }
            )
          }
          HomeSectionType.OVERDUE_UPCOMING -> {
            OverdueUpcomingHomeSection(
              subscriptions = subscriptions,
              currencySymbol = currencySymbol,
              onNavigateToRecurring = { onNavigateToTab(NavTabDestination.RECURRING) }
            )
          }
          HomeSectionType.PIE_CHART -> {
            PieChartHomeSection(
              categorySummaries = categorySummaries,
              totalSpent = totalSpentThisMonth,
              currencySymbol = currencySymbol,
              onNavigateToAnalytics = { onNavigateToTab(NavTabDestination.ANALYTICS) }
            )
          }
          HomeSectionType.SPENDING_GRAPH -> {
            SpendingGraphHomeSection(
              expenses = expenses,
              netWorth = netWorth,
              currencySymbol = currencySymbol
            )
          }
          HomeSectionType.LOANS -> {
            LoansHomeSection(
              totalLent = totalLent,
              totalBorrowed = totalBorrowed,
              loansList = loans,
              currencySymbol = currencySymbol,
              onNavigateToLoans = { onNavigateToTab(NavTabDestination.LOANS) }
            )
          }
          HomeSectionType.STACKED_BAR -> {
            StackedBarHomeSection(
              income = totalIncomeThisMonth,
              expense = totalSpentThisMonth,
              categorySummaries = categorySummaries,
              currencySymbol = currencySymbol
            )
          }
          HomeSectionType.PINNED_TRANSACTIONS -> {
            PinnedTransactionsHomeSection(
              expenses = expenses.take(6),
              currencySymbol = currencySymbol,
              onExpenseClick = onExpenseClick,
              onNavigateToTransactions = { onNavigateToTab(NavTabDestination.TRANSACTIONS) }
            )
          }
          HomeSectionType.STOCKS -> {
            StocksHomeSection(
              currencySymbol = currencySymbol,
              onNavigateToStocks = { onNavigateToTab(NavTabDestination.STOCKS) }
            )
          }
        }
      }
    }
  }
}

// 1. Homepage Banner (Greeting & Rudra Yerunkar)
@Composable
private fun HomepageBannerSection(
  greeting: String,
  userName: String,
  onOpenEditHome: () -> Unit,
  onOpenGeminiAssistant: () -> Unit = {}
) {
  val dateStr = remember {
    SimpleDateFormat("EEEE, d MMMM", Locale.US).format(Calendar.getInstance().time)
  }

  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("banner_welcome_card")
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          Brush.horizontalGradient(
            listOf(
              MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
              MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
              MaterialTheme.colorScheme.surface
            )
          )
        )
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
          Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.surface),
            shadowElevation = 2.dp,
            modifier = Modifier.size(46.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Text(
                text = userName.firstOrNull()?.uppercase() ?: "B",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
              )
            }
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = greeting,
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = userName,
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
              ) {
                Text(
                  text = "📅 $dateStr",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Medium,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }
          }
        }

        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
            shadowElevation = 1.dp,
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .clickable(onClick = onOpenGeminiAssistant)
              .testTag("btn_banner_gemini_ai")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 9.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "Gemini AI",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Gemini",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
            }
          }

          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            shadowElevation = 1.dp,
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .clickable(onClick = onOpenEditHome)
              .testTag("btn_banner_edit_home")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.DashboardCustomize,
                contentDescription = "Edit Home Screen",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Edit",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
            }
          }
        }
      }
    }
  }
}

// 2. Accounts Horizontal Carousel (Bank, Cash, Shares, etc.)
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AccountsCarouselSection(
  accounts: List<AccountEntity>,
  expenses: List<ExpenseEntity>,
  currencySymbol: String,
  onAddAccount: () -> Unit,
  onAccountClick: (AccountEntity) -> Unit = {}
) {
  val scrollState = rememberScrollState()

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .horizontalScroll(scrollState),
    horizontalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    accounts.forEach { account ->
      val txCount = expenses.count { it.account == account.name }
      val accColor = Color(account.colorHex)
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, accColor.copy(alpha = 0.45f)),
        modifier = Modifier
          .width(155.dp)
          .clip(RoundedCornerShape(16.dp))
          .combinedClickable(
            onClick = { onAccountClick(account) },
            onDoubleClick = { onAccountClick(account) }
          )
          .testTag("card_account_${account.name.lowercase()}")
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(
              Brush.verticalGradient(
                listOf(accColor.copy(alpha = 0.14f), MaterialTheme.colorScheme.surface)
              )
            )
            .padding(14.dp)
        ) {
          Column {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = account.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Box(
                modifier = Modifier
                  .size(14.dp)
                  .clip(CircleShape)
                  .background(accColor)
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "▲",
                color = Color(0xFF2ECC71),
                fontSize = 11.sp,
                modifier = Modifier.padding(end = 4.dp)
              )
              Text(
                text = "$currencySymbol${String.format(Locale.US, "%,.2f", account.balance)}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B664B)
              )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
              text = "$txCount transactions",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 10.sp
            )
          }
        }
      }
    }

    // Add Account Card
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
      ),
      elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
      modifier = Modifier
        .width(100.dp)
        .clickable { onAddAccount() }
        .testTag("btn_add_account_carousel")
    ) {
      Column(
        modifier = Modifier
          .padding(14.dp)
          .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        Icon(
          imageVector = Icons.Default.Add,
          contentDescription = "Add Account",
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(26.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "New",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.primary,
          fontWeight = FontWeight.Bold
        )
      }
    }
  }
}

// 3. Accounts Vertical List
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AccountsListSection(
  accounts: List<AccountEntity>,
  currencySymbol: String,
  onAccountClick: (AccountEntity) -> Unit = {}
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    modifier = Modifier.fillMaxWidth().testTag("card_accounts_vertical_list")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      accounts.forEach { account ->
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .combinedClickable(
              onClick = { onAccountClick(account) },
              onDoubleClick = { onAccountClick(account) }
            )
            .padding(vertical = 4.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(Color(account.colorHex))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
              text = account.name,
              style = MaterialTheme.typography.bodyLarge,
              fontWeight = FontWeight.Medium
            )
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "▲",
              color = Color(0xFF2ECC71),
              fontSize = 11.sp,
              modifier = Modifier.padding(end = 4.dp)
            )
            Text(
              text = "$currencySymbol${String.format(Locale.US, "%,.2f", account.balance)}",
              style = MaterialTheme.typography.bodyLarge,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF2ECC71)
            )
          }
        }
      }
    }
  }
}

// 4. Budgets Home Section
@Composable
private fun BudgetsHomeSection(
  budgets: List<BudgetEntity>,
  overallBudget: Double?,
  totalSpent: Double,
  currencySymbol: String,
  onNavigateToBudgets: () -> Unit
) {
  val budgetLimit = overallBudget ?: 2500.0
  val remaining = (budgetLimit - totalSpent).coerceAtLeast(0.0)
  val progress = (totalSpent / budgetLimit).coerceIn(0.0, 1.0).toFloat()
  val percent = (progress * 100).toInt()

  val cal = Calendar.getInstance()
  val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
  val currentDay = cal.get(Calendar.DAY_OF_MONTH)
  val daysRemaining = (daysInMonth - currentDay).coerceAtLeast(1)
  val dailySpendable = remaining / daysRemaining

  val monthFormat = remember { SimpleDateFormat("MMMM", Locale.US) }
  val monthName = remember { monthFormat.format(cal.time) }

  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onNavigateToBudgets() }
      .testTag("home_budget_card")
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Monthly Budget",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "$currencySymbol${String.format(Locale.US, "%,.0f", remaining)} left of $currencySymbol${String.format(Locale.US, "%,.0f", budgetLimit)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        IconButton(
          onClick = onNavigateToBudgets,
          modifier = Modifier.size(36.dp)
        ) {
          Icon(
            imageVector = Icons.Default.History,
            contentDescription = "Budget History",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Dates and progress badge
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "1 $monthName",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier.padding(horizontal = 4.dp)
        ) {
          Text(
            text = "Today $percent%",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
          )
        }
        Text(
          text = "$daysInMonth $monthName",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      LinearProgressIndicator(
        progress = { progress },
        modifier = Modifier
          .fillMaxWidth()
          .height(8.dp)
          .clip(RoundedCornerShape(4.dp)),
        color = if (percent > 90) Color(0xFFD9534F) else MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant
      )

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = "You can spend $currencySymbol${String.format(Locale.US, "%,.2f", dailySpendable)}/day for $daysRemaining more days",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

// 5. Goals Home Section
@Composable
private fun GoalsHomeSection(
  goals: List<SavingsGoalEntity>,
  currencySymbol: String,
  onNavigateToGoals: () -> Unit
) {
  val goal = goals.firstOrNull() ?: return

  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onNavigateToGoals() }
      .testTag("home_goal_card")
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = goal.title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(2.dp))
          val remainingDays = maxOf(1L, TimeUnit.MILLISECONDS.toDays(goal.deadlineTimestamp - System.currentTimeMillis()))
          val needed = (goal.targetAmount - goal.currentAmount).coerceAtLeast(0.0)
          val dailyNeeded = needed / remainingDays
          Text(
            text = "$currencySymbol${String.format(Locale.US, "%,.2f", dailyNeeded)}/day needed",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Surface(
          shape = CircleShape,
          color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
          modifier = Modifier.size(40.dp)
        ) {
          Icon(
            imageVector = Icons.Default.EmojiEvents,
            contentDescription = "Goal",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(10.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "$currencySymbol${String.format(Locale.US, "%,.0f", goal.currentAmount)} / $currencySymbol${String.format(Locale.US, "%,.0f", goal.targetAmount)}",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "${goal.progressPercent}%",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      LinearProgressIndicator(
        progress = { goal.progressFraction },
        modifier = Modifier
          .fillMaxWidth()
          .height(8.dp)
          .clip(RoundedCornerShape(4.dp)),
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant
      )
    }
  }
}

// 6. Income & Expenses Dual Cards
@Composable
private fun IncomeExpensesHomeSection(
  income: Double,
  expense: Double,
  expensesList: List<ExpenseEntity>,
  currencySymbol: String
) {
  val expenseTxCount = expensesList.count { it.type == "EXPENSE" }
  val incomeTxCount = expensesList.count { it.type == "INCOME" }

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF4F2)),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
      border = BorderStroke(1.dp, Color(0xFFFFCDD2)),
      modifier = Modifier
        .weight(1f)
        .testTag("home_card_expense")
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Surface(
            shape = CircleShape,
            color = Color(0xFFFF5B5B).copy(alpha = 0.15f),
            modifier = Modifier.size(24.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Text("▼", color = Color(0xFFFF5B5B), fontSize = 11.sp)
            }
          }
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Expense",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFC0392B)
          )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "$currencySymbol${String.format(Locale.US, "%,.2f", expense)}",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = Color(0xFFFF5B5B)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "x$expenseTxCount All Time",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontSize = 11.sp
        )
      }
    }

    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
      border = BorderStroke(1.dp, Color(0xFFC8E6C9)),
      modifier = Modifier
        .weight(1f)
        .testTag("home_card_income")
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Surface(
            shape = CircleShape,
            color = Color(0xFF2ECC71).copy(alpha = 0.15f),
            modifier = Modifier.size(24.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Text("▲", color = Color(0xFF2ECC71), fontSize = 11.sp)
            }
          }
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Income",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1B664B)
          )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "$currencySymbol${String.format(Locale.US, "%,.2f", income)}",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF2ECC71)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "x$incomeTxCount All Time",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontSize = 11.sp
        )
      }
    }
  }
}

// 7. Net Worth Home Section (Restored classic Hero card)
@Composable
private fun NetWorthHomeSection(
  netWorth: Double,
  income: Double = 0.0,
  expense: Double = 0.0,
  currencySymbol: String,
  onOpenNetWorthTab: (() -> Unit)? = null
) {
  val net = income - expense
  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    modifier = Modifier.fillMaxWidth().testTag("home_card_net_worth")
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
        Row(verticalAlignment = Alignment.CenterVertically) {
          Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
            modifier = Modifier.size(36.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                contentDescription = "Net Worth",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
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
              text = "Assets & Liquidity",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 11.sp
            )
          }
        }

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
          modifier = Modifier.clickable { onOpenNetWorthTab?.invoke() }
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Graph & Details",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary,
              fontSize = 10.sp
            )
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(10.dp).padding(start = 2.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = "$currencySymbol${String.format(Locale.US, "%,.2f", netWorth)}",
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.ExtraBold,
        color = Color(0xFF1B664B)
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Flow row: Income, Spent, Net Saved
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color(0xFF00B894).copy(alpha = 0.12f),
          border = BorderStroke(1.dp, Color(0xFF00B894).copy(alpha = 0.25f)),
          modifier = Modifier.weight(1f)
        ) {
          Column(modifier = Modifier.padding(vertical = 10.dp, horizontal = 10.dp)) {
            Text(
              text = "Income",
              style = MaterialTheme.typography.labelSmall,
              color = Color(0xFF00896F),
              fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "+$currencySymbol${String.format(Locale.US, "%,.0f", income)}",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF00B894)
            )
          }
        }

        Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color(0xFFFF6B6B).copy(alpha = 0.12f),
          border = BorderStroke(1.dp, Color(0xFFFF6B6B).copy(alpha = 0.25f)),
          modifier = Modifier.weight(1f)
        ) {
          Column(modifier = Modifier.padding(vertical = 10.dp, horizontal = 10.dp)) {
            Text(
              text = "Spent",
              style = MaterialTheme.typography.labelSmall,
              color = Color(0xFFD63031),
              fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "-$currencySymbol${String.format(Locale.US, "%,.0f", expense)}",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = Color(0xFFFF6B6B)
            )
          }
        }

        Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color(0xFF0984E3).copy(alpha = 0.12f),
          border = BorderStroke(1.dp, Color(0xFF0984E3).copy(alpha = 0.25f)),
          modifier = Modifier.weight(1f)
        ) {
          Column(modifier = Modifier.padding(vertical = 10.dp, horizontal = 10.dp)) {
            Text(
              text = "Net Saved",
              style = MaterialTheme.typography.labelSmall,
              color = Color(0xFF0984E3),
              fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = (if (net >= 0) "+$currencySymbol" else "-$currencySymbol") + String.format(Locale.US, "%,.0f", kotlin.math.abs(net)),
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = if (net >= 0) Color(0xFF0984E3) else Color(0xFFFF6B6B)
            )
          }
        }
      }
    }
  }
}

// 8. Overdue & Upcoming Section
@Composable
private fun OverdueUpcomingHomeSection(
  subscriptions: List<SubscriptionEntity>,
  currencySymbol: String,
  onNavigateToRecurring: () -> Unit
) {
  val upcomingSum = subscriptions.fold(0.0) { acc, sub -> acc + sub.amount }
  val upcomingCount = subscriptions.size

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
      modifier = Modifier
        .weight(1f)
        .clickable { onNavigateToRecurring() }
        .testTag("home_card_upcoming")
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.CalendarMonth,
            contentDescription = "Upcoming",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Upcoming",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
          )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "$currencySymbol${String.format(Locale.US, "%,.0f", upcomingSum)}",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "x$upcomingCount All Time",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontSize = 11.sp
        )
      }
    }

    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
      modifier = Modifier
        .weight(1f)
        .clickable { onNavigateToRecurring() }
        .testTag("home_card_overdue")
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Schedule,
            contentDescription = "Overdue",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Overdue",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
          )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "${currencySymbol}0",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "x0 All Time",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontSize = 11.sp
        )
      }
    }
  }
}

// 9. Pie Chart Section
@Composable
private fun PieChartHomeSection(
  categorySummaries: List<CategorySpendSummary>,
  totalSpent: Double,
  currencySymbol: String,
  onNavigateToAnalytics: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onNavigateToAnalytics() }
      .testTag("home_card_pie_chart")
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Text(
        text = "Expense Distribution",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )
      Spacer(modifier = Modifier.height(12.dp))
      CategoryDonutChart(
        categorySummaries = categorySummaries,
        totalSpent = totalSpent
      )
    }
  }
}

// 10. Spending Graph Curve Section
@Composable
private fun SpendingGraphHomeSection(
  expenses: List<ExpenseEntity>,
  netWorth: Double,
  currencySymbol: String
) {
  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    modifier = Modifier.fillMaxWidth().testTag("home_card_spending_graph")
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Spending & Balance Trend",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "$currencySymbol${String.format(Locale.US, "%,.1fK", netWorth / 1000.0)}",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF2ECC71)
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Visual line canvas
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(110.dp)
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val w = size.width
          val h = size.height

          // Sample trend line points
          val points = listOf(
            Offset(0f, h * 0.35f),
            Offset(w * 0.15f, h * 0.40f),
            Offset(w * 0.28f, h * 0.90f),
            Offset(w * 0.38f, h * 0.82f),
            Offset(w * 0.48f, h * 0.22f),
            Offset(w * 0.65f, h * 0.20f),
            Offset(w * 0.85f, h * 0.15f),
            Offset(w, h * 0.10f)
          )

          val linePath = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (p in points.drop(1)) {
              lineTo(p.x, p.y)
            }
          }

          val fillPath = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (p in points.drop(1)) {
              lineTo(p.x, p.y)
            }
            lineTo(w, h)
            lineTo(0f, h)
            close()
          }

          drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
              colors = listOf(
                Color(0xFF2ECC71).copy(alpha = 0.35f),
                Color(0xFF2ECC71).copy(alpha = 0.02f)
              )
            )
          )

          drawPath(
            path = linePath,
            color = Color(0xFF2ECC71),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text("18 Aug", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("26 Aug", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("2 Sept", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("10 Sept", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("17 Sept", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
    }
  }
}

// 11. Loans Home Section (Lent & Borrowed)
@Composable
private fun LoansHomeSection(
  totalLent: Double,
  totalBorrowed: Double,
  loansList: List<LoanEntity>,
  currencySymbol: String,
  onNavigateToLoans: () -> Unit
) {
  val lentCount = loansList.count { it.type == "LENT" }
  val borrowedCount = loansList.count { it.type == "LOAN" }

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF8FF)),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
      border = BorderStroke(1.dp, Color(0xFFBBDEFB)),
      modifier = Modifier
        .weight(1f)
        .clickable { onNavigateToLoans() }
        .testTag("home_card_lent")
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text("▲", color = Color(0xFF00B894), fontSize = 12.sp, modifier = Modifier.padding(end = 4.dp))
          Text(
            text = "Lent",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
          )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "$currencySymbol${String.format(Locale.US, "%,.0f", totalLent)}",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF00B894)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "x$lentCount All Time",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontSize = 11.sp
        )
      }
    }

    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF7EE)),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
      border = BorderStroke(1.dp, Color(0xFFFFE0B2)),
      modifier = Modifier
        .weight(1f)
        .clickable { onNavigateToLoans() }
        .testTag("home_card_borrowed")
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text("▼", color = Color(0xFFE55039), fontSize = 12.sp, modifier = Modifier.padding(end = 4.dp))
          Text(
            text = "Borrowed",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
          )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "$currencySymbol${String.format(Locale.US, "%,.0f", totalBorrowed)}",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = Color(0xFFE55039)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "x$borrowedCount All Time",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontSize = 11.sp
        )
      }
    }
  }
}

// 12. Stacked Bar Graph Section
@Composable
private fun StackedBarHomeSection(
  income: Double,
  expense: Double,
  categorySummaries: List<CategorySpendSummary>,
  currencySymbol: String
) {
  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    modifier = Modifier.fillMaxWidth().testTag("home_card_stacked_bar")
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Text(
        text = "Spending Breakdown",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )

      Spacer(modifier = Modifier.height(14.dp))

      // Stacked Bar Row
      val colors = listOf(
        Color(0xFF2ECC71),
        Color(0xFF3498DB),
        Color(0xFFF1C40F),
        Color(0xFFE74C3C),
        Color(0xFF9B59B6),
        Color(0xFF1ABC9C)
      )

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .height(18.dp)
          .clip(RoundedCornerShape(9.dp))
      ) {
        if (categorySummaries.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(MaterialTheme.colorScheme.surfaceVariant)
          )
        } else {
          categorySummaries.forEachIndexed { idx, cat ->
            val fraction = cat.percentageOfTotal / 100f
            if (fraction > 0.01f) {
              Box(
                modifier = Modifier
                  .weight(fraction.coerceAtLeast(0.05f))
                  .height(18.dp)
                  .background(colors[idx % colors.size])
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = "${currencySymbol}0",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
          text = "$currencySymbol${String.format(Locale.US, "%,.0f", expense)}",
          style = MaterialTheme.typography.bodySmall,
          fontWeight = FontWeight.Bold
        )
      }
    }
  }
}

// 13. Pinned Transactions List Section
@Composable
private fun PinnedTransactionsHomeSection(
  expenses: List<ExpenseEntity>,
  currencySymbol: String,
  onExpenseClick: (ExpenseEntity) -> Unit = {},
  onNavigateToTransactions: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    modifier = Modifier.fillMaxWidth().testTag("home_card_pinned_tx")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Recent Transactions",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onNavigateToTransactions() }
        ) {
          Text(
            text = "View All",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(4.dp))

      expenses.forEach { tx ->
        val category = ExpenseCategory.fromName(tx.category)
        val isIncome = tx.type == "INCOME"
        val isTransfer = tx.type == "TRANSFER"

        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.surface,
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
          shadowElevation = 0.5.dp,
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onExpenseClick(tx) }
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            val isScheduled = tx.isScheduled
            val dateTimeFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.US) }
            val formattedDateTime = remember(tx.timestamp) { dateTimeFormat.format(Date(tx.timestamp)) }

            Row(
              modifier = Modifier.weight(1f),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Surface(
                shape = CircleShape,
                color = when {
                  isIncome -> Color(0xFF2ECC71).copy(alpha = 0.15f)
                  isTransfer -> Color(0xFF3498DB).copy(alpha = 0.15f)
                  else -> category.color.copy(alpha = 0.15f)
                },
                modifier = Modifier.size(38.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = category.icon,
                    contentDescription = category.displayName,
                    tint = when {
                      isIncome -> Color(0xFF2ECC71)
                      isTransfer -> Color(0xFF3498DB)
                      else -> category.color
                    },
                    modifier = Modifier.size(20.dp)
                  )
                }
              }

              Spacer(modifier = Modifier.width(10.dp))

              Column(modifier = Modifier.weight(1f, fill = false)) {
                Text(
                  text = tx.title.ifBlank { category.displayName },
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurface,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                  if (isScheduled) {
                    Surface(
                      shape = RoundedCornerShape(4.dp),
                      color = Color(0xFF0984E3).copy(alpha = 0.15f)
                    ) {
                      Text(
                        text = "Scheduled",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF0984E3),
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                      )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                  }
                  Text(
                    text = category.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                  val detail = tx.note.ifBlank { tx.account }
                  if (detail.isNotBlank()) {
                    Text(
                      text = " • $detail",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                  }
                }
              }
            }

            Spacer(modifier = Modifier.width(10.dp))

            val sign = if (isIncome) "+" else if (isTransfer) "" else "-"
            val amtColor = when {
              isIncome -> Color(0xFF2ECC71)
              isTransfer -> Color(0xFF3498DB)
              else -> Color(0xFFFF5B5B)
            }

            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = "$sign$currencySymbol${String.format(Locale.US, "%,.2f", tx.amount)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = amtColor,
                maxLines = 1,
                softWrap = false
              )
              Spacer(modifier = Modifier.height(2.dp))
              if (isScheduled) {
                Text(
                  text = "🕒 Scheduled",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF0984E3),
                  fontSize = 10.sp
                )
                Text(
                  text = formattedDateTime,
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                  fontSize = 9.sp
                )
              } else {
                Text(
                  text = formattedDateTime,
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                  fontSize = 10.sp
                )
              }
            }
          }
        }
      }
    }
  }
}

// 14. Investments (Stocks & Mutual Funds) Section
@Composable
private fun StocksHomeSection(
  currencySymbol: String,
  onNavigateToStocks: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onNavigateToStocks() },
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ShowChart,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Investments & Markets",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.width(8.dp))
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = Color(0xFF4285F4).copy(alpha = 0.12f)
          ) {
            Text(
              text = "Live Search",
              style = MaterialTheme.typography.labelSmall,
              color = Color(0xFF1967D2),
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }

        Text(
          text = "Open →",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.primary,
          fontWeight = FontWeight.SemiBold
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Real-time market indices preview
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          modifier = Modifier.weight(1f)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("NIFTY 50", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("25,320.65", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
          }
        }

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          modifier = Modifier.weight(1f)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("SENSEX", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("82,890.94", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "Track live Stocks, Mutual Funds NAV, IPO GMP & Market Indices grounded in real-time with Google Search.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}
