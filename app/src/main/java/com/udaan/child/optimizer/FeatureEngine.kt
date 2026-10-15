package com.udaan.child.optimizer

import com.udaan.child.data.model.DailyBar

class FeatureEngine {
    fun extract(bars: List<DailyBar>): DoubleArray = DoubleArray(10) { 0.0 }
}
