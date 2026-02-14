package com.pedrorau.callfilter.repository

import com.pedrorau.callfilter.model.Rule
import com.pedrorau.callfilter.model.RuleConfig
import com.pedrorau.callfilter.model.RuleType
import com.russhwolf.settings.Settings
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class RuleRepository(private val settings: Settings = Settings()) {

    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        private const val KEY_RULES = "rules"

        val DEFAULT_RULES = listOf(
            Rule(
                id = "block_digit_count",
                type = RuleType.BLOCK_DIGIT_COUNT,
                enabled = true,
                config = RuleConfig.DigitCount(count = 8)
            ),
            Rule(
                id = "block_from_list",
                type = RuleType.BLOCK_FROM_LIST,
                enabled = true,
                config = RuleConfig.None
            ),
            Rule(
                id = "block_regex",
                type = RuleType.BLOCK_REGEX,
                enabled = false,
                config = RuleConfig.RegexPattern(pattern = "")
            ),
            Rule(
                id = "block_all",
                type = RuleType.BLOCK_ALL,
                enabled = false,
                config = RuleConfig.None
            )
        )
    }

    fun getRules(): List<Rule> {
        val stored = settings.getStringOrNull(KEY_RULES) ?: return DEFAULT_RULES
        return try {
            json.decodeFromString<List<Rule>>(stored)
        } catch (_: Exception) {
            DEFAULT_RULES
        }
    }

    fun saveRules(rules: List<Rule>) {
        settings.putString(KEY_RULES, json.encodeToString(rules))
    }

    fun updateRule(ruleId: String, transform: (Rule) -> Rule) {
        val rules = getRules().map { if (it.id == ruleId) transform(it) else it }
        saveRules(rules)
    }

    fun setRuleEnabled(ruleId: String, enabled: Boolean) {
        updateRule(ruleId) { it.copy(enabled = enabled) }
    }
}
