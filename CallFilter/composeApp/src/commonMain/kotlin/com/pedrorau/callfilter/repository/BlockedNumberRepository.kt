package com.pedrorau.callfilter.repository

import com.pedrorau.callfilter.model.BlockedNumber
import com.russhwolf.settings.Settings
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.random.Random

class BlockedNumberRepository(private val settings: Settings = Settings()) {

    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        private const val KEY_BLOCKED_NUMBERS = "blocked_numbers"
        private var counter = 0L
    }

    fun getBlockedNumbers(): List<BlockedNumber> {
        val stored = settings.getStringOrNull(KEY_BLOCKED_NUMBERS) ?: return emptyList()
        return try {
            json.decodeFromString<List<BlockedNumber>>(stored)
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun addNumber(number: String) {
        val list = getBlockedNumbers().toMutableList()
        if (list.any { it.number == number }) return
        val id = "${number.hashCode()}_${Random.nextInt()}"
        list.add(
            BlockedNumber(
                id = id,
                number = number,
                addedTimestamp = ++counter
            )
        )
        save(list)
    }

    fun removeNumber(id: String) {
        val list = getBlockedNumbers().filter { it.id != id }
        save(list)
    }

    fun containsNumber(number: String): Boolean {
        return getBlockedNumbers().any { it.number == number }
    }

    private fun save(list: List<BlockedNumber>) {
        settings.putString(KEY_BLOCKED_NUMBERS, json.encodeToString(list))
    }
}
