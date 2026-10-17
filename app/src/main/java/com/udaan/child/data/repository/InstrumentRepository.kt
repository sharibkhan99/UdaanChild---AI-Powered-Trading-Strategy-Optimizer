package com.udaan.child.data.repository

import com.udaan.child.data.model.Instrument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

object InstrumentRepository {
    private const val CSV_URL = "https://images.dhan.co/api-data/api-scrip-master.csv"
    private val client = OkHttpClient()
    private var cached: List<Instrument> = emptyList()

    suspend fun load() = withContext(Dispatchers.IO) {
        if (cached.isNotEmpty()) return@withContext
        try {
            val resp = client.newCall(Request.Builder().url(CSV_URL).build()).execute()
            val text = resp.body?.string() ?: return@withContext
            cached = text.lines().drop(1).mapNotNull { line ->
                val c = line.split(",")
                if (c.size < 14) return@mapNotNull null
                Instrument(
                    securityId = c[0].trim(),
                    exchangeSegment = c[1].trim(),
                    instrument = c[2].trim(),
                    tradingSymbol = c[5].trim(),
                    displayName = c[5].trim()
                )
            }
        } catch (_: Exception) {}
    }

    fun find(query: String): Instrument? {
        val q = query.uppercase().trim()
        if (q.isEmpty()) return null
        return cached.find { 
            it.tradingSymbol.equals(q, true) || 
            it.displayName.equals(q, true) ||
            it.tradingSymbol.contains(q, true) ||
            it.displayName.contains(q, true)
        }
    }

    fun search(query: String): List<Instrument> {
        val q = query.uppercase().trim()
        if (q.isEmpty()) return emptyList()
        return cached.filter { 
            it.tradingSymbol.contains(q, true) || 
            it.displayName.contains(q, true)
        }.take(20)
    }
}
