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

    // Hardcoded indices (CSV mein kabhi miss ho jaate hain)
    private val INDEX_MAP = mapOf(
        "NIFTY" to Instrument("13", "IDX_I", "INDEX", "NIFTY", "Nifty 50"),
        "NIFTY50" to Instrument("13", "IDX_I", "INDEX", "NIFTY", "Nifty 50"),
        "BANKNIFTY" to Instrument("25", "IDX_I", "INDEX", "BANKNIFTY", "Nifty Bank"),
        "FINNIFTY" to Instrument("27", "IDX_I", "INDEX", "FINNIFTY", "Nifty Financial"),
        "MIDCPNIFTY" to Instrument("442", "IDX_I", "INDEX", "MIDCPNIFTY", "Nifty Midcap")
    )

    suspend fun load() = withContext(Dispatchers.IO) {
        if (cached.isNotEmpty()) return@withContext
        try {
            val resp = client.newCall(Request.Builder().url(CSV_URL).build()).execute()
            val text = resp.body?.string() ?: return@withContext
            cached = text.lines().drop(1).mapNotNull { line ->
                val c = line.split(",")
                if (c.size < 8) return@mapNotNull null
                Instrument(
                    securityId = c[2].trim(),
                    exchangeSegment = c[0].trim() + "_" + c[1].trim(),
                    instrument = c[3].trim(),
                    tradingSymbol = c[5].trim(),
                    displayName = c[7].trim()
                )
            }
        } catch (_: Exception) {}
    }

    fun find(query: String): Instrument? {
        val q = query.uppercase().trim()
        if (q.isEmpty()) return null
        
        // Pehle hardcoded indices check karo
        INDEX_MAP[q]?.let { return it }
        
        return cached.find { 
            it.tradingSymbol.equals(q, true) || 
            it.displayName.equals(q, true) ||
            it.tradingSymbol.contains(q, true)
        }
    }

    fun search(query: String): List<Instrument> {
        val q = query.uppercase().trim()
        if (q.isEmpty()) return emptyList()
        
        val result = mutableListOf<Instrument>()
        INDEX_MAP[q]?.let { result.add(it) }
        
        result.addAll(cached.filter { 
            it.tradingSymbol.contains(q, true) || 
            it.displayName.contains(q, true)
        }.take(20))
        
        return result.distinctBy { it.securityId }.take(20)
    }

    fun getCount(): Int = cached.size + INDEX_MAP.size
}
