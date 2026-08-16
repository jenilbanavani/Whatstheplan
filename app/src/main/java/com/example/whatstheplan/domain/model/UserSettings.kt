package com.example.whatstheplan.domain.model

data class UserSettings(
    val setupComplete: Boolean = false,
    val morningReminderEnabled: Boolean = true,
    val checkInsEnabled: Boolean = true,
    val checkInIntervalMinutes: Int = 60,
    val activeStartMinutes: Int = 9 * 60,
    val activeEndMinutes: Int = 22 * 60,
    val funFactsEnabled: Boolean = true,
    val screenTimeInsightsEnabled: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val notificationSound: Boolean = true,
    val recentFactIds: List<Int> = emptyList(),
    val focusModeUntilMillis: Long = 0L,
    val lastMorningReminderDate: String = "",
    val exactAlarmsEnabled: Boolean = false,
)
