package com.pedrorau.callfilter.engine

import kotlin.test.Test
import kotlin.test.assertEquals

class LibPhoneNumberNormalizerTest {

    private val peru = LibPhoneNumberNormalizer { "PE" }

    @Test
    fun localAndInternationalFormsNormalizeToSameE164() {
        val expected = "+51987654321"
        assertEquals(expected, peru.normalize("987654321").e164)
        assertEquals(expected, peru.normalize("987 654 321").e164)
        assertEquals(expected, peru.normalize("+51 987 654 321").e164)
        assertEquals(expected, peru.normalize("0051987654321").e164)
    }

    @Test
    fun foreignNumberKeepsItsOwnCountryCode() {
        val mexico = peru.normalize("+52 55 1234 5678")
        assertEquals("+525512345678", mexico.e164)
        assertEquals("5512345678", mexico.national)

        val us = peru.normalize("+1 (800) 555-0199")
        assertEquals("+18005550199", us.e164)
    }

    @Test
    fun nationalNumberDropsCountryCodeAndTrunkPrefix() {
        assertEquals("987654321", peru.normalize("+51987654321").national)
        assertEquals("17123456", peru.normalize("01 712 3456").national)
    }

    @Test
    fun deviceRegionChangesHowLocalNumbersAreRead() {
        val mexico = LibPhoneNumberNormalizer { "MX" }
        assertEquals("+525512345678", mexico.normalize("55 1234 5678").e164)
    }

    @Test
    fun unparseableInputFallsBackToDigits() {
        val unknownRegion = LibPhoneNumberNormalizer { "ZZ" }
        assertEquals("12345678", unknownRegion.normalize("1234-5678").e164)
        assertEquals("", peru.normalize("").e164)
    }
}
