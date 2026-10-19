package com.pedrorau.callfilter.ui

import com.pedrorau.callfilter.model.RuleConfig
import com.pedrorau.callfilter.repository.PreferencesRepository
import com.pedrorau.callfilter.repository.RuleRepository
import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals

class RulesViewModelTest {

    private val settings = MapSettings()
    private val ruleRepository = RuleRepository(settings)
    private val viewModel = RulesViewModel(ruleRepository, PreferencesRepository(settings))

    private fun storedDigitCount(): Int? =
        (ruleRepository.getRules().single { it.id == "block_digit_count" }.config as? RuleConfig.DigitCount)?.count

    @Test
    fun setDigitCountPersistsAndUpdatesState() {
        viewModel.setDigitCount("block_digit_count", 7)

        assertEquals(7, storedDigitCount())
        val inState = viewModel.state.value.rules.single { it.id == "block_digit_count" }.config
        assertEquals(RuleConfig.DigitCount(7), inState)
    }

    @Test
    fun setDigitCountIgnoresValuesOutOfRange() {
        viewModel.setDigitCount("block_digit_count", RuleConfig.DigitCount.MIN - 1)
        viewModel.setDigitCount("block_digit_count", RuleConfig.DigitCount.MAX + 1)

        assertEquals(8, storedDigitCount())
    }
}
