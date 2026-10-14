# UdaanChild - AI-Powered Trading Strategy Optimizer

An Android application that uses Genetic Algorithms to optimize trading strategies by evolving technical indicator parameters and feature combinations. Built with Kotlin, Coroutines, WorkManager, and Dhan API integration.

## 🚀 Features

- **Genetic Algorithm Optimization**: Evolves 14 technical indicators + custom features (Wavelets, FFT, Fractional Calculus, Malliavin proxy, Lunar cycles)
- **Walk-Forward Validation**: 3-fold cross-validation with 66/34 train/test split
- **Transaction Cost Aware**: Includes 0.05% slippage + brokerage per trade
- **Background Processing**: WorkManager-based optimization runs without blocking UI
- **Persistent Storage**: DataStore saves best parameters across app restarts
- **Dhan API Integration**: Fetches historical daily data for Indian markets
- **CI/CD Ready**: GitHub Actions workflow for lint, test, and APK builds

## 📊 Feature Pool (14 Indicators)

| # | Feature | Description |
|---|---------|-------------|
| 0 | EMA30/94 Cross | Fresh bullish crossover |
| 1 | EMA9/21 Cross | Fast momentum crossover |
| 2 | WT30/94 Cross | Haar wavelet medium-term |
| 3 | WT9/21 Cross | Haar wavelet fast |
| 4 | FFT Phase | Dominant cycle phase > threshold |
| 5 | Volume Ratio | Volume / EMA20(volume) |
| 6 | Price Band | Close ∈ (100, 3000) |
| 7 | Caputo Cross | Fractional derivative α=0.3 > α=0.7 |
| 8 | Nakshatra | Lunar mansion bullish buckets |
| 9 | Tithi Shukla | Waxing lunar phase (1-15) |
| 10 | Malliavin Proxy | Local volatility of log returns |
| 11 | Close/EMA50 | Price above medium trend |
| 12 | RSI(14) | Momentum > threshold |
| 13 | ATR(14) | Volatility > threshold |

## 🏗 Architecture
