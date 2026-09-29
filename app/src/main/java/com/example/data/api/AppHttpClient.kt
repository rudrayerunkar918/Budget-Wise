package com.example.data.api

import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * Shared, connection-pooled OkHttpClient singleton to prevent connection leaks,
 * reduce latency, and enable HTTP/2 connection reuse across all network calls.
 */
object AppHttpClient {
  val client: OkHttpClient by lazy {
    OkHttpClient.Builder()
      .connectionPool(ConnectionPool(5, 5, TimeUnit.MINUTES))
      .connectTimeout(30, TimeUnit.SECONDS)
      .readTimeout(45, TimeUnit.SECONDS)
      .writeTimeout(30, TimeUnit.SECONDS)
      .retryOnConnectionFailure(true)
      .build()
  }
}
