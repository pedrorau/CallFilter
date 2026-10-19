package com.pedrorau.callfilter.platform

import android.content.Context

/** Application context for shared Android code. Set once from the app's Application.onCreate. */
object AppContext {
    lateinit var value: Context
        private set

    fun init(context: Context) {
        value = context.applicationContext
    }
}
