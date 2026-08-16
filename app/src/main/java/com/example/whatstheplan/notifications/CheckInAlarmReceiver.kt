package com.example.whatstheplan.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.whatstheplan.data.local.repository.SettingsRepository
import com.example.whatstheplan.domain.model.UserSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalTime

class CheckInAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val settingsRepository = SettingsRepository(context.applicationContext)

        CoroutineScope(Dispatchers.IO).launch {
            val settings = settingsRepository.settingsFlow.first()
            if (!settings.checkInsEnabled) return@launch

            val now = LocalTime.now()
            val currentMinutes = now.hour * 60 + now.minute
            val start = settings.activeStartMinutes
            val end = settings.activeEndMinutes
            val inWindow = if (start <= end) currentMinutes in start..end else currentMinutes >= start || currentMinutes <= end

            val inFocus = System.currentTimeMillis() < settings.focusModeUntilMillis

            if (inWindow && !inFocus) {
                NotificationHelper.showCheckInNotification(
                    context = context,
                    soundEnabled = settings.notificationSound,
                )
            }

            // Reschedule next exact alarm if needed
            AlarmScheduler.scheduleNextExactCheckIn(context, settings)
        }
    }
}
