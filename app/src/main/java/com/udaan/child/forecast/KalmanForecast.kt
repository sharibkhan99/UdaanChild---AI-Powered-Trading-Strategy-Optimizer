
package com.udaan.child.forecast

import com.udaan.child.data.model.DailyBar
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.math.sqrt

data class PricePrediction(
    val horizon: String,
    val targetDateTime: String,
    val currentPrice: Double,
    val predictedPrice: Double,
    val lowerPrice: Double,
    val upperPrice: Double,
    val signal: String
)

/** Kalman smoothing with a simple local-trend projection. Estimates are not guarantees. */
object KalmanForecast {

    fun predict(bars: List<DailyBar>, timeframe: String): List<PricePrediction> {
        if (bars.size < 8) return emptyList()

        val closes = bars.map { it.close }
        val smoothed = filter(closes)
        val current = closes.last()

        val lastDt = runCatching {
            LocalDateTime.parse(bars.last().date)
        }.getOrElse {
            LocalDateTime.now(ZoneId.of("Asia/Kolkata"))
        }

        val tfMinutes = minutes(timeframe)
        val intradaySteps = 3
        val swingSteps = if (timeframe.equals("1D", true)) {
            5
        } else {
            ((5 * 375.0) / tfMinutes).toInt().coerceAtLeast(1)
        }

        return listOf(
            makePrediction(
                "Intraday / next 3 candles",
                bars, smoothed, current, lastDt,
                intradaySteps, tfMinutes, false
            ),
            makePrediction(
                "Swing / 5 trading days",
                bars, smoothed, current, lastDt,
                swingSteps, tfMinutes, true
            )
        )
    }

    private fun makePrediction(
        label: String,
        bars: List<DailyBar>,
        smooth: List<Double>,
        current: Double,
        lastDt: LocalDateTime,
        steps: Int,
        tfMinutes: Int,
        swing: Boolean
    ): PricePrediction {

        val n = smooth.size
        val lookback = minOf(6, n - 1)
        val slope = (smooth.last() - smooth[n - 1 - lookback]) / lookback
        val projected = smooth.last() + slope * steps

        val residuals = bars.indices.map {
            bars[it].close - smooth[it]
        }

        val sigma = sqrt(
            residuals.takeLast(minOf(30, residuals.size))
                .map { it * it }
                .average()
                .coerceAtLeast(0.0)
        )

        val band = maxOf(
            sigma * sqrt(steps.toDouble()),
            current * 0.001
        )

        val signal = when {
            projected > current + band * 0.2 -> "BUY"
            projected < current - band * 0.2 -> "SELL"
            else -> "HOLD"
        }

        val target = if (swing || tfMinutes >= 375) {
            addTradingDays(lastDt, 5)
        } else {
            lastDt.plusMinutes((steps * tfMinutes).toLong())
        }

        return PricePrediction(
            horizon = label,
            targetDateTime = target.toString().replace('T', ' '),
            currentPrice = current,
            predictedPrice = projected,
            lowerPrice = (projected - band).coerceAtLeast(0.0),
            upperPrice = projected + band,
            signal = signal
        )
    }

    private fun filter(values: List<Double>): List<Double> {
        var estimate = values.first()
        var covariance = 1.0
        val processNoise = 0.0001
        val measurementNoise = 0.02

        return values.map { measurement ->
            covariance += processNoise
            val gain = covariance / (covariance + measurementNoise)
            estimate += gain * (measurement - estimate)
            covariance *= (1.0 - gain)
            estimate
        }
    }

    private fun addTradingDays(
        start: LocalDateTime,
        count: Int
    ): LocalDateTime {
        var dt = start
        var added = 0

        while (added < count) {
            dt = dt.plusDays(1)
            if (
                dt.dayOfWeek != DayOfWeek.SATURDAY &&
                dt.dayOfWeek != DayOfWeek.SUNDAY
            ) {
                added++
            }
        }

        return dt
    }

    private fun minutes(tf: String): Int {
        val value = tf.trim().uppercase()

        if (value == "1D") return 375

        Regex("^(\\d+)M$").matchEntire(value)?.let {
            return it.groupValues[1].toInt()
        }

        Regex("^(\\d+)H$").matchEntire(value)?.let {
            return it.groupValues[1].toInt() * 60
        }

        return 5
    }
}
