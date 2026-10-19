package com.pedrorau.callfilter

import android.app.Application
import android.content.Context

class CallFilterApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }

    companion object {
        lateinit var context: Context
            private set
    }
}
