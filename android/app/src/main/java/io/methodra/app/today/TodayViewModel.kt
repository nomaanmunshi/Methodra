package io.methodra.app.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.methodra.app.data.local.ActiveProtocolEntity
import io.methodra.app.data.local.DailyProtocolStateEntity
import io.methodra.app.data.repository.CatalogRepository
import io.methodra.app.data.repository.ProtocolRepository
import io.methodra.app.domain.MethodDefinition
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TodayUiState(
    val active: ActiveProtocolEntity? = null,
    val method: MethodDefinition? = null,
    val daily: DailyProtocolStateEntity? = null
) {
    val completedSteps: Set<Int>
        get() = daily?.completedStepIndexesCsv?.split(',')?.mapNotNull { it.toIntOrNull() }?.toSet().orEmpty()
}

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val protocolRepository: ProtocolRepository,
    private val catalog: CatalogRepository
) : ViewModel() {
    val state: StateFlow<TodayUiState> = protocolRepository.activeProtocol.flatMapLatest { active ->
        if (active == null) flowOf(TodayUiState())
        else protocolRepository.dailyState(active.id).map { daily ->
            TodayUiState(active, catalog.method(active.methodId), daily)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TodayUiState())

    fun toggleStep(index: Int) {
        val active = state.value.active ?: return
        viewModelScope.launch { protocolRepository.toggleStep(active.id, index) }
    }

    fun checkIn(rating: Int, note: String, automaticity: Int?, recoveryReason: String) {
        val active = state.value.active ?: return
        viewModelScope.launch {
            protocolRepository.checkIn(active.id, rating, note, automaticity, recoveryReason)
        }
    }
}
