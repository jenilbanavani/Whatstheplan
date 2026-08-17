package com.example.whatstheplan.domain.model

data class UserSettings(
    val setupComplete: Boolean = false,
    val userName: String = "",
    val wakeTimeMinutes: Int = 7 * 60 + 30, // 7:30 AM
    val dailyCommitment: String = "", // e.g. "Work 9am - 5pm", "Classes", "Caregiving"
    val tonePreference: TonePreference = TonePreference.CALM,
    val morningReminderEnabled: Boolean = true,
    val checkInsEnabled: Boolean = true,
    val checkInIntervalMinutes: Int = 60,
    val activeStartMinutes: Int = 9 * 60,
    val activeEndMinutes: Int = 22 * 60,
    val quietHoursStartMinutes: Int = 22 * 60,
    val quietHoursEndMinutes: Int = 7 * 60 + 30,
    val pausedTodayDate: String = "",
    val funFactsEnabled: Boolean = true,
    val screenTimeInsightsEnabled: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val notificationSound: Boolean = true,
    val recentFactIds: List<Int> = emptyList(),
    val focusModeUntilMillis: Long = 0L,
    val lastMorningReminderDate: String = "",
    val exactAlarmsEnabled: Boolean = false,
)
