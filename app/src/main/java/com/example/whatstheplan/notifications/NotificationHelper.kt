package com.example.whatstheplan.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.whatstheplan.MainActivity
import com.example.whatstheplan.R
import com.example.whatstheplan.domain.model.TonePreference

object NotificationHelper {
    const val CHECK_IN_CHANNEL_ID = "daily_followups"
    const val MORNING_CHANNEL_ID = "morning_reminders"
    const val EVENING_CHANNEL_ID = "evening_reflections"

    const val CHECK_IN_NOTIFICATION_ID = 1001
    const val MORNING_NOTIFICATION_ID = 1002
    const val EVENING_NOTIFICATION_ID = 1003

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService(NotificationManager::class.java) ?: return

        val checkInChannel = NotificationChannel(
            CHECK_IN_CHANNEL_ID,
            "Daily follow-up",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "A single calm follow-up on your planned intention."
        }

        val morningChannel = NotificationChannel(
            MORNING_CHANNEL_ID,
            "Morning planning",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Set your daily intention in the morning."
        }

        val eveningChannel = NotificationChannel(
            EVENING_CHANNEL_ID,
            "Evening recovery",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Reflect on how your day went."
        }

        manager.createNotificationChannels(listOf(checkInChannel, morningChannel, eveningChannel))
    }

    fun canPostNotifications(context: Context): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }

    fun showFollowUpNotification(
        context: Context,
        soundEnabled: Boolean,
        tone: TonePreference = TonePreference.CALM,
        intention: String? = null,
        firstStep: String? = null,
    ) {
        if (!canPostNotifications(context)) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_DESTINATION, MainActivity.DESTINATION_CHECK_IN)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            CHECK_IN_NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val title = tone.followUpTitle()
        val body = tone.followUpBody(intention, firstStep)

        val builder = NotificationCompat.Builder(context, CHECK_IN_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setSilent(!soundEnabled)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        // Action 1: Start 10 min
        val startIntent = Intent(context, CheckInActionReceiver::class.java).apply {
            action = CheckInActionReceiver.ACTION_START_10_MIN
        }
        val startPending = PendingIntent.getBroadcast(
            context,
            CHECK_IN_NOTIFICATION_ID + 1,
            startIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        builder.addAction(0, "⏱️ Start 10 min", startPending)

        // Action 2: Move it
        val moveIntent = Intent(context, CheckInActionReceiver::class.java).apply {
            action = CheckInActionReceiver.ACTION_MOVE_IT
        }
        val movePending = PendingIntent.getBroadcast(
            context,
            CHECK_IN_NOTIFICATION_ID + 2,
            moveIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        builder.addAction(0, "➡️ Move it", movePending)

        // Action 3: Not today
        val notTodayIntent = Intent(context, CheckInActionReceiver::class.java).apply {
            action = CheckInActionReceiver.ACTION_NOT_TODAY
        }
        val notTodayPending = PendingIntent.getBroadcast(
            context,
            CHECK_IN_NOTIFICATION_ID + 3,
            notTodayIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        builder.addAction(0, "🛑 Not today", notTodayPending)

        NotificationManagerCompat.from(context).notify(CHECK_IN_NOTIFICATION_ID, builder.build())
    }

    // Backward-compatible overload
    fun showCheckInNotification(context: Context, soundEnabled: Boolean) {
        showFollowUpNotification(context, soundEnabled)
    }

    fun showMorningReminderNotification(
        context: Context,
        soundEnabled: Boolean,
        tone: TonePreference = TonePreference.CALM,
        userName: String? = null,
    ) {
        if (!canPostNotifications(context)) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_DESTINATION, MainActivity.DESTINATION_MORNING)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            MORNING_NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val greeting = tone.morningGreeting(userName)

        val notification = NotificationCompat.Builder(context, MORNING_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Good morning ☀️")
            .setContentText(greeting)
            .setStyle(NotificationCompat.BigTextStyle().bigText(greeting))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setSilent(!soundEnabled)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(context).notify(MORNING_NOTIFICATION_ID, notification)
    }

    fun showEveningReflectionNotification(
        context: Context,
        soundEnabled: Boolean,
        tone: TonePreference = TonePreference.CALM,
    ) {
        if (!canPostNotifications(context)) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_DESTINATION, MainActivity.DESTINATION_REFLECTION)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            EVENING_NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val prompt = tone.eveningPrompt()

        val notification = NotificationCompat.Builder(context, EVENING_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Evening recovery 🌙")
            .setContentText(prompt)
            .setStyle(NotificationCompat.BigTextStyle().bigText(prompt))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setSilent(!soundEnabled)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(context).notify(EVENING_NOTIFICATION_ID, notification)
    }
}
