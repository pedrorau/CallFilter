package com.pedrorau.callfilter.repository

import com.pedrorau.callfilter.engine.PhoneNumberNormalizer
import com.pedrorau.callfilter.engine.platformPhoneNumberNormalizer
import com.pedrorau.callfilter.model.BlockedNumber
import com.russhwolf.settings.Settings
import kotlinx.serialization.json.Json
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class BlockedNumberRepository(
    private val settings: Settings = Settings(),
    private val normalizer: PhoneNumberNormalizer = platformPhoneNumberNormalizer(),
    private val now: () -> Long = ::currentTimeMillis
) {

    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        private const val KEY_BLOCKED_NUMBERS = "blocked_numbers"
    }

    fun getBlockedNumbers(): List<BlockedNumber> {
        val stored = settings.getStringOrNull(KEY_BLOCKED_NUMBERS) ?: return emptyList()
        return try {
            json.decodeFromString<List<BlockedNumber>>(stored)
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun addNumber(input: String) {
        val number = normalizer.normalize(input).e164
        if (number.isEmpty()) return
        val list = getBlockedNumbers().toMutableList()
        if (list.any { normalizer.normalize(it.number).e164 == number }) return
        val id = "${number.hashCode()}_${Random.nextInt()}"
        list.add(
            BlockedNumber(
                id = id,
                number = number,
                addedTimestamp = now()
            )
        )
        save(list)
    }

    fun removeNumber(id: String) {
        val list = getBlockedNumbers().filter { it.id != id }
        save(list)
    }

    /** [e164] must already be normalized. Stored entries are normalized too, covering ones saved before normalization existed. */
    fun containsNumber(e164: String): Boolean {
        return getBlockedNumbers().any { normalizer.normalize(it.number).e164 == e164 }
    }

    private fun save(list: List<BlockedNumber>) {
        settings.putString(KEY_BLOCKED_NUMBERS, json.encodeToString(list))
    }
}

@OptIn(ExperimentalTime::class)
private fun currentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()
