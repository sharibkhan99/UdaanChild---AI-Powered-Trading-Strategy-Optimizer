package com.udaan.child.ui.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.ExistingWorkPolicy
import com.udaan.child.data.local.PreferencesManager
import com.udaan.child.data.model.GAResult
import com.udaan.child.data.repository.MarketDataRepository
import com.udaan.child.data.repository.MarketDataRepositoryImpl
import com.udaan.child.optimizer.GeneticOptimizer
import com.udaan.child.worker.OptimizationWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: MarketDataRepository = MarketDataRepositoryImpl(application),
    private val prefs: PreferencesManager = PreferencesManager(application)
) : AndroidViewModel(application) {

    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState.Idle)
    val uiState = _uiState.asStateFlow()

    sealed interface UiState {
        data class Optimizing(val progress: String) : UiState
        data class Result(val result: GAResult) : UiState
        data class Error(val message: String) : UiState
        object Idle : UiState
    }

    fun triggerOptimization(symbol: String = "NIFTY") {
        viewModelScope.launch {
            _uiState.value = UiState.Optimizing("Fetching data…")
            try {
                val bars = repository.getHistoricalData(symbol)
                if (bars.size < 500) {
                    _uiState.value = UiState.Error("Insufficient data (need 500+ bars)")
                    return@launch
                }

                _uiState.value = UiState.Optimizing("Running Genetic Algorithm…")
                val optimizer = GeneticOptimizer()
                val result = optimizer.optimize(bars)

                _uiState.value = UiState.Result(result)
            } catch (e: Exception) {
                Log.e("MainViewModel", "Optimization failed", e)
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun scheduleBackgroundOptimization(symbol: String = "NIFTY") {
        val work = OneTimeWorkRequestBuilder<OptimizationWorker>()
            .setInputData(
                androidx.work.Data.Builder()
                    .putString(OptimizationWorker.KEY_SYMBOL, symbol)
                    .build()
            )
            .setInitialDelay(1, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(getApplication())
            .enqueueUniqueWork(
                OptimizationWorker.WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                work
            )
    }
}
