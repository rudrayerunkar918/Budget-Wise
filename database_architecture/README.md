# Financial Trading Platform Database Architecture

A multi-tier, production-ready database topology tailored for real-time market data ingestion, high-concurrency order placement, and multi-year time-series financial analysis.

---

## Architecture Topology

```
                                 [ Web & Mobile Clients ]
                                             │
                                             ▼
                             [ API Gateway & Ingestion Services ]
                                 │           │            │
            Hot Quotes (<5ms)    │           │ ACID Trades│ Historical Charts
                                 ▼           │            ▼
                     ┌───────────────────┐   │   ┌───────────────────┐
                     │       REDIS       │   │   │    TIMESCALEDB    │
                     │  (Write-Through)  │   │   │   (Time-Series)   │
                     ├───────────────────┤   │   ├───────────────────┤
                     │ • quote:NSE:TATA  │   │   │ • stock_eod_      │
                     │ • active_symbols  │   │   │   candles         │
                     │ • Token Buckets   │   │   │ • user_networth_  │
                     │ • Order Pub/Sub   │   │   │   snapshots       │
                     └─────────▲─────────┘   │   │ • 90% Compression │
                               │             │   └───────────────────┘
               Proactive Sync  │             ▼
            ┌──────────────────┴──┐  ┌───────────────────┐
            │  Background Ingest  │  │    POSTGRESQL     │
            │  Worker Pool        │  │    (ACID Core)    │
            └──────────▲──────────┘  ├───────────────────┤
                       │             │ • users & auth    │
            Rate-Limit │             │ • accounts        │
            Compliant  │             │ • watchlists      │
                       ▼             │ • orders          │
            ┌─────────────────────┐  │ • Monthly Ledger  │
            │ Third-Party APIs    │  │   Partitions      │
            │ (Alpha Vantage /    │  └───────────────────┘
            │  Finnhub)           │
            └─────────────────────┘
```

---

## Database Components & Separation of Concerns

### 1. PostgreSQL (ACID Financial Core)
- **Role**: Source of truth for identity, authentication, portfolios, watchlists, and trades.
- **Double-Entry Ledgering**: Balances are not updated with mutable `UPDATE balance = ...` calls. Instead, balanced Debit/Credit records are appended into `ledger_entries`.
- **Monthly Partitioning**: `ledger_entries` is partitioned by range on `created_at` (e.g. `ledger_entries_2026_09`), keeping query index B-trees compact and memory-resident.
- **DDL File**: `01_postgresql_schema.sql`

### 2. Redis (Market Data Write-Through Cache)
- **Role**: Rate-limit shield and high-frequency in-memory cache.
- **Write-Through Ingestion**: Clients never trigger upstream calls on cache misses. Proactive worker daemons query third-party APIs (Alpha Vantage, Finnhub) on fixed schedules for all symbols in the `active_symbols` set.
- **Sliding-Window Rate Limiter**: Atomic Lua script tracks request counts within a 60-second rolling window across all worker instances.
- **Strategy & Worker Blueprint**: `02_redis_caching_strategy.md`

### 3. TimescaleDB (Historical Price Candles & Net-Worth Tracking)
- **Role**: Time-series engine for EOD historical charts and portfolio equity tracking.
- **Hypertables**:
  - `stock_eod_candles`: Daily price bars partitioned into 1-month chunks.
  - `user_networth_snapshots`: Daily account equity snapshots partitioned into 3-month chunks.
- **Columnar Compression**: Automated policy compresses chunks older than 30 days, yielding ~90% disk space reduction.
- **Continuous Aggregates**: Automated rollups for weekly and monthly candlesticks.
- **DDL File**: `03_timescaledb_hypertables.sql`

---

## Quick-Start Local Development (Docker Compose)

```yaml
version: '3.8'

services:
  postgres-timescale:
    image: timescale/timescaledb:latest-pg16
    container_name: trading_timescale_pg
    environment:
      POSTGRES_USER: trading_admin
      POSTGRES_PASSWORD: trading_secret_password
      POSTGRES_DB: trading_platform
    ports:
      - "5432:5432"
    volumes:
      - ./01_postgresql_schema.sql:/docker-entrypoint-initdb.d/01_postgresql.sql
      - ./03_timescaledb_hypertables.sql:/docker-entrypoint-initdb.d/02_timescale.sql
      - timescale_data:/var/lib/postgresql/data

  redis-cache:
    image: redis:7.2-alpine
    container_name: trading_redis
    command: redis-server --maxmemory 512mb --maxmemory-policy volatile-lru
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data

volumes:
  timescale_data:
  redis_data:
```
