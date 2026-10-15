package com.udaan.child.data.model

import kotlinx.serialization.Serializable

// -------------------------------------------------------------------
// Simple DTOs used by the Genetic‑Algorithm optimiser
// -------------------------------------------------------------------
@Serializable
data class Gene(
    val paramName: String,
    val minValue: Double,
    val maxValue: Double,
    val currentValue: Double
)

@Serializable
data class Chromosome(
    val genes: List<Gene>,
    val fitness: Double = 0.0
)

@Serializable
data class Population(
    val chromosomes: List<Chromosome>,
    val generation: Int = 0
)

// -------------------------------------------------------------------
// Portfolio object stored in PreferencesManager
// -------------------------------------------------------------------
@Serializable
data class Portfolio(
    val id: String,
    val name: String,
    val symbols: List<String>,
    val createdAt: Long = System.currentTimeMillis()
)
