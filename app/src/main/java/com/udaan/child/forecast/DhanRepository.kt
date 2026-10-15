package com.udaan.child.forecast

import com.dhanhq.sdk.DhanClient
import com.dhanhq.sdk.models.IntradayMinuteDataRequest
import com.dhanhq.sdk.models.IntradayMinuteDataResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromString
import kotlinx.serialization.json.ignoreUnknownKeys

class DhanRepository(private val clientId: String, private val accessToken: String) {

    private val dhanClient = DhanClient(clientId, accessToken)

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Fetches the last [limit] 1‑minute candles for [securityId] (exchange‑segment format, e.g. "NSE_EQ|INE12345").
     */
    suspend fun fetchIntradayCandles(
        securityId: String,
        limit: Int = 200
    ): List<Candle> = withContext(Dispatchers.IO) {
        val request = IntradayMinuteDataRequest(
            securityId = securityId,
            // The SDK expects a "from" timestamp – we ask for the last `limit` minutes.
            from = System.currentTimeMillis() - (limit * 60_000L)
        )

        val response: IntradayMinuteDataResponse = dhanClient.intradayMinuteData(request)

        // The SDK returns a JSON string in `response.data`.  Parse it with kotlinx‑serialization.
        val rawJson = response.data
        val typedList = json.decodeFromString<List<Candle>>(rawJson)

        // The SDK already orders newest‑first; we need oldest‑first for the engine.
        typedList.sortedBy { it.timestamp }.takeLast(limit)
    }
}
