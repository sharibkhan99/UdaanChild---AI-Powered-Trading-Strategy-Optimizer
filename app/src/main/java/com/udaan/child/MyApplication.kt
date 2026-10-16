package com.udaan.child

import android.app.Application
import com.udaan.child.utils.CrashReporter

class UdaanChildApp : Application() {
    override fun onCreate() {
        super.onCreate()
        CrashReporter.init(this, "http://139.59.12.88:8080/log")
    }
}
