package com.udaan.child.data.repository

import com.udaan.child.data.model.DailyBar

interface MarketDataRepository {
    suspend fun getHistoricalData(symbol: String): List<DailyBar>
}

class MarketDataRepositoryImpl : MarketDataRepository {
    override suspend fun getHistoricalData(symbol: String): List<DailyBar> {
        // Stub — replace with real data fetch
        return emptyList()
    }
}
