package io.methodra.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "active_protocols")
data class ActiveProtocolEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val methodId: String,
    val desiredOutcome: String,
    val matchReasonsText: String = "",
    val setupSummaryText: String = "",
    val startedAtEpochMillis: Long,
    val active: Boolean = true
)

@Entity(tableName = "daily_protocol_state", indices = [Index(value = ["protocolId", "localDate"], unique = true)])
data class DailyProtocolStateEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val protocolId: String,
    val localDate: String,
    val completedStepIndexesCsv: String = "",
    val checkInRating: Int? = null,
    val automaticityRating: Int? = null,
    val recoveryReason: String = "",
    val note: String = "",
    val updatedAtEpochMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "focus_rules")
data class FocusRuleEntity(
    @PrimaryKey val packageName: String,
    val appLabel: String,
    val blockDuringFocus: Boolean = true,
    val dailyBudgetMinutes: Int = 0,
    val updatedAtEpochMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val methodId: String? = null,
    val startedAtEpochMillis: Long,
    val plannedEndEpochMillis: Long,
    val endedAtEpochMillis: Long? = null,
    val emergencyExit: Boolean = false,
    val exitReason: String = ""
)

/** Monday is bit 0, Sunday is bit 6. Overnight windows are supported. */
@Entity(tableName = "focus_schedules")
data class FocusScheduleEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val daysMask: Int,
    val startMinuteOfDay: Int,
    val endMinuteOfDay: Int,
    val enabled: Boolean = true,
    val updatedAtEpochMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "personal_trials")
data class PersonalTrialEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val question: String,
    val methodId: String,
    val primaryMetricName: String,
    val startedLocalDate: String,
    val plannedDays: Int = 14,
    val status: String = "ACTIVE",
    val decision: String = "INCONCLUSIVE"
)

@Entity(tableName = "trial_entries", indices = [Index(value = ["trialId", "localDate"], unique = true)])
data class TrialEntryEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val trialId: String,
    val localDate: String,
    val adhered: Boolean,
    val metricValue: Double?,
    val contextNote: String = ""
)
