package com.pedrorau.callfilter

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform