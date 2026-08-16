package com.example.whatstheplan

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SchedulerTimingTest {

    private fun isWithinActiveHours(currentMinutes: Int, startMinutes: Int, endMinutes: Int): Boolean {
        return if (startMinutes <= endMinutes) {
            currentMinutes in startMinutes..endMinutes
        } else {
            currentMinutes >= startMinutes || currentMinutes <= endMinutes
        }
    }

    @Test
    fun testDaytimeActiveWindow() {
        val start = 9 * 60 // 09:00
        val end = 22 * 60 // 22:00

        assertTrue(isWithinActiveHours(12 * 60, start, end)) // Noon
        assertTrue(isWithinActiveHours(9 * 60, start, end))  // Exactly start
        assertTrue(isWithinActiveHours(22 * 60, start, end)) // Exactly end
        assertFalse(isWithinActiveHours(8 * 60, start, end)) // 08:00 (before start)
        assertFalse(isWithinActiveHours(23 * 60, start, end))// 23:00 (after end)
    }

    @Test
    fun testOvernightActiveWindow() {
        val start = 20 * 60 // 20:00 (8 PM)
        val end = 4 * 60   // 04:00 (4 AM next day)

        assertTrue(isWithinActiveHours(21 * 60, start, end)) // 21:00
        assertTrue(isWithinActiveHours(2 * 60, start, end))  // 02:00
        assertFalse(isWithinActiveHours(12 * 60, start, end))// Noon
        assertFalse(isWithinActiveHours(5 * 60, start, end)) // 05:00
    }
}
