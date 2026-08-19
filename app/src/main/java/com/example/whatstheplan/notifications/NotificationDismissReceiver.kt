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

class NotificationDismissReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as? WhatsThePlanApplication ?: return
        val container = app.container
        val postedAt = intent.getLongExtra(EXTRA_POSTED_TIME, 0L)
        val now = System.currentTimeMillis()
        val latencyMs = if (postedAt > 0L) (now - postedAt).coerceAtLeast(0L) else 5000L

        CoroutineScope(Dispatchers.IO).launch {
            val settings = container.settingsRepository.settingsFlow.first()
            val todayDate = DateUtils.todayString()

            val consecutiveDismissals = container.settingsRepository.recordDismissal()
            val newLevel = SignalEvaluator.evaluateDismissal(latencyMs, consecutiveDismissals)

            container.settingsRepository.setEngagementLevel(newLevel)

            when (newLevel) {
                EngagementLevel.LEVEL_2_STRESSED -> {
                    // Activate Dormant Mode for rest of day
                    container.settingsRepository.setDormantUntilDate(todayDate)
                    container.userCorrectionRepository.addCorrection(
                        category = "SIGNAL",
                        note = "Level 2 (Stressed): Dormant mode activated after rapid dismissal ($latencyMs ms) / $consecutiveDismissals swipes",
                        source = "LEARNED",
                    )
                }
                EngagementLevel.LEVEL_1_DISTRACTED -> {
                    // Delay next ping +3.5 hours
                    val delayUntil = now + SignalEvaluator.DELAY_DISTRACTED_MILLIS
                    container.settingsRepository.setDelayMiddayUntil(delayUntil)
                    container.userCorrectionRepository.addCorrection(
                        category = "SIGNAL",
                        note = "Level 1 (Distracted): Delayed next ping +3.5h after notification swipe ($latencyMs ms latency)",
                        source = "LEARNED",
                    )
                }
                else -> Unit
            }
        }
    }

    companion object {
        const val ACTION_NOTIFICATION_DISMISSED = "com.example.whatstheplan.ACTION_NOTIFICATION_DISMISSED"
        const val EXTRA_POSTED_TIME = "extra_posted_time"
        const val EXTRA_NOTIFICATION_TYPE = "extra_notification_type"
    }
}
