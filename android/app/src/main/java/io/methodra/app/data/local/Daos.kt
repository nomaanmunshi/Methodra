package io.methodra.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProtocolDao {
    @Query("SELECT * FROM active_protocols WHERE active = 1 ORDER BY startedAtEpochMillis DESC LIMIT 1")
    fun observeActive(): Flow<ActiveProtocolEntity?>

    @Query("SELECT * FROM active_protocols WHERE active = 1 ORDER BY startedAtEpochMillis DESC LIMIT 1")
    suspend fun getActive(): ActiveProtocolEntity?

    @Query("SELECT * FROM active_protocols ORDER BY startedAtEpochMillis")
    suspend fun getAllProtocols(): List<ActiveProtocolEntity>

    @Query("SELECT * FROM daily_protocol_state ORDER BY localDate")
    suspend fun getAllDailyStates(): List<DailyProtocolStateEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(protocol: ActiveProtocolEntity)

    @Query("UPDATE active_protocols SET active = 0 WHERE active = 1")
    suspend fun deactivateAll()

    @Query("SELECT * FROM daily_protocol_state WHERE protocolId = :protocolId AND localDate = :localDate LIMIT 1")
    fun observeDaily(protocolId: String, localDate: String): Flow<DailyProtocolStateEntity?>

    @Query("SELECT * FROM daily_protocol_state WHERE protocolId = :protocolId AND localDate = :localDate LIMIT 1")
    suspend fun getDaily(protocolId: String, localDate: String): DailyProtocolStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDaily(state: DailyProtocolStateEntity)
}

@Dao
interface FocusDao {
    @Query("SELECT * FROM focus_rules ORDER BY appLabel COLLATE NOCASE")
    fun observeRules(): Flow<List<FocusRuleEntity>>

    @Query("SELECT * FROM focus_rules")
    suspend fun getRules(): List<FocusRuleEntity>

    @Query("SELECT * FROM focus_sessions ORDER BY startedAtEpochMillis")
    suspend fun getAllSessions(): List<FocusSessionEntity>

    @Query("SELECT * FROM focus_schedules ORDER BY name COLLATE NOCASE")
    fun observeSchedules(): Flow<List<FocusScheduleEntity>>

    @Query("SELECT * FROM focus_schedules ORDER BY name COLLATE NOCASE")
    suspend fun getSchedules(): List<FocusScheduleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSchedule(schedule: FocusScheduleEntity)

    @Query("DELETE FROM focus_schedules WHERE id = :id")
    suspend fun deleteSchedule(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRule(rule: FocusRuleEntity)

    @Query("DELETE FROM focus_rules WHERE packageName = :packageName")
    suspend fun deleteRule(packageName: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FocusSessionEntity)

    @Query("SELECT * FROM focus_sessions WHERE endedAtEpochMillis IS NULL ORDER BY startedAtEpochMillis DESC LIMIT 1")
    fun observeActiveSession(): Flow<FocusSessionEntity?>

    @Query("SELECT * FROM focus_sessions WHERE endedAtEpochMillis IS NULL ORDER BY startedAtEpochMillis DESC LIMIT 1")
    suspend fun getActiveSession(): FocusSessionEntity?

    @Query("UPDATE focus_sessions SET endedAtEpochMillis = :endedAt, emergencyExit = :emergencyExit, exitReason = :reason WHERE id = :id")
    suspend fun endSession(id: String, endedAt: Long, emergencyExit: Boolean, reason: String)

    @Query("SELECT * FROM focus_sessions ORDER BY startedAtEpochMillis DESC LIMIT :limit")
    fun observeRecentSessions(limit: Int = 20): Flow<List<FocusSessionEntity>>
}

@Dao
interface TrialDao {
    @Query("SELECT * FROM personal_trials WHERE status = 'ACTIVE' ORDER BY startedLocalDate DESC LIMIT 1")
    fun observeActiveTrial(): Flow<PersonalTrialEntity?>

    @Query("SELECT * FROM personal_trials ORDER BY startedLocalDate")
    suspend fun getAllTrials(): List<PersonalTrialEntity>

    @Query("SELECT * FROM trial_entries ORDER BY localDate")
    suspend fun getAllEntries(): List<TrialEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTrial(trial: PersonalTrialEntity)

    @Query("UPDATE personal_trials SET decision = :decision, status = :status WHERE id = :id")
    suspend fun decide(id: String, decision: String, status: String)

    @Query("SELECT * FROM trial_entries WHERE trialId = :trialId ORDER BY localDate")
    fun observeEntries(trialId: String): Flow<List<TrialEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEntry(entry: TrialEntryEntity)
}
