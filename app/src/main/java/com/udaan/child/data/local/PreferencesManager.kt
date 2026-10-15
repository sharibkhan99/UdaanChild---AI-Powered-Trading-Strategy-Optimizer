package com.udaan.child.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.udaan.child.data.model.GAResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "udaan_prefs")

class PreferencesManager(private val context: Context) {
    private val BEST_SHARPE = floatPreferencesKey("best_sharpe")
    private val LAST_OPT_TIME = longPreferencesKey("last_opt_time")

    suspend fun saveOptimizationResult(result: GAResult) {
        context.dataStore.edit { prefs ->
            prefs[BEST_SHARPE] = result.bestSharpe.toFloat()
            prefs[LAST_OPT_TIME] = System.currentTimeMillis()
        }
    }

    fun getBestSharpe(): Flow<Float> = context.dataStore.data.map { it[BEST_SHARPE] ?: 0f }
    fun getLastOptimizationTime(): Flow<Long> = context.dataStore.data.map { it[LAST_OPT_TIME] ?: 0L }
}
