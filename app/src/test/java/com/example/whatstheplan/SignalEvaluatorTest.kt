package com.example.whatstheplan

import com.example.whatstheplan.domain.model.EngagementLevel
import com.example.whatstheplan.notifications.SignalEvaluator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SignalEvaluatorTest {

    @Test
    fun `test fast swipe under 2000ms triggers Level 2 Stressed`() {
        val level = SignalEvaluator.evaluateDismissal(
            dismissalLatencyMs = 850L, // Fast swipe < 2s
            consecutiveDismissals = 1,
        )
        assertEquals(EngagementLevel.LEVEL_2_STRESSED, level)
        assertTrue(level.isDormant)
    }

    @Test
    fun `test consecutive dismissals 2 or more triggers Level 2 Stressed`() {
        val level = SignalEvaluator.evaluateDismissal(
            dismissalLatencyMs = 5000L,
            consecutiveDismissals = 2,
        )
        assertEquals(EngagementLevel.LEVEL_2_STRESSED, level)
    }

    @Test
    fun `test single standard dismissal triggers Level 1 Distracted`() {
        val level = SignalEvaluator.evaluateDismissal(
            dismissalLatencyMs = 6000L, // > 2s
            consecutiveDismissals = 1,
        )
        assertEquals(EngagementLevel.LEVEL_1_DISTRACTED, level)
        assertFalse(level.isDormant)
    }

    @Test
    fun `test terse text replies trigger Level 2 Stressed`() {
        assertEquals(EngagementLevel.LEVEL_2_STRESSED, SignalEvaluator.evaluateTextReply("busy"))
        assertEquals(EngagementLevel.LEVEL_2_STRESSED, SignalEvaluator.evaluateTextReply("stop"))
        assertEquals(EngagementLevel.LEVEL_2_STRESSED, SignalEvaluator.evaluateTextReply("k"))
        assertEquals(EngagementLevel.LEVEL_2_STRESSED, SignalEvaluator.evaluateTextReply("no"))
        assertEquals(EngagementLevel.LEVEL_2_STRESSED, SignalEvaluator.evaluateTextReply("later"))
    }

    @Test
    fun `test meaningful intention reply triggers Level 0 Normal`() {
        val level = SignalEvaluator.evaluateTextReply("Finish reading chapter 4 and write summary")
        assertEquals(EngagementLevel.LEVEL_0_NORMAL, level)
    }

    @Test
    fun `test multi-day inactivity 48h triggers Level 3 Ghosting`() {
        val now = System.currentTimeMillis()
        val interaction49hAgo = now - (49 * 60 * 60 * 1000L)
        val interaction10hAgo = now - (10 * 60 * 60 * 1000L)

        assertEquals(EngagementLevel.LEVEL_3_GHOSTING, SignalEvaluator.checkInactivityLevel(interaction49hAgo, now))
        assertNull(SignalEvaluator.checkInactivityLevel(interaction10hAgo, now))
    }

    @Test
    fun `test zero-overlap 4-hour window guardrail`() {
        val now = System.currentTimeMillis()
        val posted1hAgo = now - (1 * 60 * 60 * 1000L)
        val posted5hAgo = now - (5 * 60 * 60 * 1000L)

        // Under 4 hours: overlap violated
        assertFalse(SignalEvaluator.canPostNotificationWithoutOverlap(posted1hAgo, now))

        // 5 hours: overlap satisfied
        assertTrue(SignalEvaluator.canPostNotificationWithoutOverlap(posted5hAgo, now))
        assertTrue(SignalEvaluator.canPostNotificationWithoutOverlap(0L, now))
    }
}
