# Clean Slate & Disable Automatic Sample Data Seeding

Disable automatic insertion of starter demo data on application launch, completely purge all existing mock/demo entries across database tables, and establish a clean-slate architecture that preserves user-created data exclusively.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following user preferences were confirmed during clarification and govern this implementation:
> - **Starter Sample Data**: Never automatically add sample data when opening the app.
> - **Existing Demo Data**: Clear all existing demo data across tables (transactions, accounts, budgets, goals, loans, notifications, and portfolio stocks) to start with a pristine, empty database.

- **Confirmed Decision 1**: Permanently eliminate the startup call `repository.preseedDataIfEmpty()` in `ExpenseViewModel.kt` and replace it with a one-time migration/purge routine so the app never generates unsolicited mock data.
- **Confirmed Decision 2**: Provide clear, inviting zero-data empty states across the Home Dashboard, Transactions, Accounts, Budgets, and Stocks screens, allowing users to enter their own real financial entries seamlessly without clutter.

---

### 1. Overview & Core Concept

- **What It Does**: Stops BudgetWise from injecting fake accounts (Main Checking, Cash Wallet, Sapphire Credit Card), dummy expenses (Netflix, Spotify, Tech Salary), demo loans, and sample stocks whenever transactions are empty. Clears all existing dummy data so users have a 100% clean database for their actual financial life.
- **Target Audience / Persona**: Users seeking personal financial tracking who want their actual numbers, not pre-populated fictional records that distort net worth, cash flow, and stock portfolios.
- **Key Value**: Eliminates recurring ghost records, provides true privacy and accuracy, and ensures a clean blank slate where users only see transactions and assets they explicitly add.

---

### 2. User Experience & Visual Design

- **Key User Flows**:
  1. **App Launch**: App opens directly to the Dashboard with real or zero-state data; no background thread silently inserts demo records into Room.
  2. **Zero-State Experience**: When empty, screens show clean Material 3 illustrated placeholder cards with quick actions:
     - Home Dashboard: "Welcome to BudgetWise" with a single action "Add Your First Account or Transaction".
     - Transactions: "No transactions yet" with a primary "+ Add Transaction" button.
     - Accounts: "No accounts connected" with an "Add Account" shortcut.
     - Stocks & Investments: "Your portfolio is empty" with an "Add Stock / Mutual Fund" search button.
  3. **Optional Demo Data Generator in Database Hub**: Move sample seeding to an explicit, opt-in button in the **Database Hub & Backups** settings screen ("Load Sample Data for Testing"), ensuring it only runs when explicitly tapped by the user.

- **Visual Identity & Theme**:
  - *Aesthetic Direction*: Refined, minimal financial ledger aesthetic adhering to Material Design 3.
  - *Color Palette*: Slate dark theme and clean light theme using existing `MaterialTheme.colorScheme` tokens.
  - *Empty State Styling*: M3 tonal cards with subtle rounded corners (`16.dp`), filled primary action buttons, and descriptive icons (`ReceiptLong`, `AccountBalance`, `TrendingUp`).

---

### 3. Key Product Decisions & Trade-Offs

- **Decision 1: Removal of Automatic Pre-seeding on Startup**
  - *Chosen Approach*: Completely remove `repository.preseedDataIfEmpty()` from `ExpenseViewModel.init`. Add a persistent DataStore/Preference flag `sampleDataPurged = true` and a dedicated repository cleanup function `clearAllDemoData()`.
  - *Why*: Eliminates the root cause where empty state triggered automatic repopulation of dummy records.
  - *Alternatives Considered*: Only checking if user has any accounts. Rejected because if a user wanted to wipe everything to reset, the old code would immediately repopulate dummy accounts and transactions.

- **Decision 2: Comprehensive Database Purge**
  - *Chosen Approach*: Clear all tables (`expenses`, `accounts`, `subscriptions`, `budgets`, `savings_goals`, `loans`, `notification_logs`, `stocks`, `sip_investments`, `conditional_mandates`).
  - *Why*: Delivers the requested clean slate immediately upon updating.
  - *Alternatives Considered*: Retaining the dummy accounts with $0 balance. Rejected because the user specifically chose to clear all demo data.

---

### 4. Technical Architecture & Data Strategy

```
┌─────────────────────────────────────────────────────────────┐
│                       BudgetWise App                        │
│                                                             │
│   ┌─────────────────────────────────────────────────────┐   │
│   │                 ExpenseViewModel                    │   │
│   │  • Removes repository.preseedDataIfEmpty() on init  │   │
│   │  • Runs one-time clearAllDemoData() migration       │   │
│   │  • Exposes empty states reactively                  │   │
│   └──────────────────────────┬──────────────────────────┘   │
│                              │                              │
│                              ▼                              │
│   ┌─────────────────────────────────────────────────────┐   │
│   │                 ExpenseRepository                   │   │
│   │  • clearAllData() / purgeDemoData()                 │   │
│   │  • seedSampleData() [Only manual trigger in Hub]    │   │
│   └──────────────────────────┬──────────────────────────┘   │
│                              │                              │
│                              ▼                              │
│   ┌─────────────────────────────────────────────────────┐   │
│   │                  Room Database                      │   │
│   │  • ExpensesDao   • AccountDao   • StockDao          │   │
│   │  • BudgetDao     • GoalDao      • LoanDao           │   │
│   │  • SubDao        • MandateDao   • SipDao            │   │
│   └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

- **Data Model & State**:
  - Room tables are cleared cleanly via Room transactions or individual DAO clear methods.
  - `UserPreferencesManager` stores `sampleDataPurged = true` so the purge executes once and the app remains permanently clean.
- **Interactive Component & State Mapping**:
  - `ExpenseViewModel`:
    - `clearAllData()`: Invokes repository purge, resets state flows (`geminiStockVerdict`, `allStocks`, `allExpenses`).
    - `seedDemoDataManually()`: Exclusively triggered if user clicks "Load Demo Data" in Settings/Database Hub.
  - `DatabaseHubScreen`:
    - "Database Actions" card includes "Reset to Clean Database" and "Load Sample Data (Optional)" with confirmation dialogs.
  - UI screens:
    - Display standard M3 Empty States when item counts are 0.
