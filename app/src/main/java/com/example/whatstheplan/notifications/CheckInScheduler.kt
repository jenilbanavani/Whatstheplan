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
    private const val WORK_NAME_CHECK_IN = "hourly_check_in_work"
    private const val WORK_NAME_MORNING = "morning_planning_work"
    private const val WORK_NAME_EVENING = "evening_reflection_work"

    fun schedule(context: Context, settings: UserSettings) {
        val workManager = WorkManager.getInstance(context.applicationContext)

        // 1. Hourly check-ins
        if (!settings.checkInsEnabled) {
            workManager.cancelUniqueWork(WORK_NAME_CHECK_IN)
        } else {
            val intervalMinutes = settings.checkInIntervalMinutes.coerceAtLeast(15).toLong()
            val request = PeriodicWorkRequestBuilder<CheckInWorker>(
                intervalMinutes,
                TimeUnit.MINUTES,
            )
                .setInitialDelay(initialDelayMillis(settings), TimeUnit.MILLISECONDS)
                .build()

            workManager.enqueueUniquePeriodicWork(
                WORK_NAME_CHECK_IN,
                ExistingPeriodicWorkPolicy.UPDATE,
                request,
            )
        }

        // 2. Morning reminder
        if (!settings.morningReminderEnabled) {
            workManager.cancelUniqueWork(WORK_NAME_MORNING)
        } else {
            val morningRequest = PeriodicWorkRequestBuilder<MorningReminderWorker>(
                24,
                TimeUnit.HOURS,
            )
                .setInitialDelay(dailyDelayMillis(settings.activeStartMinutes), TimeUnit.MILLISECONDS)
                .build()

            workManager.enqueueUniquePeriodicWork(
                WORK_NAME_MORNING,
                ExistingPeriodicWorkPolicy.UPDATE,
                morningRequest,
            )
        }

        // 3. Evening reflection reminder
        val eveningRequest = PeriodicWorkRequestBuilder<EveningReflectionWorker>(
            24,
            TimeUnit.HOURS,
        )
            .setInitialDelay(dailyDelayMillis(settings.activeEndMinutes), TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            WORK_NAME_EVENING,
            ExistingPeriodicWorkPolicy.UPDATE,
            eveningRequest,
        )
    }

    fun cancel(context: Context) {
        val workManager = WorkManager.getInstance(context.applicationContext)
        workManager.cancelUniqueWork(WORK_NAME_CHECK_IN)
        workManager.cancelUniqueWork(WORK_NAME_MORNING)
        workManager.cancelUniqueWork(WORK_NAME_EVENING)
    }

    private fun dailyDelayMillis(targetMinuteOfDay: Int): Long {
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
        return Duration.between(now, targetDateTime).toMillis().coerceAtLeast(10_000L)
    }

    private fun initialDelayMillis(settings: UserSettings): Long {
        val now = LocalDateTime.now()
        val start = LocalTime.of(
            (settings.activeStartMinutes / 60).coerceIn(0, 23),
            (settings.activeStartMinutes % 60).coerceIn(0, 59),
        )
        val end = LocalTime.of(
            (settings.activeEndMinutes / 60).coerceIn(0, 23),
            (settings.activeEndMinutes % 60).coerceIn(0, 59),
        )
        val current = now.toLocalTime()
        val inWindow = if (start <= end) current in start..end else current >= start || current <= end

        val nextRun = when {
            inWindow -> now.plusMinutes(settings.checkInIntervalMinutes.toLong())
            start <= end && current < start -> LocalDateTime.of(LocalDate.now(), start)
            else -> LocalDateTime.of(LocalDate.now().plusDays(1), start)
        }
        return Duration.between(now, nextRun).toMillis().coerceAtLeast(10_000L)
    }
}
