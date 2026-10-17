
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
import java.time.Instant
import java.time.ZoneId

class DhanRepository {

    private val client = OkHttpClient()
    private val jsonMediaType = "application/json".toMediaType()

    suspend fun fetchCandles(
        clientId: String,
        token: String,
        symbol: String,
        timeframe: String,
        asOfDate: String
    ): List<DailyBar> = withContext(Dispatchers.IO) {

        if (clientId.isBlank() || token.isBlank()) {
            CrashHandler.sendLog("DHAN AUTH: Missing credentials")
            return@withContext emptyList()
        }

        val inst = InstrumentRepository.find(symbol)
        if (inst == null) {
            CrashHandler.sendLog("DHAN: Instrument not found")
            return@withContext emptyList()
        }

        try {
            val isDaily = timeframe == "1D" || timeframe == "D"
            val endpoint = if (isDaily) "historical" else "intraday"
            val url = "https://api.dhan.co/v2/charts/$endpoint"

            val end = LocalDate.parse(asOfDate)
            val start = if (isDaily) {
                end.minusDays(365)
            } else {
                end.minusDays(5)
            }

            val interval = when (timeframe) {
                "1m", "2m", "3m" -> "1"
                "5m" -> "5"
                "10m" -> "10"
                "15m" -> "15"
                "30m" -> "30"
                "1H" -> "60"
                "2H" -> "120"
                "4H" -> "240"
                else -> "5"
            }

            val jsonBody = JSONObject().apply {
                put("securityId", inst.securityId)
                put("exchangeSegment", inst.exchangeSegment)
                put("instrument", inst.instrument)
                put("expiryCode", 0)
                put("oi", false)
                put("fromDate", start.toString())
                put("toDate", end.plusDays(1).toString())

                if (!isDaily) {
                    put("interval", interval)
                }
            }.toString()

            val request = Request.Builder()
                .url(url)
                .header("client-id", clientId.trim())
                .header("access-token", token.trim())
                .header("Content-Type", "application/json")
                .post(jsonBody.toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->

                val responseText = response.body?.string().orEmpty()

                if (!response.isSuccessful) {
                    CrashHandler.sendLog(
                        "DHAN HTTP ERROR: ${response.code}"
                    )
                    return@withContext emptyList()
                }

                val json = JSONObject(responseText)

                val open = json.optJSONArray("open")
                    ?: return@withContext emptyList()
                val high = json.optJSONArray("high")
                    ?: return@withContext emptyList()
                val low = json.optJSONArray("low")
                    ?: return@withContext emptyList()
                val close = json.optJSONArray("close")
                    ?: return@withContext emptyList()

                val volume = json.optJSONArray("volume")
                val timestamps = json.optJSONArray("timestamp")

                (0 until close.length()).mapNotNull { i ->
                    if (
                        i >= open.length() ||
                        i >= high.length() ||
                        i >= low.length()
                    ) {
                        return@mapNotNull null
                    }

                    val timestamp = timestamps?.optLong(i) ?: 0L

                    val date = if (timestamp > 0L) {
                        val epochMillis = if (timestamp < 1_000_000_000_000L) {
                            timestamp * 1000L
                        } else {
                            timestamp
                        }

                        Instant.ofEpochMilli(epochMillis)
                            .atZone(ZoneId.of("Asia/Kolkata"))
                            .toLocalDateTime()
                            .toString()
                    } else {
                        ""
                    }

                    DailyBar(
                        date,
                        timestamp,
                        open.optDouble(i),
                        high.optDouble(i),
                        low.optDouble(i),
                        close.optDouble(i),
                        volume?.optLong(i) ?: 0L
                    )
                }
            }
        } catch (e: Exception) {
            CrashHandler.sendLog(
                "DHAN EXCEPTION: ${e.javaClass.simpleName}"
            )
            emptyList()
        }
    }
}
