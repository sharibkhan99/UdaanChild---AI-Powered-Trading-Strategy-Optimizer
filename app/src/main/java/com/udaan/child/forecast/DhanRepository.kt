// app/src/main/java/com/udaan/child/forecast/DhanRepository.kt
package com.udaan.child.forecast

import com.dhanhq.DhanClient
import com.dhanhq.model.intraday.IntradayMinuteDataRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class DhanRepository private constructor(
    private val client: DhanClient,
    private val json: Json = Json { ignoreUnknownKeys = true }
) {

    companion object {
        @Volatile private var INSTANCE: DhanRepository? = null
        fun getInstance(clientId: String, accessToken: String): DhanRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: DhanRepository(DhanClient(clientId, accessToken)).also { INSTANCE = it }
            }
    }

    /** Supported granularities – map to Dhan’s `interval` string */
    enum class Granularity(val dhanInterval: String, val label: String) {
        MIN_1   ("1",  "1 min"),
        MIN_2   ("2",  "2 min"),
        MIN_3   ("3",  "3 min"),
        MIN_5   ("5",  "5 min"),
        HOUR_1  ("60", "1 hour"),
        DAY_1   ("D",  "1 day")
    }

    /**
     * Pull the **last `limit` closed candles** for a given `securityId` & `exchange`.
     * Returns a **chronologically sorted** list (oldest → newest).
     */
    suspend fun fetchCandles(
        securityId: String,
        exchange: String = "NSE",
        granularity: Granularity = Granularity.MIN_5,
        limit: Int = 500               // enough for a stable Kalman state
    ): Result<List<Candle>> = withContext(Dispatchers.IO) {
        try {
            val request = IntradayMinuteDataRequest(
                securityId = securityId,
                exchangeSegment = exchange,
                interval = granularity.dhanInterval
            )
            // The SDK returns a JSON string – we deserialize ourselves
            val rawJson = client.intradayMinuteData(request)   // <-- blocking network call
            val wrapper = json.decodeFromString<DhanCandleResponse>(rawJson)

            // Dhan returns newest‑first → reverse
            val candles = wrapper.data
                .map { Candle(it.timestamp, it.open, it.high, it.low, it.close, it.volume) }
                .sortedBy { it.timestamp }
                .takeLast(limit)

            Result.success(candles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Minimal DTO that matches Dhan’s intraday response */
    @kotlinx.serialization.Serializable
    private data class DhanCandleResponse(
        val data: List<DhanRawCandle>
    )
    @kotlinx.serialization.Serializable
    private data class DhanRawCandle(
        val timestamp: Long,
        val open: Double,
        val high: Double,
        val low: Double,
        val close: Double,
        val volume: Long
    )
}
