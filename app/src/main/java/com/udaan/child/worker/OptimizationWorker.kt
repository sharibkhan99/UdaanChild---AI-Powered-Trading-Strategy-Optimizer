package com.udaan.child.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.ListenableWorker
import com.udaan.child.data.local.PreferencesManager
import com.udaan.child.data.model.DailyBar
import com.udaan.child.data.model.GAResult
import com.udaan.child.data.repository.MarketDataRepository
import com.udaan.child.data.repository.MarketDataRepositoryImpl
import com.udaan.child.optimizer.GeneticOptimizer
import kotlinx.coroutines.Dispatchers

/**
 * CoroutineWorker that runs the genetic optimisation in the background.
 * The required dependencies are created from the supplied Context – this
 * keeps the worker’s constructor compatible with WorkManager.
 */
class OptimizationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    // ---------------------------------------------------------------------
    // Dependencies – instantiated locally (no extra constructor params)
    // ---------------------------------------------------------------------
    private val repository: MarketDataRepository = MarketDataRepositoryImpl(applicationContext)
    private val prefs: PreferencesManager = PreferencesManager(applicationContext)

    override suspend fun doWork(): ListenableWorker.Result {
        return try {
            val symbol = inputData.getString(KEY_SYMBOL) ?: "NIFTY"
            val bars = repository.getHistoricalData(symbol)

            if (bars.size < 500) {
                // Not enough data – ask WorkManager to retry later (exponential back‑off)
                ListenableWorker.Result.retry()
            } else {
                val optimizer = GeneticOptimizer()
                val result = optimizer.optimize(bars)

                prefs.saveOptimizationResult(result)

                ListenableWorker.Result.success()
            }
        } catch (e: Exception) {
            // Any unexpected error → retry (you may want a more sophisticated policy)
            ListenableWorker.Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "OptimizationWorker"
        const val KEY_SYMBOL = "SYMBOL"
    }
}
