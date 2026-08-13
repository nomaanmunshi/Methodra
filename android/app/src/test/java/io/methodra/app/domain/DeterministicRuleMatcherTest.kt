package io.methodra.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeterministicRuleMatcherTest {
    private fun method(id: String, obstacle: ObstacleType) = MethodDefinition(
        id = id,
        name = id,
        shortExplanation = "x",
        intendedProblem = "x",
        mayHelpWhen = emptyList(),
        unsuitableWhen = emptyList(),
        evidence = EvidenceAssessment(EvidenceLevel.B, "limit", "rationale"),
        inspiration = "test",
        setupQuestions = emptyList(),
        steps = listOf(ProtocolStep(1, "first", "do it")),
        minimumVersion = "one minute",
        focusRule = null,
        outcomeMetric = "attempts",
        reviewDays = 14,
        stopConditions = emptyList(),
        sourceLabels = emptyList(),
        sourceUrls = emptyList(),
        goalDomains = setOf(GoalDomain.STUDY),
        obstacles = setOf(obstacle)
    )

    @Test
    fun `returns no more than three and keeps reasons explainable`() {
        val methods = listOf(
            method("if_then", ObstacleType.DELAYED_START),
            method("attention", ObstacleType.DIGITAL_DISTRACTION),
            method("progress", ObstacleType.INCONSISTENCY),
            method("other", ObstacleType.RECOVERY)
        )
        val rules = listOf(
            MatchingRule("r1", "if_then", "obstacle", "eq", listOf("DELAYED_START"), 60, "Initiation obstacle."),
            MatchingRule("r2", "attention", "highScreenTime", "eq", listOf("true"), 40, "Screen distraction matters."),
            MatchingRule("r3", "progress", "goalDomain", "eq", listOf("STUDY"), 10, "Study goal."),
            MatchingRule("r4", "other", "goalDomain", "eq", listOf("STUDY"), 5, "Study goal.")
        )
        val input = MatchingInput(GoalDomain.STUDY, "Study OS", ObstacleType.DELAYED_START, 35, true, StructureLevel.MODERATE)
        val result = DeterministicRuleMatcher.match(methods, rules, input)
        assertEquals(3, result.size)
        assertEquals("if_then", result.first().method.id)
        assertTrue(result.first().reasons.isNotEmpty())
    }

    @Test
    fun `ties are deterministic by method id`() {
        val methods = listOf(method("b", ObstacleType.RECOVERY), method("a", ObstacleType.RECOVERY))
        val input = MatchingInput(GoalDomain.STUDY, "x", ObstacleType.DELAYED_START, 40, false, StructureLevel.LIGHT)
        val result = DeterministicRuleMatcher.match(methods, emptyList(), input)
        assertEquals(listOf("a", "b"), result.map { it.method.id })
    }
}
