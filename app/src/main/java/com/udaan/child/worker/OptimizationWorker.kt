package com.udaan.child.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.udaan.child.data.local.PreferencesManager
import com.udaan.child.data.model.DailyBar
import com.udaan.child.data.model.GAResult
import com.udaan.child.data.repository.MarketDataRepository
import com.udaan.child.optimizer.GeneticOptimizer
import kotlinx.coroutines.Dispatchers

class OptimizationWorker(
    context: Context,
    params: WorkerParameters,
    private val repository: MarketDataRepository,
    private val prefs: PreferencesManager
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result = try {
        val symbol = inputData.getString("SYMBOL") ?: "NIFTY"
        val bars = repository.getHistoricalData(symbol)
        
        if (bars.size < 500) {
            return@doWork Result.retry()
        }
        
        val optimizer = GeneticOptimizer()
        val result = optimizer.optimize(bars)
        
        prefs.saveOptimizationResult(result)
        
        Result.success()
    } catch (e: Exception) {
        Result.retry()
    }
    
    companion object {
        const val WORK_NAME = "OptimizationWorker"
        const val KEY_SYMBOL = "SYMBOL"
    }
}
