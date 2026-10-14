package com.udaan.child.ui.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.ExistingWorkPolicy
import com.udaan.child.data.local.PreferencesManager
import com.udaan.child.data.model.Chromosome
import com.udaan.child.data.model.GAResult
import com.udaan.child.data.repository.MarketDataRepository
import com.udaan.child.optimizer.GeneticOptimizer
import com.udaan.child.worker.OptimizationWorker
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.TimeUnit

class MainViewModel(
    private val repository: MarketDataRepository,
    private val prefs: PreferencesManager
) : ViewModel() {

    /*  Explicitly type the MutableStateFlow as the sealed interface **UiState**.
        Without the explicit type Kotlin infers the type as the singleton object
        `UiState.Idle`, which later makes assignments of `Optimizing`, `Result`,
        `Error` a type‑mismatch. */
    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState.Idle)
    val uiState = _uiState.asStateFlow()

    sealed interface UiState {
        data class Optimizing(val progress: String) : UiState
        data class Result(val result: GAResult) : UiState
        data class Error(val message: String) : UiState
        object Idle : UiState
    }

    /** Starts a foreground optimisation run. */
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

                // Persist the result – the concrete implementation lives in PreferencesManager
                prefs.saveOptimizationResult(result)

                _uiState.value = UiState.Result(result)
            } catch (e: Exception) {
                Log.e("MainViewModel", "Optimization failed", e)
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    /** Schedules a one‑shot background optimisation via WorkManager. */
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

    /** Loads a previously saved optimisation result (if any). */
    suspend fun loadSavedResult(): GAResult? {
        val chromo = prefs.getBestChromosome() ?: return null
        val sharpe = prefs.getBestSharpe()
        val features = prefs.getActiveFeatures()
        val timestamp = prefs.getLastOptimizationTime()
        return GAResult(chromo, sharpe, features, emptyList(), timestamp)
    }

    /** Helper to obtain a `Context` for WorkManager without leaking the ViewModel. */
    private fun getApplication() = (repository as? MarketDataRepository)?.let {
        // If your repository implementation holds a Context (e.g. via a `context` property)
        // expose it here.  Otherwise replace with a proper `Application` reference
        // (e.g. via Hilt/Koin injection).
        (it as? { val context: android.content.Context })?.context
    } ?: throw IllegalStateException("Repository does not provide a Context for WorkManager")
}
