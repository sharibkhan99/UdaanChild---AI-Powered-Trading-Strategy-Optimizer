package com.udaan.child.forecast

import com.udaan.child.data.model.DailyBar

class ForecastUseCase(private val repo: DhanRepository) {
    suspend fun run(symbol: String, hSteps: Int = 1): ForecastEngine.ForecastResult {
        val bars: List<DailyBar> = repo.getHistoricalData(symbol)
        // Direct DailyBar list ko ForecastEngine mein pass kar rahe hain agar ForecastEngine overload supported hai, 
        // ya phir hum unhe Candles mein map kar sakte hain bina external package import ke.
        val dummyCandles = bars.map { bar ->
            com.udaan.child.data.model.Candle(
                timestamp = System.currentTimeMillis(),
                open = bar.open,
                high = bar.high,
                low = bar.low,
                close = bar.close,
                volume = bar.volume
            )
        }
        return ForecastEngine.forecast(dummyCandles, hSteps)
    }
}
