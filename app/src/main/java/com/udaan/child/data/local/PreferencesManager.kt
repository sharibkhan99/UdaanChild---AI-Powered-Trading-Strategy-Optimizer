package com.udaan.child.data.local

import android.content.Context

class PreferencesManager(private val context: Context) {
    fun saveOptimizationResult(result: Any) {}
    fun getLastOptimizationTime(): Long = 0L
}
