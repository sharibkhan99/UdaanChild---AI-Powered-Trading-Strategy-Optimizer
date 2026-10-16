fun runAnalysis(..., asOfDate: String) {
    val datePart = asOfDate.substringBefore("T")
    val bars = dhanRepo.fetchCandles(token, symbol, timeframe, datePart)
    // ...
}
