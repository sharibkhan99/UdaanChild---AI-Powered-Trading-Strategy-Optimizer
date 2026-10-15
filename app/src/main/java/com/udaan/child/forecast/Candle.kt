package com.udaan.child.forecast

import kotlinx.serialization.Serializable

@Serializable
data class Candle(
    val timestamp: Long = 0L,  // Instant ki jagah Long
    val open: Double = 0.0,
    val high: Double = 0.0,
    val low: Double = 0.0,
    val close: Double = 0.0,
    val volume: Long = 0
)
