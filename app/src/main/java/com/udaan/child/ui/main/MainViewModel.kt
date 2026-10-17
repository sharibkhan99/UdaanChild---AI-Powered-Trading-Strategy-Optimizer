private fun detectReversals(bars: List<DailyBar>): List<Reversal> {
    val result = mutableListOf<Reversal>()
    if (bars.size < 5) return result
    for (i in 2 until bars.size - 2) {
        val prev = bars[i - 1].close
        val curr = bars[i].close
        val next = bars[i + 1].close
        if (prev < curr && curr > next) {
            result.add(Reversal(bars[i].date, false, curr))
        } else if (prev > curr && curr < next) {
            result.add(Reversal(bars[i].date, true, curr))
        }
    }
    return result.takeLast(10)
}

data class Reversal(val dateTime: String, val bullish: Boolean, val price: Double)
