package com.udaan.child.optimizer

import com.udaan.child.data.model.Chromosome
import com.udaan.child.data.model.DailyBar
import com.udaan.child.data.model.FeatureRegistry   // <-- added
import com.udaan.child.data.model.GAResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.*
import kotlin.random.Random

/**
 * Very small helper that turns the raw price‑bars into the 14 feature‑vectors
 * expected by the GA.  The real implementation can be far more sophisticated –
 * this stub simply returns zero‑filled arrays so that the optimiser compiles.
 */
private class FeatureEngine(private val bars: List<DailyBar>) {
    // 14 features × N bars
    fun getAllFeatures(): Array<FloatArray> = Array(FeatureRegistry.COUNT) {
        FloatArray(bars.size) { 0f }
    }
}

class GeneticOptimizer(
    private val populationSize: Int = 150,
    private val generations: Int = 500,
    private val mutationRate: Float = 0.15f,
    private val elitismCount: Int = 5,
    private val folds: Int = 3,
    private val trainRatio: Float = 0.66f,
    private val holdDays: Int = 10,
    private val costPerTrade: Float = 0.0005f
) {

    suspend fun optimize(bars: List<DailyBar>): GAResult = withContext(Dispatchers.Default) {
        val engine = FeatureEngine(bars)
        val features = engine.getAllFeatures()
        val rand = Random(42)

        var population = MutableList(populationSize) { randomChromosome(rand) }
        var bestFitness = -1000f
        var bestChromo = population.first()

        repeat(generations) { gen ->
            val scored = population.map { chromo ->
                chromo to fitness(chromo, features, bars)
            }.sortedByDescending { it.second }

            if (scored.first().second > bestFitness) {
                bestFitness = scored.first().second
                bestChromo = scored.first().first
            }

            val newPop = scored.take(elitismCount).map { it.first }.toMutableList()

            while (newPop.size < populationSize) {
                val p1 = tournamentSelect(scored, rand)
                val p2 = tournamentSelect(scored, rand)
                val child = crossover(p1, p2, rand)
                newPop.add(mutate(child, rand))
            }
            population = newPop

            if (gen % 50 == 0) {
                println("GA Gen $gen | Best Sharpe: $bestFitness")
            }
        }

        val activeFeats = FeatureRegistry.NAMES.filterIndexed { i, _ -> bestChromo.featureMask[i] }
        GAResult(bestChromo, bestFitness, activeFeats, emptyList())
    }

    // -------------------------------------------------------------------------
    // Helpers – unchanged logic, only the missing imports were added
    // -------------------------------------------------------------------------
    private fun randomChromosome(rand: Random): Chromosome {
        val mask = BooleanArray(FeatureRegistry.COUNT) { rand.nextFloat() < 0.4f }
        val thresholds = FloatArray(FeatureRegistry.COUNT) { f -> randomThreshold(f, rand) }
        val weights = FloatArray(FeatureRegistry.COUNT) { rand.nextFloat() * 0.8f + 0.2f }
        return Chromosome(mask, thresholds, weights)
    }

    private fun randomThreshold(f: Int, rand: Random): Float = when (f) {
        4  -> rand.nextFloat() * 0.8f - 0.4f
        5  -> 1f + rand.nextFloat() * 0.8f
        6  -> 1000f + rand.nextFloat() * 3000f
        8  -> 9f + rand.nextFloat() * 18f
        9  -> 10f + rand.nextFloat() * 20f
        10 -> 0.01f + rand.nextFloat() * 0.03f
        11 -> 1f + rand.nextFloat() * 0.1f
        12 -> 45f + rand.nextFloat() * 20f
        13 -> 1f + rand.nextFloat() * 3f
        else -> 0f
    }

    private fun fitness(chromo: Chromosome, features: Array<FloatArray>, bars: List<DailyBar>): Float {
        val n = bars.size
        var totalSharpe = 0f
        var validFolds = 0

        for (fold in 0 until folds) {
            val foldStart = (fold * n / folds)
            val foldEnd = ((fold + 1) * n / folds)
            val trainEnd = foldStart + (trainRatio * (foldEnd - foldStart)).toInt()
            val testStart = trainEnd + 1
            if (testStart + holdDays >= foldEnd) continue

            val signals = generateSignals(chromo, features, testStart, foldEnd)
            val returns = computeReturns(signals, bars, testStart, foldEnd)

            if (returns.size < 5) continue

            val mean = returns.average().toFloat()
            val std = sqrt(returns.map { (it - mean) * (it - mean) }.average().toFloat())
            val sharpe = if (std == 0f) 0f else (mean / std) * sqrt(252f)
            totalSharpe += sharpe
            validFolds++
        }
        return if (validFolds > 0) totalSharpe / validFolds else -1000f
    }

    private fun generateSignals(chromo: Chromosome, features: Array<FloatArray>, start: Int, end: Int): BooleanArray {
        val signals = BooleanArray(end) { false }
        for (i in start until end) {
            var score = 0f
            var activeCount = 0
            for (f in 0..13) {
                if (!chromo.featureMask[f]) continue
                activeCount++
                val v = features[f][i]
                val thresh = chromo.thresholds[f]
                val passed = when (f) {
                    0, 1, 2, 3, 7 -> v > 0.5f
                    4 -> sin(v) > thresh
                    5 -> v > thresh
                    6 -> v > 100f && v < thresh
                    8 -> (v % 27) < thresh
                    9 -> v < thresh
                    10 -> v > thresh
                    11 -> v > thresh
                    12 -> v > thresh
                    13 -> v > thresh
                    else -> false
                }
                if (passed) score += chromo.weights[f]
            }
            if (activeCount >= 3 && score >= activeCount * 0.6f) signals[i] = true
        }
        return signals
    }

    private fun computeReturns(signals: BooleanArray, bars: List<DailyBar>, start: Int, end: Int): List<Float> {
        val returns = mutableListOf<Float>()
        var i = start
        while (i < end - holdDays) {
            if (signals[i]) {
                val entry = bars[i + 1].open
                val exitIdx = min(i + holdDays, end - 1)
                val exit = bars[exitIdx].close
                val ret = (exit / entry) - 1f - 2 * costPerTrade
                returns.add(ret)
                i = exitIdx + 1
            } else i++
        }
        return returns
    }

    private fun tournamentSelect(scored: List<Pair<Chromosome, Float>>, rand: Random): Chromosome {
        var best = scored[rand.nextInt(scored.size)]
        repeat(4) {
            val c = scored[rand.nextInt(scored.size)]
            if (c.second > best.second) best = c
        }
        return best.first
    }

    private fun crossover(p1: Chromosome, p2: Chromosome, rand: Random): Chromosome {
        val mask = BooleanArray(FeatureRegistry.COUNT) { i -> if (rand.nextBoolean()) p1.featureMask[i] else p2.featureMask[i] }
        val thresh = FloatArray(FeatureRegistry.COUNT) { i ->
            val t = rand.nextFloat()
            p1.thresholds[i] * t + p2.thresholds[i] * (1 - t)
        }
        val weights = FloatArray(FeatureRegistry.COUNT) { i ->
            val t = rand.nextFloat()
            p1.weights[i] * t + p2.weights[i] * (1 - t)
        }
        return Chromosome(mask, thresh, weights)
    }

    private fun mutate(chromo: Chromosome, rand: Random): Chromosome {
        val mask = chromo.featureMask.copyOf()
        val thresh = chromo.thresholds.copyOf()
        val weights = chromo.weights.copyOf()
        for (i in 0..13) {
            if (rand.nextFloat() < mutationRate) {
                mask[i] = !mask[i]
                thresh[i] *= 1f + (rand.nextFloat() - 0.5f) * 0.3f
                weights[i] = (weights[i] + (rand.nextFloat() - 0.5f) * 0.2f).coerceIn(0.1f, 1f)
            }
        }
        return Chromosome(mask, thresh, weights)
    }
}
