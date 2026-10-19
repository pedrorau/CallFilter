package com.pedrorau.callfilter.engine

import com.pedrorau.callfilter.model.PhoneNumber
import com.pedrorau.callfilter.model.Rule
import com.pedrorau.callfilter.model.RuleConfig
import com.pedrorau.callfilter.model.RuleResult
import com.pedrorau.callfilter.model.RuleType

class RuleEngine(
    private val blockedNumberLookup: (e164: String) -> Boolean = { false }
) {

    companion object {
        /** Long patterns aren't needed for phone numbers and raise the risk of catastrophic backtracking. */
        const val MAX_PATTERN_LENGTH = 100
    }

    fun evaluate(phoneNumber: String, rules: List<Rule>): RuleResult =
        evaluate(PhoneNumber.fromRaw(phoneNumber), rules)

    fun evaluate(phoneNumber: PhoneNumber, rules: List<Rule>): RuleResult {
        val activeRules = rules.filter { it.enabled }
        for (rule in activeRules) {
            if (matches(phoneNumber, rule)) {
                return RuleResult.REJECT
            }
        }
        return RuleResult.ALLOW
    }

    private fun matches(phoneNumber: PhoneNumber, rule: Rule): Boolean {
        return when (rule.type) {
            RuleType.BLOCK_DIGIT_COUNT -> matchesDigitCount(phoneNumber, rule.config)
            RuleType.BLOCK_FROM_LIST -> phoneNumber.e164.isNotEmpty() && blockedNumberLookup(phoneNumber.e164)
            RuleType.BLOCK_REGEX -> matchesRegex(phoneNumber, rule.config)
            RuleType.BLOCK_ALL -> true
        }
    }

    // Counts digits as received and as a national number, so "+51 1234 5678" and "12345678" both count as 8.
    private fun matchesDigitCount(phoneNumber: PhoneNumber, config: RuleConfig): Boolean {
        val count = (config as? RuleConfig.DigitCount)?.count ?: return false
        val rawDigits = phoneNumber.raw.count { it.isDigit() }
        return rawDigits == count || phoneNumber.national.length == count
    }

    // Tries every form so users can write patterns in international (^\+51) or local (^\d{8}$) style.
    private fun matchesRegex(phoneNumber: PhoneNumber, config: RuleConfig): Boolean {
        val pattern = (config as? RuleConfig.RegexPattern)?.pattern
        if (pattern.isNullOrBlank() || pattern.length > MAX_PATTERN_LENGTH) return false
        return try {
            val regex = Regex(pattern)
            listOf(phoneNumber.raw, phoneNumber.e164, phoneNumber.national)
                .any { regex.containsMatchIn(it) }
        } catch (_: Exception) {
            false
        }
    }
}
