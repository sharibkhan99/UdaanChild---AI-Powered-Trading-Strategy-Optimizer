package com.udaan.child.forecast

import com.udaan.child.data.model.DailyBar
import kotlin.math.exp

class ForecastEngine {
    fun forecast(bars: List<DailyBar>): Double {
        if (bars.isEmpty()) return 0.0
        return bars.last().close * (1.0 + 0.01 * exp(-0.1))
    }
}
