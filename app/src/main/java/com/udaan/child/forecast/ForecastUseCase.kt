// app/src/main/java/com/udaan/child/forecast/ForecastUseCase.kt
package com.udaan.child.forecast

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ForecastUseCase(
    private val repo: DhanRepository = DhanRepository.getInstance(
        BuildConfig.DHAN_CLIENT_ID,
        BuildConfig.DHAN_ACCESS_TOKEN
    )
) {

    /** Pull candles for *all* six granularities and return a map Granularity → ForecastResult */
    suspend fun getAllForecasts(
        securityId: String,
        exchange: String = "NSE",
        horizonDays: Int = 5
    ): Map<DhanRepository.Granularity, ForecastEngine.ForecastResult> = withContext(Dispatchers.IO) {
        val results = mutableMapOf<DhanRepository.Granularity, ForecastEngine.ForecastResult>()

        for (gran in DhanRepository.Granularity.values()) {
            val candlesResult = repo.fetchCandles(securityId, exchange, gran, limit = 500)
            val candles = candlesResult.getOrThrow()
            val forecast = ForecastEngine.forecast(candles, horizonDays)
            results[gran] = forecast
        }
        results
    }
}
