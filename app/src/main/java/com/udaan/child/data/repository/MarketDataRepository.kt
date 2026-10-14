package com.udaan.child.data.repository

import com.udaan.child.data.model.DailyBar
import java.util.Random

/**
 * Very small stub implementation – replace with a real data source
 * (Retrofit, Room, local CSV, …).  The interface is kept tiny so the
 * rest of the app can compile.
 */
interface MarketDataRepository {
    suspend fun getHistoricalData(symbol: String): List<DailyBar>
}

/** Default dummy implementation used by the Worker / ViewModel when no DI is set up. */
class MarketDataRepositoryImpl(private val context: android.content.Context) : MarketDataRepository {

    override suspend fun getHistoricalData(symbol: String): List<DailyBar> {
        // TODO: replace with real loading logic.
        // Here we generate 600 synthetic bars so the optimiser has enough data.
        return (1..600).map { i ->
            val rnd = Random(i.toLong())
            DailyBar(
                date = System.currentTimeMillis() - (600 - i) * 86_400_000L,
                close = 10000f + rnd.nextFloat() * 5000f,
                volume = 1_000_000f + rnd.nextFloat() * 5_000_000f,
                open = 10000f + rnd.nextFloat() * 5000f,
                high = 10000f + rnd.nextFloat() * 5000f,
                low = 10000f + rnd.nextFloat() * 5000f
            )
        }
    }
}
