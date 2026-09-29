-- ============================================================================
-- FINANCIAL TRADING PLATFORM: TIMESCALEDB PRODUCTION DDL
-- Historical EOD Candles, Portfolio Net-Worth Snapshots, Columnar Compression
-- & Continuous Aggregate Rollups
-- ============================================================================

CREATE EXTENSION IF NOT EXISTS timescaledb CASCADE;

-- ----------------------------------------------------------------------------
-- 1. HISTORICAL STOCK EOD (END-OF-DAY) CANDLES
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS stock_eod_candles (
    time TIMESTAMPTZ NOT NULL,
    symbol VARCHAR(32) NOT NULL,
    exchange VARCHAR(16) NOT NULL DEFAULT 'NSE',
    open NUMERIC(14, 4) NOT NULL,
    high NUMERIC(14, 4) NOT NULL,
    low NUMERIC(14, 4) NOT NULL,
    close NUMERIC(14, 4) NOT NULL,
    volume BIGINT NOT NULL,
    adjusted_close NUMERIC(14, 4),
    vwap NUMERIC(14, 4),
    corporate_action_notes VARCHAR(128),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (time, symbol, exchange)
);

-- Convert to TimescaleDB Hypertable with 1-month chunk interval
SELECT create_hypertable(
    'stock_eod_candles',
    by_range('time', INTERVAL '1 month'),
    if_not_exists => TRUE
);

-- Compound index for fast single-stock historical range scans
CREATE INDEX IF NOT EXISTS idx_stock_eod_symbol_time 
    ON stock_eod_candles (symbol, exchange, time DESC);

-- Enable Native Columnar Compression for EOD price bars
-- Group by symbol and exchange so chunks compress each equity's time series together
ALTER TABLE stock_eod_candles SET (
    timescaledb.compress,
    timescaledb.compress_segmentby = 'symbol, exchange',
    timescaledb.compress_orderby = 'time DESC'
);

-- Automated compression policy: Compress chunks older than 30 days
-- Provides ~90% disk space reduction while maintaining fast historical analytics
SELECT add_compression_policy('stock_eod_candles', INTERVAL '30 days', if_not_exists => TRUE);

-- ----------------------------------------------------------------------------
-- 2. HISTORICAL USER PORTFOLIO NET-WORTH SNAPSHOTS
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS user_networth_snapshots (
    time TIMESTAMPTZ NOT NULL,
    user_id UUID NOT NULL,
    cash_balance NUMERIC(18, 4) NOT NULL,
    invested_equity NUMERIC(18, 4) NOT NULL,
    invested_debt NUMERIC(18, 4) NOT NULL DEFAULT 0,
    total_net_worth NUMERIC(18, 4) NOT NULL,
    day_gain_loss NUMERIC(18, 4) NOT NULL DEFAULT 0,
    day_gain_loss_percent NUMERIC(8, 4) NOT NULL DEFAULT 0,
    currency VARCHAR(10) NOT NULL DEFAULT 'INR',
    PRIMARY KEY (time, user_id)
);

-- Convert to Hypertable with 3-month chunk interval
SELECT create_hypertable(
    'user_networth_snapshots',
    by_range('time', INTERVAL '3 months'),
    if_not_exists => TRUE
);

CREATE INDEX IF NOT EXISTS idx_user_networth_user_time 
    ON user_networth_snapshots (user_id, time DESC);

-- Enable Columnar Compression for User Net Worth Snapshots
ALTER TABLE user_networth_snapshots SET (
    timescaledb.compress,
    timescaledb.compress_segmentby = 'user_id',
    timescaledb.compress_orderby = 'time DESC'
);

-- Automated compression policy: Compress chunks older than 60 days
SELECT add_compression_policy('user_networth_snapshots', INTERVAL '60 days', if_not_exists => TRUE);

-- ----------------------------------------------------------------------------
-- 3. CONTINUOUS AGGREGATES: WEEKLY & MONTHLY ROLLUP CANDLES
-- ----------------------------------------------------------------------------

-- Weekly Candlestick Materialized Rollup
CREATE MATERIALIZED VIEW IF NOT EXISTS stock_weekly_candles
WITH (timescaledb.continuous) AS
SELECT
    time_bucket('1 week', time) AS bucket,
    symbol,
    exchange,
    first(open, time) AS open,
    max(high) AS high,
    min(low) AS low,
    last(close, time) AS close,
    sum(volume) AS volume
FROM stock_eod_candles
GROUP BY bucket, symbol, exchange
WITH NO DATA;

-- Schedule automatic refresh for weekly rollup
SELECT add_continuous_aggregate_policy('stock_weekly_candles',
    start_offset => INTERVAL '3 months',
    end_offset => INTERVAL '1 day',
    schedule_interval => INTERVAL '1 day',
    if_not_exists => TRUE
);

-- Monthly Candlestick Materialized Rollup
CREATE MATERIALIZED VIEW IF NOT EXISTS stock_monthly_candles
WITH (timescaledb.continuous) AS
SELECT
    time_bucket('1 month', time) AS bucket,
    symbol,
    exchange,
    first(open, time) AS open,
    max(high) AS high,
    min(low) AS low,
    last(close, time) AS close,
    sum(volume) AS volume
FROM stock_eod_candles
GROUP BY bucket, symbol, exchange
WITH NO DATA;

SELECT add_continuous_aggregate_policy('stock_monthly_candles',
    start_offset => INTERVAL '1 year',
    end_offset => INTERVAL '1 day',
    schedule_interval => INTERVAL '1 day',
    if_not_exists => TRUE
);

-- ----------------------------------------------------------------------------
-- 4. FINANCIAL ANALYTICS QUERIES
-- ----------------------------------------------------------------------------

-- A. Portfolio Maximum Drawdown Calculation over Past Year
CREATE OR REPLACE FUNCTION get_user_portfolio_drawdown(p_user_id UUID)
RETURNS TABLE (
    peak_net_worth NUMERIC(18, 4),
    trough_net_worth NUMERIC(18, 4),
    max_drawdown_amount NUMERIC(18, 4),
    max_drawdown_percent NUMERIC(8, 4)
) AS $$
BEGIN
    RETURN QUERY
    WITH ranked_networth AS (
        SELECT 
            time,
            total_net_worth,
            MAX(total_net_worth) OVER (ORDER BY time ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW) AS rolling_peak
        FROM user_networth_snapshots
        WHERE user_id = p_user_id
          AND time >= NOW() - INTERVAL '1 year'
    ),
    drawdowns AS (
        SELECT
            rolling_peak,
            total_net_worth,
            (rolling_peak - total_net_worth) AS drawdown,
            CASE WHEN rolling_peak > 0 THEN ((rolling_peak - total_net_worth) / rolling_peak) * 100 ELSE 0 END AS dd_pct
        FROM ranked_networth
    )
    SELECT 
        rolling_peak,
        total_net_worth,
        drawdown,
        dd_pct
    FROM drawdowns
    ORDER BY dd_pct DESC
    LIMIT 1;
END;
$$ LANGUAGE plpgsql;

-- B. Chunk Compression Status & Disk Savings Inspector
CREATE OR REPLACE VIEW view_timescaledb_savings AS
SELECT
    hypertable_name,
    uncompressed_total_bytes / (1024 * 1024) AS uncompressed_mb,
    compressed_total_bytes / (1024 * 1024) AS compressed_mb,
    ROUND((1.0 - (compressed_total_bytes::numeric / NULLIF(uncompressed_total_bytes, 0))) * 100, 2) AS compression_ratio_pct
FROM timescaledb_information.compression_settings;
