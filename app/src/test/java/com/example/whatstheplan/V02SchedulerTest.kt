package com.example.whatstheplan

import com.example.whatstheplan.notifications.CheckInScheduler
import org.junit.Assert.assertTrue
import org.junit.Test

class V02SchedulerTest {

    @Test
    fun `test daily delay is positive and reasonable`() {
        val delay = CheckInScheduler.dailyDelayMillis(9 * 60) // 9:00 AM
        assertTrue(delay >= 5_000L)
        assertTrue(delay <= 24 * 60 * 60 * 1000L + 60_000L)
    }

    @Test
    fun `test midnight wrap around calculation`() {
        val delay = CheckInScheduler.dailyDelayMillis(23 * 60 + 59) // 23:59
        assertTrue(delay >= 5_000L)
    }
}
