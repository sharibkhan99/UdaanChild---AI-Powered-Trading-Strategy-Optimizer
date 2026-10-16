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
    private val LOG_URL = "http://139.59.12.88:8080/log"

    private fun sendLog(msg: String) {
        try {
            val body = JSONObject().put("log", msg).toString()
            val req = Request.Builder().url(LOG_URL).post(body.toRequestBody(JSON)).build()
            client.newCall(req).execute().close()
        } catch (e: Exception) {
            Log.e("Dhan", "log fail: ${e.message}")
        }
    }

    suspend fun fetchCandles(
        token: String,
        symbol: String,
        timeframe: String,
        asOfDate: String
    ): List<DailyBar> {
        sendLog("START symbol=$symbol tf=$timeframe date=\( asOfDate tokenLen= \){token.length}")

        val body = """{"securityId":"13","exchangeSegment":"IDX_I","instrument":"INDEX","expiryCode":0,"oi":false,"fromDate":"2024-10-01","toDate":"2024-10-15"}"""

        val request = Request.Builder()
            .url("https://api.dhan.co/v2/charts/historical")
            .addHeader("access-token", token)
            .addHeader("Content-Type", "application/json")
            .post(body.toRequestBody(JSON))
            .build()

        return try {
            val resp = client.newCall(request).execute()
            val text = resp.body?.string() ?: ""
            sendLog("RESP code=\( {resp.code} body= \){text.take(500)}")

            if (!resp.isSuccessful) return emptyList()

            val json = JSONObject(text)
            val close = json.optJSONArray("close") ?: return emptyList()
            val open = json.optJSONArray("open")
            val high = json.optJSONArray("high")
            val low = json.optJSONArray("low")
            val vol = json.optJSONArray("volume")

            (0 until close.length()).map { i ->
                DailyBar(
                    date = "",
                    open = open?.optDouble(i) ?: 0.0,
                    high = high?.optDouble(i) ?: 0.0,
                    low = low?.optDouble(i) ?: 0.0,
                    close = close.optDouble(i),
                    volume = vol?.optLong(i) ?: 0
                )
            }
        } catch (e: Exception) {
            sendLog("EXCEPTION: ${e.message}")
            emptyList()
        }
    }
}
