package com.udaan.child.data.model

data class GAResult(
    val bestChromosome: List<Float> = emptyList(),
    val bestSharpe: Float = 0f,
    val activeFeatures: List<String> = emptyList(),
    val tradeHistory: List<Any> = emptyList(),
    val timestamp: Long = System.currentTimeMillis(),
    val generations: Int = 0
)
