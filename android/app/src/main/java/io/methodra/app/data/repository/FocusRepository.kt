package io.methodra.app.data.repository

import android.content.Context
import android.content.Intent
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import io.methodra.app.data.local.FocusDao
import io.methodra.app.data.local.FocusRuleEntity
import io.methodra.app.data.local.FocusScheduleEntity
import io.methodra.app.data.local.FocusSessionEntity
import io.methodra.app.domain.UsageApp
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FocusRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: FocusDao,
    private val guardStore: FocusGuardStore,
    private val usageStatsReader: UsageStatsReader
) {
    val rules: Flow<List<FocusRuleEntity>> = dao.observeRules()
    val schedules: Flow<List<FocusScheduleEntity>> = dao.observeSchedules()
    val activeSession: Flow<FocusSessionEntity?> = dao.observeActiveSession()
    val recentSessions: Flow<List<FocusSessionEntity>> = dao.observeRecentSessions()

    suspend fun refreshGuardMirror() {
        val existing = dao.getActiveSession()
        if (existing != null && existing.plannedEndEpochMillis <= System.currentTimeMillis()) {
            dao.endSession(existing.id, existing.plannedEndEpochMillis, false, "Completed while app was closed")
            guardStore.clearActiveSession()
        } else if (existing != null) {
            guardStore.setActiveSession(existing.id, existing.plannedEndEpochMillis)
        }
        guardStore.mirrorRules(dao.getRules())
        guardStore.mirrorSchedules(dao.getSchedules())
    }

    suspend fun toggleRule(app: UsageApp, selected: Boolean) {
        if (selected) {
            dao.upsertRule(FocusRuleEntity(app.packageName, app.label, blockDuringFocus = true, dailyBudgetMinutes = 30))
        } else {
            dao.deleteRule(app.packageName)
        }
        guardStore.mirrorRules(dao.getRules())
    }

    suspend fun setBudget(packageName: String, appLabel: String, minutes: Int) {
        dao.upsertRule(FocusRuleEntity(packageName, appLabel, blockDuringFocus = true, dailyBudgetMinutes = minutes.coerceAtLeast(0)))
        guardStore.mirrorRules(dao.getRules())
    }

    suspend fun saveSchedule(schedule: FocusScheduleEntity) {
        require(schedule.daysMask in 1..127) { "Choose at least one day" }
        require(schedule.startMinuteOfDay in 0..1439 && schedule.endMinuteOfDay in 0..1439) { "Invalid schedule time" }
        require(schedule.startMinuteOfDay != schedule.endMinuteOfDay) { "Start and end cannot be identical" }
        dao.upsertSchedule(schedule.copy(updatedAtEpochMillis = System.currentTimeMillis()))
        guardStore.mirrorSchedules(dao.getSchedules())
    }

    suspend fun deleteSchedule(id: String) {
        dao.deleteSchedule(id)
        guardStore.mirrorSchedules(dao.getSchedules())
    }

    suspend fun setScheduleEnabled(schedule: FocusScheduleEntity, enabled: Boolean) {
        saveSchedule(schedule.copy(enabled = enabled))
    }

    suspend fun startSession(durationMinutes: Int, methodId: String?): FocusSessionEntity {
        dao.getActiveSession()?.let { existing ->
            dao.endSession(existing.id, System.currentTimeMillis(), false, "Replaced by a new session")
        }
        val now = System.currentTimeMillis()
        val session = FocusSessionEntity(
            methodId = methodId,
            startedAtEpochMillis = now,
            plannedEndEpochMillis = now + durationMinutes.coerceIn(5, 180) * 60_000L
        )
        dao.insertSession(session)
        guardStore.mirrorRules(dao.getRules())
        guardStore.mirrorSchedules(dao.getSchedules())
        guardStore.setActiveSession(session.id, session.plannedEndEpochMillis)
        return session
    }

    suspend fun endSession(reason: String = "", emergency: Boolean = false) {
        val current = dao.getActiveSession() ?: return
        dao.endSession(current.id, System.currentTimeMillis(), emergency, reason)
        guardStore.clearActiveSession()
    }

    fun hasUsageAccess(): Boolean = usageStatsReader.hasPermission()
    fun todayUsage(): List<UsageApp> = usageStatsReader.todayUsage()

    fun openUsageAccessSettings() {
        context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    fun openAccessibilitySettings() {
        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
