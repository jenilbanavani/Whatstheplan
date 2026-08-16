package com.example.whatstheplan.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.whatstheplan.WhatsThePlanApplication
import com.example.whatstheplan.data.local.repository.SettingsRepository
import kotlinx.coroutines.flow.first

class MorningReminderWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as? WhatsThePlanApplication
        val container = app?.container
        val settings = container?.settingsRepository?.settingsFlow?.first()
            ?: SettingsRepository(applicationContext).settingsFlow.first()

        if (!settings.setupComplete || !settings.morningReminderEnabled) {
            return Result.success()
        }

        val todayPlan = container?.dailyPlanRepository?.observeToday()?.first()
        if (todayPlan != null) {
            return Result.success()
        }

        NotificationHelper.showMorningReminderNotification(
            context = applicationContext,
            soundEnabled = settings.notificationSound,
        )
        return Result.success()
    }
}
