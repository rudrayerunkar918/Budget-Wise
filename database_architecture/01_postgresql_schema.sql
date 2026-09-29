-- ============================================================================
-- FINANCIAL TRADING PLATFORM: POSTGRESQL PRODUCTION DDL
-- Core Accounts, Watchlists, Trade Orders & Monthly Partitioned Double-Entry Ledger
-- ============================================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ----------------------------------------------------------------------------
-- 1. USER & ACCOUNT MANAGEMENT
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    phone_number VARCHAR(32),
    kyc_status VARCHAR(32) NOT NULL DEFAULT 'PENDING' CHECK (kyc_status IN ('PENDING', 'VERIFIED', 'REJECTED')),
    base_currency VARCHAR(10) NOT NULL DEFAULT 'INR',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- Account classification for Double-Entry bookkeeping
CREATE TYPE account_type AS ENUM (
    'CASH_WALLET',      -- Liquid user fiat cash
    'EQUITY_HOLDING',   -- Long asset position (stocks, ETFs)
    'CLEARING_ESCROW',  -- Temporary settlement escrow
    'FEE_EXPENSE',      -- Platform brokerage and exchange STT/taxes
    'SYSTEM_RESERVE'    -- Platform liquidity reserve
);

CREATE TABLE IF NOT EXISTS accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    account_number VARCHAR(64) UNIQUE NOT NULL,
    account_type account_type NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'INR',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_accounts_user ON accounts(user_id, account_type);

-- ----------------------------------------------------------------------------
-- 2. USER WATCHLISTS & SYMBOLS
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS watchlists (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(64) NOT NULL DEFAULT 'Primary Watchlist',
    display_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_watchlists_user ON watchlists(user_id);

CREATE TABLE IF NOT EXISTS watchlist_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    watchlist_id UUID NOT NULL REFERENCES watchlists(id) ON DELETE CASCADE,
    symbol VARCHAR(32) NOT NULL,
    exchange VARCHAR(16) NOT NULL DEFAULT 'NSE',
    display_order INT NOT NULL DEFAULT 0,
    added_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_watchlist_symbol UNIQUE (watchlist_id, symbol, exchange)
);

CREATE INDEX IF NOT EXISTS idx_watchlist_items_lookup ON watchlist_items(symbol, exchange);

-- ----------------------------------------------------------------------------
-- 3. TRADE ORDERS
-- ----------------------------------------------------------------------------

CREATE TYPE order_side AS ENUM ('BUY', 'SELL');
CREATE TYPE order_type AS ENUM ('MARKET', 'LIMIT', 'STOP_LOSS');
CREATE TYPE order_status AS ENUM ('PENDING', 'ACCEPTED', 'FILLED', 'PARTIALLY_FILLED', 'CANCELLED', 'REJECTED');

CREATE TABLE IF NOT EXISTS orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    symbol VARCHAR(32) NOT NULL,
    exchange VARCHAR(16) NOT NULL,
    side order_side NOT NULL,
    order_type order_type NOT NULL DEFAULT 'MARKET',
    quantity NUMERIC(18, 4) NOT NULL CHECK (quantity > 0),
    filled_quantity NUMERIC(18, 4) NOT NULL DEFAULT 0 CHECK (filled_quantity >= 0),
    limit_price NUMERIC(18, 4),
    execution_price NUMERIC(18, 4),
    brokerage_fee NUMERIC(18, 4) NOT NULL DEFAULT 0,
    status order_status NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_orders_user_status ON orders(user_id, status, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_orders_symbol ON orders(symbol, exchange, created_at DESC);

-- ----------------------------------------------------------------------------
-- 4. DOUBLE-ENTRY FINANCIAL LEDGER (PARTITIONED BY MONTH)
-- ----------------------------------------------------------------------------

-- In double-entry bookkeeping:
-- Positive amount = DEBIT
-- Negative amount = CREDIT
-- Every transaction MUST have sum(amount) == 0 across its entries.
CREATE TABLE IF NOT EXISTS ledger_entries (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    transaction_id UUID NOT NULL,       -- Ties corresponding debit/credit pairs
    account_id UUID NOT NULL REFERENCES accounts(id),
    order_id UUID REFERENCES orders(id),
    amount NUMERIC(18, 4) NOT NULL,     -- Signed decimal (+ for debit, - for credit)
    currency VARCHAR(10) NOT NULL DEFAULT 'INR',
    description VARCHAR(255) NOT NULL,
    entry_hash VARCHAR(64),             -- SHA-256 tamper-evident integrity hash
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (id, created_at)
) PARTITION BY RANGE (created_at);

-- Monthly partition tables
CREATE TABLE IF NOT EXISTS ledger_entries_2026_08 PARTITION OF ledger_entries
    FOR VALUES FROM ('2026-08-01 00:00:00+00') TO ('2026-09-01 00:00:00+00');

CREATE TABLE IF NOT EXISTS ledger_entries_2026_09 PARTITION OF ledger_entries
    FOR VALUES FROM ('2026-09-01 00:00:00+00') TO ('2026-10-01 00:00:00+00');

CREATE TABLE IF NOT EXISTS ledger_entries_2026_10 PARTITION OF ledger_entries
    FOR VALUES FROM ('2026-10-01 00:00:00+00') TO ('2026-11-01 00:00:00+00');

CREATE TABLE IF NOT EXISTS ledger_entries_2026_11 PARTITION OF ledger_entries
    FOR VALUES FROM ('2026-11-01 00:00:00+00') TO ('2026-12-01 00:00:00+00');

CREATE TABLE IF NOT EXISTS ledger_entries_2026_12 PARTITION OF ledger_entries
    FOR VALUES FROM ('2026-12-01 00:00:00+00') TO ('2027-01-01 00:00:00+00');

-- Partition maintenance function to auto-create upcoming months
CREATE OR REPLACE FUNCTION create_next_monthly_partition()
RETURNS void AS $$
DECLARE
    next_month_start DATE := date_trunc('month', CURRENT_DATE + INTERVAL '1 month');
    next_month_end DATE := date_trunc('month', CURRENT_DATE + INTERVAL '2 month');
    partition_name TEXT := 'ledger_entries_' || to_char(next_month_start, 'YYYY_MM');
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_tables WHERE tablename = partition_name) THEN
        EXECUTE format(
            'CREATE TABLE %I PARTITION OF ledger_entries FOR VALUES FROM (%L) TO (%L);',
            partition_name, next_month_start, next_month_end
        );
        RAISE NOTICE 'Created partition %', partition_name;
    END IF;
END;
$$ LANGUAGE plpgsql;

-- Partition Indices
CREATE INDEX IF NOT EXISTS idx_ledger_account_created ON ledger_entries(account_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_ledger_tx_id ON ledger_entries(transaction_id);
CREATE INDEX IF NOT EXISTS idx_ledger_order_id ON ledger_entries(order_id);

-- ----------------------------------------------------------------------------
-- 5. MATERIALIZED CHECKPOINTS & INTEGRITY VIEWS
-- ----------------------------------------------------------------------------

-- Checkpoint table to avoid summing millions of ledger rows on every balance query
CREATE TABLE IF NOT EXISTS account_balance_checkpoints (
    account_id UUID PRIMARY KEY REFERENCES accounts(id),
    current_balance NUMERIC(18, 4) NOT NULL DEFAULT 0,
    last_entry_id UUID,
    last_checkpoint_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Integrity verification view: ensures all transactions strictly balance
CREATE OR REPLACE VIEW view_ledger_imbalance_audit AS
SELECT 
    transaction_id,
    COUNT(*) AS entry_count,
    SUM(amount) AS net_imbalance,
    MIN(created_at) AS transaction_time
FROM ledger_entries
GROUP BY transaction_id
HAVING SUM(amount) <> 0;

-- ----------------------------------------------------------------------------
-- 6. ATOMIC TRADE EXECUTION STORED PROCEDURE
-- ----------------------------------------------------------------------------

CREATE OR REPLACE FUNCTION execute_stock_purchase(
    p_user_id UUID,
    p_cash_account_id UUID,
    p_equity_account_id UUID,
    p_symbol VARCHAR(32),
    p_exchange VARCHAR(16),
    p_quantity NUMERIC(18, 4),
    p_price NUMERIC(18, 4),
    p_fee NUMERIC(18, 4) DEFAULT 20.00
) RETURNS UUID AS $$
DECLARE
    v_order_id UUID;
    v_tx_id UUID := gen_random_uuid();
    v_total_cost NUMERIC(18, 4) := (p_quantity * p_price) + p_fee;
    v_current_cash NUMERIC(18, 4);
BEGIN
    -- 1. Check current cash balance
    SELECT COALESCE(SUM(amount), 0) INTO v_current_cash
    FROM ledger_entries
    WHERE account_id = p_cash_account_id;

    IF v_current_cash < v_total_cost THEN
        RAISE EXCEPTION 'Insufficient funds: required %, available %', v_total_cost, v_current_cash;
    END IF;

    -- 2. Insert filled order record
    INSERT INTO orders (
        user_id, symbol, exchange, side, order_type, quantity, filled_quantity,
        limit_price, execution_price, brokerage_fee, status
    ) VALUES (
        p_user_id, p_symbol, p_exchange, 'BUY', 'MARKET', p_quantity, p_quantity,
        p_price, p_price, p_fee, 'FILLED'
    ) RETURNING id INTO v_order_id;

    -- 3. Append Double-Entry Ledger Pair (Net Sum == 0)
    -- Credit Cash Account (money leaves wallet)
    INSERT INTO ledger_entries (
        transaction_id, account_id, order_id, amount, currency, description
    ) VALUES (
        v_tx_id, p_cash_account_id, v_order_id, -v_total_cost, 'INR',
        format('Buy %s shares of %s @ %s + fee %s', p_quantity, p_symbol, p_price, p_fee)
    );

    -- Debit Equity Account (stock asset value enters portfolio)
    INSERT INTO ledger_entries (
        transaction_id, account_id, order_id, amount, currency, description
    ) VALUES (
        v_tx_id, p_equity_account_id, v_order_id, (p_quantity * p_price), 'INR',
        format('Acquired equity asset: %s x %s', p_quantity, p_symbol)
    );

    -- Debit Platform Fee Expense (if applicable)
    IF p_fee > 0 THEN
        -- Uses clearing/fee account
        INSERT INTO ledger_entries (
            transaction_id, account_id, order_id, amount, currency, description
        ) VALUES (
            v_tx_id, p_equity_account_id, v_order_id, p_fee, 'INR',
            format('Brokerage & exchange regulatory fee for %s', p_symbol)
        );
    END IF;

    RETURN v_order_id;
END;
$$ LANGUAGE plpgsql;
