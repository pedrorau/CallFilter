package com.pedrorau.callfilter.engine

import com.pedrorau.callfilter.model.Rule
import com.pedrorau.callfilter.model.RuleConfig
import com.pedrorau.callfilter.model.RuleResult
import com.pedrorau.callfilter.model.RuleType
import kotlin.test.Test
import kotlin.test.assertEquals

class RuleEngineTest {

    @Test
    fun eightDigitNumberWithActiveRuleIsRejected() {
        val engine = RuleEngine()
        val rules = listOf(
            Rule("1", RuleType.BLOCK_DIGIT_COUNT, enabled = true, config = RuleConfig.DigitCount(8))
        )
        assertEquals(RuleResult.REJECT, engine.evaluate("12345678", rules))
    }

    @Test
    fun eightDigitNumberWithInactiveRuleIsAllowed() {
        val engine = RuleEngine()
        val rules = listOf(
            Rule("1", RuleType.BLOCK_DIGIT_COUNT, enabled = false, config = RuleConfig.DigitCount(8))
        )
        assertEquals(RuleResult.ALLOW, engine.evaluate("12345678", rules))
    }

    @Test
    fun numberInBlockedListIsRejected() {
        val blockedNumbers = setOf("+56912345678")
        val engine = RuleEngine(blockedNumberLookup = { it in blockedNumbers })
        val rules = listOf(
            Rule("2", RuleType.BLOCK_FROM_LIST, enabled = true)
        )
        assertEquals(RuleResult.REJECT, engine.evaluate("+56912345678", rules))
    }

    @Test
    fun numberNotInBlockedListIsAllowed() {
        val blockedNumbers = setOf("+56912345678")
        val engine = RuleEngine(blockedNumberLookup = { it in blockedNumbers })
        val rules = listOf(
            Rule("2", RuleType.BLOCK_FROM_LIST, enabled = true)
        )
        assertEquals(RuleResult.ALLOW, engine.evaluate("+56999999999", rules))
    }

    @Test
    fun regexMatchIsRejected() {
        val engine = RuleEngine()
        val rules = listOf(
            Rule("3", RuleType.BLOCK_REGEX, enabled = true, config = RuleConfig.RegexPattern("^\\+569\\d{8}$"))
        )
        assertEquals(RuleResult.REJECT, engine.evaluate("+56912345678", rules))
    }

    @Test
    fun regexNoMatchIsAllowed() {
        val engine = RuleEngine()
        val rules = listOf(
            Rule("3", RuleType.BLOCK_REGEX, enabled = true, config = RuleConfig.RegexPattern("^\\+569\\d{8}$"))
        )
        assertEquals(RuleResult.ALLOW, engine.evaluate("+1234567890", rules))
    }

    @Test
    fun blockAllRejectsEverything() {
        val engine = RuleEngine()
        val rules = listOf(
            Rule("4", RuleType.BLOCK_ALL, enabled = true)
        )
        assertEquals(RuleResult.REJECT, engine.evaluate("+56912345678", rules))
        assertEquals(RuleResult.REJECT, engine.evaluate("12345", rules))
    }

    @Test
    fun noActiveRulesAllowsEverything() {
        val engine = RuleEngine()
        val rules = listOf(
            Rule("1", RuleType.BLOCK_DIGIT_COUNT, enabled = false, config = RuleConfig.DigitCount(8)),
            Rule("4", RuleType.BLOCK_ALL, enabled = false)
        )
        assertEquals(RuleResult.ALLOW, engine.evaluate("12345678", rules))
    }

    @Test
    fun emptyRulesAllows() {
        val engine = RuleEngine()
        assertEquals(RuleResult.ALLOW, engine.evaluate("12345678", emptyList()))
    }

    @Test
    fun combinationOfRules() {
        val blockedNumbers = setOf("+56999999999")
        val engine = RuleEngine(blockedNumberLookup = { it in blockedNumbers })
        val rules = listOf(
            Rule("1", RuleType.BLOCK_DIGIT_COUNT, enabled = true, config = RuleConfig.DigitCount(8)),
            Rule("2", RuleType.BLOCK_FROM_LIST, enabled = true),
            Rule("3", RuleType.BLOCK_REGEX, enabled = false, config = RuleConfig.RegexPattern(".*"))
        )
        assertEquals(RuleResult.REJECT, engine.evaluate("12345678", rules))
        assertEquals(RuleResult.REJECT, engine.evaluate("+56999999999", rules))
        assertEquals(RuleResult.ALLOW, engine.evaluate("+56911111111", rules))
    }

    @Test
    fun invalidRegexDoesNotCrash() {
        val engine = RuleEngine()
        val rules = listOf(
            Rule("3", RuleType.BLOCK_REGEX, enabled = true, config = RuleConfig.RegexPattern("[invalid"))
        )
        assertEquals(RuleResult.ALLOW, engine.evaluate("12345678", rules))
    }

    @Test
    fun emptyRegexPatternDoesNotMatch() {
        val engine = RuleEngine()
        val rules = listOf(
            Rule("3", RuleType.BLOCK_REGEX, enabled = true, config = RuleConfig.RegexPattern(""))
        )
        assertEquals(RuleResult.ALLOW, engine.evaluate("12345678", rules))
    }

    @Test
    fun digitCountWithFormattedNumber() {
        val engine = RuleEngine()
        val rules = listOf(
            Rule("1", RuleType.BLOCK_DIGIT_COUNT, enabled = true, config = RuleConfig.DigitCount(8))
        )
        assertEquals(RuleResult.REJECT, engine.evaluate("1234-5678", rules))
        assertEquals(RuleResult.REJECT, engine.evaluate("1234 5678", rules))
    }
}
