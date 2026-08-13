package io.methodra.app.domain

object DeterministicRuleMatcher {
    fun match(methods: List<MethodDefinition>, rules: List<MatchingRule>, input: MatchingInput): List<MethodMatch> {
        val scores = mutableMapOf<String, Int>()
        val reasons = mutableMapOf<String, MutableList<String>>()
        rules.forEach { rule ->
            if (matchesRule(rule, input)) {
                scores[rule.methodId] = (scores[rule.methodId] ?: 0) + rule.score
                reasons.getOrPut(rule.methodId) { mutableListOf() }.add(rule.reason)
            }
        }
        methods.forEach { method ->
            if (input.goalDomain in method.goalDomains) scores[method.id] = (scores[method.id] ?: 0) + 6
            if (input.obstacle in method.obstacles) scores[method.id] = (scores[method.id] ?: 0) + 8
        }
        return methods.map { method ->
            MethodMatch(method, scores[method.id] ?: 0, reasons[method.id].orEmpty().distinct())
        }.filter { it.score > 0 }
            .sortedWith(compareByDescending<MethodMatch> { it.score }.thenBy { it.method.id })
            .take(3)
            .map { if (it.reasons.isEmpty()) it.copy(reasons = listOf("This method matches the goal domain and obstacle you selected.")) else it }
    }

    fun matchesRule(rule: MatchingRule, input: MatchingInput): Boolean {
        val actual = when (rule.field) {
            "goalDomain" -> input.goalDomain.name
            "obstacle" -> input.obstacle.name
            "highScreenTime" -> input.highScreenTime.toString()
            "structureLevel" -> input.structureLevel.name
            "availableMinutes" -> input.availableMinutes.toString()
            else -> return false
        }
        return when (rule.operator) {
            "eq" -> rule.values.firstOrNull()?.equals(actual, ignoreCase = true) == true
            "in" -> rule.values.any { it.equals(actual, ignoreCase = true) }
            "lte" -> actual.toIntOrNull()?.let { a -> rule.values.firstOrNull()?.toIntOrNull()?.let { a <= it } } == true
            "gte" -> actual.toIntOrNull()?.let { a -> rule.values.firstOrNull()?.toIntOrNull()?.let { a >= it } } == true
            else -> false
        }
    }
}
