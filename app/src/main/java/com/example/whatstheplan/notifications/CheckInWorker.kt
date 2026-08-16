package com.example.whatstheplan.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.whatstheplan.data.local.repository.SettingsRepository
import com.example.whatstheplan.domain.model.UserSettings
import kotlinx.coroutines.flow.first
import java.time.LocalTime

class CheckInWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val settings = SettingsRepository(applicationContext).settingsFlow.first()
        if (!settings.checkInsEnabled || !isWithinActiveHours(settings) || System.currentTimeMillis() < settings.focusModeUntilMillis) {
            return Result.success()
        }

        NotificationHelper.showCheckInNotification(
            context = applicationContext,
            soundEnabled = settings.notificationSound,
        )
        return Result.success()
    }

    private fun isWithinActiveHours(settings: UserSettings): Boolean {
        val now = LocalTime.now()
        val currentMinutes = now.hour * 60 + now.minute
        val start = settings.activeStartMinutes
        val end = settings.activeEndMinutes
        return if (start <= end) {
            currentMinutes in start..end
        } else {
            currentMinutes >= start || currentMinutes <= end
        }
    }
}
