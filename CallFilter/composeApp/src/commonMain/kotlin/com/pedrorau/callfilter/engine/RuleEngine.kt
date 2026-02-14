package com.pedrorau.callfilter.engine

import com.pedrorau.callfilter.model.Rule
import com.pedrorau.callfilter.model.RuleConfig
import com.pedrorau.callfilter.model.RuleResult
import com.pedrorau.callfilter.model.RuleType

class RuleEngine(
    private val blockedNumberLookup: (String) -> Boolean = { false }
) {

    fun evaluate(phoneNumber: String, rules: List<Rule>): RuleResult {
        val activeRules = rules.filter { it.enabled }
        for (rule in activeRules) {
            if (matches(phoneNumber, rule)) {
                return RuleResult.REJECT
            }
        }
        return RuleResult.ALLOW
    }

    private fun matches(phoneNumber: String, rule: Rule): Boolean {
        return when (rule.type) {
            RuleType.BLOCK_DIGIT_COUNT -> matchesDigitCount(phoneNumber, rule.config)
            RuleType.BLOCK_FROM_LIST -> blockedNumberLookup(phoneNumber)
            RuleType.BLOCK_REGEX -> matchesRegex(phoneNumber, rule.config)
            RuleType.BLOCK_ALL -> true
        }
    }

    private fun matchesDigitCount(phoneNumber: String, config: RuleConfig): Boolean {
        val count = (config as? RuleConfig.DigitCount)?.count ?: return false
        val digitsOnly = phoneNumber.filter { it.isDigit() }
        return digitsOnly.length == count
    }

    private fun matchesRegex(phoneNumber: String, config: RuleConfig): Boolean {
        val pattern = (config as? RuleConfig.RegexPattern)?.pattern
        if (pattern.isNullOrBlank()) return false
        return try {
            Regex(pattern).containsMatchIn(phoneNumber)
        } catch (_: Exception) {
            false
        }
    }
}
