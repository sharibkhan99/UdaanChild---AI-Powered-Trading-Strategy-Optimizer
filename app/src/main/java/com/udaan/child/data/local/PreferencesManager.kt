package com.udaan.child.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromString
import kotlinx.serialization.json.encodeToString
import kotlinx.serialization.serializer

class PreferencesManager(private val prefs: SharedPreferences) {

    private val json = Json { ignoreUnknownKeys = true }

    // -----------------------------------------------------------------
    // Generic helpers
    // -----------------------------------------------------------------
    private fun <T> putObject(key: String, value: T, serializer: kotlinx.serialization.KSerializer<T>) {
        val encoded = json.encodeToString(serializer, value)
        prefs.edit().putString(key, encoded).apply()
    }

    private fun <T> getObject(key: String, serializer: kotlinx.serialization.KSerializer<T>): T? {
        val string = prefs.getString(key, null) ?: return null
        return try {
            json.decodeFromString(serializer, string)
        } catch (e: Exception) {
            null
        }
    }

    // -----------------------------------------------------------------
    // Concrete preferences used by the app
    // -----------------------------------------------------------------
    companion object {
        private const val PREFS_NAME = "udaan_prefs"
        fun create(context: Context): PreferencesManager =
            PreferencesManager(context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE))
    }

    // Example: user‑id (String)
    fun setUserId(userId: String?) {
        if (userId.isNullOrEmpty()) prefs.edit().remove("user_id").apply()
        else prefs.edit().putString("user_id", userId).apply()
    }

    fun getUserId(): String? = prefs.getString("user_id", null)

    // Example: a data‑class that is @Serializable
    fun setLastPortfolio(value: com.udaan.child.data.model.Portfolio?) {
        if (value == null) prefs.edit().remove("last_portfolio").apply()
        else putObject("last_portfolio", value, com.udaan.child.data.model.Portfolio.serializer())
    }

    fun getLastPortfolio(): com.udaan.child.data.model.Portfolio? =
        getObject("last_portfolio", com.udaan.child.data.model.Portfolio.serializer())
}
