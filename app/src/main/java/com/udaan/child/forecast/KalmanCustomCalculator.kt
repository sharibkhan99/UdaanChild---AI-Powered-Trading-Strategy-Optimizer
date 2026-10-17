
package com.udaan.child.forecast

import kotlin.math.sqrt

data class KalmanCustomInput(
    val currentPrice: Double,
    val observations: List<Double> = emptyList(),
    val initialEstimate: Double? = null,
    val initialUncertainty: Double = 1.0,
    val processNoise: Double = 0.0001,
    val measurementNoise: Double = 0.02,
    val horizonSteps: Int = 3,
    val confidenceMultiplier: Double = 1.96
)

data class KalmanCustomResult(
    val filteredPrice: Double,
    val trendPerStep: Double,
    val predictedPrice: Double,
    val lowerPrice: Double,
    val upperPrice: Double,
    val signal: String,
    val finalUncertainty: Double
)

object KalmanCustomCalculator {

    fun calculate(input: KalmanCustomInput): KalmanCustomResult {
        require(input.currentPrice > 0.0) {
            "Current price must be greater than zero."
        }
        require(input.initialUncertainty > 0.0) {
            "Initial uncertainty must be positive."
        }
        require(input.processNoise > 0.0) {
            "Process noise Q must be positive."
        }
        require(input.measurementNoise > 0.0) {
            "Measurement noise R must be positive."
        }
        require(input.horizonSteps in 1..10000) {
            "Horizon must be between 1 and 10000 steps."
        }
        require(input.confidenceMultiplier > 0.0) {
            "Confidence multiplier must be positive."
        }

        val measurements = (
            input.observations.filter {
                it.isFinite() && it > 0.0
            } + input.currentPrice
        ).takeLast(500)

        var estimate =
            input.initialEstimate?.takeIf {
                it.isFinite() && it > 0.0
            } ?: measurements.first()

        var covariance = input.initialUncertainty
        val filtered = mutableListOf<Double>()

        for (measurement in measurements) {
            covariance += input.processNoise

            val gain = covariance /
                (covariance + input.measurementNoise)

            estimate += gain * (measurement - estimate)
            covariance *= (1.0 - gain)

            filtered.add(estimate)
        }

        val recent = filtered.takeLast(
            minOf(6, filtered.size)
        )

        val trend = if (recent.size >= 2) {
            (recent.last() - recent.first()) /
                (recent.size - 1)
        } else {
            0.0
        }

        val steps = input.horizonSteps.toDouble()
        val predicted = estimate + trend * steps

        val projectedVariance =
            covariance + input.processNoise * steps

        val uncertainty =
            input.confidenceMultiplier *
            sqrt(projectedVariance.coerceAtLeast(0.0))

        val lower = (predicted - uncertainty).coerceAtLeast(0.0)
        val upper = predicted + uncertainty

        val signal = when {
            predicted > input.currentPrice + uncertainty ->
                "BUY"
            predicted < input.currentPrice - uncertainty ->
                "SELL"
            else -> "HOLD"
        }

        return KalmanCustomResult(
            filteredPrice = estimate,
            trendPerStep = trend,
            predictedPrice = predicted,
            lowerPrice = lower,
            upperPrice = upper,
            signal = signal,
            finalUncertainty = covariance
        )
    }
}
