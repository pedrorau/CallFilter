package com.pedrorau.callfilter.repository

import com.pedrorau.callfilter.engine.PhoneNumberNormalizer
import com.pedrorau.callfilter.model.PhoneNumber
import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BlockedNumberRepositoryTest {

    // Minimal Peru-style normalizer: 9 local digits get +51, anything with '+' is kept.
    private val normalizer = PhoneNumberNormalizer { raw ->
        val digits = raw.filter { it.isDigit() }
        val e164 = when {
            raw.trim().startsWith('+') -> "+$digits"
            digits.length == 9 -> "+51$digits"
            else -> digits
        }
        PhoneNumber(raw, e164, digits)
    }

    private fun repository(settings: MapSettings = MapSettings(), now: Long = 1_000L) =
        BlockedNumberRepository(settings, normalizer, now = { now })

    @Test
    fun addedNumberIsStoredNormalized() {
        val repository = repository()
        repository.addNumber("987 654 321")
        assertEquals("+51987654321", repository.getBlockedNumbers().single().number)
    }

    @Test
    fun sameNumberInAnotherFormatIsNotAddedTwice() {
        val repository = repository()
        repository.addNumber("987654321")
        repository.addNumber("+51 987 654 321")
        assertEquals(1, repository.getBlockedNumbers().size)
    }

    @Test
    fun blankInputIsIgnored() {
        val repository = repository()
        repository.addNumber("   ")
        assertTrue(repository.getBlockedNumbers().isEmpty())
    }

    @Test
    fun containsNumberMatchesEntriesSavedBeforeNormalization() {
        val settings = MapSettings()
        settings.putString(
            "blocked_numbers",
            """[{"id":"legacy","number":"987 654 321","addedTimestamp":1}]"""
        )
        val repository = repository(settings)
        assertTrue(repository.containsNumber("+51987654321"))
        assertFalse(repository.containsNumber("+51911111111"))
    }

    @Test
    fun addedTimestampUsesClock() {
        val repository = repository(now = 1_760_000_000_000L)
        repository.addNumber("987654321")
        assertEquals(1_760_000_000_000L, repository.getBlockedNumbers().single().addedTimestamp)
    }

    @Test
    fun removeNumberDeletesById() {
        val repository = repository()
        repository.addNumber("987654321")
        val id = repository.getBlockedNumbers().single().id
        repository.removeNumber(id)
        assertTrue(repository.getBlockedNumbers().isEmpty())
    }
}
