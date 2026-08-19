package com.example.whatstheplan.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.whatstheplan.WhatsThePlanApplication
import com.example.whatstheplan.domain.model.EngagementLevel
import com.example.whatstheplan.utils.DateUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalTime

class FirstMorningUnlockReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_USER_PRESENT && intent.action != Intent.ACTION_BOOT_COMPLETED) {
            return
        }

        val app = context.applicationContext as? WhatsThePlanApplication ?: return
        val container = app.container

        CoroutineScope(Dispatchers.IO).launch {
            val settings = container.settingsRepository.settingsFlow.first()
            if (!settings.setupComplete || !settings.morningReminderEnabled) return@launch

            val today = DateUtils.todayString()
            val nowTime = LocalTime.now()

            // 1. Check Wake Window (Between 7:00 AM and 10:00 AM, or active start)
            val wakeHour = settings.wakeTimeMinutes / 60
            val isInWakeWindow = nowTime.hour in 7..10 || (nowTime.hour in wakeHour..(wakeHour + 3).coerceAtMost(12))
            if (!isInWakeWindow) return@launch

            // 2. Track Unlock Count for Today
            val unlockCount = container.settingsRepository.recordUnlock(today)

            // 3. 2nd Unlock Rule: Wait for 2nd unlock before sending prompt
            if (unlockCount < 2) {
                // First unlock (e.g. turning off alarm) — wait for 2nd unlock
                return@launch
            }

            // 4. Check if intention prompt was already sent or intention is already set
            if (settings.lastMorningReminderDate == today) return@launch

            val todayPlan = container.dailyPlanRepository.observeToday().first()
            if (todayPlan != null) return@launch

            // 5. Evaluate State Machine & Suppression (Passive Observer / Dormant / Zero-Overlap / DND)
            if (SignalEvaluator.shouldSuppressNotification(context, settings, today)) {
                return@launch
            }

            // 6. Send Morning Intention Prompt on 2nd unlock!
            container.settingsRepository.setLastMorningReminderDate(today)
            container.settingsRepository.recordNotificationPosted()

            NotificationHelper.showMorningReminderNotification(
                context = context,
                soundEnabled = settings.notificationSound,
                tone = settings.tonePreference,
                userName = settings.userName,
            )

            container.userCorrectionRepository.addCorrection(
                category = "RHYTHM",
                note = "Morning prompt triggered on 2nd unlock at ${DateUtils.formatClock(nowTime.hour * 60 + nowTime.minute)}",
                source = "LEARNED",
            )
        }
    }
}
