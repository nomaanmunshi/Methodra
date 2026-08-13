package io.methodra.app.focus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.methodra.app.data.local.FocusRuleEntity
import io.methodra.app.data.local.FocusScheduleEntity
import io.methodra.app.data.local.FocusSessionEntity
import io.methodra.app.data.repository.FocusRepository
import io.methodra.app.domain.UsageApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FocusUiState(
    val rules: List<FocusRuleEntity> = emptyList(),
    val schedules: List<FocusScheduleEntity> = emptyList(),
    val activeSession: FocusSessionEntity? = null,
    val apps: List<UsageApp> = emptyList(),
    val usage: List<UsageApp> = emptyList(),
    val hasUsageAccess: Boolean = false,
    val durationMinutes: Int = 35
)

@HiltViewModel
class FocusViewModel @Inject constructor(
    private val repository: FocusRepository,
    private val appsReader: InstalledAppsReader
) : ViewModel() {
    private val extras = MutableStateFlow(FocusUiState())

    val state: StateFlow<FocusUiState> = combine(
        repository.rules,
        repository.schedules,
        repository.activeSession,
        extras
    ) { rules, schedules, session, extra ->
        extra.copy(rules = rules, schedules = schedules, activeSession = session)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FocusUiState())

    init {
        viewModelScope.launch { repository.refreshGuardMirror() }
        refreshAppsAndUsage()
    }

    fun refreshAppsAndUsage() {
        viewModelScope.launch(Dispatchers.IO) {
            val apps = appsReader.launchableApps()
            val access = repository.hasUsageAccess()
            val usage = if (access) repository.todayUsage() else emptyList()
            extras.update { it.copy(apps = apps, usage = usage, hasUsageAccess = access) }
        }
    }

    fun setDuration(minutes: Int) { extras.update { it.copy(durationMinutes = minutes.coerceIn(5, 180)) } }

    fun toggleApp(app: UsageApp, selected: Boolean) = viewModelScope.launch { repository.toggleRule(app, selected) }
    fun setBudget(rule: FocusRuleEntity, minutes: Int) = viewModelScope.launch { repository.setBudget(rule.packageName, rule.appLabel, minutes) }
    fun saveSchedule(schedule: FocusScheduleEntity) = viewModelScope.launch { repository.saveSchedule(schedule) }
    fun deleteSchedule(schedule: FocusScheduleEntity) = viewModelScope.launch { repository.deleteSchedule(schedule.id) }
    fun setScheduleEnabled(schedule: FocusScheduleEntity, enabled: Boolean) = viewModelScope.launch { repository.setScheduleEnabled(schedule, enabled) }
    fun start(methodId: String? = null) = viewModelScope.launch { repository.startSession(state.value.durationMinutes, methodId) }
    fun end() = viewModelScope.launch { repository.endSession() }
    fun emergencyExit(reason: String) = viewModelScope.launch { repository.endSession(reason, true) }
    fun openUsageSettings() = repository.openUsageAccessSettings()
    fun openAccessibilitySettings() = repository.openAccessibilitySettings()
}
