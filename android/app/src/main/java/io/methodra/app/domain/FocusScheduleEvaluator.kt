package io.methodra.app.domain

data class FocusWindow(
    val daysMask: Int,
    val startMinuteOfDay: Int,
    val endMinuteOfDay: Int,
    val enabled: Boolean = true
)

object FocusScheduleEvaluator {
    /** dayOfWeek is ISO-8601: Monday=1 ... Sunday=7. */
    fun isActive(window: FocusWindow, dayOfWeek: Int, minuteOfDay: Int): Boolean {
        if (!window.enabled || window.daysMask !in 1..127 || dayOfWeek !in 1..7 || minuteOfDay !in 0..1439) return false
        val start = window.startMinuteOfDay.coerceIn(0, 1439)
        val end = window.endMinuteOfDay.coerceIn(0, 1439)
        if (start == end) return false

        val todayBit = 1 shl (dayOfWeek - 1)
        val previousBit = 1 shl ((dayOfWeek + 5) % 7)
        return if (start < end) {
            window.daysMask and todayBit != 0 && minuteOfDay in start until end
        } else {
            (window.daysMask and todayBit != 0 && minuteOfDay >= start) ||
                (window.daysMask and previousBit != 0 && minuteOfDay < end)
        }
    }
}
