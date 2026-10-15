package com.udaan.child

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.udaan.child.di.NetworkModule
import com.udaan.child.logic.CycleEngine
import com.udaan.child.logic.GannLevels
import com.udaan.child.logic.SignalEngine
import com.udaan.child.network.DhanLogin
import kotlinx.coroutines.launch
import java.time.LocalDate

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Programmatically TextView create kar rahe hain taaki XML layout ki zaroorat na pade
        val statusText = TextView(this).apply {
            text = "Loading Gann Data..."
            textSize = 18f
            setPadding(40, 40, 40, 40)
            setTextColor(android.graphics.Color.WHITE)
            setBackgroundColor(android.graphics.Color.parseColor("#1A1A2E"))
        }
        setContentView(statusText)

        lifecycleScope.launch {
            try {
                statusText.text = "Authenticating..."
                DhanLogin.generateAccessToken()
                
                statusText.text = "Fetching Market Data..."
                val repo = NetworkModule.marketDataRepo
                val candles = repo.fetchDailyCandles(fromDate = "2025-01-01", toDate = "2026-10-10")
                
                val high = candles.maxOf { it.high.toDouble() }
                val low = candles.minOf { it.low.toDouble() }
                
                val (top, bot) = CycleEngine.findLastTopBottom(candles)
                val cycle = CycleEngine.compute(LocalDate.now(), top, bot)
                val g50 = GannLevels.g50(high, low)
                val scale = (high - low) / 100.0
                
                val cfg = SignalEngine.Config(
                    g50,
                    GannLevels.angle45(low, candles.size, true, scale),
                    GannLevels.angle2x1(low, candles.size, true, scale),
                    GannLevels.angle4x1(low, candles.size, true, scale),
                    cycle.monthsFromTop, cycle.monthsFromBottom, cycle.activeCycles, cycle.digit
                )
                val signal = SignalEngine.evaluate(candles.last(), cfg)
                
                statusText.text = "Signal: ${signal.signal}\nConfidence: ${signal.confidence}"
                Log.d("GANN", "${signal.signal} conf=${signal.confidence}")
            } catch (e: Exception) { 
                statusText.text = "Error: ${e.localizedMessage}"
                Log.e("GANN", "Error", e) 
            }
        }
    }
}
