package com.pedrorau.callfilter.engine

import com.pedrorau.callfilter.model.PhoneNumber

// iOS is a UI placeholder with no real call blocking yet.
actual fun platformPhoneNumberNormalizer(): PhoneNumberNormalizer =
    PhoneNumberNormalizer { PhoneNumber.fromRaw(it) }
