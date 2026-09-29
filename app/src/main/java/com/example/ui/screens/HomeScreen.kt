package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import com.example.ui.components.GeminiSpendingInsightsSheet
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AccountEntity
import com.example.data.model.BudgetEntity
import com.example.data.model.ExpenseCategory
import com.example.data.model.ExpenseEntity
import com.example.data.model.ExpenseLogEntity
import com.example.data.model.LoanEntity
import com.example.data.model.SavingsGoalEntity
import com.example.data.model.SubscriptionEntity
import com.example.data.model.TransactionShortcutEntity
import com.example.data.preferences.NavTabDestination
import com.example.data.preferences.ThemeMode
import com.example.notifications.GoalNotificationHelper
import com.example.ui.components.AddAccountDialog
import com.example.ui.components.AddDailyExpenseLogDialog
import com.example.ui.components.AddEditShortcutDialog
import com.example.ui.components.AddExpenseDialog
import com.example.ui.components.AddGoalDialog
import com.example.ui.components.AddLoanDialog
import com.example.ui.components.AddSubscriptionDialog
import com.example.ui.components.AddTransactionDialog
import com.example.ui.components.BudgetAllocationPlannerDialog
import com.example.ui.components.BudgetProgressCard
import com.example.ui.components.CategoryBudgetDialog
import com.example.ui.components.CategoryDonutChart
import com.example.ui.components.ContributeGoalDialog
import com.example.ui.components.CurrencySelectionDialog
import com.example.ui.components.EditAccountDialog
import com.example.ui.components.EditSavingsGoalDialog
import com.example.ui.components.EditSubscriptionDialog
import com.example.ui.components.EditTransactionDialog
import com.example.ui.components.IncomeExpenseComparisonCard
import com.example.ui.components.NavBarCustomizationDialog
import com.example.ui.components.NotificationHistorySheet
import com.example.ui.components.SetBudgetDialog
import com.example.ui.components.SpendingTrendBarChart
import com.example.ui.components.ThemeSelectionDialog
import com.example.ui.components.getNavTabIcon
import com.example.ui.screens.EditHomeScreen
import com.example.ui.screens.HomeDashboardScreen
import com.example.ui.screens.MoreScreen
import com.example.ui.viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
  viewModel: ExpenseViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val notificationHelper = remember { GoalNotificationHelper(context) }

  var hasNotificationPermission by remember {
    mutableStateOf(notificationHelper.hasNotificationPermission())
  }

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    hasNotificationPermission = isGranted
  }

  LaunchedEffect(Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
      permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
  }

  // Preferences & Customization state
  val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
  val navTabs by viewModel.navTabs.collectAsStateWithLifecycle()
  val homeSections by viewModel.homeSections.collectAsStateWithLifecycle()
  val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
  val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()
  val userName by viewModel.userName.collectAsStateWithLifecycle()
  val greeting by viewModel.greeting.collectAsStateWithLifecycle()
  val isEditHomeScreenOpen by viewModel.isEditHomeScreenOpen.collectAsStateWithLifecycle()

  val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
  val unreadNotificationsCount by viewModel.unreadCount.collectAsStateWithLifecycle()
  val allNotifications by viewModel.notifications.collectAsStateWithLifecycle()

  val netWorth by viewModel.netWorth.collectAsStateWithLifecycle()
  val allAccounts by viewModel.allAccounts.collectAsStateWithLifecycle()
  val allStocks by viewModel.allStocks.collectAsStateWithLifecycle()
  val allExpenses by viewModel.allExpenses.collectAsStateWithLifecycle()
  val allLoans by viewModel.allLoans.collectAsStateWithLifecycle()
  val allSubscriptions by viewModel.allSubscriptions.collectAsStateWithLifecycle()
  val totalMonthlySubscriptionsCost by viewModel.totalMonthlySubscriptionsCost.collectAsStateWithLifecycle()
  val dayGroupedTransactions by viewModel.dayGroupedTransactions.collectAsStateWithLifecycle()

  val totalIncomeThisMonth by viewModel.currentMonthTotalIncome.collectAsStateWithLifecycle()
  val totalSpentThisMonth by viewModel.currentMonthTotalSpent.collectAsStateWithLifecycle()
  val netSavingsThisMonth by viewModel.currentMonthNetSavings.collectAsStateWithLifecycle()

  val overallBudget by viewModel.overallMonthlyBudget.collectAsStateWithLifecycle()
  val categorySummaries by viewModel.categorySpendSummaries.collectAsStateWithLifecycle()
  val last7DaysSpend by viewModel.last7DaysSpend.collectAsStateWithLifecycle()
  val allGoals by viewModel.allGoals.collectAsStateWithLifecycle()
  val allBudgets by viewModel.allBudgets.collectAsStateWithLifecycle()
  val totalLent by viewModel.totalLentActive.collectAsStateWithLifecycle()
  val totalBorrowed by viewModel.totalBorrowedActive.collectAsStateWithLifecycle()
  val netDebt by viewModel.netDebtPosition.collectAsStateWithLifecycle()

  val selectedFilterCategory by viewModel.selectedFilterCategory.collectAsStateWithLifecycle()
  val selectedAccountFilter by viewModel.selectedAccountFilter.collectAsStateWithLifecycle()
  val selectedTransactionType by viewModel.selectedTransactionType.collectAsStateWithLifecycle()
  val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
  val allShortcuts by viewModel.allShortcuts.collectAsStateWithLifecycle()
  val todayExpenseLogs by viewModel.todayExpenseLogs.collectAsStateWithLifecycle()
  val todayExpenseTotal by viewModel.todayExpenseTotal.collectAsStateWithLifecycle()

  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  // Dialog States
  var showAddTransactionDialog by remember { mutableStateOf(false) }
  var showAddDailyExpenseDialog by remember { mutableStateOf(false) }
  var expenseToEdit by remember { mutableStateOf<ExpenseEntity?>(null) }
  var showAddLoanDialog by remember { mutableStateOf(false) }
  var showAddSubscriptionDialog by remember { mutableStateOf(false) }
  var subscriptionToEdit by remember { mutableStateOf<SubscriptionEntity?>(null) }
  var showAddAccountDialog by remember { mutableStateOf(false) }
  var accountToEdit by remember { mutableStateOf<AccountEntity?>(null) }
  var showSetBudgetDialog by remember { mutableStateOf(false) }
  var budgetCategoryToEdit by remember { mutableStateOf("TOTAL") }
  var budgetLimitToEdit by remember { mutableStateOf<Double?>(null) }
  var showCategoryBudgetDialog by remember { mutableStateOf(false) }
  var categoryBudgetToEdit by remember { mutableStateOf<BudgetEntity?>(null) }
  var showBudgetAllocationPlanner by remember { mutableStateOf(false) }
  var showAddShortcutDialog by remember { mutableStateOf(false) }
  var shortcutToEdit by remember { mutableStateOf<TransactionShortcutEntity?>(null) }
  var showAddGoalDialog by remember { mutableStateOf(false) }
  var goalToEdit by remember { mutableStateOf<SavingsGoalEntity?>(null) }
  var goalToContribute by remember { mutableStateOf<SavingsGoalEntity?>(null) }
  var showNotificationSheet by remember { mutableStateOf(false) }

  var showThemeDialog by remember { mutableStateOf(false) }
  var showNavBarDialog by remember { mutableStateOf(false) }
  var showCurrencyDialog by remember { mutableStateOf(false) }
  var showGeminiInsightsSheet by remember { mutableStateOf(false) }
  var geminiSheetInitialTab by remember { mutableStateOf(0) }
  var showMandatesScreen by remember { mutableStateOf(false) }

  if (showMandatesScreen) {
    ConditionalMandatesScreen(
      viewModel = viewModel,
      onBackClick = { showMandatesScreen = false }
    )
    return
  }

  if (isEditHomeScreenOpen) {
    EditHomeScreen(
      sections = homeSections,
      userName = userName,
      greeting = greeting,
      onToggleSection = { viewModel.toggleHomeSection(it) },
      onMoveUp = { viewModel.moveHomeSectionUp(it) },
      onMoveDown = { viewModel.moveHomeSectionDown(it) },
      onResetToDefault = { viewModel.resetHomeSections() },
      onSaveUserProfile = { name, greet -> viewModel.setUserProfile(name, greet) },
      onBack = { viewModel.closeEditHomeScreen() },
      modifier = modifier
    )
  } else {
    Scaffold(
      modifier = modifier.fillMaxSize(),
      snackbarHost = { SnackbarHost(snackbarHostState) },
      topBar = {
        TopAppBar(
          title = {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(end = 4.dp)
            ) {
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Text(
                    text = "B",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                  )
                }
              }
              Spacer(modifier = Modifier.width(8.dp))
              Column(modifier = Modifier.weight(1f, fill = false)) {
                Text(
                  text = "BudgetWise",
                  style = MaterialTheme.typography.titleMedium,
                  fontSize = 17.sp,
                  fontWeight = FontWeight.Bold,
                  maxLines = 1,
                  softWrap = false,
                  overflow = TextOverflow.Ellipsis
                )
                val monthLabel = remember {
                  SimpleDateFormat("MMM yyyy", Locale.US).format(Calendar.getInstance().time)
                }
                Text(
                  text = if (selectedAccountFilter != null) "$selectedAccountFilter • $monthLabel" else "All Accounts • $monthLabel",
                  style = MaterialTheme.typography.bodySmall,
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 1,
                  softWrap = false,
                  overflow = TextOverflow.Ellipsis
                )
              }
            }
          },
          actions = {
            // Quick Theme Switcher
            val themeIcon = when (themeMode) {
              ThemeMode.SYSTEM -> Icons.Default.SettingsBrightness
              ThemeMode.LIGHT -> Icons.Default.LightMode
              ThemeMode.DARK -> Icons.Default.DarkMode
            }
            IconButton(
              onClick = { showThemeDialog = true },
              modifier = Modifier.size(36.dp).testTag("btn_top_bar_theme")
            ) {
              Icon(
                imageVector = themeIcon,
                contentDescription = "Theme Options",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp)
              )
            }

            // Quick Customize Home Button
            IconButton(
              onClick = { viewModel.openEditHomeScreen() },
              modifier = Modifier.size(36.dp).testTag("btn_top_bar_edit_home")
            ) {
              Icon(
                imageVector = Icons.Default.DashboardCustomize,
                contentDescription = "Edit Home Screen",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp)
              )
            }

            // Conditional Transfer Mandates
            IconButton(
              onClick = { showMandatesScreen = true },
              modifier = Modifier.size(36.dp).testTag("btn_top_bar_mandates")
            ) {
              Icon(
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = "Transfer Mandates",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp)
              )
            }

            // Gemini AI Assistant & Chatbot
            IconButton(
              onClick = {
                geminiSheetInitialTab = 0
                showGeminiInsightsSheet = true
              },
              modifier = Modifier.size(36.dp).testTag("gemini_insights_top_button")
            ) {
              Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "Gemini AI Assistant & Chatbot",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
              )
            }

            // Notifications Bell
            BadgedBox(
              badge = {
                if (unreadNotificationsCount > 0) {
                  Badge(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                  ) {
                    Text(text = "$unreadNotificationsCount")
                  }
                }
              },
              modifier = Modifier.padding(end = 4.dp)
            ) {
              IconButton(
                onClick = { showNotificationSheet = true },
                modifier = Modifier.size(36.dp).testTag("notifications_bell_button")
              ) {
                Icon(
                  imageVector = Icons.Default.Notifications,
                  contentDescription = "Goal & Budget Notifications",
                  tint = MaterialTheme.colorScheme.onSurface,
                  modifier = Modifier.size(20.dp)
                )
              }
            }
          },
          colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            actionIconContentColor = MaterialTheme.colorScheme.onBackground
          )
        )
      },
      bottomBar = {
        Surface(
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
          color = MaterialTheme.colorScheme.surface,
          shadowElevation = 8.dp
        ) {
          NavigationBar(
            modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
          ) {
            navTabs.forEach { tab ->
              NavigationBarItem(
                selected = activeTab == tab,
                onClick = { viewModel.setActiveTab(tab) },
                icon = { Icon(getNavTabIcon(tab), contentDescription = tab.label) },
                label = {
                  val tabLabel = when (tab) {
                    NavTabDestination.TRANSACTIONS -> "Activity"
                    NavTabDestination.STOCKS -> "Invest"
                    NavTabDestination.NET_WORTH -> "Net Worth"
                    else -> tab.label
                  }
                  Text(
                    text = tabLabel,
                    maxLines = 1,
                    softWrap = false,
                    fontSize = 11.sp
                  )
                },
                modifier = Modifier.testTag("nav_tab_${tab.id}")
              )
            }
          }
        }
      },
      floatingActionButton = {
        val (fabText, fabAction) = when (activeTab) {
          NavTabDestination.BUDGETS -> "Add Category Budget" to {
            categoryBudgetToEdit = null
            showCategoryBudgetDialog = true
          }
          NavTabDestination.LOANS -> "Add Loan / Lent" to { showAddLoanDialog = true }
          NavTabDestination.RECURRING -> "Add Subscription" to { showAddSubscriptionDialog = true }
          NavTabDestination.GOALS -> "Add Goal" to { showAddGoalDialog = true }
          NavTabDestination.NET_WORTH -> "Add Bank Account" to { showAddAccountDialog = true }
          NavTabDestination.STOCKS -> null to {}
          NavTabDestination.MORE -> null to {}
          else -> "New Transaction" to { showAddTransactionDialog = true }
        }

        if (fabText != null) {
          ExtendedFloatingActionButton(
            onClick = fabAction,
            icon = { Icon(Icons.Default.Add, contentDescription = fabText) },
            text = { Text(fabText) },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.testTag("fab_add_expense")
          )
        }
      }
    ) { paddingValues ->
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues)
      ) {
        when (activeTab) {
          NavTabDestination.HOME -> {
            HomeDashboardScreen(
              sections = homeSections,
              userName = userName,
              greeting = greeting,
              currencySymbol = currencySymbol,
              netWorth = netWorth,
              accounts = allAccounts,
              expenses = allExpenses,
              budgets = allBudgets,
              goals = allGoals,
              subscriptions = allSubscriptions,
              loans = allLoans,
              totalLent = totalLent,
              totalBorrowed = totalBorrowed,
              totalIncomeThisMonth = totalIncomeThisMonth,
              totalSpentThisMonth = totalSpentThisMonth,
              categorySummaries = categorySummaries,
              overallBudget = overallBudget,
              dailyExpenseLogs = todayExpenseLogs,
              todayDailyTotal = todayExpenseTotal,
              onNavigateToTab = { viewModel.setActiveTab(it) },
              onOpenEditHomeScreen = { viewModel.openEditHomeScreen() },
              onAddAccount = { showAddAccountDialog = true },
              onAccountClick = { accountToEdit = it },
              onExpenseClick = { expenseToEdit = it },
              onOpenGeminiAssistant = {
                geminiSheetInitialTab = 0
                showGeminiInsightsSheet = true
              },
              onOpenAddExpenseLog = { showAddDailyExpenseDialog = true },
              onDeleteExpenseLog = { viewModel.deleteExpenseLog(it) }
            )
          }
          NavTabDestination.TRANSACTIONS -> {
            // Transactions (BudgetWise Timeline with Net Worth & Account Filter)
            TransactionsScreen(
              netWorth = netWorth,
              accounts = allAccounts,
              selectedAccount = selectedAccountFilter,
              onSelectAccount = { viewModel.selectedAccountFilter.value = it },
              onAddAccount = { showAddAccountDialog = true },
              income = totalIncomeThisMonth,
              expense = totalSpentThisMonth,
              net = netSavingsThisMonth,
              totalLent = totalLent,
              totalBorrowed = totalBorrowed,
              currencySymbol = currencySymbol,
              onNavigateToLoans = { viewModel.setActiveTab(NavTabDestination.LOANS) },
              dayGroups = dayGroupedTransactions,
              selectedType = selectedTransactionType,
              onSelectType = { viewModel.selectedTransactionType.value = it },
              searchQuery = searchQuery,
              onSearchQueryChange = { viewModel.searchQuery.value = it },
              selectedCategory = selectedFilterCategory,
              onSelectCategory = { viewModel.selectedFilterCategory.value = it },
              onDeleteExpense = { viewModel.deleteExpense(it) },
              onExpenseClick = { expenseToEdit = it },
              onAddTransactionClick = { showAddTransactionDialog = true },
              shortcuts = allShortcuts,
              onExecuteShortcut = { shortcut ->
                viewModel.executeShortcut(shortcut) { title, amt ->
                  scope.launch {
                    snackbarHostState.showSnackbar("Logged: $title ($currencySymbol${formatAmount(amt)})")
                  }
                }
              },
              onAddShortcut = {
                shortcutToEdit = null
                showAddShortcutDialog = true
              },
              onEditShortcut = { shortcut ->
                shortcutToEdit = shortcut
              }
            )
          }
          NavTabDestination.BUDGETS -> {
            // Budgets (BudgetWise Budgets with daily allowance & sum synchronization)
            val categoryBudgetsList = allBudgets.filter { it.category != "TOTAL" }
            BudgetsScreen(
              overallBudget = overallBudget,
              totalSpentThisMonth = totalSpentThisMonth,
              categoryBudgets = categoryBudgetsList,
              categorySummaries = categorySummaries,
              currencySymbol = currencySymbol,
              onSetOverallBudget = {
                budgetCategoryToEdit = "TOTAL"
                budgetLimitToEdit = overallBudget
                showSetBudgetDialog = true
              },
              onAddCategoryBudget = {
                categoryBudgetToEdit = null
                showCategoryBudgetDialog = true
              },
              onEditCategoryBudget = { b ->
                categoryBudgetToEdit = b
                showCategoryBudgetDialog = true
              },
              onDeleteBudget = { viewModel.deleteBudget(it) },
              onOpenAllocator = { showBudgetAllocationPlanner = true },
              onSyncOverallToSum = { viewModel.syncOverallBudgetToCategorySum() },
              onScaleToOverall = { target -> viewModel.scaleCategoryBudgetsToOverall(target) }
            )
          }
          NavTabDestination.LOANS -> {
            // Loans & Lent (BudgetWise Debts)
            LoansScreen(viewModel = viewModel)
          }
          NavTabDestination.RECURRING -> {
            // Recurring (BudgetWise Subscriptions & Bills)
            RecurringScreen(
              subscriptions = allSubscriptions,
              monthlyTotal = totalMonthlySubscriptionsCost,
              currencySymbol = currencySymbol,
              onAddSubscription = { showAddSubscriptionDialog = true },
              onSubscriptionClick = { subscriptionToEdit = it },
              onLogPayment = { viewModel.logSubscriptionPayment(it) },
              onDeleteSubscription = { viewModel.deleteSubscription(it) }
            )
          }
          NavTabDestination.GOALS -> {
            // Goals (BudgetWise Savings Goals)
            GoalsScreen(
              goals = allGoals,
              currencySymbol = currencySymbol,
              onAddGoal = { showAddGoalDialog = true },
              onGoalClick = { goalToEdit = it },
              onContribute = { goalToContribute = it },
              onDeleteGoal = { viewModel.deleteGoal(it) }
            )
          }
          NavTabDestination.ANALYTICS -> {
            // Analytics (Donut chart, Cash flow bar, 7-day spending)
            AnalyticsScreen(
              categorySummaries = categorySummaries,
              totalSpent = totalSpentThisMonth,
              income = totalIncomeThisMonth,
              expense = totalSpentThisMonth,
              net = netSavingsThisMonth,
              last7DaysSpend = last7DaysSpend,
              currencySymbol = currencySymbol,
              onCategoryClick = { cat ->
                viewModel.selectedFilterCategory.value = cat
                viewModel.setActiveTab(NavTabDestination.TRANSACTIONS)
              },
              onOpenGeminiInsights = {
                geminiSheetInitialTab = 1
                viewModel.analyzeSpendingWithGemini()
                showGeminiInsightsSheet = true
              }
            )
          }
          NavTabDestination.STOCKS -> {
            StocksScreen(viewModel = viewModel)
          }
          NavTabDestination.NET_WORTH -> {
            NetWorthScreen(
              netWorth = netWorth,
              accounts = allAccounts,
              expenses = allExpenses,
              stocks = allStocks,
              loans = allLoans,
              totalLent = totalLent,
              totalBorrowed = totalBorrowed,
              income = totalIncomeThisMonth,
              expense = totalSpentThisMonth,
              currencySymbol = currencySymbol,
              onAddAccount = { showAddAccountDialog = true },
              onAddStock = { viewModel.setActiveTab(NavTabDestination.STOCKS) },
              onNavigateToLoans = { viewModel.setActiveTab(NavTabDestination.LOANS) },
              onAccountClick = { accountToEdit = it }
            )
          }
          NavTabDestination.MORE -> {
            MoreScreen(
              themeMode = themeMode,
              currencySymbol = currencySymbol,
              userName = userName,
              onOpenThemeDialog = { showThemeDialog = true },
              onOpenNavBarDialog = { showNavBarDialog = true },
              onOpenCurrencyDialog = { showCurrencyDialog = true },
              onOpenEditHomeScreen = { viewModel.openEditHomeScreen() },
              onNavigateToTab = { viewModel.setActiveTab(it) },
              onOpenNotifications = { showNotificationSheet = true },
              onOpenGeminiAssistant = {
                geminiSheetInitialTab = 0
                showGeminiInsightsSheet = true
              },
              viewModel = viewModel
            )
          }
        }
      }
    }
  }


  // Dialogs
  if (showAddLoanDialog) {
    AddLoanDialog(
      availableAccounts = allAccounts,
      initialType = "LENT",
      currencySymbol = currencySymbol,
      onDismiss = { showAddLoanDialog = false },
      onConfirm = { person, type, total, account, dueDate, interest, note, updateBalance ->
        viewModel.addLoan(
          personName = person,
          type = type,
          totalAmount = total,
          account = account,
          dueDate = dueDate,
          interestRate = interest,
          note = note,
          updateAccountBalance = updateBalance
        )
        showAddLoanDialog = false
      }
    )
  }

  if (showAddTransactionDialog) {
    AddTransactionDialog(
      availableAccounts = allAccounts,
      availableGoals = allGoals,
      currencySymbol = currencySymbol,
      onDismiss = { showAddTransactionDialog = false },
      onConfirm = { title, amount, category, note, type, account, toAccount, timestamp ->
        viewModel.addTransaction(
          title = title,
          amount = amount,
          category = category,
          note = note,
          type = type,
          account = account,
          toAccount = toAccount,
          timestamp = timestamp
        )
        showAddTransactionDialog = false
      }
    )
  }

  if (showAddDailyExpenseDialog) {
    AddDailyExpenseLogDialog(
      onDismiss = { showAddDailyExpenseDialog = false },
      onSave = { title, amount, category, paymentMode, isEssential ->
        viewModel.addExpenseLog(
          title = title,
          amount = amount,
          category = category,
          paymentMode = paymentMode,
          isEssential = isEssential
        ) {
          showAddDailyExpenseDialog = false
          scope.launch {
            snackbarHostState.showSnackbar("Logged: $title ($currencySymbol${formatAmount(amount)}) to Room DB")
          }
        }
      }
    )
  }

  expenseToEdit?.let { expense ->
    EditTransactionDialog(
      expense = expense,
      availableAccounts = allAccounts,
      currencySymbol = currencySymbol,
      onDismiss = { expenseToEdit = null },
      onConfirm = { updated ->
        viewModel.updateExpense(updated)
        expenseToEdit = null
      },
      onDelete = {
        viewModel.deleteExpense(expense)
        expenseToEdit = null
      }
    )
  }

  if (showAddSubscriptionDialog) {
    AddSubscriptionDialog(
      availableAccounts = allAccounts,
      currencySymbol = currencySymbol,
      onDismiss = { showAddSubscriptionDialog = false },
      onConfirm = { title, amount, cycle, category, account, nextDueDays, note ->
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, nextDueDays) }
        viewModel.addSubscription(
          title = title,
          amount = amount,
          billingCycle = cycle,
          category = category,
          account = account,
          nextDueDate = cal.timeInMillis,
          note = note
        )
        showAddSubscriptionDialog = false
      }
    )
  }

  subscriptionToEdit?.let { sub ->
    EditSubscriptionDialog(
      subscription = sub,
      availableAccounts = allAccounts,
      currencySymbol = currencySymbol,
      onDismiss = { subscriptionToEdit = null },
      onConfirm = { updated ->
        viewModel.updateSubscription(updated)
        subscriptionToEdit = null
      },
      onDelete = {
        viewModel.deleteSubscription(sub)
        subscriptionToEdit = null
      }
    )
  }

  if (showAddAccountDialog) {
    AddAccountDialog(
      currencySymbol = currencySymbol,
      onDismiss = { showAddAccountDialog = false },
      onConfirm = { name, type, balance, colorHex ->
        viewModel.addAccount(name, type, balance, colorHex)
        showAddAccountDialog = false
      }
    )
  }

  accountToEdit?.let { account ->
    EditAccountDialog(
      account = account,
      currencySymbol = currencySymbol,
      onDismiss = { accountToEdit = null },
      onConfirm = { updated ->
        viewModel.updateAccount(updated)
        accountToEdit = null
      },
      onDelete = { deleted ->
        viewModel.deleteAccount(deleted)
        accountToEdit = null
      }
    )
  }

  if (showSetBudgetDialog) {
    val categoryBudgetsList = allBudgets.filter { it.category != "TOTAL" }
    val categorySum = categoryBudgetsList.sumOf { it.monthlyLimit }
    SetBudgetDialog(
      initialCategory = budgetCategoryToEdit,
      initialLimit = budgetLimitToEdit,
      categorySum = categorySum,
      currencySymbol = currencySymbol,
      onDismiss = { showSetBudgetDialog = false },
      onConfirm = { cat, limit ->
        viewModel.setBudget(cat, limit)
        showSetBudgetDialog = false
      }
    )
  }

  if (showCategoryBudgetDialog || categoryBudgetToEdit != null) {
    val b = categoryBudgetToEdit
    val categoryBudgetsList = allBudgets.filter { it.category != "TOTAL" }
    val otherCategoriesSum = categoryBudgetsList
      .filter { it.category != (b?.category ?: "") }
      .sumOf { it.monthlyLimit }
    CategoryBudgetDialog(
      initialCategory = b?.category ?: "",
      initialLimit = b?.monthlyLimit,
      overallBudget = overallBudget,
      otherCategoriesSum = otherCategoriesSum,
      currencySymbol = currencySymbol,
      onDismiss = {
        showCategoryBudgetDialog = false
        categoryBudgetToEdit = null
      },
      onConfirm = { cat, limit, syncOverall ->
        viewModel.setBudget(cat, limit)
        if (syncOverall) {
          viewModel.syncOverallBudgetToCategorySum()
        }
        showCategoryBudgetDialog = false
        categoryBudgetToEdit = null
      },
      onDelete = if (b != null) {
        {
          viewModel.deleteBudget(b)
          showCategoryBudgetDialog = false
          categoryBudgetToEdit = null
        }
      } else null
    )
  }

  if (showBudgetAllocationPlanner) {
    val categoryBudgetsList = allBudgets.filter { it.category != "TOTAL" }
    BudgetAllocationPlannerDialog(
      overallBudget = overallBudget,
      categoryBudgets = categoryBudgetsList,
      currencySymbol = currencySymbol,
      onDismiss = { showBudgetAllocationPlanner = false },
      onSyncOverallToSum = { viewModel.syncOverallBudgetToCategorySum() },
      onScaleCategoriesToOverall = { viewModel.scaleCategoryBudgetsToOverall(it) },
      onEditCategoryBudget = { budget ->
        categoryBudgetToEdit = budget
        showBudgetAllocationPlanner = false
      },
      onAddCategoryBudget = {
        categoryBudgetToEdit = null
        showCategoryBudgetDialog = true
        showBudgetAllocationPlanner = false
      }
    )
  }

  if (showAddShortcutDialog || shortcutToEdit != null) {
    val sc = shortcutToEdit
    AddEditShortcutDialog(
      shortcut = sc,
      availableAccounts = allAccounts,
      currencySymbol = currencySymbol,
      onDismiss = {
        showAddShortcutDialog = false
        shortcutToEdit = null
      },
      onSave = { title, amt, cat, type, acc, emoji ->
        if (sc != null) {
          viewModel.updateShortcut(
            sc.copy(
              title = title,
              amount = amt,
              category = cat,
              type = type,
              account = acc,
              iconEmoji = emoji
            )
          )
        } else {
          viewModel.addShortcut(
            title = title,
            amount = amt,
            category = cat,
            type = type,
            account = acc,
            iconEmoji = emoji
          )
        }
        showAddShortcutDialog = false
        shortcutToEdit = null
      },
      onDelete = if (sc != null) {
        {
          viewModel.deleteShortcut(sc)
          showAddShortcutDialog = false
          shortcutToEdit = null
        }
      } else null
    )
  }

  if (showAddGoalDialog) {
    AddGoalDialog(
      currencySymbol = currencySymbol,
      onDismiss = { showAddGoalDialog = false },
      onConfirm = { title, target, saved, deadlineDays ->
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, deadlineDays) }
        viewModel.addGoal(
          title = title,
          targetAmount = target,
          currentAmount = saved,
          deadlineTimestamp = cal.timeInMillis
        )
        showAddGoalDialog = false
      }
    )
  }

  goalToEdit?.let { goal ->
    EditSavingsGoalDialog(
      goal = goal,
      currencySymbol = currencySymbol,
      onDismiss = { goalToEdit = null },
      onConfirm = { updated ->
        viewModel.updateGoal(updated)
        goalToEdit = null
      },
      onDelete = {
        viewModel.deleteGoal(goal)
        goalToEdit = null
      }
    )
  }

  if (goalToContribute != null) {
    val goal = goalToContribute ?: return
    ContributeGoalDialog(
      goal = goal,
      currencySymbol = currencySymbol,
      onDismiss = { goalToContribute = null },
      onConfirm = { depositAmount ->
        viewModel.contributeToGoal(goal, depositAmount)
        goalToContribute = null
      }
    )
  }

  if (showNotificationSheet) {
    NotificationHistorySheet(
      notifications = allNotifications,
      onDismiss = { showNotificationSheet = false },
      onSendTestNotification = { viewModel.sendTestNotification() },
      onMarkAllRead = { viewModel.markAllNotificationsAsRead() },
      onClearAll = { viewModel.clearAllNotifications() }
    )
  }

  if (showThemeDialog) {
    ThemeSelectionDialog(
      currentMode = themeMode,
      onSelectMode = { viewModel.setThemeMode(it) },
      onDismiss = { showThemeDialog = false }
    )
  }

  if (showNavBarDialog) {
    NavBarCustomizationDialog(
      currentTabs = navTabs,
      onSaveTabs = { viewModel.setNavTabs(it) },
      onResetToDefault = { viewModel.resetNavTabs() },
      onDismiss = { showNavBarDialog = false }
    )
  }

  if (showCurrencyDialog) {
    CurrencySelectionDialog(
      currentSymbol = currencySymbol,
      onSelectCurrency = { viewModel.setCurrencySymbol(it) },
      onDismiss = { showCurrencyDialog = false }
    )
  }

  if (showGeminiInsightsSheet) {
    GeminiSpendingInsightsSheet(
      viewModel = viewModel,
      initialTab = geminiSheetInitialTab,
      onDismiss = { showGeminiInsightsSheet = false }
    )
  }
}


// -------------------------------------------------------------
// TAB 0: TRANSACTIONS SCREEN (CASHEW TIMELINE)
// -------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TransactionsScreen(
  netWorth: Double,
  accounts: List<AccountEntity>,
  selectedAccount: String?,
  onSelectAccount: (String?) -> Unit,
  onAddAccount: () -> Unit,
  income: Double,
  expense: Double,
  net: Double,
  totalLent: Double = 0.0,
  totalBorrowed: Double = 0.0,
  currencySymbol: String = "₹",
  onNavigateToLoans: () -> Unit = {},
  dayGroups: List<com.example.ui.viewmodel.DayGroupedTransactions>,
  selectedType: String?,
  onSelectType: (String?) -> Unit,
  searchQuery: String,
  onSearchQueryChange: (String) -> Unit,
  selectedCategory: ExpenseCategory?,
  onSelectCategory: (ExpenseCategory?) -> Unit,
  onDeleteExpense: (ExpenseEntity) -> Unit,
  onExpenseClick: (ExpenseEntity) -> Unit = {},
  onAddTransactionClick: () -> Unit,
  shortcuts: List<TransactionShortcutEntity> = emptyList(),
  onExecuteShortcut: (TransactionShortcutEntity) -> Unit = {},
  onAddShortcut: () -> Unit = {},
  onEditShortcut: (TransactionShortcutEntity) -> Unit = {}
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Net Worth & Flow Hero Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
        ) {
          Text(
            text = "Total Net Worth",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "$currencySymbol${formatAmount(netWorth)}",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1B664B)
          )

          Spacer(modifier = Modifier.height(16.dp))

          // Mini Flow Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = Color(0xFF00B894).copy(alpha = 0.12f),
              modifier = Modifier.weight(1f)
            ) {
              Column(modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp)) {
                Text(
                  text = "Income",
                  style = MaterialTheme.typography.labelSmall,
                  color = Color(0xFF00896F)
                )
                Text(
                  text = "+$currencySymbol${formatAmount(income)}",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF00B894)
                )
              }
            }

            Surface(
              shape = RoundedCornerShape(12.dp),
              color = Color(0xFFFF6B6B).copy(alpha = 0.12f),
              modifier = Modifier.weight(1f)
            ) {
              Column(modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp)) {
                Text(
                  text = "Spent",
                  style = MaterialTheme.typography.labelSmall,
                  color = Color(0xFFD63031)
                )
                Text(
                  text = "-$currencySymbol${formatAmount(expense)}",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFFFF6B6B)
                )
              }
            }

            Surface(
              shape = RoundedCornerShape(12.dp),
              color = Color(0xFF0984E3).copy(alpha = 0.12f),
              modifier = Modifier.weight(1f)
            ) {
              Column(modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp)) {
                Text(
                  text = "Net Saved",
                  style = MaterialTheme.typography.labelSmall,
                  color = Color(0xFF0984E3)
                )
                Text(
                  text = (if (net >= 0) "+$currencySymbol" else "-$currencySymbol") + formatAmount(kotlin.math.abs(net)),
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = if (net >= 0) Color(0xFF0984E3) else Color(0xFFFF6B6B)
                )
              }
            }
          }

          if (totalLent > 0 || totalBorrowed > 0) {
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onNavigateToLoans)
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.Handshake,
                    contentDescription = null,
                    tint = Color(0xFF1B664B),
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "Debts: $currencySymbol${formatAmount(totalLent)} lent • $currencySymbol${formatAmount(totalBorrowed)} owed",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
                Text(
                  text = "View Loans →",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF1B664B)
                )
              }
            }
          }
        }
      }
    }

    // 2. Account Filter Carousel
    item {
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Accounts & Wallets",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          TextButton(onClick = onAddAccount) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Add", style = MaterialTheme.typography.labelMedium)
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
          item {
            val isAll = selectedAccount == null
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = if (isAll) Color(0xFF1B664B) else MaterialTheme.colorScheme.surface,
              contentColor = if (isAll) Color.White else MaterialTheme.colorScheme.onSurface,
              shadowElevation = 1.dp,
              modifier = Modifier.clickable { onSelectAccount(null) }
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.Payments,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                  Text(
                    text = "All Accounts",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = "$currencySymbol${formatAmount(netWorth)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isAll) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }
          }

          items(accounts, key = { it.id }) { acc ->
            val isSelected = selectedAccount == acc.name
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = if (isSelected) Color(0xFF1B664B) else MaterialTheme.colorScheme.surface,
              contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
              shadowElevation = 1.dp,
              modifier = Modifier.clickable {
                if (isSelected) onSelectAccount(null) else onSelectAccount(acc.name)
              }
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = when (acc.type) {
                    "CASH" -> Icons.Default.Payments
                    "SAVINGS" -> Icons.Default.Savings
                    "CREDIT" -> Icons.Default.CreditCard
                    else -> Icons.Default.AccountBalance
                  },
                  contentDescription = null,
                  tint = if (isSelected) Color.White else Color(acc.colorHex),
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                  Text(
                    text = acc.name,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = (if (acc.balance < 0) "-$currencySymbol" else "$currencySymbol") + formatAmount(kotlin.math.abs(acc.balance)),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }
          }
        }
      }
    }

    // 2.5 Quick Transaction Shortcuts (Up to 5 shortcuts)
    item {
      Column(modifier = Modifier.fillMaxWidth()) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.FlashOn,
              contentDescription = null,
              tint = Color(0xFFE09A26),
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Quick Shortcuts",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "(${shortcuts.size}/5)",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          if (shortcuts.size < 5) {
            TextButton(
              onClick = onAddShortcut,
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
              modifier = Modifier.testTag("btn_add_transaction_shortcut")
            ) {
              Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(2.dp))
              Text("Add Shortcut", style = MaterialTheme.typography.labelMedium)
            }
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
          items(shortcuts, key = { it.id }) { sc ->
            Surface(
              shape = RoundedCornerShape(16.dp),
              color = MaterialTheme.colorScheme.surface,
              shadowElevation = 2.dp,
              border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (sc.type == "INCOME") Color(0xFF1B664B).copy(alpha = 0.35f) else Color(0xFFE09A26).copy(alpha = 0.35f)
              ),
              modifier = Modifier
                .clickable { onExecuteShortcut(sc) }
                .testTag("shortcut_item_${sc.id}")
            ) {
              Column(
                modifier = Modifier
                  .padding(10.dp)
                  .widthIn(min = 96.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(text = sc.iconEmoji, style = MaterialTheme.typography.titleMedium)
                  IconButton(
                    onClick = { onEditShortcut(sc) },
                    modifier = Modifier
                      .size(22.dp)
                      .testTag("btn_edit_shortcut_${sc.id}")
                  ) {
                    Icon(
                      imageVector = Icons.Default.Edit,
                      contentDescription = "Edit Shortcut",
                      modifier = Modifier.size(13.dp),
                      tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = sc.title,
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Text(
                  text = (if (sc.type == "INCOME") "+$currencySymbol" else "$currencySymbol") + formatAmount(sc.amount),
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.SemiBold,
                  color = if (sc.type == "INCOME") Color(0xFF1B664B) else MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = "Tap to log",
                  style = MaterialTheme.typography.labelSmall,
                  fontSize = 10.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }

          if (shortcuts.size < 5) {
            item {
              Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                  .clickable { onAddShortcut() }
                  .testTag("btn_new_shortcut_slot")
              ) {
                Column(
                  modifier = Modifier
                    .padding(12.dp)
                    .widthIn(min = 90.dp),
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Shortcut",
                    tint = Color(0xFF1B664B),
                    modifier = Modifier.size(24.dp)
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = "New\nShortcut",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                  )
                }
              }
            }
          }
        }
      }
    }

    // 3. Search & Type filter
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        OutlinedTextField(
          value = searchQuery,
          onValueChange = onSearchQueryChange,
          placeholder = { Text("Search transactions...") },
          leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "Search")
          },
          trailingIcon = {
            if (searchQuery.isNotEmpty()) {
              IconButton(onClick = { onSearchQueryChange("") }) {
                Icon(Icons.Default.Clear, contentDescription = "Clear")
              }
            }
          },
          singleLine = true,
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier
            .weight(1f)
            .testTag("search_expenses_input")
        )

        // Type filter pills
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ) {
          Row(modifier = Modifier.padding(4.dp)) {
            listOf(
              null to "All",
              "EXPENSE" to "Expenses",
              "INCOME" to "Income",
              "SCHEDULED" to "Scheduled"
            ).forEach { (typeKey, label) ->
              val isSelected = selectedType == typeKey
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) Color(0xFF1B664B) else Color.Transparent,
                contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.clickable { onSelectType(typeKey) }
              ) {
                Text(
                  text = label,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                )
              }
            }
          }
        }
      }
    }

    // 4. Day-by-Day Grouped Feed
    if (dayGroups.isEmpty()) {
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
              contentDescription = null,
              tint = Color(0xFF1B664B).copy(alpha = 0.4f),
              modifier = Modifier.size(54.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "No Transactions Found",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Tap '+ New Transaction' to log an expense, income, or transfer!",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
              onClick = onAddTransactionClick,
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B664B))
            ) {
              Text("+ New Transaction")
            }
          }
        }
      }
    } else {
      dayGroups.forEach { dayGroup ->
        item {
          // Day Header
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = dayGroup.dateLabel,
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = (if (dayGroup.dayNet >= 0) "+$currencySymbol" else "-$currencySymbol") + formatAmount(kotlin.math.abs(dayGroup.dayNet)),
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.SemiBold,
              color = if (dayGroup.dayNet >= 0) Color(0xFF00B894) else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        items(dayGroup.transactions, key = { it.id }) { expense ->
          val cat = ExpenseCategory.fromName(expense.category)
          val isIncome = expense.type == "INCOME"
          val isTransfer = expense.type == "TRANSFER"
          val isScheduled = expense.isScheduled
          val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.US) }
          val txTimeStr = remember(expense.timestamp) { timeFormat.format(Date(expense.timestamp)) }
          val dateFormat = remember { SimpleDateFormat("dd MMM", Locale.US) }
          val txDateStr = remember(expense.timestamp) { dateFormat.format(Date(expense.timestamp)) }

          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onExpenseClick(expense) },
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              // Category Icon Circle
              Box(
                modifier = Modifier
                  .size(44.dp)
                  .clip(CircleShape)
                  .background(
                    when {
                      isIncome -> Color(0xFF00B894).copy(alpha = 0.15f)
                      isTransfer -> Color(0xFF0984E3).copy(alpha = 0.15f)
                      else -> cat.color.copy(alpha = 0.15f)
                    }
                  ),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = when {
                    isIncome -> Icons.Default.Payments
                    isTransfer -> Icons.Default.SwapHoriz
                    else -> cat.icon
                  },
                  contentDescription = cat.displayName,
                  tint = when {
                    isIncome -> Color(0xFF00B894)
                    isTransfer -> Color(0xFF0984E3)
                    else -> cat.color
                  },
                  modifier = Modifier.size(22.dp)
                )
              }

              Spacer(modifier = Modifier.width(12.dp))

              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = expense.title,
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                  if (isScheduled) {
                    Surface(
                      shape = RoundedCornerShape(6.dp),
                      color = Color(0xFF0984E3).copy(alpha = 0.15f),
                      border = BorderStroke(0.5.dp, Color(0xFF0984E3).copy(alpha = 0.4f))
                    ) {
                      Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        Icon(
                          imageVector = Icons.Default.Schedule,
                          contentDescription = "Scheduled",
                          tint = Color(0xFF0984E3),
                          modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                          text = "Scheduled • $txDateStr, $txTimeStr",
                          style = MaterialTheme.typography.labelSmall,
                          fontWeight = FontWeight.Bold,
                          color = Color(0xFF0984E3),
                          fontSize = 10.sp
                        )
                      }
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                  } else {
                    Surface(
                      shape = RoundedCornerShape(6.dp),
                      color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                    ) {
                      Text(
                        text = "📅 $txDateStr • 🕒 $txTimeStr",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                      )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                  }

                  // Account Pill
                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                  ) {
                    Text(
                      text = expense.account,
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                  }
                  if (expense.isInvestmentTransaction || expense.stockSymbol != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Surface(
                      shape = RoundedCornerShape(6.dp),
                      color = Color(0xFF1B664B).copy(alpha = 0.12f)
                    ) {
                      Text(
                        text = if (expense.stockSymbol != null) "📈 ${expense.stockSymbol}" else "📈 Investment",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF1B664B),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                      )
                    }
                  }
                  if (expense.note.isNotBlank()) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = "• ${expense.note}",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.outline,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.width(8.dp))

              // Amount & Status
              Column(horizontalAlignment = Alignment.End) {
                Text(
                  text = when {
                    isIncome -> "+$currencySymbol${formatAmount(expense.amount)}"
                    isTransfer -> "$currencySymbol${formatAmount(expense.amount)}"
                    else -> "-$currencySymbol${formatAmount(expense.amount)}"
                  },
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.ExtraBold,
                  color = when {
                    isIncome -> Color(0xFF00B894)
                    isTransfer -> Color(0xFF0984E3)
                    else -> MaterialTheme.colorScheme.onSurface
                  }
                )
                if (isScheduled) {
                  Text(
                    text = "Scheduled",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0984E3)
                  )
                }
              }

              IconButton(
                onClick = { onDeleteExpense(expense) },
                modifier = Modifier.size(32.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Delete,
                  contentDescription = "Delete",
                  tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
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

// -------------------------------------------------------------
// TAB 1: BUDGETS SCREEN (CASHEW BUDGETS & DAILY ALLOWANCE)
// -------------------------------------------------------------
@Composable
private fun BudgetsScreen(
  overallBudget: Double?,
  totalSpentThisMonth: Double,
  categoryBudgets: List<BudgetEntity>,
  categorySummaries: List<com.example.ui.viewmodel.CategorySpendSummary>,
  currencySymbol: String = "₹",
  onSetOverallBudget: () -> Unit,
  onAddCategoryBudget: () -> Unit,
  onEditCategoryBudget: (BudgetEntity) -> Unit = {},
  onDeleteBudget: (BudgetEntity) -> Unit,
  onOpenAllocator: () -> Unit = {},
  onSyncOverallToSum: () -> Unit = {},
  onScaleToOverall: (Double) -> Unit = {}
) {
  val calendar = Calendar.getInstance()
  val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
  val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
  val remainingDays = (daysInMonth - currentDay + 1).coerceAtLeast(1)

  val categorySum = categoryBudgets.sumOf { it.monthlyLimit }
  val targetOverall = overallBudget ?: 0.0
  val diff = categorySum - targetOverall
  val isBalanced = overallBudget != null && overallBudget > 0 && kotlin.math.abs(diff) < 0.01

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Overall Monthly Budget Card with BudgetWise Daily Allowance
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onSetOverallBudget() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
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
                text = "Overall Monthly Budget",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Total limit for this month (tap to adjust)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            if (overallBudget == null) {
              TextButton(onClick = onSetOverallBudget) {
                Text("Set Budget")
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          if (overallBudget != null && overallBudget > 0) {
            val progress = (totalSpentThisMonth / overallBudget).toFloat()
            val remaining = overallBudget - totalSpentThisMonth
            val dailyAllowance = if (remaining > 0) remaining / remainingDays else 0.0

            val statusColor = when {
              progress >= 1.0f -> MaterialTheme.colorScheme.error
              progress >= 0.8f -> Color(0xFFE67E22)
              else -> Color(0xFF1B664B)
            }

            // Progress Bar
            LinearProgressIndicator(
              progress = { progress.coerceIn(0f, 1f) },
              modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp)),
              color = statusColor,
              trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text("Spent", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                  text = "$currencySymbol${formatAmount(totalSpentThisMonth)}",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold
                )
              }

              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Remaining", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                  text = (if (remaining >= 0) "$currencySymbol" else "-$currencySymbol") + formatAmount(kotlin.math.abs(remaining)),
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  color = if (remaining >= 0) Color(0xFF1B664B) else MaterialTheme.colorScheme.error
                )
              }

              Column(horizontalAlignment = Alignment.End) {
                Text("Limit", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                  text = "$currencySymbol${formatAmount(overallBudget)}",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold
                )
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // BudgetWise Signature: Daily Allowance Pill
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = Color(0xFF1B664B).copy(alpha = 0.12f),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = Color(0xFF1B664B),
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "Daily Allowance",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B664B)
                  )
                }
                Text(
                  text = "$currencySymbol${formatAmount(dailyAllowance)} / day ($remainingDays days left)",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF1B664B)
                )
              }
            }
          } else {
            Text(
              text = "No overall monthly budget set. Set a budget to track daily allowances and receive alerts when nearing limits!",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }

    // 1.5 Budget Balance Status & Quick Sync Card
    item {
      val hasOverall = overallBudget != null && overallBudget > 0
      val hasCategories = categoryBudgets.isNotEmpty()

      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
          containerColor = if (isBalanced) Color(0xFF1B664B).copy(alpha = 0.08f)
          else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        border = androidx.compose.foundation.BorderStroke(
          1.dp,
          if (isBalanced) Color(0xFF1B664B).copy(alpha = 0.35f)
          else if (hasOverall && kotlin.math.abs(diff) > 0.01) MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
          else MaterialTheme.colorScheme.outlineVariant
        )
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
              Icon(
                imageVector = if (isBalanced) Icons.Default.CheckCircle else Icons.Default.Balance,
                contentDescription = null,
                tint = if (isBalanced) Color(0xFF1B664B) else if (hasOverall && kotlin.math.abs(diff) > 0.01) Color(0xFFE67E22) else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = if (isBalanced) "Budget Balanced" else "Budget Allocation",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isBalanced) Color(0xFF1B664B) else MaterialTheme.colorScheme.onSurface
              )
            }

            OutlinedButton(
              onClick = onOpenAllocator,
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
              modifier = Modifier.testTag("btn_open_budget_allocator")
            ) {
              Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Planner", style = MaterialTheme.typography.labelSmall)
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text("Categories Sum", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Text(
                text = "$currencySymbol${formatAmount(categorySum)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text("Status", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Text(
                text = when {
                  isBalanced -> "Balanced (=)"
                  diff > 0 -> "Over by $currencySymbol${formatAmount(diff)}"
                  diff < 0 -> "Under by $currencySymbol${formatAmount(-diff)}"
                  else -> "Not Set"
                },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = when {
                  isBalanced -> Color(0xFF1B664B)
                  diff > 0 -> MaterialTheme.colorScheme.error
                  else -> Color(0xFFE67E22)
                }
              )
            }

            Column(horizontalAlignment = Alignment.End) {
              Text("Overall Limit", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Text(
                text = "$currencySymbol${formatAmount(targetOverall)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
            }
          }

          if (!isBalanced && hasCategories) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Button(
                onClick = onSyncOverallToSum,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B664B)),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier
                  .weight(1f)
                  .testTag("btn_sync_overall_to_sum")
              ) {
                Text("Sync Overall to Sum", style = MaterialTheme.typography.labelSmall, maxLines = 1)
              }

              if (hasOverall) {
                OutlinedButton(
                  onClick = { onScaleToOverall(targetOverall) },
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                  modifier = Modifier
                    .weight(1f)
                    .testTag("btn_scale_categories_to_overall")
                ) {
                  Text("Scale to Overall", style = MaterialTheme.typography.labelSmall, maxLines = 1)
                }
              }
            }
          }
        }
      }
    }

    // 2. Category Budgets Header
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Category Budgets",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "${categoryBudgets.size} active • Total $currencySymbol${formatAmount(categorySum)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        Row {
          TextButton(onClick = onAddCategoryBudget, modifier = Modifier.testTag("btn_add_category_budget")) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Add")
          }
        }
      }
    }

    // 3. Category Budgets List
    if (categoryBudgets.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = "No category budgets set yet.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
              onClick = onAddCategoryBudget,
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B664B))
            ) {
              Text("Set First Category Budget")
            }
          }
        }
      }
    } else {
      items(categoryBudgets, key = { it.id }) { b ->
        val cat = ExpenseCategory.fromName(b.category)
        val spent = categorySummaries.firstOrNull { it.category == cat }?.spent ?: 0.0
        val pct = if (b.monthlyLimit > 0) (spent / b.monthlyLimit).toFloat() else 0f
        val remaining = b.monthlyLimit - spent

        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onEditCategoryBudget(b) },
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
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
                Box(
                  modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(cat.color.copy(alpha = 0.15f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = cat.icon,
                    contentDescription = null,
                    tint = cat.color,
                    modifier = Modifier.size(18.dp)
                  )
                }
                Spacer(modifier = Modifier.width(10.dp))
                val displayCatName = if (cat == ExpenseCategory.OTHER && b.category.isNotEmpty() && b.category != "OTHER") b.category else cat.displayName
                val sharePct = if (categorySum > 0) (b.monthlyLimit / categorySum * 100).toInt() else 0
                Column {
                  Text(
                    text = displayCatName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = "$currencySymbol${formatAmount(spent)} of $currencySymbol${formatAmount(b.monthlyLimit)} ($sharePct%)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }

              Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                  onClick = { onEditCategoryBudget(b) },
                  modifier = Modifier.size(32.dp).testTag("btn_edit_budget_${b.id}")
                ) {
                  Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                  )
                }
                IconButton(
                  onClick = { onDeleteBudget(b) },
                  modifier = Modifier.size(32.dp).testTag("btn_delete_budget_${b.id}")
                ) {
                  Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(16.dp)
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
              progress = { pct.coerceIn(0f, 1f) },
              modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
              color = if (pct >= 1f) MaterialTheme.colorScheme.error else cat.color,
              trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = "${(pct * 100).toInt()}% used",
                style = MaterialTheme.typography.labelSmall,
                color = if (pct >= 1f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = if (remaining >= 0) "$currencySymbol${formatAmount(remaining)} left" else "$currencySymbol${formatAmount(-remaining)} over limit",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (remaining >= 0) Color(0xFF1B664B) else MaterialTheme.colorScheme.error
              )
            }
          }
        }
      }
    }
  }
}

// -------------------------------------------------------------
// TAB 2: RECURRING SCREEN (CASHEW SUBSCRIPTIONS & BILLS)
// -------------------------------------------------------------
@Composable
private fun RecurringScreen(
  subscriptions: List<SubscriptionEntity>,
  monthlyTotal: Double,
  currencySymbol: String = "₹",
  onAddSubscription: () -> Unit,
  onSubscriptionClick: (SubscriptionEntity) -> Unit = {},
  onLogPayment: (SubscriptionEntity) -> Unit,
  onDeleteSubscription: (SubscriptionEntity) -> Unit
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Hero Banner
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                text = "Recurring & Subscriptions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "${subscriptions.size} active subscriptions",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Button(
              onClick = onAddSubscription,
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B664B))
            ) {
              Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Add")
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1B664B).copy(alpha = 0.12f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = "Estimated Monthly Cost",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = "$currencySymbol${formatAmount(monthlyTotal)} / month",
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.ExtraBold,
                  color = Color(0xFF1B664B)
                )
              }
              Icon(
                imageVector = Icons.Default.Repeat,
                contentDescription = null,
                tint = Color(0xFF1B664B),
                modifier = Modifier.size(32.dp)
              )
            }
          }
        }
      }
    }

    if (subscriptions.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(
              imageVector = Icons.Default.Repeat,
              contentDescription = null,
              tint = Color(0xFF1B664B).copy(alpha = 0.4f),
              modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "No Subscriptions Added",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Track Netflix, Spotify, gym memberships, rent, and recurring bills in one place!",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
              onClick = onAddSubscription,
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B664B))
            ) {
              Text("+ Add First Subscription")
            }
          }
        }
      }
    } else {
      items(subscriptions, key = { it.id }) { sub ->
        val cat = ExpenseCategory.fromName(sub.category)
        val now = System.currentTimeMillis()
        val diffDays = TimeUnit.MILLISECONDS.toDays(sub.nextDueDate - now)
        val dueDateFormatted = SimpleDateFormat("MMM d, yyyy", Locale.US).format(Date(sub.nextDueDate))

        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onSubscriptionClick(sub) },
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                Box(
                  modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(cat.color.copy(alpha = 0.15f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = cat.icon,
                    contentDescription = null,
                    tint = cat.color,
                    modifier = Modifier.size(20.dp)
                  )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                  Text(
                    text = sub.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = "${sub.billingCycle} • Billed to ${sub.account}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }

              Column(horizontalAlignment = Alignment.End) {
                Text(
                  text = "$currencySymbol${formatAmount(sub.amount)}",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF1B664B)
                )
                Text(
                  text = sub.billingCycle.lowercase(),
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.outline
                )
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Due Date & Quick Log Action
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (diffDays <= 3) Color(0xFFFF6B6B).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
              ) {
                Text(
                  text = if (diffDays <= 0) "Due today" else "Due in $diffDays days ($dueDateFormatted)",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.SemiBold,
                  color = if (diffDays <= 3) Color(0xFFD63031) else MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }

              Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(
                  onClick = { onLogPayment(sub) },
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                  Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Pay Now", style = MaterialTheme.typography.labelSmall)
                }
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(onClick = { onDeleteSubscription(sub) }) {
                  Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.outline,
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
}

// -------------------------------------------------------------
// TAB 3: GOALS SCREEN (CASHEW SAVINGS GOALS)
// -------------------------------------------------------------
@Composable
private fun GoalsScreen(
  goals: List<SavingsGoalEntity>,
  currencySymbol: String = "₹",
  onAddGoal: () -> Unit,
  onGoalClick: (SavingsGoalEntity) -> Unit = {},
  onContribute: (SavingsGoalEntity) -> Unit,
  onDeleteGoal: (SavingsGoalEntity) -> Unit
) {
  val totalTarget = goals.sumOf { it.targetAmount }
  val totalSaved = goals.sumOf { it.currentAmount }
  val overallPct = if (totalTarget > 0) ((totalSaved / totalTarget) * 100).toInt() else 0

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Hero Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                text = "Savings Goals",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "$currencySymbol${formatAmount(totalSaved)} of $currencySymbol${formatAmount(totalTarget)} saved ($overallPct%)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Button(
              onClick = onAddGoal,
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B664B)),
              modifier = Modifier.testTag("add_goal_button")
            ) {
              Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("New Goal")
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          LinearProgressIndicator(
            progress = { (overallPct / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
              .fillMaxWidth()
              .height(10.dp)
              .clip(RoundedCornerShape(5.dp)),
            color = Color(0xFF1B664B),
            trackColor = MaterialTheme.colorScheme.surfaceVariant
          )
        }
      }
    }

    if (goals.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(
              imageVector = Icons.Default.EmojiEvents,
              contentDescription = null,
              tint = Color(0xFF1B664B).copy(alpha = 0.4f),
              modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "No Savings Goals Set",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Set a goal for vacation, emergency fund, or buying a car to receive milestone alerts!",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
              onClick = onAddGoal,
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B664B))
            ) {
              Text("+ Create Savings Goal")
            }
          }
        }
      }
    } else {
      items(goals, key = { it.id }) { goal ->
        val now = System.currentTimeMillis()
        val daysLeft = TimeUnit.MILLISECONDS.toDays(goal.deadlineTimestamp - now).coerceAtLeast(0)

        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onGoalClick(goal) },
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                Box(
                  modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                      if (goal.isCompleted) Color(0xFF00B894).copy(alpha = 0.15f)
                      else Color(0xFF1B664B).copy(alpha = 0.15f)
                    ),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = if (goal.isCompleted) Icons.Default.CheckCircle else Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = if (goal.isCompleted) Color(0xFF00B894) else Color(0xFF1B664B),
                    modifier = Modifier.size(20.dp)
                  )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                  Text(
                    text = goal.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = "$daysLeft days left",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }

              Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (goal.isCompleted) Color(0xFF00B894).copy(alpha = 0.15f) else Color(0xFF1B664B).copy(alpha = 0.12f)
              ) {
                Text(
                  text = if (goal.isCompleted) "Completed 🎉" else "${goal.progressPercent}%",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = if (goal.isCompleted) Color(0xFF00B894) else Color(0xFF1B664B),
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LinearProgressIndicator(
              progress = { (goal.progressPercent / 100f).coerceIn(0f, 1f) },
              modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
              color = if (goal.isCompleted) Color(0xFF00B894) else Color(0xFF1B664B),
              trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "$currencySymbol${formatAmount(goal.currentAmount)} / $currencySymbol${formatAmount(goal.targetAmount)}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
              )

              Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                  onClick = { onContribute(goal) },
                  colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B664B)),
                  contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                  Text("+ Deposit", style = MaterialTheme.typography.labelSmall)
                }
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(onClick = { onDeleteGoal(goal) }) {
                  Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Goal",
                    tint = MaterialTheme.colorScheme.outline,
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
}

// -------------------------------------------------------------
// TAB 4: ANALYTICS SCREEN (CASHEW VISUAL REPORTS)
// -------------------------------------------------------------
@Composable
private fun AnalyticsScreen(
  categorySummaries: List<com.example.ui.viewmodel.CategorySpendSummary>,
  totalSpent: Double,
  income: Double,
  expense: Double,
  net: Double,
  last7DaysSpend: List<com.example.ui.viewmodel.DailySpend>,
  currencySymbol: String = "₹",
  onCategoryClick: (ExpenseCategory) -> Unit,
  onOpenGeminiInsights: () -> Unit
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 0. Gemini AI Spending Insights Banner
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onOpenGeminiInsights() }
          .testTag("gemini_analytics_banner"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
          ) {
            Box(
              modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(22.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Gemini AI Spending Insights",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Discover personalized tips & habit leaks to save money",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = "Open",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
          )
        }
      }
    }

    // 1. Donut Chart
    item {
      CategoryDonutChart(
        categorySummaries = categorySummaries,
        totalSpent = totalSpent,
        currencySymbol = currencySymbol,
        onCategoryClick = onCategoryClick
      )
    }

    // 2. Cash Flow Comparison
    item {
      IncomeExpenseComparisonCard(
        income = income,
        expense = expense,
        net = net,
        currencySymbol = currencySymbol
      )
    }

    // 3. 7-Day Trend Bar Chart
    item {
      SpendingTrendBarChart(
        dailySpends = last7DaysSpend,
        currencySymbol = currencySymbol
      )
    }
  }
}

private fun formatAmount(amount: Double): String {
  return String.format(Locale.US, "%,.2f", amount)
}
