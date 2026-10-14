package com.udaan.child.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.udaan.child.data.model.GAResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

/**
 * Thin wrapper around a **Preferences DataStore**.
 * All public functions are suspend – call them from a coroutine scope
 * (viewModelScope, lifecycleScope, …).
 */
class PreferencesManager(private val context: Context) {

    // ---------------------------------------------------------------------
    // DataStore instance (created once per process)
    // ---------------------------------------------------------------------
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
    private val dataStore = context.dataStore

    // ---------------------------------------------------------------------
    // Preference keys – typed, so the compiler knows the value type
    // ---------------------------------------------------------------------
    private val floatKey          = floatPreferencesKey("float_key")
    private val stringKey         = stringPreferencesKey("string_key")
    private val longKey           = longPreferencesKey("long_key")
    private val gaResultKey       = stringPreferencesKey("ga_result_json")   // stores GAResult as JSON

    // ---------------------------------------------------------------------
    // Simple scalar helpers (already used elsewhere)
    // ---------------------------------------------------------------------
    suspend fun putFloat(value: Float) { dataStore.edit { it[floatKey] = value } }
    suspend fun getFloat(defaultValue: Float = 0f): Float =
        dataStore.data.map { it[floatKey] ?: defaultValue }.first()

    suspend fun putString(value: String) { dataStore.edit { it[stringKey] = value } }
    suspend fun getString(defaultValue: String = ""): String =
        dataStore.data.map { it[stringKey] ?: defaultValue }.first()

    suspend fun putLong(value: Long) { dataStore.edit { it[longKey] = value } }
    suspend fun getLong(defaultValue: Long = 0L): Long =
        dataStore.data.map { it[longKey] ?: defaultValue }.first()

    // ---------------------------------------------------------------------
    // GAResult persistence (JSON ↔️ DataStore)
    // ---------------------------------------------------------------------
    private val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }

    /** Save the whole optimisation result */
    suspend fun saveOptimizationResult(result: GAResult) {
        val jsonString = json.encodeToString(GAResult.serializer(), result)
        dataStore.edit { it[gaResultKey] = jsonString }
    }

    /** Retrieve the last saved optimisation result (or null) */
    suspend fun getSavedOptimizationResult(): GAResult? {
        val jsonString = dataStore.data.map { it[gaResultKey] }.first()
        return if (jsonString.isNullOrBlank()) null
        else json.decodeFromString(GAResult.serializer(), jsonString)
    }

    // ---------------------------------------------------------------------
    // Convenience getters used by MainViewModel / Worker
    // ---------------------------------------------------------------------
    suspend fun getBestChromosome() = getSavedOptimizationResult()?.bestChromosome
    suspend fun getBestSharpe()     = getSavedOptimizationResult()?.bestSharpe ?: 0f
    suspend fun getActiveFeatures() = getSavedOptimizationResult()?.activeFeatures ?: emptyList()
    suspend fun getLastOptimizationTime() = getSavedOptimizationResult()?.timestamp ?: 0L
}
