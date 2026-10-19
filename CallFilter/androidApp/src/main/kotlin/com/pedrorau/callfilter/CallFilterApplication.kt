package com.pedrorau.callfilter

import android.app.Application
import com.pedrorau.callfilter.platform.AppContext

class CallFilterApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AppContext.init(this)
    }
}
