package com.udaan.child.forecast

import com.udaan.child.data.model.DailyBar

class DhanRepository {
    suspend fun getHistoricalData(symbol: String): List<DailyBar> = emptyList()
}
