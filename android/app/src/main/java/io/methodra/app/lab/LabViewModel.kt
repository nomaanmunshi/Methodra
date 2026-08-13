package io.methodra.app.lab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.methodra.app.data.local.PersonalTrialEntity
import io.methodra.app.data.local.TrialEntryEntity
import io.methodra.app.data.repository.CatalogRepository
import io.methodra.app.data.repository.ProtocolRepository
import io.methodra.app.data.repository.TrialRepository
import io.methodra.app.domain.TrialSummary
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LabUiState(
    val trial: PersonalTrialEntity? = null,
    val entries: List<TrialEntryEntity> = emptyList(),
    val summary: TrialSummary = TrialSummary(0, 0, null, "Insufficient data."),
    val suggestedMethodId: String = "if_then_start"
)

@HiltViewModel
class LabViewModel @Inject constructor(
    private val trials: TrialRepository,
    protocolRepository: ProtocolRepository
) : ViewModel() {
    private val suggestedMethod = protocolRepository.activeProtocol.map { it?.methodId ?: "if_then_start" }

    val state: StateFlow<LabUiState> = combine(trials.activeTrial, suggestedMethod) { trial, methodId -> trial to methodId }
        .flatMapLatest { (trial, methodId) ->
            if (trial == null) flowOf(LabUiState(suggestedMethodId = methodId))
            else trials.entries(trial.id).map { entries ->
                LabUiState(trial, entries, trials.summarize(entries), methodId)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LabUiState())

    fun create(question: String, metric: String) = viewModelScope.launch {
        trials.create(question, state.value.suggestedMethodId, metric)
    }

    fun log(adhered: Boolean, value: Double?, note: String) {
        val trial = state.value.trial ?: return
        viewModelScope.launch { trials.log(trial.id, adhered, value, note) }
    }

    fun decide(decision: String) {
        val trial = state.value.trial ?: return
        viewModelScope.launch { trials.decide(trial.id, decision) }
    }
}
