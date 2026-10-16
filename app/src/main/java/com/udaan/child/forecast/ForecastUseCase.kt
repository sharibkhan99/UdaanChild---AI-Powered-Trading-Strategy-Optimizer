package com.udaan.child.forecast

import com.udaan.child.data.model.DailyBar

class ForecastUseCase(private val repo: DhanRepository) {
    suspend fun run(symbol: String, hSteps: Int = 1): ForecastEngine.ForecastResult {
        val bars: List<DailyBar> = repo.getHistoricalData(symbol)
        return ForecastEngine.forecast(bars, hSteps)
    }
}
