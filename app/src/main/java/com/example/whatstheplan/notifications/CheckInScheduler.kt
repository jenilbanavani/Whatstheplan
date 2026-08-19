package com.example.whatstheplan.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.whatstheplan.domain.model.UserSettings
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

object CheckInScheduler {
    private const val WORK_NAME_FOLLOW_UP = "daily_follow_up_work"
    private const val WORK_NAME_MORNING = "morning_planning_work"
    private const val WORK_NAME_EVENING = "evening_reflection_work"
    private const val WORK_NAME_NIGHT_RESET = "night_reset_work"

    fun schedule(context: Context, settings: UserSettings) {
        val workManager = WorkManager.getInstance(context.applicationContext)

        // 1. Single Daily Follow-Up Work (Midday / Afternoon context check)
        if (!settings.followUpEnabled) {
            workManager.cancelUniqueWork(WORK_NAME_FOLLOW_UP)
        } else {
            val middayMinute = if (settings.activeEndMinutes > settings.activeStartMinutes) {
                (settings.activeStartMinutes + settings.activeEndMinutes) / 2
            } else {
                14 * 60 // 2:00 PM
            }

            val followUpRequest = PeriodicWorkRequestBuilder<CheckInWorker>(
                24,
                TimeUnit.HOURS,
            )
                .setInitialDelay(dailyDelayMillis(middayMinute), TimeUnit.MILLISECONDS)
                .build()

            workManager.enqueueUniquePeriodicWork(
                WORK_NAME_FOLLOW_UP,
                ExistingPeriodicWorkPolicy.UPDATE,
                followUpRequest,
            )
        }

        // 2. Morning Intention Work (Wake time / Morning start)
        if (!settings.morningReminderEnabled) {
            workManager.cancelUniqueWork(WORK_NAME_MORNING)
        } else {
            val morningMinute = settings.wakeTimeMinutes.coerceAtLeast(0)
            val morningRequest = PeriodicWorkRequestBuilder<MorningReminderWorker>(
                24,
                TimeUnit.HOURS,
            )
                .setInitialDelay(dailyDelayMillis(morningMinute), TimeUnit.MILLISECONDS)
                .build()

            workManager.enqueueUniquePeriodicWork(
                WORK_NAME_MORNING,
                ExistingPeriodicWorkPolicy.UPDATE,
                morningRequest,
            )
        }

        // 3. Evening Recovery Work (Day close)
        if (!settings.eveningReflectionEnabled) {
            workManager.cancelUniqueWork(WORK_NAME_EVENING)
        } else {
            val eveningMinute = settings.activeEndMinutes.coerceAtLeast(18 * 60)
            val eveningRequest = PeriodicWorkRequestBuilder<EveningReflectionWorker>(
                24,
                TimeUnit.HOURS,
            )
                .setInitialDelay(dailyDelayMillis(eveningMinute), TimeUnit.MILLISECONDS)
                .build()

            workManager.enqueueUniquePeriodicWork(
                WORK_NAME_EVENING,
                ExistingPeriodicWorkPolicy.UPDATE,
                eveningRequest,
            )
        }

        // 4. Night Reset Work (Midnight reset phase / auto-archive loops)
        val nightResetMinute = settings.sleepTimeMinutes.coerceIn(0, 23 * 60 + 59)
        val nightResetRequest = PeriodicWorkRequestBuilder<NightResetWorker>(
            24,
            TimeUnit.HOURS,
        )
            .setInitialDelay(dailyDelayMillis(nightResetMinute), TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            WORK_NAME_NIGHT_RESET,
            ExistingPeriodicWorkPolicy.UPDATE,
            nightResetRequest,
        )
    }

    fun cancel(context: Context) {
        val workManager = WorkManager.getInstance(context.applicationContext)
        workManager.cancelUniqueWork(WORK_NAME_FOLLOW_UP)
        workManager.cancelUniqueWork(WORK_NAME_MORNING)
        workManager.cancelUniqueWork(WORK_NAME_EVENING)
        workManager.cancelUniqueWork(WORK_NAME_NIGHT_RESET)
    }

    fun dailyDelayMillis(targetMinuteOfDay: Int): Long {
        val now = LocalDateTime.now()
        val targetTime = LocalTime.of(
            (targetMinuteOfDay / 60).coerceIn(0, 23),
            (targetMinuteOfDay % 60).coerceIn(0, 59),
        )
        val targetDateTime = if (now.toLocalTime().isBefore(targetTime)) {
            LocalDateTime.of(LocalDate.now(), targetTime)
        } else {
            LocalDateTime.of(LocalDate.now().plusDays(1), targetTime)
        }
        return Duration.between(now, targetDateTime).toMillis().coerceAtLeast(5_000L)
    }
}
