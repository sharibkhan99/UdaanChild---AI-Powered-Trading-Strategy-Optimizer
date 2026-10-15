package com.udaan.child.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.ListenableWorker
import com.udaan.child.data.repository.MarketDataRepositoryImpl
import com.udaan.child.optimizer.GeneticOptimizer

class OptimizationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): ListenableWorker.Result {
        return try {
            val symbol = inputData.getString(KEY_SYMBOL) ?: "NIFTY"
            val repo = MarketDataRepositoryImpl()
            val bars = repo.getHistoricalData(symbol)

            if (bars.size < 100) {
                return ListenableWorker.Result.retry()
            }

            val optimizer = GeneticOptimizer()
            optimizer.optimize(bars)

            ListenableWorker.Result.success()
        } catch (e: Exception) {
            ListenableWorker.Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "OptimizationWorker"
        const val KEY_SYMBOL = "SYMBOL"
    }
}
