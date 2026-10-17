
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

    private val _resultText = MutableStateFlow("Ready")
    val resultText: StateFlow<String> = _resultText

    private val _reversalsText = MutableStateFlow("")
    val reversalsText: StateFlow<String> = _reversalsText

    fun runAnalysis(
        clientId: String,
        token: String,
        symbol: String,
        timeframe: String,
        dateTime: String
    ) {
        viewModelScope.launch {
            try {
                if (clientId.isBlank() || token.isBlank()) {
                    _resultText.value = "Client ID aur Access Token bharein"
                    return@launch
                }

                _resultText.value = "Fetching market data..."
                _reversalsText.value = ""

                InstrumentRepository.load()

                val inst = InstrumentRepository.find(symbol)
                if (inst == null) {
                    _resultText.value = "Symbol not found: $symbol"
                    return@launch
                }

                val repo = DhanRepository()

                // Repository ko sirf date chahiye, datetime nahi.
                val asOfDate = dateTime.substringBefore('T')

                val bars = repo.fetchCandles(
                    clientId = clientId.trim(),
                    token = token.trim(),
                    symbol = symbol.trim(),
                    timeframe = timeframe.trim(),
                    asOfDate = asOfDate
                )

                if (bars.isEmpty()) {
                    _resultText.value =
                        "No market data received. Check credentials, API access and symbol."
                    return@launch
                }

                val reversals = detectReversals(bars)

                _resultText.value = "Bars received: ${bars.size}"

                _reversalsText.value =
                    if (reversals.isEmpty()) {
                        "No reversal signals detected."
                    } else {
                        reversals.joinToString("\n") {
                            "${it.dateTime} ${if (it.bullish) "BUY" else "SELL"} @ ${it.price}"
                        }
                    }

            } catch (e: Exception) {
                _resultText.value =
                    "Error: ${e.message ?: e.javaClass.simpleName}"
            }
        }
    }

    private data class ReversalSignal(
        val dateTime: String,
        val bullish: Boolean,
        val price: Double
    )

    private fun detectReversals(
        bars: List<DailyBar>
    ): List<ReversalSignal> {
        if (bars.size < 3) return emptyList()

        val signals = mutableListOf<ReversalSignal>()

        for (i in 1 until bars.lastIndex) {
            val previous = bars[i - 1]
            val current = bars[i]
            val next = bars[i + 1]

            val bullish =
                current.low < previous.low &&
                current.low < next.low &&
                current.close > current.open

            val bearish =
                current.high > previous.high &&
                current.high > next.high &&
                current.close < current.open

            if (bullish) {
                signals.add(
                    ReversalSignal(
                        dateTime = current.date,
                        bullish = true,
                        price = current.close
                    )
                )
            } else if (bearish) {
                signals.add(
                    ReversalSignal(
                        dateTime = current.date,
                        bullish = false,
                        price = current.close
                    )
                )
            }
        }

        return signals
    }
}
