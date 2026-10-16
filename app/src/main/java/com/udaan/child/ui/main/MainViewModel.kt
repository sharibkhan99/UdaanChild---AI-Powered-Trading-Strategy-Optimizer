package com.udaan.child.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.udaan.child.data.model.DailyBar
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

    private val _forecastResult = MutableStateFlow<ForecastEngine.ForecastResult?>(null)
    val forecastResult: StateFlow<ForecastEngine.ForecastResult?> = _forecastResult.asStateFlow()

    private val _gaResult = MutableStateFlow<GAResult?>(null)
    val gaResult: StateFlow<GAResult?> = _gaResult.asStateFlow()

    private val _customForecastResult = MutableStateFlow<ForecastEngine.ForecastResult?>(null)
    val customForecastResult: StateFlow<ForecastEngine.ForecastResult?> = _customForecastResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun runAnalysis(symbol: String = "NIFTY") {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val forecast: ForecastEngine.ForecastResult = forecastUseCase.run(symbol, hSteps = 1)
                _forecastResult.value = forecast

                val dailyBars: List<DailyBar> = dhanRepo.getHistoricalData(symbol)
                val optimizationResult = geneticOptimizer.optimize(dailyBars)
                _gaResult.value = optimizationResult

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun runCustomForecast(inputString: String) {
        viewModelScope.launch {
            try {
                val numbers: List<Double> = inputString.split(",")
                    .map { token: String -> token.trim().toDoubleOrNull() }
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
