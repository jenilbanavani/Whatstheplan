package com.example.whatstheplan.domain.model

data class UserSettings(
    val setupComplete: Boolean = false,
    val userName: String = "",
    val wakeTimeMinutes: Int = 7 * 60 + 30, // 7:30 AM
    val sleepTimeMinutes: Int = 23 * 60 + 30, // 11:30 PM
    val dailyCommitment: String = "", // e.g. "College: 9:00 AM - 3:00 PM"
    val tonePreference: TonePreference = TonePreference.CALM,
    val morningReminderEnabled: Boolean = true,
    val followUpEnabled: Boolean = true,
    val checkInsEnabled: Boolean = true, // alias for followUpEnabled
    val eveningReflectionEnabled: Boolean = true,
    val checkInIntervalMinutes: Int = 60,
    val activeStartMinutes: Int = 9 * 60,
    val activeEndMinutes: Int = 22 * 60,
    val quietHoursStartMinutes: Int = 22 * 60,
    val quietHoursEndMinutes: Int = 7 * 60 + 30,
    val pausedTodayDate: String = "",
    val notificationFeedback: String = "", // "Useful", "Too early", "Too late", "Not relevant", "Too much"
    val funFactsEnabled: Boolean = true,
    val screenTimeInsightsEnabled: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val notificationSound: Boolean = true,
    val recentFactIds: List<Int> = emptyList(),
    val focusModeUntilMillis: Long = 0L,
    val lastMorningReminderDate: String = "",
    val exactAlarmsEnabled: Boolean = false,
    // Adaptive State Machine Fields (Levels 0 - 3)
    val engagementLevel: EngagementLevel = EngagementLevel.LEVEL_0_NORMAL,
    val unlockCountToday: Int = 0,
    val lastUnlockDate: String = "",
    val lastInteractionTimestamp: Long = System.currentTimeMillis(),
    val lastNotificationPostedTimestamp: Long = 0L,
    val consecutiveDismissalsToday: Int = 0,
    val dormantUntilDate: String = "",
    val delayMiddayUntilMillis: Long = 0L,
)
