package io.methodra.app.data.repository

import io.methodra.app.domain.DeterministicRuleMatcher
import io.methodra.app.domain.MatchingInput
import io.methodra.app.domain.MethodMatch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MethodEngine @Inject constructor(
    private val catalog: CatalogRepository
) {
    fun match(input: MatchingInput): List<MethodMatch> = DeterministicRuleMatcher.match(catalog.methods, catalog.rules, input)
}
