
package com.udaan.child.data.repository

import com.udaan.child.data.model.Instrument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

object InstrumentRepository {
    private const val CSV_URL =
        "https://images.dhan.co/api-data/api-scrip-master.csv"

    private val client = OkHttpClient()
    @Volatile private var cached: List<Instrument> = emptyList()

    private val indexMap = mapOf(
        "NIFTY" to Instrument(
            securityId = "13",
            tradingSymbol = "NIFTY",
            displayName = "Nifty 50",
            exchangeSegment = "IDX_I",
            instrument = "INDEX"
        ),
        "NIFTY50" to Instrument(
            securityId = "13",
            tradingSymbol = "NIFTY",
            displayName = "Nifty 50",
            exchangeSegment = "IDX_I",
            instrument = "INDEX"
        ),
        "BANKNIFTY" to Instrument(
            securityId = "25",
            tradingSymbol = "BANKNIFTY",
            displayName = "Nifty Bank",
            exchangeSegment = "IDX_I",
            instrument = "INDEX"
        ),
        "FINNIFTY" to Instrument(
            securityId = "27",
            tradingSymbol = "FINNIFTY",
            displayName = "Nifty Financial Services",
            exchangeSegment = "IDX_I",
            instrument = "INDEX"
        ),
        "MIDCPNIFTY" to Instrument(
            securityId = "442",
            tradingSymbol = "MIDCPNIFTY",
            displayName = "Nifty Midcap Select",
            exchangeSegment = "IDX_I",
            instrument = "INDEX"
        )
    )

    suspend fun load(forceRefresh: Boolean = false) =
        withContext(Dispatchers.IO) {
            if (cached.isNotEmpty() && !forceRefresh) return@withContext

            try {
                val request = Request.Builder()
                    .url(CSV_URL)
                    .get()
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@withContext

                    val csv = response.body?.string()
                        ?: return@withContext

                    val instruments = csv.lineSequence()
                        .drop(1)
                        .mapNotNull { line ->
                            // Compact master fields are comma-separated.
                            val c = line.split(",")
                            if (c.size < 8) return@mapNotNull null

                            val exchange = c[0].trim()
                            val segment = c[1].trim()
                            val securityId = c[2].trim()
                            val instrument = c[3].trim()
                            val tradingSymbol = c[5].trim()
                            val displayName = c[7].trim()

                            if (
                                securityId.isBlank() ||
                                tradingSymbol.isBlank()
                            ) return@mapNotNull null

                            Instrument(
                                securityId = securityId,
                                tradingSymbol = tradingSymbol,
                                displayName = displayName,
                                exchangeSegment = "${exchange}_${segment}",
                                instrument = instrument
                            )
                        }
                        .toList()

                    if (instruments.isNotEmpty()) {
                        cached = instruments
                    }
                }
            } catch (_: Exception) {
                // Keep the previous cache if refresh fails.
            }
        }

    fun find(query: String): Instrument? {
        val q = query.trim().uppercase()
        if (q.isBlank()) return null

        indexMap[q]?.let { return it }

        return cached.firstOrNull {
            it.tradingSymbol.equals(q, true) ||
            it.displayName.equals(q, true)
        }
    }

    /** Search the loaded instrument master across NSE segments. */
    fun searchNse(
        query: String,
        includeDerivatives: Boolean = true,
        limit: Int = 30
    ): List<Instrument> {
        val q = query.trim()

        if (q.isBlank()) return emptyList()

        val matches = cached.filter { instrument ->
            val segment = instrument.exchangeSegment.uppercase()
            val isNse = segment == "NSE_EQ" ||
                (includeDerivatives && segment == "NSE_FNO")

            isNse && (
                instrument.tradingSymbol.contains(q, true) ||
                instrument.displayName.contains(q, true)
            )
        }

        val indexMatches = indexMap.values.filter {
            it.tradingSymbol.contains(q, true) ||
            it.displayName.contains(q, true)
        }

        return (indexMatches + matches)
            .distinctBy {
                "${it.exchangeSegment}:${it.securityId}"
            }
            .take(limit)
    }

    fun getAllNseSymbols(
        includeDerivatives: Boolean = true
    ): List<Instrument> {
        return cached.filter {
            val segment = it.exchangeSegment.uppercase()
            segment == "NSE_EQ" ||
                (includeDerivatives && segment == "NSE_FNO")
        }.distinctBy {
            "${it.exchangeSegment}:${it.securityId}"
        }
    }

    fun getCount(): Int =
        cached.size + indexMap.size
}
