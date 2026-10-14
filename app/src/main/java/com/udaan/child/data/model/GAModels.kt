package com.udaan.child.data.model

import kotlinx.serialization.Serializable

@Serializable
data class DailyBar(
    val date: Long,
    val close: Float,
    val volume: Float,
    val open: Float = 0f,
    val high: Float = 0f,
    val low: Float = 0f
)

@Serializable
data class Chromosome(
    val featureMask: BooleanArray,
    val thresholds: FloatArray,
    val weights: FloatArray
) {
    override fun equals(other: Any?): Boolean {
        if (other !is Chromosome) return false
        return featureMask.contentEquals(other.featureMask) &&
               thresholds.contentEquals(other.thresholds) &&
               weights.contentEquals(other.weights)
    }

    override fun hashCode(): Int =
        31 * featureMask.contentHashCode() + thresholds.contentHashCode()
}

@Serializable
data class GAResult(
    val bestChromosome: Chromosome,
    val bestSharpe: Float,
    val activeFeatures: List<String>,
    val testReturns: List<Float>,
    val timestamp: Long = System.currentTimeMillis()
)

object FeatureRegistry {
    val NAMES = listOf(
        "EMA30_above_EMA94_cross",
        "EMA9_above_EMA21_cross",
        "WT30_above_WT94_cross",
        "WT9_above_WT21_cross",
        "FFT_phase_positive",
        "volume_above_EMA20",
        "price_band_100_3000",
        "caputo_alpha_cross",
        "nakshatra_bull",
        "tithi_shukla",
        "malliavin_above_threshold",
        "close_above_EMA50",
        "rsi_above_50",
        "atr_above_2"
    )

    const val COUNT = 14
}
