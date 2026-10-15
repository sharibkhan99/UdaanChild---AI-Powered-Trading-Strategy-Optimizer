// app/src/main/java/com/udaan/child/forecast/Candle.kt
package com.udaan.child.forecast

import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class Candle(
    val timestamp: Long,          // epoch seconds (UTC) – as returned by Dhan
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long
) {
    /** Human‑readable UTC instant */
    val instant: Instant = Instant.ofEpochSecond(timestamp)
}
