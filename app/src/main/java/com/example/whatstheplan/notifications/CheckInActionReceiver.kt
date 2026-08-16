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
        val activityName = intent.getStringExtra(EXTRA_ACTIVITY) ?: return
        val activity = ActivityType.fromCode(activityName)

        val app = context.applicationContext as? WhatsThePlanApplication ?: return
        val container = app.container

        CoroutineScope(Dispatchers.IO).launch {
            container.checkInRepository.saveCheckIn(
                activity = activity,
                note = "",
            )
            WidgetUpdater.updateAllWidgets(context)

            // Dismiss the notification upon action click
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.cancel(NotificationHelper.CHECK_IN_NOTIFICATION_ID)
        }
    }

    companion object {
        const val ACTION_QUICK_CHECK_IN = "com.example.whatstheplan.ACTION_QUICK_CHECK_IN"
        const val EXTRA_ACTIVITY = "extra_activity"
    }
}
