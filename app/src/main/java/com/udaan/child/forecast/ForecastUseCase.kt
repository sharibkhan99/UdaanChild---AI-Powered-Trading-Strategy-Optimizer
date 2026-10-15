package com.udaan.child.forecast

import com.udaan.child.data.model.DailyBar

class ForecastUseCase(private val repo: DhanRepository) {
    suspend fun run(symbol: String): Double {
        val bars = repo.getHistoricalData(symbol)
        return ForecastEngine().forecast(bars)
    }
}
