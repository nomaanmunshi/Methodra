package io.methodra.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.methodra.app.data.repository.CatalogRepository
import io.methodra.app.data.repository.PreferencesRepository
import io.methodra.app.data.repository.ProtocolRepository
import io.methodra.app.domain.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    preferences: PreferencesRepository,
    protocolRepository: ProtocolRepository
) : ViewModel() {
    val onboardingComplete: StateFlow<Boolean?> = preferences.onboardingComplete
        .map { value -> value as Boolean? }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val activeProtocol = protocolRepository.activeProtocol
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val engine: io.methodra.app.data.repository.MethodEngine,
    private val protocolRepository: ProtocolRepository,
    private val preferences: PreferencesRepository
) : ViewModel() {
    private val _draft = MutableStateFlow(AssessmentDraft())
    val draft: StateFlow<AssessmentDraft> = _draft.asStateFlow()

    private val _matches = MutableStateFlow<List<MethodMatch>>(emptyList())
    val matches: StateFlow<List<MethodMatch>> = _matches.asStateFlow()
    val reduceMotion = preferences.reduceMotion.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)
    val haptics = preferences.haptics.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    fun update(block: (AssessmentDraft) -> AssessmentDraft) { _draft.update(block) }
    fun calculateMatches() { _matches.value = engine.match(_draft.value.toInput()) }

    fun activate(match: MethodMatch, answers: List<String>) {
        val setupSummary = match.method.setupQuestions.zip(answers).joinToString("\n") { (question, answer) ->
            "$question — ${answer.trim()}"
        }
        viewModelScope.launch {
            protocolRepository.activate(match.method.id, _draft.value.desiredOutcome, match.reasons, setupSummary)
            preferences.setOnboardingComplete(true)
        }
    }
}

@HiltViewModel
class MethodsViewModel @Inject constructor(
    catalog: CatalogRepository
) : ViewModel() {
    val methods: List<MethodDefinition> = catalog.methods
    val collections: List<BookProtocolCollection> = catalog.collections
    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()
    fun setQuery(value: String) { _query.value = value }
}
