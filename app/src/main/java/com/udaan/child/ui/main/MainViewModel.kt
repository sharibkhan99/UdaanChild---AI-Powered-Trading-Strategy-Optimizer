
package com.udaan.child.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.udaan.child.data.repository.InstrumentRepository
import com.udaan.child.forecast.BacktestEngine
import com.udaan.child.forecast.DhanRepository
import com.udaan.child.forecast.KalmanForecast
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.DecimalFormat

class MainViewModel : ViewModel() {

    private val _resultText = MutableStateFlow("Ready")
    val resultText: StateFlow<String> = _resultText

    private val _analysisText = MutableStateFlow("")
    val analysisText: StateFlow<String> = _analysisText

    fun runAnalysis(
        clientId: String,
        token: String,
        symbol: String,
        timeframe: String,
        fromDate: String,
        toDate: String
    ) {
        viewModelScope.launch {
            try {
                if (clientId.isBlank() || token.isBlank()) {
                    _resultText.value =
                        "Client ID और Access Token भरें।"
                    return@launch
                }

                _resultText.value = "Fetching Dhan market data..."
                _analysisText.value = ""

                InstrumentRepository.load()

                if (InstrumentRepository.find(symbol) == null) {
                    _resultText.value = "Symbol नहीं मिला: $symbol"
                    return@launch
                }

                val result = DhanRepository().fetchCandles(
                    clientId,
                    token,
                    symbol.trim(),
                    timeframe,
                    fromDate.trim(),
                    toDate.trim()
                )

                if (result.bars.isEmpty()) {
                    _resultText.value =
                        result.error ?: "Market data नहीं मिला।"
                    return@launch
                }

                val bars = result.bars
                val fmt = DecimalFormat("#,##0.00")
                val predictions = KalmanForecast.predict(bars, timeframe)
                val bt = BacktestEngine.run(bars)

                _resultText.value =
                    "Data OK: ${bars.size} candles | " +
                    "${bars.first().date} → ${bars.last().date}"

                _analysisText.value = buildString {
                    appendLine("KALMAN FILTER PREDICTION (estimate only)")
                    appendLine(
                        "Symbol: ${symbol.uppercase()} | Timeframe: $timeframe"
                    )
                    appendLine(
                        "Current close: ${fmt.format(bars.last().close)}"
                    )

                    predictions.forEach {
                        appendLine("\n${it.horizon}")
                        appendLine(
                            "Estimated date/time: ${it.targetDateTime}"
                        )
                        appendLine(
                            "Predicted price: ${fmt.format(it.predictedPrice)}"
                        )
                        appendLine(
                            "Possible range: ${fmt.format(it.lowerPrice)} – " +
                            fmt.format(it.upperPrice)
                        )
                        appendLine("Signal: ${it.signal}")
                    }

                    appendLine("\nDATE-RANGE WALK-FORWARD BACKTEST")
                    appendLine(
                        "Trades: ${bt.trades} | Wins: ${bt.wins} | " +
                        "Win rate: ${fmt.format(bt.winRate)}%"
                    )
                    appendLine(
                        "Net points (before charges/slippage): " +
                        fmt.format(bt.netPoints)
                    )
                    appendLine(
                        "Max drawdown (points): " +
                        fmt.format(bt.maxDrawdown)
                    )
                    appendLine(
                        "\nNote: estimates are not guaranteed. " +
                        "Backtest excludes brokerage, taxes, slippage " +
                        "and live execution effects."
                    )
                }
            } catch (e: Exception) {
                _resultText.value =
                    "Error: ${e.message ?: e.javaClass.simpleName}"
            }
        }
    }
}
