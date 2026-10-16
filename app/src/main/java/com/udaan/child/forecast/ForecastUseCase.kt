package com.udaan.child.forecast

import com.udaan.child.data.model.DailyBar

class ForecastUseCase(private val repo: DhanRepository) {
    suspend fun run(symbol: String, hSteps: Int = 1): ForecastEngine.ForecastResult {
        val bars = repo.fetchCandles("", symbol, "1D", java.time.LocalDate.now().toString())
        return ForecastEngine.forecast(bars, hSteps)
    }
}
