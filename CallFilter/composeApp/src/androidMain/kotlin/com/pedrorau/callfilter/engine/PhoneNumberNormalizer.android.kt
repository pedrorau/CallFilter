package com.pedrorau.callfilter.engine

import android.content.Context
import android.telephony.TelephonyManager
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.pedrorau.callfilter.CallFilterApplication
import com.pedrorau.callfilter.model.PhoneNumber
import java.util.Locale

actual fun platformPhoneNumberNormalizer(): PhoneNumberNormalizer =
    LibPhoneNumberNormalizer { deviceRegion(CallFilterApplication.context) }

class LibPhoneNumberNormalizer(
    private val regionProvider: () -> String
) : PhoneNumberNormalizer {

    private val util = PhoneNumberUtil.getInstance()

    override fun normalize(raw: String): PhoneNumber {
        if (raw.isBlank()) return PhoneNumber.fromRaw(raw)
        return try {
            val parsed = util.parse(raw, regionProvider())
            PhoneNumber(
                raw = raw,
                e164 = util.format(parsed, PhoneNumberUtil.PhoneNumberFormat.E164),
                national = util.getNationalSignificantNumber(parsed)
            )
        } catch (_: NumberParseException) {
            PhoneNumber.fromRaw(raw)
        }
    }
}

/** ISO country of the SIM, then the network, then the device locale. "ZZ" if unknown. */
internal fun deviceRegion(context: Context): String {
    val telephony = context.getSystemService(TelephonyManager::class.java)
    return listOf(
        telephony?.simCountryIso,
        telephony?.networkCountryIso,
        Locale.getDefault().country
    ).firstOrNull { !it.isNullOrBlank() }?.uppercase() ?: "ZZ"
}
