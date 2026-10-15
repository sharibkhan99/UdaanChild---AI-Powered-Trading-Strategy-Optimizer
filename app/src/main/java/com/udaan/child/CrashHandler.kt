package com.udaan.child

import android.content.Context
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
        
        // Crash ko server par synchronous ya background thread mein bhejte hain
        sendCrashToServerSync(stackTrace)

        // Phir default handler ko call karte hain taaki app properly close ho
        defaultHandler?.uncaughtException(thread, throwable) ?: exitProcess(1)
    }

    private fun sendCrashToServerSync(errorDetails: String) {
        try {
            val url = URL("http://139.59.12.88:3000/api/crash")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                doOutput = true
                connectTimeout = 3000
                readTimeout = 3000
            }

            val payload = JSONObject().apply {
                put("deviceName", "${Build.MANUFACTURER} ${Build.MODEL}")
                put("error", errorDetails)
            }

            OutputStreamWriter(conn.outputStream, "UTF-8").use { writer ->
                writer.write(payload.toString())
                writer.flush()
            }

            conn.responseCode // Request trigger karne ke liye zaroori hai
            conn.disconnect()
        } catch (e: Exception) {
            Log.e("CrashHandler", "Failed to send crash log to server", e)
        }
    }
}
