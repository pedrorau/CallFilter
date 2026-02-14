package com.pedrorau.callfilter.ui

import androidx.lifecycle.ViewModel
import com.pedrorau.callfilter.model.Rule
import com.pedrorau.callfilter.repository.PreferencesRepository
import com.pedrorau.callfilter.repository.RuleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class RulesState(
    val rules: List<Rule> = emptyList(),
    val notificationsEnabled: Boolean = false
)

class RulesViewModel(
    private val ruleRepository: RuleRepository = RuleRepository(),
    private val preferencesRepository: PreferencesRepository = PreferencesRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(RulesState())
    val state: StateFlow<RulesState> = _state

    fun loadRules() {
        _state.value = RulesState(
            rules = ruleRepository.getRules(),
            notificationsEnabled = preferencesRepository.isNotificationsEnabled()
        )
    }

    fun toggleRule(ruleId: String, enabled: Boolean) {
        ruleRepository.setRuleEnabled(ruleId, enabled)
        loadRules()
    }

    fun toggleNotifications(enabled: Boolean) {
        preferencesRepository.setNotificationsEnabled(enabled)
        _state.value = _state.value.copy(notificationsEnabled = enabled)
    }
}
