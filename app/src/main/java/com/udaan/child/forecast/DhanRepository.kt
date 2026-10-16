package com.udaan.child.forecast

import com.udaan.child.CrashHandler
import com.udaan.child.data.model.DailyBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class DhanRepository {
    private val client = OkHttpClient()
    private val JSON = "application/json".toMediaType()

    suspend fun fetchCandles(
        token: String,
        symbol: String,
        timeframe: String,
        asOfDate: String
    ): List<DailyBar> = withContext(Dispatchers.IO) {
        CrashHandler.sendLog("START symbol=$symbol tf=$timeframe")

        val jsonBody = JSONObject().apply {
            put("securityId", "13")
            put("exchangeSegment", "IDX_I")
            put("instrument", "INDEX")
            put("expiryCode", 0)
            put("oi", false)
            put("fromDate", "2024-10-01")
            put("toDate", "2024-10-15")
        }.toString()

        val request = Request.Builder()
            .url("https://api.dhan.co/v2/charts/historical")
            .addHeader("access-token", token)
            .post(jsonBody.toRequestBody(JSON))
            .build()

        try {
            val resp = client.newCall(request).execute()
            val text = resp.body?.string() ?: ""
            CrashHandler.sendLog("RESP " + resp.code + ": " + text.take(400))

            if (!resp.isSuccessful) return@withContext emptyList()
            val json = JSONObject(text)
            val close = json.optJSONArray("close") ?: return@withContext emptyList()
            (0 until close.length()).map { i ->
                DailyBar("", 0.0, 0.0, 0.0, close.optDouble(i), 0)
            }
        } catch (e: Exception) {
            CrashHandler.sendLog("EX: " + e.javaClass.simpleName + " | " + e.message)
            emptyList()
        }
    }
}
