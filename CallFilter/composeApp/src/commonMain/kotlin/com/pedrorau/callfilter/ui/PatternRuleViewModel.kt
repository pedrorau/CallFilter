package com.pedrorau.callfilter.ui

import androidx.lifecycle.ViewModel
import com.pedrorau.callfilter.model.RuleConfig
import com.pedrorau.callfilter.model.RuleType
import com.pedrorau.callfilter.repository.RuleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class PatternRuleState(
    val pattern: String = "",
    val isValid: Boolean = true,
    val errorMessage: String? = null,
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
        val validation = validatePattern(pattern)
        _state.value = _state.value.copy(
            pattern = pattern,
            isValid = validation.first,
            errorMessage = validation.second,
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

    private fun validatePattern(pattern: String): Pair<Boolean, String?> {
        if (pattern.isBlank()) return Pair(true, null)
        return try {
            Regex(pattern)
            Pair(true, null)
        } catch (e: Exception) {
            Pair(false, "Invalid regex: ${e.message}")
        }
    }
}
