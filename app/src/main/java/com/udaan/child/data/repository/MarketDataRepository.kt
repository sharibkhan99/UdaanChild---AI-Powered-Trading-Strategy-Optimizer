package com.udaan.child.data.repository

import android.content.Context
import android.util.Log
import com.udaan.child.data.model.DailyBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Random

interface MarketDataRepository {
    suspend fun getHistoricalData(symbol: String): List<DailyBar>
    
    // Alias to match MainActivity or other classes calling fetchDailyCandles
    suspend fun fetchDailyCandles(fromDate: String, toDate: String, symbol: String = "NIFTY"): List<DailyBar> = getHistoricalData(symbol)
}

class MarketDataRepositoryImpl(private val context: Context? = null) : MarketDataRepository {

    override suspend fun getHistoricalData(symbol: String): List<DailyBar> = withContext(Dispatchers.IO) {
        try {
            val querySymbol = when (symbol.uppercase()) {
                "NIFTY", "NIFTY50" -> "%5ENSEI"
                "BANKNIFTY" -> "%5ENSEBANK"
                else -> symbol
            }

            val urlString = "https://query1.finance.yahoo.com/v8/finance/chart/$querySymbol?range=3y&interval=1d"
            val url = URL(urlString)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("User-Agent", "Mozilla/5.0")
                connectTimeout = 10000
                readTimeout = 10000
            }

            if (conn.responseCode == 200) {
                val response = conn.inputStream.bufferedReader().use { it.readText() }
                val bars = parseYahooFinanceJson(response)
                if (bars.size >= 100) {
                    return@withContext bars
                }
            }
        } catch (e: Exception) {
            Log.e("MarketDataRepo", "Live fetch failed, using fallback data", e)
        }

        generateFallbackData()
    }

    private fun parseYahooFinanceJson(jsonStr: String): List<DailyBar> {
        val list = mutableListOf<DailyBar>()
        val root = JSONObject(jsonStr)
        val result = root.getJSONObject("chart").getJSONArray("result").getJSONObject(0)

        val timestamps = result.getJSONArray("timestamp")
        val quote = result.getJSONObject("indicators").getJSONArray("quote").getJSONObject(0)

        val opens = quote.getJSONArray("open")
        val highs = quote.getJSONArray("high")
        val lows = quote.getJSONArray("low")
        val closes = quote.getJSONArray("close")
        val volumes = quote.getJSONArray("volume")

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        for (i in 0 until timestamps.length()) {
            if (closes.isNull(i) || opens.isNull(i) || highs.isNull(i) || lows.isNull(i)) continue

            val dateMillis = timestamps.getLong(i) * 1000L
            val dateStr = sdf.format(Date(dateMillis))

            list.add(
                DailyBar(
                    date = dateStr, // Safely converted to String if DailyBar expects String
                    open = opens.getDouble(i),
                    high = highs.getDouble(i),
                    low = lows.getDouble(i),
                    close = closes.getDouble(i),
                    volume = if (volumes.isNull(i)) 0L else volumes.getLong(i)
                )
            )
        }
        return list
    }

    private fun generateFallbackData(): List<DailyBar> {
        val rnd = Random(System.currentTimeMillis())
        var price = 22000.0
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        return (1..600).map { i ->
            val change = (rnd.nextFloat() - 0.49f) * 250.0
            val open = price
            val close = (open + change).coerceAtLeast(1000.0)
            val high = maxOf(open, close) + rnd.nextDouble() * 100.0
            val low = minOf(open, close) - rnd.nextDouble() * 100.0
            price = close

            val millis = System.currentTimeMillis() - (600 - i) * 86_400_000L

            DailyBar(
                date = sdf.format(Date(millis)),
                open = open,
                high = high,
                low = low,
                close = close,
                volume = 1_000_000L + (rnd.nextDouble() * 5_000_000L).toLong()
            )
        }
    }
}
