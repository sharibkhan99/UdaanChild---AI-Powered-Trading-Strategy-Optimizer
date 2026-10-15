package com.udaan.child.ui.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.udaan.child.forecast.DhanRepository
import com.udaan.child.forecast.ForecastEngine
import com.udaan.child.forecast.ForecastUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel @JvmOverloads constructor(
    application: Application,
    // The use‑case hides all the repository / engine wiring.
    private val forecastUseCase: ForecastUseCase = ForecastUseCase()
) : AndroidViewModel(application) {

    // -----------------------------------------------------------------
    // UI‑state definition – only what the new screen needs.
    // -----------------------------------------------------------------
    sealed interface UiState {
        data class Loading(val message: String) : UiState
        data class ForecastReady(
            val forecasts: Map<DhanRepository.Granularity, ForecastEngine.ForecastResult>
        ) : UiState
        data class Error(val message: String) : UiState
        object Idle : UiState
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState = _uiState.asStateFlow()

    // -----------------------------------------------------------------
    // Public API – called from the Activity / Fragment when the user
    // presses **START OPTIMIZATION** (or any other trigger you like).
    // -----------------------------------------------------------------
    fun loadForecasts(securityId: String = "1333",   // Nifty‑50 default
                      exchange: String = "NSE",
                      horizonDays: Int = 5) {

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = UiState.Loading("Fetching live candles…")
            try {
                // One call – it internally hits Dhan for **all six granularities**
                // and runs the continuous‑time Kalman forecast for each.
                val resultMap = forecastUseCase.getAllForecasts(
                    securityId = securityId,
                    exchange   = exchange,
                    horizonDays = horizonDays
                )

                // Success – push the map to the UI layer.
                _uiState.value = UiState.ForecastReady(resultMap)

            } catch (e: Exception) {
                Log.e("MainViewModel", "Forecast failed", e)
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    // -----------------------------------------------------------------
    // (Optional) If you still want a background‑worker entry point you can
    // keep a thin wrapper – the heavy lifting is already in the use‑case.
    // -----------------------------------------------------------------
    fun scheduleBackgroundForecast(
        securityId: String = "1333",
        exchange: String = "NSE",
        horizonDays: Int = 5
    ) {
        // You can enqueue a WorkManager job that simply calls
        // `forecastUseCase.getAllForecasts(...)` and posts the result
        // via a local broadcast / Room / DataStore if you need persistence.
        // Implementation left out because the current UI‑flow is synchronous
        // and runs on the viewModelScope.
    }
}
