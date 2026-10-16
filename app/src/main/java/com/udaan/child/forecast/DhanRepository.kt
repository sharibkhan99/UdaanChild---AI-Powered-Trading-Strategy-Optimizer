package com.udaan.child.forecast

import com.udaan.child.data.model.DailyBar
import java.time.LocalDate

class DhanRepository {

    suspend fun fetchCandles(
        clientId: String,
        token: String,
        symbol: String,
        timeframe: String,
        asOfDate: String
    ): List<DailyBar> {
        // Stub — OkHttp dependency add karne ke baad actual API call
        return emptyList()
    }

    suspend fun getHistoricalData(symbol: String): List<DailyBar> = emptyList()
}
