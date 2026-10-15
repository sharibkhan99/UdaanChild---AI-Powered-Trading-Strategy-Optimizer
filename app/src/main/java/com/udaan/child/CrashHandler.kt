package com.udaan.child

import android.content.Context
import android.os.Build
import org.json.JSONObject
import java.io.OutputStream
import java.io.PrintWriter
import java.io.StringWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlin.system.exitProcess

class CrashHandler(private val context: Context) : Thread.UncaughtExceptionHandler {

    private val defaultHandler: Thread.UncaughtExceptionHandler? =
        Thread.getDefaultUncaughtExceptionHandler()

    companion object {
        fun init(context: Context) {
            Thread.setDefaultUncaughtExceptionHandler(CrashHandler(context.applicationContext))
        }
    }

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        throwable.printStackTrace(pw)

        val stackTrace = "App: com.udaan.child\n" +
                "Android Version: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})\n" +
                "Model: ${Build.MANUFACTURER} ${Build.MODEL}\n\n" +
                sw.toString()

        val deviceName = "${Build.MANUFACTURER} ${Build.MODEL}"

        val sendThread = Thread {
            try {
                val url = URL("http://139.59.12.88:3000/api/crash")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json; utf-8")
                conn.connectTimeout = 5000
                conn.doOutput = true

                val json = JSONObject().apply {
                    put("deviceName", deviceName)
                    put("error", stackTrace)
                }

                conn.outputStream.use { os: OutputStream ->
                    val input = json.toString().toByteArray(Charsets.UTF_8)
                    os.write(input, 0, input.size)
                }
                conn.responseCode
                conn.disconnect()
            } catch ( ignored: Exception) {
            }
        }

        sendThread.start()
        try {
            sendThread.join(2000)
        } catch (ignored: InterruptedException) {
        }

        if (defaultHandler != null) {
            defaultHandler.uncaughtException(thread, throwable)
        } else {
            exitProcess(1)
        }
    }
}
