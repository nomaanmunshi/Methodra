package io.methodra.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.methodraDataStore by preferencesDataStore(name = "methodra_preferences")

@Singleton
class PreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val onboardingComplete = booleanPreferencesKey("onboarding_complete")
        val reduceMotion = booleanPreferencesKey("reduce_motion")
        val haptics = booleanPreferencesKey("haptics")
    }

    val onboardingComplete: Flow<Boolean> = context.methodraDataStore.data.map { it[Keys.onboardingComplete] ?: false }
    val reduceMotion: Flow<Boolean> = context.methodraDataStore.data.map { it[Keys.reduceMotion] ?: false }
    val haptics: Flow<Boolean> = context.methodraDataStore.data.map { it[Keys.haptics] ?: true }

    suspend fun setOnboardingComplete(value: Boolean) = context.methodraDataStore.edit { it[Keys.onboardingComplete] = value }
    suspend fun setReduceMotion(value: Boolean) = context.methodraDataStore.edit { it[Keys.reduceMotion] = value }
    suspend fun setHaptics(value: Boolean) = context.methodraDataStore.edit { it[Keys.haptics] = value }
    suspend fun resetOnboarding() = context.methodraDataStore.edit { it[Keys.onboardingComplete] = false }
    suspend fun clearAll() = context.methodraDataStore.edit { it.clear() }
}
