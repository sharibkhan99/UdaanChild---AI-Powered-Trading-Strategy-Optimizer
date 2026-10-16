package com.udaan.child

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.udaan.child.di.NetworkModule
import com.udaan.child.network.DhanLogin
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val statusText = TextView(this).apply {
            text = "Initializing UdaanChild App..."
            textSize = 16f
            setPadding(40, 40, 40, 40)
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#1A1A2E"))
        }
        setContentView(statusText)

        lifecycleScope.launch {
            try {
                statusText.text = "Authenticating..."
                DhanLogin.generateAccessToken()
                
                statusText.text = "Fetching Market Data..."
                val repo = NetworkModule.marketDataRepo
                val candles = repo.getHistoricalData("NIFTY")
                
                if (candles.isEmpty()) {
                    statusText.text = "Error: No candle data received."
                    return@launch
                }
                
                val latestClose = candles.last().close
                val high = candles.maxOf { it.high.toDouble() }
                val low = candles.minOf { it.low.toDouble() }
                
                statusText.text = "Market Data Loaded!\nLatest Close: $latestClose\nHigh: $high | Low: $low"
                Log.d("UDAAN", "Loaded ${candles.size} candles successfully.")
            } catch (e: Exception) { 
                statusText.text = "Error: ${e.localizedMessage ?: "Unknown error"}"
                Log.e("UDAAN", "Execution Error", e) 
            }
        }
    }
}
