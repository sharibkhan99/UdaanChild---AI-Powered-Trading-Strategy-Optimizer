
package com.udaan.child.forecast

import com.udaan.child.data.model.DailyBar
import kotlin.math.max

data class BacktestSummary(
    val trades: Int,
    val wins: Int,
    val winRate: Double,
    val netPoints: Double,
    val maxDrawdown: Double
)

/** Walk-forward evaluation using historical candles. */
object BacktestEngine {

    fun run(bars: List<DailyBar>): BacktestSummary {
        if (bars.size < 20) {
            return BacktestSummary(0, 0, 0.0, 0.0, 0.0)
        }

        val prices = bars.map { it.close }
        val smoothed = kalman(prices)

        var trades = 0
        var wins = 0
        var pnl = 0.0
        var peak = 0.0
        var maxDd = 0.0

        for (i in 10 until bars.lastIndex) {
            val slope = smoothed[i] - smoothed[i - 5]

            val signal = when {
                slope > prices[i] * 0.00015 -> 1
                slope < -prices[i] * 0.00015 -> -1
                else -> 0
            }

            if (signal == 0) continue

            val points = (prices[i + 1] - prices[i]) * signal

            trades++
            if (points > 0) wins++

            pnl += points
            peak = max(peak, pnl)
            maxDd = max(maxDd, peak - pnl)
        }

        return BacktestSummary(
            trades = trades,
            wins = wins,
            winRate = if (trades == 0) 0.0 else wins * 100.0 / trades,
            netPoints = pnl,
            maxDrawdown = maxDd
        )
    }

    private fun kalman(values: List<Double>): List<Double> {
        var x = values.first()
        var p = 1.0

        return values.map { z ->
            p += 0.0001
            val k = p / (p + 0.02)
            x += k * (z - x)
            p *= (1 - k)
            x
        }
    }
}
