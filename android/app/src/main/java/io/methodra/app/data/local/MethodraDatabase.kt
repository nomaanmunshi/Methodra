package io.methodra.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        ActiveProtocolEntity::class,
        DailyProtocolStateEntity::class,
        FocusRuleEntity::class,
        FocusSessionEntity::class,
        FocusScheduleEntity::class,
        PersonalTrialEntity::class,
        TrialEntryEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class MethodraDatabase : RoomDatabase() {
    abstract fun protocolDao(): ProtocolDao
    abstract fun focusDao(): FocusDao
    abstract fun trialDao(): TrialDao
}
