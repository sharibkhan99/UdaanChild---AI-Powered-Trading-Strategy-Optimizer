package com.udaan.child.forecast

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

    suspend fun fetchCandles(
        token: String,
        symbol: String,
        timeframe: String,
        asOfDate: String
    ): List<DailyBar> {
        val inst = InstrumentRepository.find(symbol) ?: return emptyList()
        val isDaily = timeframe == "1D" || timeframe == "D"
        val url = "https://api.dhan.co/v2/charts/${if (isDaily) "historical" else "intraday"}"

        val end = LocalDate.parse(asOfDate)
        val start = if (isDaily) end.minusDays(365) else end.minusDays(5)

        val body = JSONObject().apply {
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
            .post(body.toRequestBody(JSON))
            .build()

        return try {
            client.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) return emptyList()
                val json = JSONObject(resp.body?.string() ?: return emptyList())
                val open = json.optJSONArray("open") ?: return emptyList()
                val high = json.optJSONArray("high") ?: return emptyList()
                val low = json.optJSONArray("low") ?: return emptyList()
                val close = json.optJSONArray("close") ?: return emptyList()
                val vol = json.optJSONArray("volume")
                (0 until open.length()).map { i ->
                    DailyBar("", open.optDouble(i), high.optDouble(i), low.optDouble(i), close.optDouble(i), vol?.optLong(i) ?: 0)
                }
            }
        } catch (e: Exception) { emptyList() }
    }
}
