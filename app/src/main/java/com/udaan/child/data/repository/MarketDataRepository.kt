package com.udaan.child.data.repository

import android.content.Context
import android.util.Log
import com.udaan.child.data.model.DailyBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Random

interface MarketDataRepository {
    suspend fun getHistoricalData(symbol: String): List<DailyBar>
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
                if (bars.size >= 500) {
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

        for (i in 0 until timestamps.length()) {
            if (closes.isNull(i) || opens.isNull(i) || highs.isNull(i) || lows.isNull(i)) continue

            val dateMillis = timestamps.getLong(i) * 1000L
            list.add(
                DailyBar(
                    date = dateMillis,
                    open = opens.getDouble(i).toFloat(),
                    high = highs.getDouble(i).toFloat(),
                    low = lows.getDouble(i).toFloat(),
                    close = closes.getDouble(i).toFloat(),
                    volume = if (volumes.isNull(i)) 0f else volumes.getDouble(i).toFloat()
                )
            )
        }
        return list
    }

    private fun generateFallbackData(): List<DailyBar> {
        val rnd = Random(System.currentTimeMillis())
        var price = 22000f
        return (1..600).map { i ->
            val change = (rnd.nextFloat() - 0.49f) * 250f
            val open = price
            val close = (open + change).coerceAtLeast(1000f)
            val high = maxOf(open, close) + rnd.nextFloat() * 100f
            val low = minOf(open, close) - rnd.nextFloat() * 100f
            price = close

            DailyBar(
                date = System.currentTimeMillis() - (600 - i) * 86_400_000L,
                open = open,
                high = high,
                low = low,
                close = close,
                volume = 1_000_000f + rnd.nextFloat() * 5_000_000f
            )
        }
    }
}
