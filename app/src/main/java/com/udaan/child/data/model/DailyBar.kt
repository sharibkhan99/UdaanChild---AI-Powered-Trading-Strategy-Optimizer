package com.udaan.child.data.model

data class DailyBar(
    val date: String = "",
    val timestamp: Long = 0L,
    val open: Double = 0.0,
    val high: Double = 0.0,
    val low: Double = 0.0,
    val close: Double = 0.0,
    val volume: Long = 0
)
