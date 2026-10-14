package com.udaan.child.optimizer

import com.udaan.child.data.model.DailyBar
import com.udaan.child.data.model.FeatureRegistry

/**
 * Very small helper that turns the raw price‑bars into the 14 feature‑vectors
 * expected by the GA.  Replace the stub logic with your real calculations.
 */
class FeatureEngine(private val bars: List<DailyBar>) {

    // Returns an array [featureCount][bars.size] filled with 0.0f for now.
    fun getAllFeatures(): Array<FloatArray> = Array(FeatureRegistry.COUNT) {
        FloatArray(bars.size) { 0f }
    }
}
