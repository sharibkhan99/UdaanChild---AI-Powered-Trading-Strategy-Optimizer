package com.udaan.child.forecast

import com.udaan.child.data.model.Candle
import org.apache.commons.math3.linear.ArrayRealVector
import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.linear.RealMatrix
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

object ForecastEngine {

    // -----------------------------------------------------------
    // 1️⃣ Model constants
    // -----------------------------------------------------------
    private const val PERIOD_DAYS = 252.0
    private const val OMEGA = 2.0 * Math.PI / PERIOD_DAYS

    private val A: RealMatrix = MatrixUtils.createRealMatrix(arrayOf(
        doubleArrayOf(0.0, 1.0, 0.0, 0.0),
        doubleArrayOf(0.0, 0.0, 0.0, 0.0),
        doubleArrayOf(0.0, 0.0, 0.0,  OMEGA),
        doubleArrayOf(0.0, 0.0, -OMEGA, 0.0)
    ))
    private val C: RealMatrix = MatrixUtils.createRealMatrix(arrayOf(doubleArrayOf(1.0, 0.0, 0.0, 0.0)))
    private const val R = 1e-3

    // Process-noise covariance Q
    private val Q: RealMatrix = MatrixUtils.createRealMatrix(arrayOf(
        doubleArrayOf(1e-4, 0.0,  0.0,  0.0),
        doubleArrayOf(0.0,  1e-6, 0.0,  0.0),
        doubleArrayOf(0.0,  0.0,  1e-5, 0.0),
        doubleArrayOf(0.0,  0.0,  0.0,  1e-5)
    ))

    // -----------------------------------------------------------
    data class ForecastResult(
        val value: Double,               // point forecast ŷ
        val stdDev: Double,              // 1‑σ
        val forecastInstant: Instant     // calendar date‑time (UTC)
    )

    // -----------------------------------------------------------
    /** Robust Matrix Exponential extension for Apache Commons Math RealMatrix */
    private fun RealMatrix.exp(): RealMatrix {
        val n = this.rowDimension
        var term = MatrixUtils.createRealIdentityMatrix(n)
        var sum = MatrixUtils.createRealIdentityMatrix(n)
        var factorial = 1.0
        for (i in 1..20) {
            factorial *= i.toDouble()
            term = term.multiply(this)
            val scaledTerm = term.scalarMultiply(1.0 / factorial)
            sum = sum.add(scaledTerm)
        }
        return sum
    }

    // -----------------------------------------------------------
    /** Business‑day adder – skips Sat/Sun */
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
     * @param hSteps       Horizon in **trading days** (1 = next trading day, 5 = 5‑day ahead ...)
     */
    fun forecast(
        candles: List<Candle>,
        hSteps: Int
    ): ForecastResult {
        require(candles.size >= 5) { "Need >=5 candles for Kalman state initialization" }
        require(hSteps >= 1) { "Horizon must be >=1 trading day" }

        // ---- 1️⃣ Build discrete‑time Kalman filter step ----
        val dt = 1.0 / (252.0 * 390.0)               // 1 minute ≈ 1/(252*390) trading‑day
        val phi = (A.scalarMultiply(dt)).exp()       // Φ(Δt)
        val qd  = phi.multiply(Q).multiply(phi.transpose()).scalarMultiply(dt)

        // ---- 2️⃣ Run continuous‑time Kalman filter over the candle series ----
        var xVec = doubleArrayOf(candles.first().close, 0.0, 0.0, 0.0)
        var pMat = MatrixUtils.createRealIdentityMatrix(4).scalarMultiply(1e3)

        for (c in candles) {
            // ---- Predict ----
            val xPred = phi.operate(xVec)
            val pPred = phi.multiply(pMat).multiply(phi.transpose()).add(qd)

            // ---- Update (measurement = close price) ----
            val y = c.close
            val sMat = C.multiply(pPred).multiply(C.transpose()).add(MatrixUtils.createRealMatrix(arrayOf(doubleArrayOf(R))))
            val sVal = sMat.getEntry(0, 0)
            
            val k = pPred.multiply(C.transpose()).scalarMultiply(1.0 / sVal)
            val innov = y - C.operate(xPred)[0]

            xVec = ArrayRealVector(xPred).add(ArrayRealVector(k.getColumn(0)).mapMultiply(innov)).toArray()
            
            val identity = MatrixUtils.createRealIdentityMatrix(4)
            pMat = identity.subtract(k.multiply(C)).multiply(pPred)
        }

        // ---- 3️⃣ h‑step ahead analytic forecast ----
        val phiH = (A.scalarMultiply(hSteps.toDouble())).exp()
        val yHat = C.multiply(phiH).multiply(MatrixUtils.createColumnRealMatrix(xVec)).getEntry(0, 0)

        val varMatrix = C.multiply(phiH).multiply(pMat).multiply(phiH.transpose()).multiply(C.transpose()).add(MatrixUtils.createRealMatrix(arrayOf(doubleArrayOf(R))))
        val forecastVariance = varMatrix.getEntry(0, 0)
        val std = Math.sqrt(forecastVariance.coerceAtLeast(0.0))

        // ---- 4️⃣ Calendar date for the horizon ----
        val lastTimestamp = candles.last().timestamp
        val lastObsInstant = Instant.ofEpochMilli(lastTimestamp)
        val forecastInstant = addTradingDays(lastObsInstant, hSteps)

        return ForecastResult(yHat, std, forecastInstant)
    }

    // -----------------------------------------------------------
    /**
     * User-provided custom numbers ke base par Kalman Filter prediction calculate karne ke liye.
     */
    fun forecastCustom(customValues: List<Double>, hSteps: Int = 1): ForecastResult {
        require(customValues.isNotEmpty()) { "Input values cannot be empty" }
        
        // Agar values 30 se kam hain, toh Kalman filter ke liye padding/interpolation kar lo
        val processedValues = if (customValues.size < 30) {
            val firstVal = customValues.first()
            val padded = mutableListOf<Double>()
            // Synthetic preceding points add karna taaki length 30 ho jaye
            val deficit = 30 - customValues.size
            for (i in 0 until deficit) {
                padded.add(firstVal)
            }
            padded.addAll(customValues)
            padded
        } else {
            customValues
        }

        val baseTime = System.currentTimeMillis()
        val dayMillis = 24 * 60 * 60 * 1000L

        val dummyCandles = processedValues.mapIndexed { index, price ->
            Candle(
                timestamp = baseTime - ((processedValues.size - index) * dayMillis),
                open = price,
                high = price * 1.005,
                low = price * 0.995,
                close = price,
                volume = 1000L
            )
        }
        
        return forecast(dummyCandles, hSteps)
    }
}
