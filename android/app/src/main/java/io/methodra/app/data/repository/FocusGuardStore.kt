package io.methodra.app.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.methodra.app.data.local.FocusRuleEntity
import io.methodra.app.data.local.FocusScheduleEntity
import io.methodra.app.domain.FocusScheduleEvaluator
import io.methodra.app.domain.FocusWindow
import org.json.JSONArray
import org.json.JSONObject
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FocusGuardStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences("methodra_focus_guard", Context.MODE_PRIVATE)

    fun mirrorRules(rules: List<FocusRuleEntity>) {
        val json = JSONArray().apply {
            rules.forEach { rule ->
                put(JSONObject().apply {
                    put("packageName", rule.packageName)
                    put("label", rule.appLabel)
                    put("blockDuringFocus", rule.blockDuringFocus)
                    put("dailyBudgetMinutes", rule.dailyBudgetMinutes)
                })
            }
        }
        prefs.edit().putString("rules", json.toString()).apply()
    }

    fun rules(): List<FocusRuleEntity> {
        val raw = prefs.getString("rules", "[]") ?: "[]"
        val array = runCatching { JSONArray(raw) }.getOrElse { JSONArray() }
        return (0 until array.length()).map { i ->
            array.getJSONObject(i).let {
                FocusRuleEntity(
                    packageName = it.getString("packageName"),
                    appLabel = it.optString("label", it.getString("packageName")),
                    blockDuringFocus = it.optBoolean("blockDuringFocus", true),
                    dailyBudgetMinutes = it.optInt("dailyBudgetMinutes", 0)
                )
            }
        }
    }

    fun mirrorSchedules(schedules: List<FocusScheduleEntity>) {
        val json = JSONArray().apply {
            schedules.forEach { schedule ->
                put(JSONObject().apply {
                    put("id", schedule.id)
                    put("name", schedule.name)
                    put("daysMask", schedule.daysMask)
                    put("startMinuteOfDay", schedule.startMinuteOfDay)
                    put("endMinuteOfDay", schedule.endMinuteOfDay)
                    put("enabled", schedule.enabled)
                })
            }
        }
        prefs.edit().putString("schedules", json.toString()).apply()
    }

    private fun schedules(): List<FocusScheduleEntity> {
        val raw = prefs.getString("schedules", "[]") ?: "[]"
        val array = runCatching { JSONArray(raw) }.getOrElse { JSONArray() }
        return (0 until array.length()).map { i ->
            array.getJSONObject(i).let {
                FocusScheduleEntity(
                    id = it.getString("id"),
                    name = it.optString("name", "Focus schedule"),
                    daysMask = it.optInt("daysMask", 0),
                    startMinuteOfDay = it.optInt("startMinuteOfDay", 0),
                    endMinuteOfDay = it.optInt("endMinuteOfDay", 0),
                    enabled = it.optBoolean("enabled", true)
                )
            }
        }
    }

    fun setActiveSession(sessionId: String, endEpochMillis: Long) {
        prefs.edit().putString("sessionId", sessionId).putLong("end", endEpochMillis).apply()
    }

    fun clearActiveSession() {
        prefs.edit().remove("sessionId").remove("end").apply()
    }

    fun clearAll() {
        prefs.edit().clear().commit()
    }

    fun activeSessionId(): String? = prefs.getString("sessionId", null)
    fun activeEndEpochMillis(): Long = prefs.getLong("end", 0L)
    fun isActive(): Boolean = activeSessionId() != null && System.currentTimeMillis() < activeEndEpochMillis()

    fun isScheduledProtectionActive(now: ZonedDateTime = ZonedDateTime.now()): Boolean {
        val minute = now.hour * 60 + now.minute
        return schedules().any { schedule ->
            FocusScheduleEvaluator.isActive(
                FocusWindow(schedule.daysMask, schedule.startMinuteOfDay, schedule.endMinuteOfDay, schedule.enabled),
                now.dayOfWeek.value,
                minute
            )
        }
    }
}
