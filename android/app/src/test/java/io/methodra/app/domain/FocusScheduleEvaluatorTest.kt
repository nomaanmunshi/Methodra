package io.methodra.app.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FocusScheduleEvaluatorTest {
    @Test
    fun weekdayWindowUsesStartInclusiveEndExclusive() {
        val weekdays = 0b0011111
        val window = FocusWindow(weekdays, 9 * 60, 12 * 60)
        assertFalse(FocusScheduleEvaluator.isActive(window, 1, 8 * 60 + 59))
        assertTrue(FocusScheduleEvaluator.isActive(window, 1, 9 * 60))
        assertTrue(FocusScheduleEvaluator.isActive(window, 5, 11 * 60 + 59))
        assertFalse(FocusScheduleEvaluator.isActive(window, 5, 12 * 60))
        assertFalse(FocusScheduleEvaluator.isActive(window, 6, 10 * 60))
    }

    @Test
    fun overnightWindowCarriesIntoFollowingDay() {
        val fridayOnly = 1 shl 4
        val window = FocusWindow(fridayOnly, 22 * 60, 2 * 60)
        assertTrue(FocusScheduleEvaluator.isActive(window, 5, 23 * 60))
        assertTrue(FocusScheduleEvaluator.isActive(window, 6, 60))
        assertFalse(FocusScheduleEvaluator.isActive(window, 6, 3 * 60))
    }

    @Test
    fun disabledAndZeroLengthWindowsAreNeverActive() {
        assertFalse(FocusScheduleEvaluator.isActive(FocusWindow(127, 600, 700, enabled = false), 1, 650))
        assertFalse(FocusScheduleEvaluator.isActive(FocusWindow(127, 600, 600), 1, 600))
    }
}
