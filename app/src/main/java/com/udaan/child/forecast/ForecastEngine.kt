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

    private val Q: RealMatrix = MatrixUtils.createRealMatrix(arrayOf(
        doubleArrayOf(1e-4, 0.0,  0.0,  0.0),
        doubleArrayOf(0.0,  1e-6, 0.0,  0.0),
        doubleArrayOf(0.0,  0.0,  1e-5, 0.0),
        doubleArrayOf(0.0,  0.0,  0.0,  1e-5)
    ))

    data class ForecastResult(
        val value: Double,
        val stdDev: Double,
        val forecastInstant: Instant
    )

    private fun RealMatrix.exp(): RealMatrix {
        val n = this.rowDimension
        var term = MatrixUtils.createRealIdentityMatrix(n)
        var sum = MatrixUtils.createRealIdentityMatrix(n)
        var factorial = 1.0
        for (i in 1..20) {
            factorial *= i.toDouble()
            term = term.multiply(this)
            sum = sum.add(term.scalarMultiply(1.0 / factorial))
        }
        return sum
    }

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

    fun forecast(candles: List<Candle>, hSteps: Int): ForecastResult {
        require(candles.size >= 5) { "Need >=5 candles for Kalman state initialization" }
        require(hSteps >= 1) { "Horizon must be >=1 trading day" }

        val dt = 1.0 / (252.0 * 390.0)
        val phi = (A.scalarMultiply(dt)).exp()
        val qd  = phi.multiply(Q).multiply(phi.transpose()).scalarMultiply(dt)

        var xVec = doubleArrayOf(candles.first().close, 0.0, 0.0, 0.0)
        var pMat = MatrixUtils.createRealIdentityMatrix(4).scalarMultiply(1e3)

        for (c in candles) {
            val xPred = phi.operate(xVec)
            val pPred = phi.multiply(pMat).multiply(phi.transpose()).add(qd)

            val y = c.close
            val sMat = C.multiply(pPred).multiply(C.transpose()).add(MatrixUtils.createRealMatrix(arrayOf(doubleArrayOf(R))))
            val sVal = sMat.getEntry(0, 0)
            
            val k = pPred.multiply(C.transpose()).scalarMultiply(1.0 / sVal)
            val innov = y - C.operate(xPred)[0]

            xVec = ArrayRealVector(xPred).add(ArrayRealVector(k.getColumn(0)).mapMultiply(innov)).toArray()
            pMat = MatrixUtils.createRealIdentityMatrix(4).subtract(k.multiply(C)).multiply(pPred)
        }

        val phiH = (A.scalarMultiply(hSteps.toDouble())).exp()
        val yHat = C.multiply(phiH).multiply(MatrixUtils.createColumnRealMatrix(xVec)).getEntry(0, 0)

        val varMatrix = C.multiply(phiH).multiply(pMat).multiply(phiH.transpose()).multiply(C.transpose()).add(MatrixUtils.createRealMatrix(arrayOf(doubleArrayOf(R))))
        val std = Math.sqrt(varMatrix.getEntry(0, 0).coerceAtLeast(0.0))

        val lastInstant = Instant.ofEpochMilli(candles.last().timestamp)
        val forecastInstant = addTradingDays(lastInstant, hSteps)

        return ForecastResult(yHat, std, forecastInstant)
    }

    fun forecastCustom(customValues: List<Double>, hSteps: Int = 1): ForecastResult {
        require(customValues.isNotEmpty()) { "Input values cannot be empty" }
        
        val processedValues = if (customValues.size < 30) {
            val firstVal = customValues.first()
            val padded = mutableListOf<Double>()
            repeat(30 - customValues.size) { padded.add(firstVal) }
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
