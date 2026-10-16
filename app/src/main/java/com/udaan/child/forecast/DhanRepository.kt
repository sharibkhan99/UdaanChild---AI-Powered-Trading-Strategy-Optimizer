package com.udaan.child.forecast

import android.util.Log
import com.udaan.child.data.model.DailyBar
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
    ): List<DailyBar> {
        // Hardcoded NIFTY test
        val body = """
            {
                "securityId": "13",
                "exchangeSegment": "IDX_I",
                "instrument": "INDEX",
                "expiryCode": 0,
                "oi": false,
                "fromDate": "2024-10-01",
                "toDate": "2024-10-15"
            }
        """.trimIndent()

        Log.d("Dhan", "Token: ${token.take(20)}...")
        Log.d("Dhan", "Body: $body")

        val request = Request.Builder()
            .url("https://api.dhan.co/v2/charts/historical")
            .addHeader("access-token", token)
            .addHeader("Content-Type", "application/json")
            .post(body.toRequestBody(JSON))
            .build()

        return try {
            val resp = client.newCall(request).execute()
            val text = resp.body?.string() ?: ""
            Log.d("Dhan", "Code: ${resp.code}")
            Log.d("Dhan", "Response: ${text.take(500)}")

            if (!resp.isSuccessful) return emptyList()
            val json = JSONObject(text)
            val close = json.optJSONArray("close") ?: return emptyList()
            (0 until close.length()).map { i ->
                DailyBar("", 0.0, 0.0, 0.0, close.optDouble(i), 0)
            }
        } catch (e: Exception) {
            Log.e("Dhan", "Error: ${e.message}")
            emptyList()
        }
    }
}
