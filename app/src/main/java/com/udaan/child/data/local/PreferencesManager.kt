package com.udaan.child.data.local

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

/**
 * Thin wrapper around a **Preferences DataStore**.
 * All public functions are suspend – call them from a coroutine scope (viewModelScope, lifecycleScope, etc.).
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
    private val floatKey  = floatPreferencesKey("float_key")
    private val stringKey = stringPreferencesKey("string_key")
    private val longKey   = longPreferencesKey("long_key")

    // ---------------------------------------------------------------------
    // Float
    // ---------------------------------------------------------------------
    suspend fun putFloat(value: Float) {
        dataStore.edit { it[floatKey] = value }
    }

    suspend fun getFloat(defaultValue: Float = 0f): Float {
        return dataStore.data
            .map { it[floatKey] ?: defaultValue }
            .first()
    }

    // ---------------------------------------------------------------------
    // String
    // ---------------------------------------------------------------------
    suspend fun putString(value: String) {
        dataStore.edit { it[stringKey] = value }
    }

    suspend fun getString(defaultValue: String = ""): String {
        return dataStore.data
            .map { it[stringKey] ?: defaultValue }
            .first()
    }

    // ---------------------------------------------------------------------
    // Long
    // ---------------------------------------------------------------------
    suspend fun putLong(value: Long) {
        dataStore.edit { it[longKey] = value }
    }

    suspend fun getLong(defaultValue: Long = 0L): Long {
        return dataStore.data
            .map { it[longKey] ?: defaultValue }
            .first()
    }
}
