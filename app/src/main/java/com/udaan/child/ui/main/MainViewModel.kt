package com.udaan.child.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.udaan.child.data.model.DailyBar
import com.udaan.child.data.repository.InstrumentRepository
import com.udaan.child.forecast.DhanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val _resultText = MutableStateFlow("")
    val resultText: StateFlow<String> = _resultText

    private val _reversalsText = MutableStateFlow("")
    val reversalsText: StateFlow<String> = _reversalsText

    fun runAnalysis(clientId: String, token: String, symbol: String, timeframe: String, dateTime: String) {
        viewModelScope.launch {
            try {
                InstrumentRepository.load()
                val repo = DhanRepository()
                val bars = repo.fetchCandles(token, symbol, timeframe, dateTime)
                if (bars.isEmpty()) {
                    _resultText.value = "No data"
                    return@launch
                }
                val reversals = detectReversals(bars)
                _resultText.value = "Bars: ${bars.size}"
                _reversalsText.value = reversals.joinToString("\n") { 
                    "${it.dateTime} ${if (it.bullish) "BUY" else "SELL"} @ ${it.price}" 
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
}

data class Reversal(val dateTime: String, val bullish: Boolean, val price: Double)
