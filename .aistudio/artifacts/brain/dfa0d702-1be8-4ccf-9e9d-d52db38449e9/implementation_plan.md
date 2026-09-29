# Financial Trading Platform Multi-Tier Database Architecture

A production-grade, highly resilient data architecture designed for financial trading applications, combining **PostgreSQL** for strict ACID transactions and double-entry ledgering, **Redis** for sub-millisecond write-through caching to protect third-party API rate limits, and **TimescaleDB** for compressed time-series analytics, historical EOD candles, and portfolio net-worth tracking.

---

### User Review & Critical Decisions

> [!IMPORTANT]
> The following architectural decisions were confirmed based on your specifications and are locked into the system design:

- **Confirmed Decision 1 (TimescaleDB Scope)**: Focused on daily End-of-Day (EOD) OHLCV candles, corporate action adjusted prices, and historical portfolio net-worth snapshots. This eliminates unnecessary tick-level write overhead while enabling aggressive columnar compression and fast long-term chart queries.
- **Confirmed Decision 2 (Redis Caching Model)**: Implemented as a proactive **write-through cache** with scheduled background worker pools (Celery / BullMQ / Go routine workers). The user-facing API reads strictly from Redis, while background workers manage rate-limited upstream calls (Alpha Vantage, Finnhub, NSE/BSE) without blocking client threads.
- **Confirmed Decision 3 (PostgreSQL Transaction Engine)**: Modeled as an **immutable, double-entry append-only ledger** partitioned by month (`RANGE (created_at)`). Balances are never modified in place; instead, balance states are verified with cryptographic hash chaining and materialized view checkpoints.

---

### 1. Overview & Core Concept

- **What It Does**: Decouples transactional banking/trading operations from volatile real-time market data streams and compute-heavy historical charting. 
- **Target Audience & Workflows**: 
  - Retail and institutional investors querying live watchlists, submitting trade orders, tracking portfolio equity curves, and running multi-year technical analytics.
  - High-concurrency mobile and web client frontends requiring deterministic <50ms response times without hitting external provider 429 errors.
- **Key Value**: 
  - **Zero Rate-Limit Breaches**: Client requests never directly hit third-party financial APIs; background ingest workers buffer and distribute rate allowances.
  - **Financial Integrity**: Append-only double-entry ledger prevents balance drift, double-spending, and concurrency race conditions.
  - **Storage & Query Efficiency**: Columnar chunk compression in TimescaleDB achieves up to 90% disk reduction on historical price bars.

---

### 2. User Experience & Visual Design

#### Key User Flows & System Interactions

1. **Portfolio & Watchlist View**:
   - Client requests user watchlist quotes.
   - API reads directly from Redis Hash keys (`stock:quote:{symbol}`).
   - Instant response (<5ms latency) with price, day high/low, change %, and data staleness timestamp.
2. **Trade Order Execution**:
   - User submits a Buy order.
   - PostgreSQL begins an explicit serializable transaction:
     - Verifies available cash in ledger checkpoint.
     - Inserts order intent into `orders` table.
     - Appends two balanced entries in `ledger_entries` (Debit: Cash Account, Credit: Stock Asset Account).
     - Updates user portfolio position.
   - Commits transaction and emits trade event to Redis Pub/Sub for immediate UI notification.
3. **Historical Chart & Net-Worth Analysis**:
   - User opens 1Y/5Y performance chart.
   - Query routes to TimescaleDB hypertable `stock_eod_candles` or continuous aggregate view.
   - Chunks older than 30 days are read directly from compressed columnar chunks.

---

### 3. Key Product Decisions & Trade-Offs

- **Decision 1: Double-Entry Append-Only Ledger vs. Mutable User Balance Columns**
  - *Chosen Approach*: An immutable ledger (`ledger_entries`) partitioned monthly by date, where every financial movement consists of equal debits and credits.
  - *Why*: Eliminates silent balance corruption, enables seamless auditing and historical reconciliation, and complies with financial regulatory standards.
  - *Alternatives Considered*: Direct `UPDATE users SET balance = balance - amount` with `SELECT FOR UPDATE`. Rejected due to deadlock vulnerabilities, lack of audit trails, and concurrency bottlenecks under rapid order submissions.

- **Decision 2: Proactive Background Polling vs. On-Demand Cache-Aside**
  - *Chosen Approach*: Central background worker cron tasks poll external APIs at regulated intervals based on market open/close states and populate Redis.
  - *Why*: Strict rate limits (e.g., Alpha Vantage 5-75 calls/min, Finnhub 30-60 calls/min) would immediately fail under traffic spikes if individual users triggered upstream calls on cache misses (Thundering Herd problem).
  - *Alternatives Considered*: Standard Cache-Aside (Lazy loading). Rejected because simultaneous cache misses for popular tickers (`AAPL`, `TATAMOTORS`, `RELIANCE`) cause downstream API throttling and cascade failures.

- **Decision 3: Dedicated TimescaleDB Engine vs. Standard PostgreSQL Tables**
  - *Chosen Approach*: TimescaleDB extension on PostgreSQL utilizing hypertables, automated retention policies, and native columnar compression.
  - *Why*: Native time-bucket partitioning, 90%+ storage savings via compression, and query performance that doesn't degrade as historical records grow into hundreds of millions.

---

### 4. Technical Architecture & Data Strategy

#### High-Level System Architecture Diagram

```
                 ┌──────────────────────────────────────────────────┐
                 │          Client Mobile & Web Applications        │
                 └─────────────────────────┬────────────────────────┘
                                           │ HTTPS / WebSocket
                                           ▼
                 ┌──────────────────────────────────────────────────┐
                 │            API Gateway & Trading Services         │
                 └───────┬─────────────────┬─────────────────┬──────┘
                         │                 │                 │
             Read Hot    │      ACID       │   Timeseries    │
            Stock Data   │   Order Exec    │  EOD & Charts   │
                         ▼                 ▼                 ▼
          ┌───────────────────┐  ┌───────────────────┐  ┌───────────────────┐
          │       REDIS       │  │    POSTGRESQL     │  │    TIMESCALEDB    │
          │  (Write-Through)  │  │   (ACID Core)     │  │   (Time-Series)   │
          ├───────────────────┤  ├───────────────────┤  ├───────────────────┤
          │ • Live Quote Hash │  │ • User Accounts   │  │ • EOD OHLCV Bars  │
          │ • Rate Limiter    │  │ • Watchlists      │  │ • Net-Worth Snaps │
          │ • Market Status   │  │ • Double-Entry    │  │ • Hypertable      │
          │ • Pub/Sub Events  │  │   Monthly Ledger  │  │   Compression     │
          └─────────▲─────────┘  └───────────────────┘  └───────────────────┘
                    │ Writes
         ┌──────────┴───────────────┐
         │ Ingest Workers & Schedulers
         │ (Respects Third-Party    │
         │  API Rate Limits)        │
         └──────────▲───────────────┘
                    │ Rate-Limited Ingest
         ┌──────────┴───────────────┐
         │ Third-Party Market APIs  │
         │ (Alpha Vantage / Finnhub)│
         └──────────────────────────┘
```

---

#### 1. PostgreSQL Schema: Core Accounts, Watchlists & Partitioned Ledger

```sql
-- 1. User & Account Identification
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    kyc_status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Internal Accounts for Double-Entry System (Assets, Liabilities, Equity)
CREATE TYPE account_type AS ENUM ('CASH_WALLET', 'EQUITY_HOLDING', 'CLEARING_ESCROW', 'FEE_EXPENSE');

CREATE TABLE accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    account_type account_type NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'INR', -- or USD
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 2. User Watchlists
CREATE TABLE watchlists (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(64) NOT NULL DEFAULT 'My Watchlist',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE watchlist_items (
    watchlist_id UUID NOT NULL REFERENCES watchlists(id) ON DELETE CASCADE,
    symbol VARCHAR(32) NOT NULL,
    exchange VARCHAR(16) NOT NULL,
    display_order INT NOT NULL DEFAULT 0,
    added_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (watchlist_id, symbol, exchange)
);

-- 3. Trade Orders
CREATE TYPE order_status AS ENUM ('PENDING', 'FILLED', 'PARTIALLY_FILLED', 'CANCELLED', 'REJECTED');
CREATE TYPE order_side AS ENUM ('BUY', 'SELL');

CREATE TABLE orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    symbol VARCHAR(32) NOT NULL,
    exchange VARCHAR(16) NOT NULL,
    side order_side NOT NULL,
    quantity NUMERIC(18, 4) NOT NULL CHECK (quantity > 0),
    filled_quantity NUMERIC(18, 4) NOT NULL DEFAULT 0,
    limit_price NUMERIC(18, 4),
    status order_status NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 4. Double-Entry Partitioned Financial Ledger
CREATE TABLE ledger_entries (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    transaction_id UUID NOT NULL, -- Grouping ID for double-entry balance pair
    account_id UUID NOT NULL REFERENCES accounts(id),
    order_id UUID REFERENCES orders(id),
    amount NUMERIC(18, 4) NOT NULL, -- Positive for Debit, Negative for Credit
    currency VARCHAR(10) NOT NULL,
    description TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (id, created_at)
) PARTITION BY RANGE (created_at);

-- Monthly Partition Tables (Automated with pg_partman or cron)
CREATE TABLE ledger_entries_2026_09 PARTITION OF ledger_entries
    FOR VALUES FROM ('2026-09-01 00:00:00+00') TO ('2026-10-01 00:00:00+00');
CREATE TABLE ledger_entries_2026_10 PARTITION OF ledger_entries
    FOR VALUES FROM ('2026-10-01 00:00:00+00') TO ('2026-11-01 00:00:00+00');

-- Indices for Ledger Performance
CREATE INDEX idx_ledger_account_created ON ledger_entries(account_id, created_at DESC);
CREATE INDEX idx_ledger_tx_id ON ledger_entries(transaction_id);
```

---

#### 2. Redis Data Structures: Proactive Write-Through Cache

| Key Pattern | Redis Type | TTL | Purpose |
| :--- | :--- | :--- | :--- |
| `quote:{exchange}:{symbol}` | `HASH` | 120s | Real-time quote snapshot (LTP, open, high, low, close, volume, timestamp) |
| `active_symbols` | `SET` | Persistent | Unique active symbols across all user watchlists and open orders |
| `rate_limit:vendor:{vendor_name}` | `STRING` | 60s | Token bucket / sliding window counter for external API calls |
| `market_status:{exchange}` | `STRING` | 300s | Current market state (`OPEN`, `PRE_OPEN`, `CLOSED`) |
| `channel:orders:{user_id}` | `Pub/Sub` | - | Real-time order fill and balance update event stream |

**Example Redis Quote Hash (`quote:NSE:TATAMOTORS`):**
```
price: "441.50"
open: "438.00"
high: "445.20"
low: "436.80"
prev_close: "437.90"
volume: "12845090"
change_percent: "0.82"
updated_at: "1790666400"
source: "ALPHA_VANTAGE"
```

---

#### 3. TimescaleDB Schema: EOD Candles & Historical Net-Worth Snapshots

```sql
-- 1. Historical Daily Stock Candles (OHLCV)
CREATE TABLE stock_eod_candles (
    time TIMESTAMPTZ NOT NULL,
    symbol VARCHAR(32) NOT NULL,
    exchange VARCHAR(16) NOT NULL,
    open NUMERIC(14, 4) NOT NULL,
    high NUMERIC(14, 4) NOT NULL,
    low NUMERIC(14, 4) NOT NULL,
    close NUMERIC(14, 4) NOT NULL,
    volume BIGINT NOT NULL,
    adjusted_close NUMERIC(14, 4),
    PRIMARY KEY (time, symbol, exchange)
);

-- Convert to TimescaleDB Hypertable
SELECT create_hypertable('stock_eod_candles', 'time', chunk_time_interval => INTERVAL '1 month');

-- Enable Columnar Compression (After 30 days)
ALTER TABLE stock_eod_candles SET (
    timescaledb.compress,
    timescaledb.compress_segmentby = 'symbol, exchange',
    timescaledb.compress_orderby = 'time DESC'
);
SELECT add_compression_policy('stock_eod_candles', INTERVAL '30 days');

-- 2. Daily User Portfolio Net-Worth Snapshots
CREATE TABLE user_networth_snapshots (
    time TIMESTAMPTZ NOT NULL,
    user_id UUID NOT NULL,
    cash_balance NUMERIC(18, 4) NOT NULL,
    invested_equity NUMERIC(18, 4) NOT NULL,
    total_net_worth NUMERIC(18, 4) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'INR',
    PRIMARY KEY (time, user_id)
);

-- Convert to Hypertable with 3-Month Chunks
SELECT create_hypertable('user_networth_snapshots', 'time', chunk_time_interval => INTERVAL '3 months');

-- Enable Compression on Historical Net-Worth Data (After 60 days)
ALTER TABLE user_networth_snapshots SET (
    timescaledb.compress,
    timescaledb.compress_segmentby = 'user_id',
    timescaledb.compress_orderby = 'time DESC'
);
SELECT add_compression_policy('user_networth_snapshots', INTERVAL '60 days');
```

---

### 5. Implementation Roadmap & Verification Plan

1. **Database Provisioning & Extensions**:
   - Install PostgreSQL 16+ with `timescaledb` extension enabled.
   - Configure Redis Cluster/Instance with eviction policy `volatile-lru`.
2. **Schema & Migration Scripts**:
   - Execute DDL for accounts, double-entry partitioned ledger, watchlists, and orders.
   - Execute TimescaleDB hypertables, compression policies, and index configurations.
3. **Background Ingestion & Caching Engine**:
   - Implement scheduler worker: iterates over Redis `active_symbols` set.
   - Respect provider rate limits using Redis sliding-window token bucket.
   - Write freshly fetched quotes to Redis hashes with atomic pipeline operations.
4. **End-of-Day Batch Worker**:
   - At market close, trigger daily reconciler:
     - Downloads final official EOD prices.
     - Bulk-inserts into `stock_eod_candles`.
     - Calculates each user's end-of-day equity balance and appends into `user_networth_snapshots`.
5. **Ledger Integrity Audit Verification**:
   - Run periodic automated test checking `SUM(amount) = 0` per `transaction_id`.
