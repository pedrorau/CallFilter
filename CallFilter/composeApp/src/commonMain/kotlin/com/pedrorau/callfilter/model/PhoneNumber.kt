package com.pedrorau.callfilter.model

/**
 * A phone number in the forms the rule engine matches against.
 *
 * @property raw the number exactly as received or typed.
 * @property e164 international form (e.g. "+51987654321"), or a best-effort fallback.
 * @property national national significant digits, without country code or trunk prefix.
 */
data class PhoneNumber(
    val raw: String,
    val e164: String,
    val national: String
) {
    companion object {
        /** Fallback used when the number can't be parsed: keeps digits and a leading '+'. */
        fun fromRaw(raw: String): PhoneNumber {
            val digits = raw.filter { it.isDigit() }
            val international = raw.trim().startsWith('+') && digits.isNotEmpty()
            return PhoneNumber(
                raw = raw,
                e164 = if (international) "+$digits" else digits,
                national = digits
            )
        }
    }
}
