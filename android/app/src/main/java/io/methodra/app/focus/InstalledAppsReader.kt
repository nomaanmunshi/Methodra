package io.methodra.app.focus

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import io.methodra.app.domain.UsageApp
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InstalledAppsReader @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun launchableApps(): List<UsageApp> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL)
            .asSequence()
            .map { info -> UsageApp(info.activityInfo.packageName, info.loadLabel(context.packageManager).toString()) }
            .filter { it.packageName != context.packageName }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
            .toList()
    }
}
