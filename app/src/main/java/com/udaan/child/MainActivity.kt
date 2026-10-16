package com.udaan.child

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

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
                statusText.text = "Fetching Market Data..."
                val closes = withContext(Dispatchers.IO) {
                    fetchClosePrices()
                }
                
                if (closes.isEmpty()) {
                    statusText.text = "UdaanChild Engine Active\nStatus: Running (Offline Mode)"
                    Log.d("UDAAN", "Running in offline/fallback mode.")
                } else {
                    statusText.text = "UdaanChild Engine Active!\nLoaded ${closes.size} Candles\nLatest Close: ${closes.last()}"
                    Log.d("UDAAN", "Loaded ${closes.size} prices successfully.")
                }
            } catch (e: Exception) { 
                statusText.text = "UdaanChild Engine Active\nStatus: Ready"
                Log.e("UDAAN", "Execution Error", e) 
            }
        }
    }

    private fun fetchClosePrices(): List<Double> {
        val list = mutableListOf<Double>()
        try {
            val urlString = "https://query1.finance.yahoo.com/v8/finance/chart/%5ENSEI?range=1mo&interval=1d"
            val url = URL(urlString)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("User-Agent", "Mozilla/5.0")
                connectTimeout = 5000
                readTimeout = 5000
            }
            if (conn.responseCode == 200) {
                val response = conn.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(response)
                val result = root.getJSONObject("chart").getJSONArray("result").getJSONObject(0)
                val quote = result.getJSONObject("indicators").getJSONArray("quote").getJSONObject(0)
                val closes = quote.getJSONArray("close")
                for (i in 0 until closes.length()) {
                    if (!closes.isNull(i)) {
                        list.add(closes.getDouble(i))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("UDAAN", "Net fetch error", e)
        }
        return list
    }
}
