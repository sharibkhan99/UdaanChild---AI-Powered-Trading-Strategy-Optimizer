package com.udaan.child.data.repository

import com.udaan.child.data.model.Instrument
import okhttp3.OkHttpClient
import okhttp3.Request

object InstrumentRepository {
    private const val CSV_URL = "https://images.dhan.co/api-data/api-scrip-master.csv"
    private val client = OkHttpClient()
    private var cached: List<Instrument> = emptyList()

    fun load(): List<Instrument> {
        if (cached.isNotEmpty()) return cached
        return try {
            val req = Request.Builder().url(CSV_URL).build()
            client.newCall(req).execute().use { resp ->
                val text = resp.body?.string() ?: return emptyList()
                cached = text.lines().drop(1).mapNotNull { line ->
                    val cols = line.split(",")
                    if (cols.size < 8) return@mapNotNull null
                    val segment = cols.getOrNull(1)?.trim() ?: ""
                    if (segment != "NSE_EQ" && segment != "IDX_I") return@mapNotNull null
                    Instrument(
                        securityId = cols[0].trim(),
                        tradingSymbol = cols.getOrNull(5)?.trim() ?: "",
                        displayName = cols.getOrNull(7)?.trim() ?: "",
                        exchangeSegment = segment,
                        instrument = cols.getOrNull(4)?.trim() ?: ""
                    )
                }
                cached
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun find(symbol: String): Instrument? =
        load().find { it.tradingSymbol.equals(symbol, true) }
}
