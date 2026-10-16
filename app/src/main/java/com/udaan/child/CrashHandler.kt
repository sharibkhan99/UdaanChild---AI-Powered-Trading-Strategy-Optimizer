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
        sendLog(throwable.stackTraceToString())
        defaultHandler?.uncaughtException(thread, throwable) ?: exitProcess(1)
    }

    fun sendLog(msg: String) {
        Thread {
            val oldPolicy = StrictMode.getThreadPolicy()
            try {
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
                    put("error", msg)
                }
                OutputStreamWriter(conn.outputStream, "UTF-8").use {
                    it.write(payload.toString())
                    it.flush()
                }
                conn.responseCode
                conn.disconnect()
            } catch (e: Exception) {
                Log.e("CrashHandler", "send fail", e)
            } finally {
                StrictMode.setThreadPolicy(oldPolicy)
            }
        }.start()
    }
}
