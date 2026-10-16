package com.udaan.child.forecast

data class Candle(
    val open: Double = 0.0,
    val high: Double = 0.0,
    val low: Double = 0.0,
    val close: Double = 0.0,
    val volume: Double = 0.0,
    val timestamp: Long = 0L
)
