package com.udaan.child.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.udaan.child.data.model.Candle
import com.udaan.child.data.model.GAResult
import com.udaan.child.forecast.ForecastEngine
import com.udaan.child.forecast.ForecastUseCase
import com.udaan.child.forecast.DhanRepository
import com.udaan.child.optimizer.GeneticOptimizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val dhanRepo = DhanRepository()
    private val forecastUseCase = ForecastUseCase(dhanRepo)
    private val geneticOptimizer = GeneticOptimizer()

    // UI States for Live Market Analysis
    private val _forecastResult = MutableStateFlow<ForecastEngine.ForecastResult?>(null)
    val forecastResult: StateFlow<ForecastEngine.ForecastResult?> = _forecastResult.asStateFlow()

    private val _gaResult = MutableStateFlow<GAResult?>(null)
    val gaResult: StateFlow<GAResult?> = _gaResult.asStateFlow()

    // UI State for Custom User Input Forecast
    private val _customForecastResult = MutableStateFlow<Double?>(null)
    val customForecastResult: StateFlow<Double?> = _customForecastResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 1. Live Market Data Analysis & Optimization
    fun runAnalysis(symbol: String = "NIFTY") {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val dailyBars = dhanRepo.getHistoricalData(symbol)
                val candles = dailyBars.map { bar ->
                    Candle(
                        timestamp = System.currentTimeMillis(),
                        open = bar.open,
                        high = bar.high,
                        low = bar.low,
                        close = bar.close,
                        volume = bar.volume
                    )
                }

                if (candles.size >= 30) {
                    val forecast = ForecastEngine.forecast(candles, hSteps = 1)
                    _forecastResult.value = forecast
                }

                val optimizationResult = geneticOptimizer.optimize(dailyBars)
                _gaResult.value = optimizationResult

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 2. Custom User-Provided Input Forecast
    fun runCustomForecast(inputString: String) {
        viewModelScope.launch {
            try {
                val numbers = inputString.split(",")
                    .map { it.trim().toDoubleOrNull() }
                    .filterNotNull()

                if (numbers.isNotEmpty()) {
                    val prediction = ForecastEngine.forecastCustom(numbers, hSteps = 1)
                    _customForecastResult.value = prediction
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
