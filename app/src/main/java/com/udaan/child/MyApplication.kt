package com.udaan.child

import android.app.Application
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // 1. Uncaught Exception Handler setup karein
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                saveCrashLocally(throwable)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            defaultHandler?.uncaughtException(thread, throwable)
        }

        // 2. App launch par pending crash server par bhejein
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
            CoroutineScope(Dispatchers.IO).launch {
                val success = sendLogToServer(crashLog)
                if (success) {
                    file.delete() // Server par successfully pahunchne par local file delete ho jayegi
                }
            }
        }
    }

    private fun sendLogToServer(logContent: String): Boolean {
        return try {
            // Aapka server IP endpoint
            val url = URL("http://139.59.12.88/api/logs") 
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
            Log.e("CrashSender", "Failed to send crash log to server", e)
            false
        }
    }
}
