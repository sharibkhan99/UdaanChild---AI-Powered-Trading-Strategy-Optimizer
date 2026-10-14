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
import kotlinx.coroutines.Dispatchers

/**
 * A **CoroutineWorker** that runs the genetic optimisation in the background.
 *
 * Because a `CoroutineWorker` must have a single‑argument constructor
 * `(Context, WorkerParameters)`, the real dependencies (`MarketDataRepository`
 * and `PreferencesManager`) are obtained from a tiny *service‑locator* object
 * (`Injection`).  In a production app you would normally wire this with Hilt,
 * Koin, Dagger, etc.; the locator below is only a compile‑time placeholder
 * that you can replace with your DI framework of choice.
 */
class OptimizationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    // ---------------------------------------------------------------------
    // Dependencies – resolved at runtime via the `Injection` helper.
    // ---------------------------------------------------------------------
    private val repository: MarketDataRepository = Injection.repository
    private val prefs: PreferencesManager = Injection.prefs

    override suspend fun doWork(): ListenableWorker.Result = try {
        val symbol = inputData.getString(KEY_SYMBOL) ?: "NIFTY"
        val bars = repository.getHistoricalData(symbol)

        if (bars.size < 500) {
            // Not enough data – ask WorkManager to retry later (exponential back‑off)
            return@doWork ListenableWorker.Result.retry()
        }

        val optimizer = GeneticOptimizer()
        val result = optimizer.optimize(bars)

        prefs.saveOptimizationResult(result)

        ListenableWorker.Result.success()
    } catch (e: Exception) {
        // Any unexpected error → retry (you may want a more sophisticated policy)
        ListenableWorker.Result.retry()
    }

    companion object {
        const val WORK_NAME = "OptimizationWorker"
        const val KEY_SYMBOL = "SYMBOL"
    }
}

/**
 * Very small *service‑locator* used only to keep the worker’s constructor
 * compatible with `CoroutineWorker`.  Replace the `object Injection` with
 * your real DI graph (Hilt/Koin/Dagger/Manual) – the worker itself does not
 * need to change.
 */
private object Injection {
    // TODO: initialise these from your Application / DI container.
    // Example (manual):
    //   val repository: MarketDataRepository = MyApplication.repository
    //   val prefs: PreferencesManager = MyApplication.prefs
    @Suppress("UNUSED_PARAMETER")
    val repository: MarketDataRepository
        get() = error("Injection.repository not initialised – provide a real instance via your DI framework")

    @Suppress("UNUSED_PARAMETER")
    val prefs: PreferencesManager
        get() = error("Injection.prefs not initialised – provide a real instance via your DI framework")
}
