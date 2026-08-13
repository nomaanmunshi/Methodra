package io.methodra.app.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.methodra.app.data.local.MethodraDatabase
import io.methodra.app.data.repository.ExportRepository
import io.methodra.app.data.repository.FocusGuardStore
import io.methodra.app.data.repository.PreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferences: PreferencesRepository,
    private val database: MethodraDatabase,
    private val exportRepository: ExportRepository,
    private val focusGuardStore: FocusGuardStore
) : ViewModel() {
    val reduceMotion = preferences.reduceMotion.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)
    val haptics = preferences.haptics.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    fun setReduceMotion(value: Boolean) = viewModelScope.launch { preferences.setReduceMotion(value) }
    fun setHaptics(value: Boolean) = viewModelScope.launch { preferences.setHaptics(value) }
    fun restartOnboarding() = viewModelScope.launch { preferences.resetOnboarding() }
    fun deleteLocalData() = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            database.clearAllTables()
            focusGuardStore.clearAll()
        }
        preferences.clearAll()
    }

    fun export(onReady: (String) -> Unit) = viewModelScope.launch {
        val data = withContext(Dispatchers.IO) { exportRepository.exportJson() }
        onReady(data)
    }
}
