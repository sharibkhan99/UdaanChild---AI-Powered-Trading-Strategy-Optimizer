package com.udaan.child

import android.app.Application
import android.os.Build
import android.util.Log
import com.udaan.child.data.local.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class MyApplication : Application() {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val client = OkHttpClient()

    override fun onCreate() {
        super.onCreate()
        CrashHandler.init(this)
        Log.d("UDAAN", "Application started")
        sendLog("APP_START", "Application started")

        scope.launch {
            try {
                val prefs = PreferencesManager(this@MyApplication)
                val symbol = prefs.getSymbol().first() ?: "NIFTY"
                sendLog("SYMBOL", "Loaded symbol: $symbol")
            } catch (e: Exception) {
                sendLog("ERROR", "Prefs error: ${e.message}")
            }
        }
    }

    fun sendLog(tag: String, msg: String) {
        Log.d("UDAAN", "$tag: $msg")
        scope.launch {
            try {
                val url = "http://139.59.12.88:3000/api/crash"
                val payload = JSONObject().apply {
                    put("deviceName", "${Build.MANUFACTURER} ${Build.MODEL}")
                    put("error", "$tag | $msg | Android ${Build.VERSION.RELEASE}")
                }
                val body = payload.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder().url(url).post(body).build()
                client.newCall(request).execute()
            } catch (e: Exception) {
                Log.e("UDAAN", "Log send failed: ${e.message}")
            }
        }
    }
}
