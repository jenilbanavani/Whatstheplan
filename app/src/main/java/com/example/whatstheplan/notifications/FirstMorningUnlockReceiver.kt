package com.example.whatstheplan.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.whatstheplan.WhatsThePlanApplication
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
            // Only fire once per calendar day
            if (settings.lastMorningReminderDate == today) return@launch

            val now = LocalTime.now()
            val startHour = settings.activeStartMinutes / 60
            // Trigger between 5 AM and noon (or activeStartMinutes)
            val isMorning = now.hour in 5..12 || (now.hour >= startHour && now.hour < 13)
            if (!isMorning) return@launch

            val todayPlan = container.dailyPlanRepository.observeToday().first()
            if (todayPlan != null) return@launch

            container.settingsRepository.setLastMorningReminderDate(today)
            NotificationHelper.showMorningReminderNotification(
                context = context,
                soundEnabled = settings.notificationSound,
            )
        }
    }
}
