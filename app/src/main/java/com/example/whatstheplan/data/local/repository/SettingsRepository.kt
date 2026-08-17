package com.example.whatstheplan.data.local.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.whatstheplan.domain.model.ThemeMode
import com.example.whatstheplan.domain.model.TonePreference
import com.example.whatstheplan.domain.model.UserSettings
import com.example.whatstheplan.notifications.AlarmScheduler
import com.example.whatstheplan.notifications.CheckInScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.userSettingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_settings",
)

class SettingsRepository(private val context: Context) {
    private val dataStore = context.applicationContext.userSettingsDataStore

    val settingsFlow: Flow<UserSettings> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map(::toSettings)

    suspend fun setUserName(name: String) {
        dataStore.edit { it[Keys.USER_NAME] = name.trim() }
    }

    suspend fun setWakeTimeMinutes(minutes: Int) {
        dataStore.edit { it[Keys.WAKE_TIME_MINUTES] = normalizeMinute(minutes) }
        reschedule()
    }

    suspend fun setDailyCommitment(commitment: String) {
        dataStore.edit { it[Keys.DAILY_COMMITMENT] = commitment.trim() }
    }

    suspend fun setTonePreference(tone: TonePreference) {
        dataStore.edit { it[Keys.TONE_PREFERENCE] = tone.name }
    }

    suspend fun setPausedTodayDate(date: String) {
        dataStore.edit { it[Keys.PAUSED_TODAY_DATE] = date }
    }

    suspend fun setQuietHours(startMinutes: Int, endMinutes: Int) {
        dataStore.edit {
            it[Keys.QUIET_HOURS_START_MINUTES] = normalizeMinute(startMinutes)
            it[Keys.QUIET_HOURS_END_MINUTES] = normalizeMinute(endMinutes)
        }
        reschedule()
    }

    suspend fun setFocusModeUntil(untilMillis: Long) {
        dataStore.edit { it[Keys.FOCUS_MODE_UNTIL_MILLIS] = untilMillis }
    }

    suspend fun clearFocusMode() {
        dataStore.edit { it[Keys.FOCUS_MODE_UNTIL_MILLIS] = 0L }
    }

    suspend fun setSetupComplete(complete: Boolean) {
        dataStore.edit { it[Keys.SETUP_COMPLETE] = complete }
        reschedule()
    }

    suspend fun setMorningReminderEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.MORNING_REMINDER_ENABLED] = enabled }
        reschedule()
    }

    suspend fun setCheckInsEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.CHECK_INS_ENABLED] = enabled }
        reschedule()
    }

    suspend fun setCheckInIntervalMinutes(minutes: Int) {
        dataStore.edit { it[Keys.CHECK_IN_INTERVAL_MINUTES] = minutes.coerceIn(15, 180) }
        reschedule()
    }

    suspend fun setActiveStartMinutes(minutes: Int) {
        dataStore.edit { it[Keys.ACTIVE_START_MINUTES] = normalizeMinute(minutes) }
        reschedule()
    }

    suspend fun setActiveEndMinutes(minutes: Int) {
        dataStore.edit { it[Keys.ACTIVE_END_MINUTES] = normalizeMinute(minutes) }
        reschedule()
    }

    suspend fun setFunFactsEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.FUN_FACTS_ENABLED] = enabled }
    }

    suspend fun setScreenTimeInsightsEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.SCREEN_TIME_INSIGHTS_ENABLED] = enabled }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setNotificationSound(enabled: Boolean) {
        dataStore.edit { it[Keys.NOTIFICATION_SOUND] = enabled }
    }

    suspend fun setLastMorningReminderDate(date: String) {
        dataStore.edit { it[Keys.LAST_MORNING_REMINDER_DATE] = date }
    }

    suspend fun setExactAlarmsEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.EXACT_ALARMS_ENABLED] = enabled }
        reschedule()
    }

    suspend fun recentFactIds(): List<Int> = settingsFlow.first().recentFactIds

    suspend fun markFactShown(factId: Int) {
        dataStore.edit { preferences ->
            val existing = preferences[Keys.RECENT_FACT_IDS]
                .orEmpty()
                .split(",")
                .mapNotNull { it.toIntOrNull() }
            preferences[Keys.RECENT_FACT_IDS] = (existing + factId)
                .distinct()
                .takeLast(25)
                .joinToString(",")
        }
    }

    suspend fun resetToDefaults() {
        dataStore.edit { it.clear() }
        CheckInScheduler.cancel(context)
        AlarmScheduler.cancelExactCheckIn(context)
    }

    private suspend fun reschedule() {
        val current = settingsFlow.first()
        CheckInScheduler.schedule(context, current)
        if (current.exactAlarmsEnabled) {
            AlarmScheduler.scheduleNextExactCheckIn(context, current)
        } else {
            AlarmScheduler.cancelExactCheckIn(context)
        }
    }

    private fun toSettings(preferences: Preferences): UserSettings =
        UserSettings(
            setupComplete = preferences[Keys.SETUP_COMPLETE] ?: false,
            userName = preferences[Keys.USER_NAME] ?: "",
            wakeTimeMinutes = preferences[Keys.WAKE_TIME_MINUTES] ?: (7 * 60 + 30),
            dailyCommitment = preferences[Keys.DAILY_COMMITMENT] ?: "",
            tonePreference = TonePreference.entries.firstOrNull {
                it.name == preferences[Keys.TONE_PREFERENCE]
            } ?: TonePreference.CALM,
            morningReminderEnabled = preferences[Keys.MORNING_REMINDER_ENABLED] ?: true,
            checkInsEnabled = preferences[Keys.CHECK_INS_ENABLED] ?: true,
            checkInIntervalMinutes = preferences[Keys.CHECK_IN_INTERVAL_MINUTES] ?: 60,
            activeStartMinutes = preferences[Keys.ACTIVE_START_MINUTES] ?: 9 * 60,
            activeEndMinutes = preferences[Keys.ACTIVE_END_MINUTES] ?: 22 * 60,
            quietHoursStartMinutes = preferences[Keys.QUIET_HOURS_START_MINUTES] ?: 22 * 60,
            quietHoursEndMinutes = preferences[Keys.QUIET_HOURS_END_MINUTES] ?: (7 * 60 + 30),
            pausedTodayDate = preferences[Keys.PAUSED_TODAY_DATE] ?: "",
            funFactsEnabled = preferences[Keys.FUN_FACTS_ENABLED] ?: true,
            screenTimeInsightsEnabled = preferences[Keys.SCREEN_TIME_INSIGHTS_ENABLED] ?: false,
            themeMode = ThemeMode.entries.firstOrNull {
                it.name == preferences[Keys.THEME_MODE]
            } ?: ThemeMode.SYSTEM,
            notificationSound = preferences[Keys.NOTIFICATION_SOUND] ?: true,
            recentFactIds = preferences[Keys.RECENT_FACT_IDS]
                .orEmpty()
                .split(",")
                .mapNotNull { it.toIntOrNull() },
            focusModeUntilMillis = preferences[Keys.FOCUS_MODE_UNTIL_MILLIS] ?: 0L,
            lastMorningReminderDate = preferences[Keys.LAST_MORNING_REMINDER_DATE] ?: "",
            exactAlarmsEnabled = preferences[Keys.EXACT_ALARMS_ENABLED] ?: false,
        )

    private fun normalizeMinute(minutes: Int): Int =
        ((minutes % (24 * 60)) + (24 * 60)) % (24 * 60)

    private object Keys {
        val SETUP_COMPLETE = booleanPreferencesKey("setup_complete")
        val USER_NAME = stringPreferencesKey("user_name")
        val WAKE_TIME_MINUTES = intPreferencesKey("wake_time_minutes")
        val DAILY_COMMITMENT = stringPreferencesKey("daily_commitment")
        val TONE_PREFERENCE = stringPreferencesKey("tone_preference")
        val MORNING_REMINDER_ENABLED = booleanPreferencesKey("morning_reminder_enabled")
        val CHECK_INS_ENABLED = booleanPreferencesKey("check_ins_enabled")
        val CHECK_IN_INTERVAL_MINUTES = intPreferencesKey("check_in_interval_minutes")
        val ACTIVE_START_MINUTES = intPreferencesKey("active_start_minutes")
        val ACTIVE_END_MINUTES = intPreferencesKey("active_end_minutes")
        val QUIET_HOURS_START_MINUTES = intPreferencesKey("quiet_hours_start_minutes")
        val QUIET_HOURS_END_MINUTES = intPreferencesKey("quiet_hours_end_minutes")
        val PAUSED_TODAY_DATE = stringPreferencesKey("paused_today_date")
        val FUN_FACTS_ENABLED = booleanPreferencesKey("fun_facts_enabled")
        val SCREEN_TIME_INSIGHTS_ENABLED = booleanPreferencesKey("screen_time_insights_enabled")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val NOTIFICATION_SOUND = booleanPreferencesKey("notification_sound")
        val RECENT_FACT_IDS = stringPreferencesKey("recent_fact_ids")
        val FOCUS_MODE_UNTIL_MILLIS = longPreferencesKey("focus_mode_until_millis")
        val LAST_MORNING_REMINDER_DATE = stringPreferencesKey("last_morning_reminder_date")
        val EXACT_ALARMS_ENABLED = booleanPreferencesKey("exact_alarms_enabled")
    }
}
