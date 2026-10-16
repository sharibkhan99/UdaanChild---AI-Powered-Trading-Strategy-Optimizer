package com.udaan.child.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.udaan.child.data.model.DailyBar
import com.udaan.child.forecast.DhanRepository
import com.udaan.child.forecast.ForecastEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val dhanRepo = DhanRepository()

    private val _resultText = MutableStateFlow("Ready")
    val resultText: StateFlow<String> = _resultText.asStateFlow()

    private val _reversalsText = MutableStateFlow("")
    val reversalsText: StateFlow<String> = _reversalsText.asStateFlow()

    fun runAnalysis(clientId: String, token: String, symbol: String, timeframe: String, asOfDate: String) {
        viewModelScope.launch {
            _resultText.value = "Fetching $timeframe data..."
            try {
                val bars = dhanRepo.fetchCandles(clientId, token, symbol, timeframe, asOfDate)
                if (bars.isEmpty()) {
                    _resultText.value = "No data returned"
                    return@launch
                }

                val forecast = ForecastEngine.forecast(bars, 1)
                _resultText.value = "Last: ${"%.2f".format(bars.last().close)} | Forecast: ${"%.2f".format(forecast.value)}"

                val reversals = detectReversals(bars)
                _reversalsText.value = reversals.joinToString("\n") { r ->
                    "${r.date} — ${if (r.bullish) "BULLISH" else "BEARISH"} @ ${"%.2f".format(r.price)}"
                }
            } catch (e: Exception) {
                _resultText.value = "Error: ${e.message}"
            }
        }
    }

    private fun detectReversals(bars: List<DailyBar>): List<Reversal> {
        val result = mutableListOf<Reversal>()
        if (bars.size < 5) return result
        for (i in 2 until bars.size - 2) {
            val prev = bars[i - 1].close
            val curr = bars[i].close
            val next = bars[i + 1].close
            if (prev < curr && curr > next) {
                result.add(Reversal(bars[i].date, false, curr))
            } else if (prev > curr && curr < next) {
                result.add(Reversal(bars[i].date, true, curr))
            }
        }
        return result.takeLast(10)
    }

    data class Reversal(val date: String, val bullish: Boolean, val price: Double)
}
