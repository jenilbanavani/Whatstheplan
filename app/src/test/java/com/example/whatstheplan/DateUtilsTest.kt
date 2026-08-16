package com.example.whatstheplan

import com.example.whatstheplan.utils.DateUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DateUtilsTest {

    @Test
    fun testFormatClock() {
        assertEquals("09:00", DateUtils.formatClock(9 * 60))
        assertEquals("22:30", DateUtils.formatClock(22 * 60 + 30))
        assertEquals("00:00", DateUtils.formatClock(0))
        assertEquals("00:15", DateUtils.formatClock(15))
    }

    @Test
    fun testFormatDuration() {
        assertEquals("2h 15m", DateUtils.formatDuration((2 * 60 + 15) * 60 * 1000L))
        assertEquals("1h", DateUtils.formatDuration(60 * 60 * 1000L))
        assertEquals("45m", DateUtils.formatDuration(45 * 60 * 1000L))
        assertEquals("0m", DateUtils.formatDuration(0L))
    }

    @Test
    fun testTodayString() {
        val today = DateUtils.todayString()
        assertTrue(today.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
    }

    @Test
    fun testFriendlyDate() {
        val friendly = DateUtils.friendlyDate("2026-08-16")
        assertTrue(friendly.isNotBlank())
    }
}
