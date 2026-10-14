package com.udaan.child.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.udaan.child.data.local.PreferencesManager
import com.udaan.child.data.model.Chromosome
import com.udaan.child.data.model.GAResult
import com.udaan.child.data.repository.MarketDataRepository
import com.udaan.child.optimizer.GeneticOptimizer
import com.udaan.child.worker.OptimizationWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.TimeUnit

class MainViewModel(
    private val repository: MarketDataRepository,
    private val prefs: PreferencesManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(UiState.Idle)
    val uiState = _uiState.asStateFlow()
    
    sealed interface UiState {
        data class Optimizing(val progress: String) : UiState
        data class Result(val result: GAResult) : UiState
        data class Error(val message: String) : UiState
        object Idle : UiState
    }
    
    fun triggerOptimization(symbol: String = "NIFTY") {
        viewModelScope.launch {
            _uiState.value = UiState.Optimizing("Fetching data...")
            try {
                val bars = repository.getHistoricalData(symbol)
                if (bars.size < 500) {
                    _uiState.value = UiState.Error("Insufficient data (need 500+ bars)")
                    return@launch
                }
                
                _uiState.value = UiState.Optimizing("Running Genetic Algorithm...")
                val optimizer = GeneticOptimizer()
                val result = optimizer.optimize(bars)
                
                prefs.saveOptimizationResult(result)
                _uiState.value = UiState.Result(result)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    fun scheduleBackgroundOptimization(symbol: String = "NIFTY") {
        val work = OneTimeWorkRequestBuilder<OptimizationWorker>()
            .setInputData(androidx.work.Data.Builder().putString(OptimizationWorker.KEY_SYMBOL, symbol).build())
            .setInitialDelay(1, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance().enqueueUniqueWork(
            OptimizationWorker.WORK_NAME,
            androidx.work.ExistingWorkPolicy.REPLACE,
            work
        )
    }
    
    suspend fun loadSavedResult(): GAResult? {
        val chromo = prefs.getBestChromosome() ?: return null
        val sharpe = prefs.getBestSharpe()
        val features = prefs.getActiveFeatures()
        return GAResult(chromo, sharpe, features, emptyList(), prefs.getLastOptimizationTime())
    }
}
