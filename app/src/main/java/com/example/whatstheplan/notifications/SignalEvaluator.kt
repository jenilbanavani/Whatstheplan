package com.example.whatstheplan.notifications

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.whatstheplan.domain.model.EngagementLevel
import com.example.whatstheplan.domain.model.UserSettings

object SignalEvaluator {

    const val MIN_NOTIFICATION_INTERVAL_MILLIS = 4 * 60 * 60 * 1000L // 4 Hours (Zero-Overlaps)
    const val DELAY_DISTRACTED_MILLIS = 3 * 60 * 60 * 1000L + 30 * 60 * 1000L // 3.5 Hours (+3.5h delay)
    const val INACTIVITY_GHOSTING_MILLIS = 48 * 60 * 60 * 1000L // 48 Hours

    private val TERSE_RESPONSES = setOf(
        "busy", "stop", "k", "no", "later", "nah", "cant", "can't", "not now", "shh", "mute",
    )

    /**
     * Evaluates dismissal signals:
     * - Fast dismissal (< 2s) OR Double swipe (>= 2 consecutive) -> LEVEL 2 (Stressed / Dormant)
     * - Single standard dismissal -> LEVEL 1 (Distracted / +3.5h delay)
     */
    fun evaluateDismissal(
        dismissalLatencyMs: Long,
        consecutiveDismissals: Int,
    ): EngagementLevel {
        return if (dismissalLatencyMs < 2000L || consecutiveDismissals >= 2) {
            EngagementLevel.LEVEL_2_STRESSED
        } else {
            EngagementLevel.LEVEL_1_DISTRACTED
        }
    }

    /**
     * Evaluates text response / action signals:
     * - Terse words ("busy", "stop", "k") -> LEVEL 2 (Stressed / Dormant)
     * - Intentional replies or < 15 min -> LEVEL 0 (Normal)
     */
    fun evaluateTextReply(text: String): EngagementLevel {
        val clean = text.trim().lowercase()
        return if (clean in TERSE_RESPONSES || clean.length <= 3) {
            EngagementLevel.LEVEL_2_STRESSED
        } else {
            EngagementLevel.LEVEL_0_NORMAL
        }
    }

    /**
     * Checks if user has been inactive for 48+ hours (Multi-Day Inactivity -> Ghosting).
     */
    fun checkInactivityLevel(lastInteractionTimestamp: Long, now: Long = System.currentTimeMillis()): EngagementLevel? {
        val elapsed = now - lastInteractionTimestamp
        return if (elapsed >= INACTIVITY_GHOSTING_MILLIS) {
            EngagementLevel.LEVEL_3_GHOSTING
        } else {
            null
        }
    }

    /**
     * Checks OS Do Not Disturb status.
     */
    fun isDndActive(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            val filter = notificationManager?.currentInterruptionFilter ?: NotificationManager.INTERRUPTION_FILTER_ALL
            return filter != NotificationManager.INTERRUPTION_FILTER_ALL && filter != NotificationManager.INTERRUPTION_FILTER_UNKNOWN
        }
        return false
    }

    /**
     * Enforces Zero-Overlaps guardrail (Strict 4-hour window between any notifications).
     */
    fun canPostNotificationWithoutOverlap(
        lastPostedTimestamp: Long,
        now: Long = System.currentTimeMillis(),
    ): Boolean {
        if (lastPostedTimestamp <= 0L) return true
        return (now - lastPostedTimestamp) >= MIN_NOTIFICATION_INTERVAL_MILLIS
    }

    /**
     * Evaluates whether a notification should be blocked by current state, DND, quiet hours, or Dormant Mode.
     */
    fun shouldSuppressNotification(
        context: Context,
        settings: UserSettings,
        todayDate: String,
        now: Long = System.currentTimeMillis(),
    ): Boolean {
        // 1. Ghosting state (Level 3) -> Passive observer mode (Cease all proactive notifications)
        if (settings.engagementLevel == EngagementLevel.LEVEL_3_GHOSTING) return true

        // 2. Dormant state (Level 2) active for today
        if (settings.engagementLevel == EngagementLevel.LEVEL_2_STRESSED || settings.dormantUntilDate == todayDate) return true

        // 3. User paused notifications today
        if (settings.pausedTodayDate == todayDate) return true

        // 4. Inactivity check (48h without interaction)
        if (checkInactivityLevel(settings.lastInteractionTimestamp, now) == EngagementLevel.LEVEL_3_GHOSTING) return true

        // 5. Zero-overlap check
        if (!canPostNotificationWithoutOverlap(settings.lastNotificationPostedTimestamp, now)) return true

        // 6. DND Active Check
        if (isDndActive(context)) return true

        return false
    }
}
