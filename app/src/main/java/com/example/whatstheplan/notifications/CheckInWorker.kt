package com.example.whatstheplan.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.whatstheplan.WhatsThePlanApplication
import com.example.whatstheplan.data.local.repository.SettingsRepository
import com.example.whatstheplan.domain.model.UserSettings
import com.example.whatstheplan.utils.DateUtils
import kotlinx.coroutines.flow.first
import java.time.LocalTime

class CheckInWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as? WhatsThePlanApplication
        val container = app?.container
        val settings = container?.settingsRepository?.settingsFlow?.first()
            ?: SettingsRepository(applicationContext).settingsFlow.first()

        val today = DateUtils.todayString()
        val now = System.currentTimeMillis()

        // 1. Basic checks
        if (!settings.setupComplete || !settings.followUpEnabled) {
            return Result.success()
        }

        // 2. Evaluator suppression (Ghosting Level 3, Dormant Level 2, Paused today, Zero-overlap, DND)
        if (SignalEvaluator.shouldSuppressNotification(applicationContext, settings, today, now)) {
            return Result.success()
        }

        // 3. Check if delayed by Level 1 (+3.5h delay)
        if (now < settings.delayMiddayUntilMillis) {
            return Result.success()
        }

        // 4. Check focus mode & quiet hours
        if (now < settings.focusModeUntilMillis || isInQuietHours(settings)) {
            return Result.success()
        }

        // 5. Check if today's plan is already DONE or DROPPED
        val todayPlan = container?.dailyPlanRepository?.observeToday()?.first()
        if (todayPlan?.status == "DONE" || todayPlan?.status == "DROPPED") {
            return Result.success()
        }

        // 6. Record notification post & show follow-up
        container?.settingsRepository?.recordNotificationPosted()

        NotificationHelper.showFollowUpNotification(
            context = applicationContext,
            soundEnabled = settings.notificationSound,
            tone = settings.tonePreference,
            intention = todayPlan?.text,
            firstStep = todayPlan?.firstStep,
        )
        return Result.success()
    }

    private fun isInQuietHours(settings: UserSettings): Boolean {
        val now = LocalTime.now()
        val currentMinutes = now.hour * 60 + now.minute
        val start = settings.quietHoursStartMinutes
        val end = settings.quietHoursEndMinutes
        return if (start <= end) {
            currentMinutes in start..end
        } else {
            currentMinutes >= start || currentMinutes <= end
        }
    }
}
