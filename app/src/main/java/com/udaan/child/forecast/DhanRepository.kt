package com.udaan.child.forecast

import com.udaan.child.CrashHandler
import com.udaan.child.data.model.DailyBar
import com.udaan.child.data.repository.InstrumentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.time.LocalDate

class DhanRepository {
    private val client = OkHttpClient()
    private val JSON = "application/json".toMediaType()

    suspend fun fetchCandles(
        token: String,
        symbol: String,
        timeframe: String,
        asOfDate: String
    ): List<DailyBar> = withContext(Dispatchers.IO) {
        val inst = InstrumentRepository.find(symbol)
        if (inst == null) {
            CrashHandler.sendLog("NOT FOUND: $symbol")
            return@withContext emptyList()
        }

        val isDaily = timeframe == "1D" || timeframe == "D"
        val url = "https://api.dhan.co/v2/charts/" + if (isDaily) "historical" else "intraday"
        val end = LocalDate.parse(asOfDate)
        val start = if (isDaily) end.minusDays(365) else end.minusDays(5)

        val jsonBody = JSONObject().apply {
            put("securityId", inst.securityId)
            put("exchangeSegment", inst.exchangeSegment)
            put("instrument", inst.instrument)
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

        val request = Request.Builder()
            .url(url)
            .addHeader("access-token", token)
            .post(jsonBody.toRequestBody(JSON))
            .build()

        try {
            val resp = client.newCall(request).execute()
            val text = resp.body?.string() ?: ""
            if (!resp.isSuccessful) return@withContext emptyList()

            val json = JSONObject(text)
            val open = json.optJSONArray("open") ?: return@withContext emptyList()
            val high = json.optJSONArray("high") ?: return@withContext emptyList()
            val low = json.optJSONArray("low") ?: return@withContext emptyList()
            val close = json.optJSONArray("close") ?: return@withContext emptyList()
            val vol = json.optJSONArray("volume")
            val ts = json.optJSONArray("timestamp")

            (0 until close.length()).map { i ->
                val t = ts?.optLong(i) ?: 0L
                val date = if (t > 0) {
                    java.time.Instant.ofEpochMilli(if (t < 1e12) t * 1000 else t)
                        .atZone(java.time.ZoneId.of("Asia/Kolkata"))
                        .toLocalDateTime().toString()
                } else ""
                DailyBar(date, t, open.optDouble(i), high.optDouble(i), low.optDouble(i), close.optDouble(i), vol?.optLong(i) ?: 0)
            }
        } catch (e: Exception) {
            CrashHandler.sendLog("EX: " + e.javaClass.simpleName)
            emptyList()
        }
    }
}
