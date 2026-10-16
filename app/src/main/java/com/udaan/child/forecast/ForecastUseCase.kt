package com.udaan.child.forecast

import com.udaan.child.data.model.Candle
import com.udaan.child.data.model.DailyBar

class ForecastUseCase(private val repo: DhanRepository) {
    suspend fun run(symbol: String, hSteps: Int = 1): ForecastEngine.ForecastResult {
        val bars: List<DailyBar> = repo.getHistoricalData(symbol)
        
        // DailyBar ko Candle mein map karna
        val candles = bars.map { bar ->
            Candle(
                timestamp = System.currentTimeMillis(), // ya bar.timestamp agar available ho
                open = bar.open,
                high = bar.high,
                low = bar.low,
                close = bar.close,
                volume = bar.volume
            )
        }
        
        // Singleton object call (no 'new' or constructor instantiation)
        return ForecastEngine.forecast(candles, hSteps)
    }
}
