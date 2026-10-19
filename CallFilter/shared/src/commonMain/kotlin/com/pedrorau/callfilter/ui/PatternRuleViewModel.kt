package com.pedrorau.callfilter.ui

import androidx.lifecycle.ViewModel
import com.pedrorau.callfilter.engine.RuleEngine
import com.pedrorau.callfilter.model.RuleConfig
import com.pedrorau.callfilter.model.RuleType
import com.pedrorau.callfilter.repository.RuleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class PatternError { TOO_LONG, INVALID_SYNTAX }

data class PatternRuleState(
    val pattern: String = "",
    val isValid: Boolean = true,
    val error: PatternError? = null,
    /** Technical detail from the regex engine, shown as-is for [PatternError.INVALID_SYNTAX]. */
    val errorDetail: String? = null,
    val saved: Boolean = false
)

class PatternRuleViewModel(
    private val ruleRepository: RuleRepository = RuleRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(PatternRuleState())
    val state: StateFlow<PatternRuleState> = _state

    fun loadPattern() {
        val rules = ruleRepository.getRules()
        val regexRule = rules.find { it.type == RuleType.BLOCK_REGEX }
        val pattern = (regexRule?.config as? RuleConfig.RegexPattern)?.pattern ?: ""
        _state.value = PatternRuleState(pattern = pattern)
    }

    fun updatePattern(pattern: String) {
        val (error, detail) = validatePattern(pattern)
        _state.value = _state.value.copy(
            pattern = pattern,
            isValid = error == null,
            error = error,
            errorDetail = detail,
            saved = false
        )
    }

    fun savePattern() {
        val current = _state.value
        if (!current.isValid) return
        ruleRepository.updateRule("block_regex") {
            it.copy(config = RuleConfig.RegexPattern(current.pattern))
        }
        _state.value = current.copy(saved = true)
    }

    private fun validatePattern(pattern: String): Pair<PatternError?, String?> {
        if (pattern.isBlank()) return null to null
        if (pattern.length > RuleEngine.MAX_PATTERN_LENGTH) return PatternError.TOO_LONG to null
        return try {
            Regex(pattern)
            null to null
        } catch (e: Exception) {
            PatternError.INVALID_SYNTAX to e.message
        }
    }
}
