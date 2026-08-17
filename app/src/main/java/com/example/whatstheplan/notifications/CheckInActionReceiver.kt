package com.example.whatstheplan.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.example.whatstheplan.WhatsThePlanApplication
import com.example.whatstheplan.domain.model.ActivityType
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
            when (intent.action) {
                ACTION_START_10_MIN -> {
                    container.dailyPlanRepository.startTimer()
                    container.userCorrectionRepository.addCorrection(
                        category = "INTERACTION",
                        note = "Tapped 'Start 10 min' from notification follow-up",
                    )
                }
                ACTION_MOVE_IT -> {
                    container.dailyPlanRepository.updateStatus("MOVED")
                    container.userCorrectionRepository.addCorrection(
                        category = "INTERACTION",
                        note = "Tapped 'Move it' from notification follow-up",
                    )
                }
                ACTION_NOT_TODAY -> {
                    container.dailyPlanRepository.updateStatus("DROPPED")
                    container.userCorrectionRepository.addCorrection(
                        category = "INTERACTION",
                        note = "Tapped 'Not today' from notification follow-up",
                    )
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
        const val ACTION_QUICK_CHECK_IN = "com.example.whatstheplan.ACTION_QUICK_CHECK_IN"
        const val EXTRA_ACTIVITY = "extra_activity"
    }
}
