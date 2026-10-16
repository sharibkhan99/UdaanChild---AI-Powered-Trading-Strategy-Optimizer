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

    private fun sendLog(msg: String) {
        try {
            val body = JSONObject().put("log", msg).toString()
            val req = Request.Builder()
                .url("http://139.59.12.88:8080/log")
                .post(body.toRequestBody(JSON))
                .build()
            client.newCall(req).execute().close()
        } catch (e: Exception) { }
    }

    suspend fun fetchCandles(
        token: String,
        symbol: String,
        timeframe: String,
        asOfDate: String
    ): List<DailyBar> {
        sendLog("START symbol=$symbol tf=$timeframe date=$asOfDate")

        val body = """{"securityId":"13","exchangeSegment":"IDX_I","instrument":"INDEX","expiryCode":0,"oi":false,"fromDate":"2024-10-01","toDate":"2024-10-15"}"""

        val request = Request.Builder()
            .url("https://api.dhan.co/v2/charts/historical")
            .addHeader("access-token", token)
            .post(body.toRequestBody(JSON))
            .build()

        return try {
            val resp = client.newCall(request).execute()
            val text = resp.body?.string() ?: ""
            sendLog("RESP ${resp.code}: ${text.take(400)}")
            if (!resp.isSuccessful) return emptyList()
            val json = JSONObject(text)
            val close = json.optJSONArray("close") ?: return emptyList()
            (0 until close.length()).map { DailyBar("", 0.0, 0.0, 0.0, close.optDouble(it), 0) }
        } catch (e: Exception) {
            sendLog("EX: ${e.message}")
            emptyList()
        }
    }
}
