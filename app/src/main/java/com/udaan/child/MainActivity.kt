package com.udaan.child

import android.graphics.Color
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
        
        val statusText = TextView(this).apply {
            text = "Initializing Gann Engine..."
            textSize = 16f
            setPadding(40, 40, 40, 40)
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#1A1A2E"))
        }
        setContentView(statusText)

        lifecycleScope.launch {
            try {
                statusText.text = "Authenticating with Dhan..."
                DhanLogin.generateAccessToken()
                
                statusText.text = "Fetching Market Data..."
                val repo = NetworkModule.marketDataRepo
                val candles = repo.fetchDailyCandles(fromDate = "2025-01-01", toDate = "2026-10-10")
                
                if (candles.isEmpty()) {
                    statusText.text = "Error: No candle data received."
                    return@launch
                }
                
                // Explicit Double conversion to avoid any BigDecimal ambiguity
                val high = candles.maxOf { it.high.toDouble() }
                val low = candles.minOf { it.low.toDouble() }
                
                // Avoid destructuring ambiguity by accessing object properties or safe calls
                val topBottom = CycleEngine.findLastTopBottom(candles)
                val top = topBottom.first.toDouble()
                val bot = topBottom.second.toDouble()

                val cycle = CycleEngine.compute(LocalDate.now(), top, bot)
                val g50 = GannLevels.g50(high, low)
                val scale = if ((high - low) > 0.0) (high - low) / 100.0 else 1.0
                
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
                statusText.text = "Error: ${e.localizedMessage ?: "Unknown error"}"
                Log.e("GANN", "Execution Error", e) 
            }
        }
    }
}
