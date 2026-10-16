package com.udaan.child

import android.content.Context
import android.os.Build
import android.os.StrictMode
import android.util.Log
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlin.system.exitProcess

object CrashHandler : Thread.UncaughtExceptionHandler {

    private var defaultHandler: Thread.UncaughtExceptionHandler? = null
    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        val stackTrace = throwable.stackTraceToString()
        
        // Server par synchronous crash log bhejte hain (Main thread safe)
        sendCrashToServerBlocking(stackTrace)

        // Phir default handler ko call karte hain taaki app properly close ho
        defaultHandler?.uncaughtException(thread, throwable) ?: exitProcess(1)
    }

    private fun sendCrashToServerBlocking(errorDetails: String) {
        val oldPolicy = StrictMode.getThreadPolicy()
        try {
            // Main thread par network allow karne ke liye Lax policy set karte hain
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.LAX)

            val url = URL("http://139.59.12.88:3000/api/crash")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                doOutput = true
                connectTimeout = 4000
                readTimeout = 4000
            }

            val payload = JSONObject().apply {
                put("deviceName", "${Build.MANUFACTURER} ${Build.MODEL}")
                put("error", errorDetails)
            }

            OutputStreamWriter(conn.outputStream, "UTF-8").use { writer ->
                writer.write(payload.toString())
                writer.flush()
            }

            val responseCode = conn.responseCode
            Log.d("CrashHandler", "Crash sent successfully, response: $responseCode")
            conn.disconnect()
        } catch (e: Exception) {
            Log.e("CrashHandler", "Failed to send crash log to server", e)
        } finally {
            StrictMode.setThreadPolicy(oldPolicy)
        }
    }
}
