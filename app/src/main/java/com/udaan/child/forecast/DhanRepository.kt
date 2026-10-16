package com.udaan.child.forecast

import android.util.Log
import com.udaan.child.data.model.DailyBar
import com.udaan.child.data.repository.InstrumentRepository
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.time.LocalDate

class DhanRepository {
    private val client = OkHttpClient()
    private val JSON = "application/json".toMediaType()
    private val TAG = "DhanRepo"

    suspend fun fetchCandles(
        token: String,
        symbol: String,
        timeframe: String,
        asOfDate: String
    ): List<DailyBar> {
        Log.d(TAG, "Searching for: $symbol")
        val inst = InstrumentRepository.find(symbol)
        if (inst == null) {
            Log.e(TAG, "Instrument not found for: $symbol")
            // Fallback for NIFTY
            if (symbol.equals("NIFTY", true)) {
                return fetchWithId(token, "13", "IDX_I", "INDEX", timeframe, asOfDate)
            }
            return emptyList()
        }
        Log.d(TAG, "Found: ${inst.securityId} ${inst.exchangeSegment}")
        return fetchWithId(token, inst.securityId, inst.exchangeSegment, inst.instrument, timeframe, asOfDate)
    }

    private fun fetchWithId(
        token: String,
        securityId: String,
        segment: String,
        instrument: String,
        timeframe: String,
        asOfDate: String
    ): List<DailyBar> {
        val isDaily = timeframe == "1D" || timeframe == "D"
        val url = "https://api.dhan.co/v2/charts/${if (isDaily) "historical" else "intraday"}"

        val end = LocalDate.parse(asOfDate)
        val start = if (isDaily) end.minusDays(30) else end.minusDays(2)

        val body = JSONObject().apply {
            put("securityId", securityId)
            put("exchangeSegment", segment)
            put("instrument", instrument)
            put("expiryCode", 0)
            put("oi", false)
            put("fromDate", start.toString())
            put("toDate", end.plusDays(1).toString())
            if (!isDaily) {
                put("interval", when (timeframe) {
                    "1m", "2m", "3m" -> "1"
                    "5m" -> "5"
                    "15m" -> "15"
                    "1H" -> "60"
                    else -> "5"
                })
            }
        }.toString()

        Log.d(TAG, "Request: $body")

        val request = Request.Builder()
            .url(url)
            .addHeader("access-token", token)
            .post(body.toRequestBody(JSON))
            .build()

        return try {
            val resp = client.newCall(request).execute()
            val bodyStr = resp.body?.string() ?: ""
            Log.d(TAG, "Response ${resp.code}: ${bodyStr.take(200)}")
            if (!resp.isSuccessful) return emptyList()
            val json = JSONObject(bodyStr)
            val open = json.optJSONArray("open") ?: return emptyList()
            val high = json.optJSONArray("high") ?: return emptyList()
            val low = json.optJSONArray("low") ?: return emptyList()
            val close = json.optJSONArray("close") ?: return emptyList()
            val vol = json.optJSONArray("volume")
            (0 until open.length()).map { i ->
                DailyBar("", open.optDouble(i), high.optDouble(i), low.optDouble(i), close.optDouble(i), vol?.optLong(i) ?: 0)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error: ${e.message}")
            emptyList()
        }
    }
}
