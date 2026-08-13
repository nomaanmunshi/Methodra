package io.methodra.app.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.methodra.app.data.local.FocusDao
import io.methodra.app.data.local.MethodraDatabase
import io.methodra.app.data.local.ProtocolDao
import io.methodra.app.data.local.TrialDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun database(@ApplicationContext context: Context): MethodraDatabase =
        Room.databaseBuilder(context, MethodraDatabase::class.java, "methodra.db")
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()

    @Provides fun protocolDao(db: MethodraDatabase): ProtocolDao = db.protocolDao()
    @Provides fun focusDao(db: MethodraDatabase): FocusDao = db.focusDao()
    @Provides fun trialDao(db: MethodraDatabase): TrialDao = db.trialDao()
}
