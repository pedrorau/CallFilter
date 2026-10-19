package com.pedrorau.callfilter.model

import kotlinx.serialization.Serializable

@Serializable
data class Rule(
    val id: String,
    val type: RuleType,
    val enabled: Boolean,
    val config: RuleConfig = RuleConfig.None
)

@Serializable
sealed class RuleConfig {
    @Serializable
    data object None : RuleConfig()

    @Serializable
    data class DigitCount(val count: Int) : RuleConfig() {
        companion object {
            const val MIN = 3
            /** E.164 numbers have at most 15 digits. */
            const val MAX = 15
        }
    }

    @Serializable
    data class RegexPattern(val pattern: String) : RuleConfig()
}
