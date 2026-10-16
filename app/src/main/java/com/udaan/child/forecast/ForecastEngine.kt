package com.udaan.child.forecast

import com.udaan.child.data.model.DailyBar

object ForecastEngine {
    fun forecast(bars: List<DailyBar>, horizon: Int): ForecastResult {
        if (bars.size < 5) return ForecastResult(bars.lastOrNull()?.close ?: 0.0, 0.0)
        val last = bars.last().close
        val slope = (last - bars[bars.size - 5].close) / 5.0
        return ForecastResult(last + slope * horizon, slope)
    }

    data class ForecastResult(val value: Double, val slope: Double)
}
