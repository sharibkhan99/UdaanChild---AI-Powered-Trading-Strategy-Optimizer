package com.udaan.child

import android.app.Application
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class MyAplication : Application() { // Ya aapka jo bhi naam ho

    override fun onCreate() {
        super.onCreate()

        // 1. Uncaught Exception Handler setup karein
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                // Crash aane par turant local file mein save kar lein (Synchronous & Fast)
                saveCrashLocally(throwable)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Default system handler call karein taaki app properly close ho
            defaultHandler?.uncaughtException(thread, throwable)
        }

        // 2. App khulte hi check karein ki pichhla koi crash saved hai kya, agar hai toh server par bhejein
        checkAndSendPendingCrash()
    }

    private fun saveCrashLocally(throwable: Throwable) {
        val crashText = throwable.stackTraceToString()
        val file = File(filesDir, "pending_crash.txt")
        file.writeText(crashText)
    }

    private fun checkAndSendPendingCrash() {
        val file = File(filesDir, "pending_crash.txt")
        if (file.exists()) {
            val crashLog = file.readText()
            
            // Background thread par server par upload karein
            CoroutineScope(Dispatchers.IO).launch {
                val success = sendLogToServer(crashLog)
                if (success) {
                    file.delete() // Server par pahunchne ke baad local file hta dein
                }
            }
        }
    }

    private fun sendLogToServer(logContent: String): Boolean {
        return try {
            // Apna server endpoint yahan dalein
            val url = URL("https://your-backend-api.com/api/logs") 
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                doOutput = true
                connectTimeout = 10000
                readTimeout = 10000
            }

            val jsonBody = "{\"crash_log\": ${JSONObject.quote(logContent)}}"
            conn.outputStream.write(jsonBody.toByteArray(Charsets.UTF_8))

            val responseCode = conn.responseCode
            responseCode in 200..299
        } catch (e: Exception) {
            Log.e("CrashSender", "Failed to send crash log", e)
            false
        }
    }
}
