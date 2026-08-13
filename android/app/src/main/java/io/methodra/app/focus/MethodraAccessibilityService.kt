package io.methodra.app.focus

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import dagger.hilt.android.AndroidEntryPoint
import io.methodra.app.data.repository.FocusGuardStore
import io.methodra.app.data.repository.UsageStatsReader
import javax.inject.Inject

@AndroidEntryPoint
class MethodraAccessibilityService : AccessibilityService() {
    @Inject lateinit var guardStore: FocusGuardStore
    @Inject lateinit var usageStatsReader: UsageStatsReader

    private var lastInterceptAt = 0L
    private var lastPackage: String? = null

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val packageName = event.packageName?.toString() ?: return
        if (packageName == applicationContext.packageName) return

        val rule = guardStore.rules().firstOrNull { it.packageName == packageName } ?: return
        val sessionActive = guardStore.isActive()
        val scheduleActive = guardStore.isScheduledProtectionActive()
        val activeProtection = (sessionActive || scheduleActive) && rule.blockDuringFocus
        val budgetExceeded = rule.dailyBudgetMinutes > 0 && usageStatsReader.hasPermission() &&
            usageStatsReader.todayMinutes(packageName) >= rule.dailyBudgetMinutes
        if (!activeProtection && !budgetExceeded) return

        val now = System.currentTimeMillis()
        if (lastPackage == packageName && now - lastInterceptAt < 1_500) return
        lastPackage = packageName
        lastInterceptAt = now

        val reason = when {
            sessionActive -> "FOCUS_SESSION"
            scheduleActive -> "FOCUS_SCHEDULE"
            else -> "DAILY_BUDGET"
        }
        // Move away from the distracting app first; the explanatory Methodra screen then sits on top.
        // The user can still deliberately disable the service or rule at any time.
        performGlobalAction(GLOBAL_ACTION_BACK)
        val intent = Intent(this, BlockInterruptionActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(BlockInterruptionActivity.EXTRA_PACKAGE, packageName)
            putExtra(BlockInterruptionActivity.EXTRA_REASON, reason)
        }
        runCatching { startActivity(intent) }
            .onFailure { performGlobalAction(GLOBAL_ACTION_BACK) }
    }

    override fun onInterrupt() = Unit
}
