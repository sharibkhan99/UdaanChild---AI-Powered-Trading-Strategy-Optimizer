package com.udaan.child.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.ListenableWorker
import com.udaan.child.data.local.PreferencesManager
import com.udaan.child.data.model.DailyBar
import com.udaan.child.data.model.GAResult
import com.udaan.child.data.repository.MarketDataRepository
import com.udaan.child.optimizer.GeneticOptimizer

class OptimizationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): ListenableWorker.Result {
        return try {
            val symbol = inputData.getString(KEY_SYMBOL) ?: "NIFTY"
            
            // TODO: Get real instances from your DI or Application class
            // For now, this will fail at runtime until you wire dependencies
            val repository = getRepository()
            val prefs = getPrefs()
            
            val bars = repository.getHistoricalData(symbol)

            if (bars.size < 500) {
                return ListenableWorker.Result.retry()
            }

            val optimizer = GeneticOptimizer()
            val result = optimizer.optimize(bars)
            prefs.saveOptimizationResult(result)

            ListenableWorker.Result.success()
        } catch (e: Exception) {
            ListenableWorker.Result.retry()
        }
    }

    private fun getRepository(): MarketDataRepository {
        // Replace with your actual DI / Application singleton
        throw UnsupportedOperationException("Wire MarketDataRepository via DI")
    }

    private fun getPrefs(): PreferencesManager {
        // Replace with your actual DI / Application singleton
        throw UnsupportedOperationException("Wire PreferencesManager via DI")
    }

    companion object {
        const val WORK_NAME = "OptimizationWorker"
        const val KEY_SYMBOL = "SYMBOL"
    }
}
