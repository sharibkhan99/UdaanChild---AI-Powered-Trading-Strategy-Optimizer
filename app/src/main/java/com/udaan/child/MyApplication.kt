package com.udaan.child

import android.app.Application
import android.util.Log

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d("UDAAN", "MyApplication initialized successfully.")
    }
}
