package io.methodra.app.data.repository

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Process
import dagger.hilt.android.qualifiers.ApplicationContext
import io.methodra.app.domain.UsageApp
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsageStatsReader @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun hasPermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun todayUsage(): List<UsageApp> {
        if (!hasPermission()) return emptyList()
        val zone = ZoneId.systemDefault()
        val start = LocalDate.now().atStartOfDay(zone).toInstant().toEpochMilli()
        val end = System.currentTimeMillis()
        val manager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        return manager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end)
            .asSequence()
            .filter { it.totalTimeInForeground > 0 }
            .mapNotNull { stat ->
                val label = runCatching {
                    val info: ApplicationInfo = context.packageManager.getApplicationInfo(stat.packageName, 0)
                    context.packageManager.getApplicationLabel(info).toString()
                }.getOrNull() ?: return@mapNotNull null
                UsageApp(stat.packageName, label, stat.totalTimeInForeground / 60_000L)
            }
            .sortedByDescending { it.minutesToday }
            .toList()
    }

    fun todayMinutes(packageName: String): Long = todayUsage().firstOrNull { it.packageName == packageName }?.minutesToday ?: 0
}
