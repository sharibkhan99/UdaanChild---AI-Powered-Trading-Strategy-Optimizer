
package com.udaan.child.forecast

import com.udaan.child.data.model.DailyBar
import com.udaan.child.data.repository.InstrumentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

data class CandleFetchResult(
    val bars: List<DailyBar>,
    val error: String? = null
)

class DhanRepository {

    private val client = OkHttpClient()
    private val mediaType =
        "application/json; charset=utf-8".toMediaType()

    private val zone = ZoneId.of("Asia/Kolkata")

    private val dateFmt =
        DateTimeFormatter.ofPattern("yyyy-MM-dd")

    private val dateTimeFmt =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    suspend fun fetchCandles(
        clientId: String,
        token: String,
        symbol: String,
        timeframe: String,
        fromDate: String,
        toDate: String
    ): CandleFetchResult = withContext(Dispatchers.IO) {

        if (clientId.isBlank() || token.isBlank()) {
            return@withContext CandleFetchResult(
                emptyList(),
                "Client ID और Access Token भरें।"
            )
        }

        val inst = InstrumentRepository.find(symbol)
            ?: return@withContext CandleFetchResult(
                emptyList(),
                "Symbol नहीं मिला: $symbol"
            )

        try {
            val startDate = LocalDate.parse(fromDate.trim())
            val endDate = LocalDate.parse(toDate.trim())

            if (endDate.isBefore(startDate)) {
                return@withContext CandleFetchResult(
                    emptyList(),
                    "End date, start date से पहले नहीं हो सकती।"
                )
            }

            val daily = timeframe.equals("1D", true)
            val days = ChronoUnit.DAYS.between(startDate, endDate)

            if (!daily && days > 90) {
                return@withContext CandleFetchResult(
                    emptyList(),
                    "Intraday Dhan request अधिकतम 90 दिन की रखें। छोटी date range चुनें।"
                )
            }

            val apiInterval = apiIntervalFor(timeframe)
            val endpoint = if (daily) "historical" else "intraday"

            val from = if (daily) {
                startDate.format(dateFmt)
            } else {
                startDate.atTime(9, 15).format(dateTimeFmt)
            }

            val toDateExclusive = endDate.plusDays(1)

            val to = if (daily) {
                toDateExclusive.format(dateFmt)
            } else {
                toDateExclusive.atStartOfDay().format(dateTimeFmt)
            }

            val payload = JSONObject().apply {
                put("securityId", inst.securityId)
                put("exchangeSegment", inst.exchangeSegment)
                put("instrument", inst.instrument)
                put("expiryCode", 0)
                put("oi", false)
                put("fromDate", from)
                put("toDate", to)

                if (!daily) {
                    put("interval", apiInterval.toString())
                }
            }.toString()

            val request = Request.Builder()
                .url("https://api.dhan.co/v2/charts/$endpoint")
                .header("client-id", clientId.trim())
                .header("access-token", token.trim())
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .post(payload.toRequestBody(mediaType))
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()

                if (!response.isSuccessful) {
                    val detail = body.take(220)
                        .replace(Regex("[\\r\\n]+"), " ")

                    val message = when (response.code) {
                        401 ->
                            "Dhan HTTP 401: Access Token invalid/expired या credentials जाँचें."
                        403 ->
                            "Dhan HTTP 403: API access/permission या Client ID जाँचें."
                        429 ->
                            "Dhan HTTP 429: API rate limit; कुछ देर बाद फिर प्रयास करें."
                        else ->
                            "Dhan HTTP ${response.code}: $detail"
                    }

                    return@withContext CandleFetchResult(
                        emptyList(),
                        message
                    )
                }

                val root = JSONObject(body)

                val json = if (
                    root.has("data") &&
                    root.opt("data") is JSONObject
                ) {
                    root.getJSONObject("data")
                } else {
                    root
                }

                val opens = json.optJSONArray("open")
                    ?: return@withContext CandleFetchResult(
                        emptyList(),
                        "API response में OHLC data नहीं है: ${body.take(180)}"
                    )

                val highs = json.optJSONArray("high")
                val lows = json.optJSONArray("low")
                val closes = json.optJSONArray("close")
                val volumes = json.optJSONArray("volume")
                val timestamps = json.optJSONArray("timestamp")

                if (
                    highs == null ||
                    lows == null ||
                    closes == null ||
                    timestamps == null
                ) {
                    return@withContext CandleFetchResult(
                        emptyList(),
                        "Dhan response में जरूरी OHLC/timestamp fields नहीं मिले।"
                    )
                }

                val raw = (0 until closes.length()).mapNotNull { i ->
                    if (
                        i >= opens.length() ||
                        i >= highs.length() ||
                        i >= lows.length() ||
                        i >= timestamps.length()
                    ) {
                        return@mapNotNull null
                    }

                    val ts = timestamps.optLong(i)
                    if (ts <= 0L) return@mapNotNull null

                    val millis = if (ts < 1_000_000_000_000L) {
                        ts * 1000L
                    } else {
                        ts
                    }

                    val dt = Instant.ofEpochMilli(millis)
                        .atZone(zone)
                        .toLocalDateTime()

                    DailyBar(
                        date = dt.toString(),
                        timestamp = ts,
                        open = opens.optDouble(i),
                        high = highs.optDouble(i),
                        low = lows.optDouble(i),
                        close = closes.optDouble(i),
                        volume = volumes?.optLong(i) ?: 0L
                    )
                }.sortedBy { it.timestamp }

                if (raw.isEmpty()) {
                    return@withContext CandleFetchResult(
                        emptyList(),
                        "Dhan API ने कोई candle नहीं लौटाई। Date range, symbol और API access जाँचें।"
                    )
                }

                val output = if (daily) {
                    raw
                } else {
                    aggregate(raw, timeframeMinutes(timeframe))
                }

                CandleFetchResult(
                    output,
                    if (output.isEmpty()) {
                        "Selected timeframe के लिए candle नहीं बनी।"
                    } else {
                        null
                    }
                )
            }
        } catch (e: Exception) {
            CandleFetchResult(
                emptyList(),
                "Request error: ${e.javaClass.simpleName}: ${e.message ?: "unknown error"}"
            )
        }
    }

    // Dhan supports 1, 5, 15, 25 and 60-minute API candles.
    private fun apiIntervalFor(tf: String): Int {
        val requested = timeframeMinutes(tf)

        return listOf(60, 25, 15, 5, 1).first {
            base -> requested >= base && requested % base == 0
        }
    }

    private fun timeframeMinutes(tf: String): Int {
        val value = tf.trim().uppercase()

        if (value == "1D" || value == "D") return 1440

        Regex("^(\\d+)M$").matchEntire(value)?.let {
            return it.groupValues[1].toInt()
        }

        Regex("^(\\d+)H$").matchEntire(value)?.let {
            return it.groupValues[1].toInt() * 60
        }

        return 1
    }

    /** Aggregate candles aligned to NSE session open at 09:15 IST. */
    private fun aggregate(
        bars: List<DailyBar>,
        minutes: Int
    ): List<DailyBar> {
        if (minutes <= 1) return bars

        return bars.groupBy { bar ->
            val dt = LocalDateTime.parse(bar.date)
            val minuteOfDay = dt.hour * 60 + dt.minute
            val sessionOffset =
                (minuteOfDay - (9 * 60 + 15)).coerceAtLeast(0)

            val bucket = sessionOffset / minutes

            dt.toLocalDate().toString() + "#" + bucket
        }.values.mapNotNull { group ->

            val sorted = group.sortedBy { it.timestamp }

            if (sorted.isEmpty()) {
                null
            } else {
                val first = sorted.first()
                val last = sorted.last()

                first.copy(
                    high = sorted.maxOf { it.high },
                    low = sorted.minOf { it.low },
                    close = last.close,
                    volume = sorted.sumOf { it.volume }
                )
            }
        }.sortedBy { it.timestamp }
    }
}
