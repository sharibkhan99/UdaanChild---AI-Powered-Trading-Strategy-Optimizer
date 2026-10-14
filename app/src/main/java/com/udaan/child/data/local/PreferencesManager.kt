package com.udaan.child.data.local

import android.content.Context
import androidx.datastore.preferences.core.mutations
import androidx.datastore.preferences.core.preferencesKey
import androidx.datastore.preferences.rx.preferencesDataStore
import com.udaan.child.data.model.Chromosome
import com.udaan.child.data.model.GAResult
import com.google.gson.Gson
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.concurrent.Executors

class PreferencesManager(private val context: Context) {
    
    private val Context.dataStore by preferencesDataStore(name = "udaan_prefs")
    private val gson = Gson()
    
    private val BEST_CHROMOSOME_JSON = stringPreferencesKey("best_chromosome_json")
    private val BEST_SHARPE = floatPreferencesKey("best_sharpe")
    private val ACTIVE_FEATURES_JSON = stringPreferencesKey("active_features_json")
    private val LAST_OPTIMIZATION_TIME = longPreferencesKey("last_optimization_time")
    
    suspend fun saveOptimizationResult(result: GAResult) {
        context.dataStore.edit { prefs ->
            prefs[BEST_CHROMOSOME_JSON] = gson.toJson(result.bestChromosome)
            prefs[BEST_SHARPE] = result.bestSharpe
            prefs[ACTIVE_FEATURES_JSON] = gson.toJson(result.activeFeatures)
            prefs[LAST_OPTIMIZATION_TIME] = result.timestamp
        }
    }
    
    suspend fun getBestChromosome(): Chromosome? {
        val json = context.dataStore.data.map { it[BEST_CHROMOSOME_JSON] }.first()
        return json?.let { gson.fromJson(it, Chromosome::class.java) }
    }
    
    suspend fun getBestSharpe(): Float = context.dataStore.data.map { it[BEST_SHARPE] ?: -1f }.first()
    
    suspend fun getActiveFeatures(): List<String> {
        val json = context.dataStore.data.map { it[ACTIVE_FEATURES_JSON] }.first()
        return json?.let { gson.fromJson(it, Array<String>::class.java).toList() } ?: emptyList()
    }
    
    suspend fun getLastOptimizationTime(): Long = context.dataStore.data.map { it[LAST_OPTIMIZATION_TIME] ?: 0L }.first()
}
