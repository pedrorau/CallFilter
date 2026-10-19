package com.pedrorau.callfilter.engine

import com.pedrorau.callfilter.model.PhoneNumber

fun interface PhoneNumberNormalizer {
    fun normalize(raw: String): PhoneNumber
}

expect fun platformPhoneNumberNormalizer(): PhoneNumberNormalizer
