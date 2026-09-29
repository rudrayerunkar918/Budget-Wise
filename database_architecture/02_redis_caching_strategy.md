# Redis Write-Through Market Data Cache & Rate-Limit Shield

## 1. Overview & Architecture

Third-party financial APIs (Alpha Vantage, Finnhub, Polygon) enforce strict request quotas (e.g. 5–75 requests/minute on standard tiers). If user requests directly trigger upstream API calls (standard Cache-Aside / Lazy Loading), sudden market moves will cause thousands of concurrent users to request the same symbols (`TATAMOTORS`, `RELIANCE`, `AAPL`), instantly exhausting API keys and triggering `429 Too Many Requests` cascade failures (the **Thundering Herd** problem).

To achieve 100% rate-limit immunity with deterministic `<5ms` response times, this architecture employs a **Proactive Write-Through Background Ingestion Pipeline**:

```
                       ┌──────────────────────────────────────────────┐
                       │          Client Users (Mobile / Web)         │
                       └──────────────────────┬───────────────────────┘
                                              │ GET /api/quotes?symbol=TATAMOTORS
                                              │ (Always hits Redis, never upstream)
                                              ▼
                       ┌──────────────────────────────────────────────┐
                       │               API Gateway                    │
                       └──────────────────────┬───────────────────────┘
                                              │ HGETALL quote:NSE:TATAMOTORS
                                              ▼
                                   ┌──────────────────────┐
                                   │      REDIS NODE      │
                                   │   (In-Memory Cache)  │
                                   └──────────▲───────────┘
                                              │
                    ┌─────────────────────────┴────────────────────────┐
                    │  Proactive Write-Through Ingestion Workers       │
                    │  (Celery / BullMQ / Go Daemon Pool)              │
                    └─────────────────────────▲────────────────────────┘
                                              │ Regulated Polling with
                                              │ Token Bucket Rate Limiter
                                              ▼
                               ┌──────────────────────────────┐
                               │  External Stock APIs         │
                               │  (Alpha Vantage / Finnhub)   │
                               └──────────────────────────────┘
```

---

## 2. Redis Key Hierarchy & Data Types

| Key Pattern | Redis Type | Memory Footprint | TTL | Description |
| :--- | :--- | :--- | :--- | :--- |
| `quote:{exchange}:{symbol}` | `HASH` | ~320 bytes | 180s | Normalized live quote metrics (LTP, high, low, open, close, volume) |
| `active_symbols` | `SET` | ~20 bytes / member | None | Union of all symbols in user watchlists and open limit orders |
| `ratelimit:vendor:{vendor_id}` | `STRING` | ~64 bytes | 60s | Sliding window token counter for upstream API calls |
| `market_status:{exchange}` | `STRING` | ~32 bytes | 300s | Current market state (`OPEN`, `PRE_OPEN`, `POST_CLOSE`, `HOLIDAY`) |
| `channel:orders:{user_id}` | `Pub/Sub` | Ephemeral | N/A | Real-time order fill and balance update event stream |

### Example Hash Structure: `quote:NSE:TATAMOTORS`

```redis
HSET quote:NSE:TATAMOTORS \
    symbol "TATAMOTORS" \
    exchange "NSE" \
    ltp "441.50" \
    open "438.00" \
    high "445.20" \
    low "436.80" \
    prev_close "437.90" \
    change "3.60" \
    change_percent "0.82" \
    volume "14280920" \
    source "ALPHA_VANTAGE" \
    updated_at "1790666400"
```

---

## 3. Atomic Sliding-Window Rate Limiter (Lua Script)

To guarantee the worker cluster never exceeds vendor rate limits across distributed worker instances, all upstream calls must acquire a token via an atomic Redis Lua script:

```lua
-- KEYS[1]: rate_limit key (e.g., "ratelimit:vendor:alphavantage")
-- ARGV[1]: max_calls_per_window (e.g., 5 or 75)
-- ARGV[2]: window_seconds (e.g., 60)
-- ARGV[3]: current_timestamp_ms

local key = KEYS[1]
local max_calls = tonumber(ARGV[1])
local window = tonumber(ARGV[2])
local now = tonumber(ARGV[3])
local clear_before = now - (window * 1000)

-- Remove expired timestamps outside the rolling window
redis.call('ZREMRANGEBYSCORE', key, '-inf', clear_before)

-- Count remaining requests in the active window
local current_requests = redis.call('ZCARD', key)

if current_requests < max_calls then
    -- Record this request with timestamp as both member and score
    redis.call('ZADD', key, now, now .. '-' .. math.random(1000, 9999))
    redis.call('EXPIRE', key, window + 1)
    return 1 -- Token granted!
else
    return 0 -- Rate limit reached, backoff required
end
```

---

## 4. Proactive Polling Worker Implementation (Python Blueprint)

```python
import time
import redis
import requests

r = redis.Redis(host="localhost", port=6379, decode_responses=True)

ALPHA_VANTAGE_API_KEY = "YOUR_API_KEY"
RATE_LIMIT_LUA = """... (Lua script above) ..."""
rate_limit_sha = r.script_load(RATE_LIMIT_LUA)

def acquire_api_token(vendor="alphavantage", max_rpm=75):
    """Atomically acquires permission to make an upstream API call."""
    now_ms = int(time.time() * 1000)
    result = r.evalsha(rate_limit_sha, 1, f"ratelimit:vendor:{vendor}", max_rpm, 60, now_ms)
    return result == 1

def run_proactive_quote_worker():
    """Continuously refreshes quotes for all symbols registered in active_symbols."""
    print("Starting Proactive Stock Ingestion Worker...")
    
    while True:
        # 1. Fetch active symbols required by currently logged-in users & watchlists
        symbols = r.smembers("active_symbols")
        if not symbols:
            time.sleep(2)
            continue
            
        for symbol_entry in symbols:
            # symbol_entry format: "NSE:TATAMOTORS"
            exchange, symbol = symbol_entry.split(":")
            
            # 2. Acquire rate-limiting token before dispatching
            while not acquire_api_token("alphavantage", max_rpm=75):
                time.sleep(0.5)  # Backoff until rolling window clears
                
            # 3. Call Upstream Provider
            url = f"https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol={symbol}.{exchange}&apikey={ALPHA_VANTAGE_API_KEY}"
            try:
                resp = requests.get(url, timeout=5)
                data = resp.json().get("Global Quote", {})
                
                if data and "05. price" in data:
                    pipe = r.pipeline()
                    cache_key = f"quote:{exchange}:{symbol}"
                    pipe.hset(cache_key, mapping={
                        "symbol": symbol,
                        "exchange": exchange,
                        "ltp": data.get("05. price", "0.0"),
                        "open": data.get("02. open", "0.0"),
                        "high": data.get("03. high", "0.0"),
                        "low": data.get("04. low", "0.0"),
                        "prev_close": data.get("08. previous close", "0.0"),
                        "change_percent": data.get("10. change percent", "0%"),
                        "volume": data.get("06. volume", "0"),
                        "updated_at": str(int(time.time())),
                        "source": "ALPHA_VANTAGE"
                    })
                    pipe.expire(cache_key, 180)  # 3-minute grace period
                    pipe.execute()
                    print(f"Cached {exchange}:{symbol} -> LTP {data.get('05. price')}")
            except Exception as e:
                print(f"Worker error fetching {symbol}: {e}")
                
        time.sleep(1)

if __name__ == "__main__":
    run_proactive_quote_worker()
```
