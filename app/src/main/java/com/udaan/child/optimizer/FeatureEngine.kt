package com.udaan.child.optimizer

import com.udaan.child.data.model.DailyBar
import com.udaan.child.data.model.FeatureRegistry
import kotlin.math.*

class FeatureEngine(private val bars: List<DailyBar>) {
    
    private val n = bars.size
    private val closes = bars.map { it.close }.toFloatArray()
    private val volumes = bars.map { it.volume }.toFloatArray()
    private val jds = bars.map { it.date / 86400000.0 + 2440587.5 }.toDoubleArray()
    
    // Precomputed indicators
    private val ema9 = ema(closes, 9)
    private val ema21 = ema(closes, 21)
    private val ema30 = ema(closes, 30)
    private val ema50 = ema(closes, 50)
    private val ema94 = ema(closes, 94)
    private val emaVol20 = ema(volumes, 20)
    
    private val wt9 = haarWavelet(closes, 9)
    private val wt21 = haarWavelet(closes, 21)
    private val wt30 = haarWavelet(closes, 30)
    private val wt94 = haarWavelet(closes, 94)
    
    private val phase = dominantPhase(closes, 128)
    private val cap03 = caputo(closes, 0.3f)
    private val cap07 = caputo(closes, 0.7f)
    private val malliavin = malliavinProxy(closes, 20)
    private val rsi = computeRSI(14)
    private val atr14 = computeATR(14)
    
    fun getAllFeatures(): Array<FloatArray> = Array(FeatureRegistry.COUNT) { idx ->
        when (idx) {
            0 -> crossSignal(ema30, ema94)
            1 -> crossSignal(ema9, ema21)
            2 -> crossSignal(wt30, wt94)
            3 -> crossSignal(wt9, wt21)
            4 -> phase
            5 -> volRatio()
            6 -> closes
            7 -> crossSignal(cap03, cap07)
            8 -> nakshatraIndex()
            9 -> tithiIndex()
            10 -> malliavin
            11 -> closeOverEMA50()
            12 -> rsi
            13 -> atr14
            else -> FloatArray(n)
        }
    }
    
    // --- Indicator computations ---
    private fun ema(data: FloatArray, period: Int): FloatArray {
        val out = FloatArray(n)
        val k = 2f / (period + 1f)
        out[0] = data[0]
        for (i in 1 until n) out[i] = data[i] * k + out[i-1] * (1 - k)
        return out
    }
    
    private fun haarWavelet(data: FloatArray, scale: Int): FloatArray {
        val out = FloatArray(n) { 0f }
        for (i in scale until n) {
            val half = scale / 2
            var sumHi = 0f; var sumLo = 0f
            for (j in 0 until half) sumHi += data[i - j]
            for (j in half until scale) sumLo += data[i - j]
            out[i] = (sumHi - sumLo) / scale
        }
        return out
    }
    
    private fun crossSignal(fast: FloatArray, slow: FloatArray): FloatArray {
        val out = FloatArray(n) { 0f }
        for (i in 1 until n) {
            if (fast[i] > slow[i] && fast[i-1] <= slow[i-1]) out[i] = 1f
        }
        return out
    }
    
    private fun dominantPhase(data: FloatArray, window: Int): FloatArray {
        val nfft = 1 shl ceil(log2(window.toFloat())).toInt()
        val out = FloatArray(n) { 0f }
        val real = FloatArray(nfft)
        val imag = FloatArray(nfft)
        
        for (i in window until n) {
            for (k in 0 until nfft) {
                real[k] = if (k < window) data[i - window + k] - data[i-window] else 0f
                imag[k] = 0f
            }
            fft(real, imag, nfft)
            var maxMag = 0f; var maxIdx = 1
            for (k in 1 until nfft/2) {
                val mag = sqrt(real[k]*real[k] + imag[k]*imag[k])
                if (mag > maxMag) { maxMag = mag; maxIdx = k }
            }
            out[i] = atan2(imag[maxIdx], real[maxIdx])
        }
        return out
    }
    
    private fun fft(real: FloatArray, imag: FloatArray, n: Int) {
        var m = n
        while (m > 1) {
            val mh = m / 2
            for (i in 0 until mh) {
                val wr = cos(2 * PI * i / m).toFloat()
                val wi = -sin(2 * PI * i / m).toFloat()
                for (j in i until n step m) {
                    val k = j + mh
                    val tr = real[k] * wr - imag[k] * wi
                    val ti = real[k] * wi + imag[k] * wr
                    real[k] = real[j] - tr
                    imag[k] = imag[j] - ti
                    real[j] += tr
                    imag[j] += ti
                }
            }
            m = mh
        }
        // bit-reversal
        var j = 0
        for (i in 1 until n) {
            var bit = n shr 1
            while ((j and bit) != 0) { j = j xor bit; bit = bit shr 1 }
            j = j or bit
            if (i < j) {
                real[i] = real[j].also { real[j] = real[i] }
                imag[i] = imag[j].also { imag[j] = imag[i] }
            }
        }
    }
    
    private fun caputo(data: FloatArray, alpha: Float, window: Int = 50): FloatArray {
        val out = FloatArray(n) { 0f }
        val coeffs = FloatArray(window)
        coeffs[0] = 1f
        for (k in 1 until window) coeffs[k] = coeffs[k-1] * (k - 1 - alpha) / k
        for (i in window until n) {
            var sum = 0f
            for (k in 0 until window) sum += coeffs[k] * data[i - k]
            out[i] = sum
        }
        return out
    }
    
    private fun malliavinProxy(data: FloatArray, window: Int): FloatArray {
        val logRet = FloatArray(n) { 0f }
        for (i in 1 until n) logRet[i] = ln(data[i] / data[i-1])
        val out = FloatArray(n) { 0f }
        for (i in window until n) {
            var sumSq = 0f
            for (k in 0 until window) sumSq += logRet[i - k] * logRet[i - k]
            out[i] = sqrt(sumSq / window)
        }
        return out
    }
    
    private fun lunarLongitude(jd: Double): Double {
        val T = (jd - 2451545.0) / 36525.0
        return (218.3164477 + 481267.88123421 * T - 0.0015786 * T*T + T*T*T/538841) % 360.0
    }
    
    private fun nakshatraIndex(): FloatArray {
        return FloatArray(n) { i -> (lunarLongitude(jds[i]) / 13.3333).toFloat() }
    }
    
    private fun tithiIndex(): FloatArray {
        return FloatArray(n) { i -> (lunarLongitude(jds[i]) / 12.0).toFloat() % 30f }
    }
    
    private fun volRatio(): FloatArray {
        return FloatArray(n) { i -> if (emaVol20[i] > 0) volumes[i] / emaVol20[i] else 1f }
    }
    
    private fun closeOverEMA50(): FloatArray {
        return FloatArray(n) { i -> if (ema50[i] > 0) closes[i] / ema50[i] else 1f }
    }
    
    private fun computeRSI(period: Int): FloatArray {
        val out = FloatArray(n) { 50f }
        var gainSum = 0f; var lossSum = 0f
        for (i in 1..period) {
            val diff = closes[i] - closes[i-1]
            if (diff > 0) gainSum += diff else lossSum -= diff
        }
        for (i in period until n) {
            val diff = closes[i] - closes[i-1]
            gainSum = gainSum * (period-1)/period.toFloat() + if (diff > 0) diff else 0f
            lossSum = lossSum * (period-1)/period.toFloat() + if (diff < 0) -diff else 0f
            val rs = if (lossSum == 0f) 100f else gainSum / lossSum
            out[i] = 100f - 100f / (1f + rs)
        }
        return out
    }
    
    private fun computeATR(period: Int): FloatArray {
        val tr = FloatArray(n) { 0f }
        for (i in 1 until n) tr[i] = abs(closes[i] - closes[i-1])
        return ema(tr, period)
    }
}
