fun runAnalysis(clientId: String, token: String, symbol: String, timeframe: String, dateTime: String) {
    viewModelScope.launch {
        try {
            InstrumentRepository.load()
            val inst = InstrumentRepository.find(symbol)
            if (inst == null) {
                _resultText.value = "Symbol not found: $symbol"
                return@launch
            }
            val repo = DhanRepository()
            val bars = repo.fetchCandles(token, symbol, timeframe, dateTime)
            if (bars.isEmpty()) {
                _resultText.value = "No data (ID: ${inst.securityId}, Seg: ${inst.exchangeSegment})"
                return@launch
            }
            val reversals = detectReversals(bars)
            _resultText.value = "Bars: ${bars.size}"
            _reversalsText.value = reversals.joinToString("\n") { 
                "${it.dateTime} ${if (it.bullish) "BUY" else "SELL"} @ ${it.price}" 
            }
        } catch (e: Exception) {
            _resultText.value = "Error: ${e.message}"
        }
    }
}
