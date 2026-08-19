package com.example.whatstheplan.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import com.example.whatstheplan.WhatsThePlanApplication
import com.example.whatstheplan.domain.model.ActivityType
import com.example.whatstheplan.domain.model.EngagementLevel
import com.example.whatstheplan.utils.DateUtils
import com.example.whatstheplan.widgets.WidgetUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CheckInActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as? WhatsThePlanApplication ?: return
        val container = app.container

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(NotificationHelper.CHECK_IN_NOTIFICATION_ID)

        CoroutineScope(Dispatchers.IO).launch {
            val todayDate = DateUtils.todayString()
            container.settingsRepository.recordInteraction()

            when (intent.action) {
                ACTION_START_10_MIN -> {
                    container.settingsRepository.setEngagementLevel(EngagementLevel.LEVEL_0_NORMAL)
                    container.dailyPlanRepository.startTimer()
                    container.userCorrectionRepository.addCorrection(
                        category = "INTERACTION",
                        note = "Tapped 'Start 10 min' from notification follow-up",
                        source = "LEARNED",
                    )
                }
                ACTION_MOVE_IT -> {
                    container.settingsRepository.setEngagementLevel(EngagementLevel.LEVEL_0_NORMAL)
                    container.dailyPlanRepository.updateStatus("MOVED")
                    container.userCorrectionRepository.addCorrection(
                        category = "INTERACTION",
                        note = "Tapped 'Move it' from notification follow-up",
                        source = "LEARNED",
                    )
                }
                ACTION_NOT_TODAY -> {
                    container.settingsRepository.setEngagementLevel(EngagementLevel.LEVEL_0_NORMAL)
                    container.dailyPlanRepository.updateStatus("DROPPED")
                    container.userCorrectionRepository.addCorrection(
                        category = "INTERACTION",
                        note = "Tapped 'Not today' from notification follow-up",
                        source = "LEARNED",
                    )
                }
                ACTION_TEXT_REPLY -> {
                    val remoteInputBundle = RemoteInput.getResultsFromIntent(intent)
                    val replyText = remoteInputBundle?.getCharSequence(NotificationHelper.KEY_TEXT_REPLY)?.toString().orEmpty()

                    val level = SignalEvaluator.evaluateTextReply(replyText)
                    container.settingsRepository.setEngagementLevel(level)

                    if (level == EngagementLevel.LEVEL_2_STRESSED) {
                        // Activate Dormant Mode for rest of day
                        container.settingsRepository.setDormantUntilDate(todayDate)
                        NotificationHelper.showDormantAcknowledgementNotification(context)
                        container.userCorrectionRepository.addCorrection(
                            category = "SIGNAL",
                            note = "Level 2 (Stressed): Terse reply \"$replyText\" activated Dormant Mode",
                            source = "LEARNED",
                        )
                    } else {
                        container.dailyPlanRepository.savePlan(text = replyText)
                        container.userCorrectionRepository.addCorrection(
                            category = "INTERACTION",
                            note = "Quick reply intention set: \"$replyText\"",
                            source = "LEARNED",
                        )
                    }
                }
                ACTION_QUICK_CHECK_IN -> {
                    val activityName = intent.getStringExtra(EXTRA_ACTIVITY)
                    if (activityName != null) {
                        val activity = ActivityType.fromCode(activityName)
                        container.checkInRepository.saveCheckIn(activity = activity, note = "")
                    }
                }
            }
            WidgetUpdater.updateAllWidgets(context)
        }
    }

    companion object {
        const val ACTION_START_10_MIN = "com.example.whatstheplan.ACTION_START_10_MIN"
        const val ACTION_MOVE_IT = "com.example.whatstheplan.ACTION_MOVE_IT"
        const val ACTION_NOT_TODAY = "com.example.whatstheplan.ACTION_NOT_TODAY"
        const val ACTION_TEXT_REPLY = "com.example.whatstheplan.ACTION_TEXT_REPLY"
        const val ACTION_QUICK_CHECK_IN = "com.example.whatstheplan.ACTION_QUICK_CHECK_IN"
        const val EXTRA_ACTIVITY = "extra_activity"
    }
}
