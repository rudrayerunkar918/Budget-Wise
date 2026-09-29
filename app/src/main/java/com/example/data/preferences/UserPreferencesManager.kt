package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

enum class ThemeMode {
  SYSTEM,
  LIGHT,
  DARK
}

enum class HomeSectionType(
  val id: String,
  val defaultTitle: String,
  val description: String
) {
  HOMEPAGE_BANNER("banner", "Homepage Banner", "Greeting and user title banner"),
  ACCOUNTS("accounts", "Accounts", "Horizontal accounts summary carousel"),
  ACCOUNTS_LIST("accounts_list", "Accounts List", "Detailed vertical accounts list"),
  BUDGETS("budgets", "Budgets", "Active budget cards with daily spend tracker"),
  GOALS("goals", "Goals", "Savings targets with progress calculation"),
  INCOME_EXPENSES("income_expenses", "Income & Expenses", "Dual income and expense overview cards"),
  NET_WORTH("net_worth", "Net Worth", "Total Net Worth and cash flow summary"),
  OVERDUE_UPCOMING("overdue_upcoming", "Overdue & Upcoming", "Upcoming recurring bills and payment status"),
  PIE_CHART("pie_chart", "Pie Chart", "Interactive category distribution donut chart"),
  SPENDING_GRAPH("spending_graph", "Spending Graph", "Timeline spending trend curve"),
  LOANS("loans", "Loans", "Money lent to others and borrowed debts summary"),
  STOCKS("stocks", "Investments Portfolio", "Stocks & Mutual Funds portfolio and P&L summary"),
  STACKED_BAR("stacked_bar", "Stacked Bar Graph", "Income vs expense proportion comparison bar"),
  PINNED_TRANSACTIONS("pinned_transactions", "Pinned Transactions", "Quick list of recent transactions")
}

data class HomeSectionItem(
  val type: HomeSectionType,
  val isEnabled: Boolean = true
)

enum class NavTabDestination(
  val id: String,
  val label: String
) {
  HOME("home", "Home"),
  TRANSACTIONS("transactions", "Transactions"),
  BUDGETS("budgets", "Budgets"),
  STOCKS("investments", "Investments"),
  LOANS("loans", "Loans"),
  RECURRING("recurring", "Recurring"),
  GOALS("goals", "Goals"),
  ANALYTICS("analytics", "Analytics"),
  NET_WORTH("net_worth", "Net Worth"),
  MORE("more", "More")
}

class UserPreferencesManager(context: Context) {

  private val prefs: SharedPreferences =
    context.getSharedPreferences("cashew_user_prefs", Context.MODE_PRIVATE)

  // 1. Theme Mode
  private val _themeMode = MutableStateFlow(loadThemeMode())
  val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

  // 2. Home Sections
  private val _homeSections = MutableStateFlow(loadHomeSections())
  val homeSections: StateFlow<List<HomeSectionItem>> = _homeSections.asStateFlow()

  // 3. Navigation Bar Tabs
  private val _navTabs = MutableStateFlow(loadNavTabs())
  val navTabs: StateFlow<List<NavTabDestination>> = _navTabs.asStateFlow()

  // 4. Currency Symbol
  private val _currencySymbol = MutableStateFlow(prefs.getString(KEY_CURRENCY, "₹") ?: "₹")
  val currencySymbol: StateFlow<String> = _currencySymbol.asStateFlow()

  // 5. User Name & Greeting
  private val _userName = MutableStateFlow(prefs.getString(KEY_USER_NAME, "Rudra Yerunkar") ?: "Rudra Yerunkar")
  val userName: StateFlow<String> = _userName.asStateFlow()

  private val _greeting = MutableStateFlow(prefs.getString(KEY_GREETING, "Hope all is well") ?: "Hope all is well")
  val greeting: StateFlow<String> = _greeting.asStateFlow()

  // 6. Finnhub API Key & Auto Sync
  private val _finnhubApiKey = MutableStateFlow(prefs.getString(KEY_FINNHUB_API_KEY, "") ?: "")
  val finnhubApiKey: StateFlow<String> = _finnhubApiKey.asStateFlow()

  private val _finnhubAutoSync = MutableStateFlow(prefs.getBoolean(KEY_FINNHUB_AUTO_SYNC, true))
  val finnhubAutoSync: StateFlow<Boolean> = _finnhubAutoSync.asStateFlow()

  fun setFinnhubApiKey(key: String) {
    val cleanKey = key.trim()
    _finnhubApiKey.value = cleanKey
    prefs.edit().putString(KEY_FINNHUB_API_KEY, cleanKey).apply()
  }

  fun setFinnhubAutoSync(enabled: Boolean) {
    _finnhubAutoSync.value = enabled
    prefs.edit().putBoolean(KEY_FINNHUB_AUTO_SYNC, enabled).apply()
  }

  // 7. Alpha Vantage API Key & Auto Sync
  private val _alphaVantageApiKey = MutableStateFlow(prefs.getString(KEY_ALPHA_VANTAGE_API_KEY, "") ?: "")
  val alphaVantageApiKey: StateFlow<String> = _alphaVantageApiKey.asStateFlow()

  private val _alphaVantageAutoSync = MutableStateFlow(prefs.getBoolean(KEY_ALPHA_VANTAGE_AUTO_SYNC, true))
  val alphaVantageAutoSync: StateFlow<Boolean> = _alphaVantageAutoSync.asStateFlow()

  fun setAlphaVantageApiKey(key: String) {
    val cleanKey = key.trim()
    _alphaVantageApiKey.value = cleanKey
    prefs.edit().putString(KEY_ALPHA_VANTAGE_API_KEY, cleanKey).apply()
  }

  fun setAlphaVantageAutoSync(enabled: Boolean) {
    _alphaVantageAutoSync.value = enabled
    prefs.edit().putBoolean(KEY_ALPHA_VANTAGE_AUTO_SYNC, enabled).apply()
  }

  // --- Theme Mode Functions ---
  fun setThemeMode(mode: ThemeMode) {
    _themeMode.value = mode
    prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
  }

  private fun loadThemeMode(): ThemeMode {
    val saved = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name)
    return try {
      ThemeMode.valueOf(saved ?: ThemeMode.SYSTEM.name)
    } catch (_: Exception) {
      ThemeMode.SYSTEM
    }
  }

  // --- Home Sections Functions ---
  fun toggleSection(type: HomeSectionType) {
    val current = _homeSections.value.toMutableList()
    val index = current.indexOfFirst { it.type == type }
    if (index != -1) {
      val item = current[index]
      current[index] = item.copy(isEnabled = !item.isEnabled)
      _homeSections.value = current
      saveHomeSections(current)
    }
  }

  fun moveSection(fromIndex: Int, toIndex: Int) {
    val current = _homeSections.value.toMutableList()
    if (fromIndex in current.indices && toIndex in current.indices && fromIndex != toIndex) {
      val item = current.removeAt(fromIndex)
      current.add(toIndex, item)
      _homeSections.value = current
      saveHomeSections(current)
    }
  }

  fun moveSectionUp(type: HomeSectionType) {
    val current = _homeSections.value.toMutableList()
    val index = current.indexOfFirst { it.type == type }
    if (index > 0) {
      val item = current.removeAt(index)
      current.add(index - 1, item)
      _homeSections.value = current
      saveHomeSections(current)
    }
  }

  fun moveSectionDown(type: HomeSectionType) {
    val current = _homeSections.value.toMutableList()
    val index = current.indexOfFirst { it.type == type }
    if (index != -1 && index < current.lastIndex) {
      val item = current.removeAt(index)
      current.add(index + 1, item)
      _homeSections.value = current
      saveHomeSections(current)
    }
  }

  fun resetHomeSectionsToDefault() {
    val defaultList = getDefaultSections()
    _homeSections.value = defaultList
    saveHomeSections(defaultList)
  }

  private fun saveHomeSections(list: List<HomeSectionItem>) {
    val array = JSONArray()
    for (item in list) {
      val obj = JSONObject()
      obj.put("type", item.type.name)
      obj.put("enabled", item.isEnabled)
      array.put(obj)
    }
    prefs.edit().putString(KEY_HOME_SECTIONS, array.toString()).apply()
  }

  private fun loadHomeSections(): List<HomeSectionItem> {
    val saved = prefs.getString(KEY_HOME_SECTIONS, null) ?: return getDefaultSections()
    return try {
      val array = JSONArray(saved)
      val list = mutableListOf<HomeSectionItem>()
      val seen = mutableSetOf<HomeSectionType>()
      for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        val typeName = obj.getString("type")
        val enabled = obj.optBoolean("enabled", true)
        val type = HomeSectionType.valueOf(typeName)
        list.add(HomeSectionItem(type, enabled))
        seen.add(type)
      }
      // Add any missing types
      for (def in getDefaultSections()) {
        if (def.type !in seen) {
          list.add(def)
        }
      }
      list
    } catch (_: Exception) {
      getDefaultSections()
    }
  }

  private fun getDefaultSections(): List<HomeSectionItem> = listOf(
    HomeSectionItem(HomeSectionType.HOMEPAGE_BANNER, true),
    HomeSectionItem(HomeSectionType.NET_WORTH, true),
    HomeSectionItem(HomeSectionType.ACCOUNTS, true),
    HomeSectionItem(HomeSectionType.ACCOUNTS_LIST, true),
    HomeSectionItem(HomeSectionType.BUDGETS, true),
    HomeSectionItem(HomeSectionType.GOALS, true),
    HomeSectionItem(HomeSectionType.INCOME_EXPENSES, true),
    HomeSectionItem(HomeSectionType.OVERDUE_UPCOMING, true),
    HomeSectionItem(HomeSectionType.PIE_CHART, true),
    HomeSectionItem(HomeSectionType.SPENDING_GRAPH, true),
    HomeSectionItem(HomeSectionType.LOANS, true),
    HomeSectionItem(HomeSectionType.STOCKS, true),
    HomeSectionItem(HomeSectionType.STACKED_BAR, true),
    HomeSectionItem(HomeSectionType.PINNED_TRANSACTIONS, true)
  )

  // --- Navigation Bar Customization Functions ---
  fun setNavTabs(tabs: List<NavTabDestination>) {
    _navTabs.value = tabs
    val array = JSONArray()
    for (t in tabs) {
      array.put(t.name)
    }
    prefs.edit().putString(KEY_NAV_TABS, array.toString()).apply()
  }

  fun resetNavTabsToDefault() {
    val defaultTabs = getDefaultNavTabs()
    setNavTabs(defaultTabs)
  }

  private fun loadNavTabs(): List<NavTabDestination> {
    val saved = prefs.getString(KEY_NAV_TABS, null) ?: return getDefaultNavTabs()
    return try {
      val array = JSONArray(saved)
      val list = mutableListOf<NavTabDestination>()
      for (i in 0 until array.length()) {
        list.add(NavTabDestination.valueOf(array.getString(i)))
      }
      if (list.isEmpty()) getDefaultNavTabs() else list
    } catch (_: Exception) {
      getDefaultNavTabs()
    }
  }

  private fun getDefaultNavTabs(): List<NavTabDestination> = listOf(
    NavTabDestination.HOME,
    NavTabDestination.TRANSACTIONS,
    NavTabDestination.STOCKS,
    NavTabDestination.BUDGETS,
    NavTabDestination.MORE
  )

  // --- Currency & User Profile ---
  fun setCurrencySymbol(symbol: String) {
    _currencySymbol.value = symbol
    prefs.edit().putString(KEY_CURRENCY, symbol).apply()
  }

  fun setUserProfile(name: String, greeting: String) {
    _userName.value = name
    _greeting.value = greeting
    prefs.edit()
      .putString(KEY_USER_NAME, name)
      .putString(KEY_GREETING, greeting)
      .apply()
  }

  fun setUserName(name: String) {
    _userName.value = name
    prefs.edit()
      .putString(KEY_USER_NAME, name)
      .apply()
  }

  companion object {
    private const val KEY_THEME_MODE = "key_theme_mode"
    private const val KEY_HOME_SECTIONS = "key_home_sections"
    private const val KEY_NAV_TABS = "key_nav_tabs"
    private const val KEY_CURRENCY = "key_currency"
    private const val KEY_USER_NAME = "key_user_name"
    private const val KEY_GREETING = "key_greeting"
    private const val KEY_FINNHUB_API_KEY = "key_finnhub_api_key"
    private const val KEY_FINNHUB_AUTO_SYNC = "key_finnhub_auto_sync"
    private const val KEY_ALPHA_VANTAGE_API_KEY = "key_alpha_vantage_api_key"
    private const val KEY_ALPHA_VANTAGE_AUTO_SYNC = "key_alpha_vantage_auto_sync"
  }
}
