// app/src/main/java/com/udaan/child/forecast/ForecastEngine.kt
package com.udaan.child.forecast

import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.linear.RealMatrix
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

object ForecastEngine {

    // -----------------------------------------------------------
    // 1️⃣  Model constants (same as the math answer you approved)
    // -----------------------------------------------------------
    private const val PERIOD_DAYS = 252.0
    private const val OMEGA = 2.0 * Math.PI / PERIOD_DAYS

    private val A: RealMatrix = MatrixUtils.createRealMatrix(arrayOf(
        doubleArrayOf(0.0, 1.0, 0.0, 0.0),
        doubleArrayOf(0.0, 0.0, 0.0, 0.0),
        doubleArrayOf(0.0, 0.0, 0.0,  OMEGA),
        doubleArrayOf(0.0, 0.0, -OMEGA, 0.0)
    ))
    private val C: RealMatrix = MatrixUtils.createRealMatrix(arrayOf(doubleArrayOf(1.0,0.0,0.0,0.0)))
    private const val R = 1e-3

    // -----------------------------------------------------------
    data class ForecastResult(
        val value: Double,               // point forecast ŷ
        val stdDev: Double,              // 1‑σ
        val forecastInstant: Instant     // calendar date‑time (UTC)
    )

    // -----------------------------------------------------------
    /** Business‑day adder – skips Sat/Sun; extend with a holiday set if you need */
    private fun addTradingDays(base: Instant, days: Int): Instant {
        var cur = base
        var added = 0
        while (added < days) {
            cur = cur.plus(1, ChronoUnit.DAYS)
            val dow = cur.atZone(ZoneOffset.UTC).dayOfWeek
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) added++
        }
        return cur
    }

    // -----------------------------------------------------------
    /**
     * @param candles      Chronologically sorted (old → new). Only the **close** price is used.
     * @param hSteps       Horizon in **trading days** (1 = next trading day, 5 = 5‑day ahead …)
     * @return ForecastResult with value, 1‑σ, and the exact UTC instant the forecast refers to.
     */
    fun forecast(
        candles: List<Candle>,
        hSteps: Int
    ): ForecastResult {
        require(candles.size >= 30) { "Need ≥30 candles for a stable Kalman state" }
        require(hSteps >= 1) { "Horizon must be ≥1 trading day" }

        // ---- 1️⃣  Build discrete‑time Kalman filter (Δt = 1 minute in trading‑day units) ----
        val dt = 1.0 / (252.0 * 390.0)               // 1 minute ≈ 1/(252*390) trading‑day
        val Phi = (A.scalarMultiply(dt)).exp()       // Φ(Δt)
        val Qd  = Phi.multiply(Q).multiply(Phi.transpose()).scalarMultiply(dt)

        // Diffuse prior
        var x = doubleArrayOf(candles.last().close, 0.0, 0.0, 0.0)
        var P = MatrixUtils.createRealIdentityMatrix(4).scalarMultiply(1e3)

        // Process every candle (minute‑level works for any granularity – just treat Δt as 1 step)
        for (c in candles) {
            // prediction
            val xPred = Phi.operate(x)
            val pPred = Phi.multiply(MatrixUtils.createRealMatrix(arrayOf(x).map { it.toDoubleArray() }[0])).multiply(Phi.transpose()).add(Qd) // simplified
            // Actually easier: keep P as RealMatrix
            var Pmat = MatrixUtils.createRealMatrix(4,4).apply { for(i in 0..3) for(j in 0..3) setEntry(i,j, if(i==j) 1e3 else 0.0) }

            // We'll run a *proper* loop below (see full implementation) – but for brevity the final
            // filtered state after the whole series is what we need.
        }

        // ---- 2️⃣  Run a *full* continuous‑time Kalman filter over the series ----
        // (The compact version below does the same math but in a clean loop.)
        var xVec = doubleArrayOf(candles.first().close, 0.0, 0.0, 0.0)
        var pMat = MatrixUtils.createRealIdentityMatrix(4).scalarMultiply(1e3)

        for (c in candles) {
            // ---- Predict ----
            val phi = (A.scalarMultiply(dt)).exp()
            val qd  = phi.multiply(Q).multiply(phi.transpose()).scalarMultiply(dt)

            val xPred = phi.operate(xVec)
            val pPred = phi.multiply(pMat).multiply(phi.transpose()).add(qd)

            // ---- Update (measurement = close price) ----
            val y = c.close
            val cMat = C
            val s = cMat.multiply(pPred).multiply(cMat.transpose()).add(R)
            val k = pPred.multiply(cMat.transpose()).multiply(1.0 / s.getEntry(0,0))
            val innov = y - cMat.operate(xPred)[0]

            xVec = xPred + k.map { it * innov }.toDoubleArray()
            pMat = (MatrixUtils.createRealIdentityMatrix(4).subtract(k.multiply(cMat))).multiply(pPred)
        }

        // ---- 3️⃣  h‑step ahead analytic forecast ----
        val phiH = (A.scalarMultiply(hSteps.toDouble())).exp()
        val yHat = C.multiply(phiH).multiply(MatrixUtils.createColumnRealMatrix(xVec)).getEntry(0,0)

        val var = C.multiply(phiH).multiply(pMat).multiply(phiH.transpose()).multiply(C.transpose()).getEntry(0,0) + R
        val std = Math.sqrt(var)

        // ---- 4️⃣  Calendar date for the horizon ----
        val lastObsInstant = candles.last().instant
        val forecastInstant = addTradingDays(lastObsInstant, hSteps)

        return ForecastResult(yHat, std, forecastInstant)
    }

    // Process‑noise covariance Q (tune if needed)
    private val Q: RealMatrix = MatrixUtils.createRealMatrix(arrayOf(
        doubleArrayOf(1e-4, 0.0,   0.0,   0.0),
        doubleArrayOf(0.0,   1e-6, 0.0,   0.0),
        doubleArrayOf(0.0,   0.0,   1e-5, 0.0),
        doubleArrayOf(0.0,   0.0,   0.0,   1e-5)
    ))
}
