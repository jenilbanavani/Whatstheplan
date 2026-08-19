package com.example.whatstheplan.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import androidx.core.content.ContextCompat
import com.example.whatstheplan.MainActivity
import com.example.whatstheplan.R
import com.example.whatstheplan.domain.model.TonePreference

object NotificationHelper {

    const val CHECK_IN_CHANNEL_ID = "check_in_channel_v2"
    const val MORNING_CHANNEL_ID = "morning_channel_v2"
    const val EVENING_CHANNEL_ID = "evening_channel_v2"

    const val CHECK_IN_NOTIFICATION_ID = 1001
    const val MORNING_NOTIFICATION_ID = 1002
    const val EVENING_NOTIFICATION_ID = 1003
    const val ACKNOWLEDGEMENT_NOTIFICATION_ID = 1004

    const val KEY_TEXT_REPLY = "key_text_reply"

    fun createChannels(context: Context) = createNotificationChannels(context)

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        val checkInChannel = NotificationChannel(
            CHECK_IN_CHANNEL_ID,
            "Daily Follow-Up",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Context-aware follow-ups with actions: Start 10 min, Move it, Not today"
            enableVibration(true)
        }

        val morningChannel = NotificationChannel(
            MORNING_CHANNEL_ID,
            "Morning Planning",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Gentle morning reminder to set your daily intention"
            enableVibration(true)
        }

        val eveningChannel = NotificationChannel(
            EVENING_CHANNEL_ID,
            "Evening Reflection",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Evening prompt to close out your day with neutral recovery"
            enableVibration(true)
        }

        manager.createNotificationChannel(checkInChannel)
        manager.createNotificationChannel(morningChannel)
        manager.createNotificationChannel(eveningChannel)
    }

    fun canPostNotifications(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    fun showCheckInNotification(
        context: Context,
        soundEnabled: Boolean,
        tone: TonePreference = TonePreference.CALM,
    ) {
        showFollowUpNotification(context, soundEnabled, tone)
    }

    fun showFollowUpNotification(
        context: Context,
        soundEnabled: Boolean,
        tone: TonePreference = TonePreference.CALM,
        intention: String? = null,
        firstStep: String? = null,
    ) {
        if (!canPostNotifications(context)) return

        val now = System.currentTimeMillis()
        val title = tone.followUpTitle()
        val body = tone.followUpBody(intention, firstStep)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_DESTINATION, MainActivity.DESTINATION_CHECK_IN)
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            CHECK_IN_NOTIFICATION_ID,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        // Dismissal / Swipe Intent
        val dismissIntent = Intent(context, NotificationDismissReceiver::class.java).apply {
            action = NotificationDismissReceiver.ACTION_NOTIFICATION_DISMISSED
            putExtra(NotificationDismissReceiver.EXTRA_POSTED_TIME, now)
            putExtra(NotificationDismissReceiver.EXTRA_NOTIFICATION_TYPE, "FOLLOW_UP")
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            3001,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        // Action 1: Start 10 min
        val start10Intent = Intent(context, CheckInActionReceiver::class.java).apply {
            action = CheckInActionReceiver.ACTION_START_10_MIN
        }
        val start10PendingIntent = PendingIntent.getBroadcast(
            context,
            2001,
            start10Intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        // Action 2: Move it
        val moveItIntent = Intent(context, CheckInActionReceiver::class.java).apply {
            action = CheckInActionReceiver.ACTION_MOVE_IT
        }
        val moveItPendingIntent = PendingIntent.getBroadcast(
            context,
            2002,
            moveItIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        // Action 3: Not today
        val notTodayIntent = Intent(context, CheckInActionReceiver::class.java).apply {
            action = CheckInActionReceiver.ACTION_NOT_TODAY
        }
        val notTodayPendingIntent = PendingIntent.getBroadcast(
            context,
            2003,
            notTodayIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        // Inline Reply Action
        val remoteInput = RemoteInput.Builder(KEY_TEXT_REPLY)
            .setLabel("Quick reply (e.g. busy, later)")
            .build()

        val replyIntent = Intent(context, CheckInActionReceiver::class.java).apply {
            action = CheckInActionReceiver.ACTION_TEXT_REPLY
        }
        val replyPendingIntent = PendingIntent.getBroadcast(
            context,
            2004,
            replyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
        )

        val replyAction = NotificationCompat.Action.Builder(
            0,
            "💬 Reply",
            replyPendingIntent,
        ).addRemoteInput(remoteInput).build()

        val builder = NotificationCompat.Builder(context, CHECK_IN_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(openAppPendingIntent)
            .setDeleteIntent(dismissPendingIntent)
            .setAutoCancel(true)
            .setSilent(!soundEnabled)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .addAction(0, "⏱️ Start 10m", start10PendingIntent)
            .addAction(0, "➡️ Move", moveItPendingIntent)
            .addAction(0, "🛑 Not today", notTodayPendingIntent)
            .addAction(replyAction)

        NotificationManagerCompat.from(context).notify(CHECK_IN_NOTIFICATION_ID, builder.build())
    }

    fun showMorningReminderNotification(
        context: Context,
        soundEnabled: Boolean,
        tone: TonePreference = TonePreference.CALM,
        userName: String? = null,
    ) {
        if (!canPostNotifications(context)) return

        val now = System.currentTimeMillis()
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

        val dismissIntent = Intent(context, NotificationDismissReceiver::class.java).apply {
            action = NotificationDismissReceiver.ACTION_NOTIFICATION_DISMISSED
            putExtra(NotificationDismissReceiver.EXTRA_POSTED_TIME, now)
            putExtra(NotificationDismissReceiver.EXTRA_NOTIFICATION_TYPE, "MORNING")
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            3002,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val greeting = tone.morningGreeting(userName)

        val notification = NotificationCompat.Builder(context, MORNING_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Your one thing today ☀️")
            .setContentText(greeting)
            .setStyle(NotificationCompat.BigTextStyle().bigText(greeting))
            .setContentIntent(pendingIntent)
            .setDeleteIntent(dismissPendingIntent)
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
        intention: String? = null,
    ) {
        if (!canPostNotifications(context)) return

        val now = System.currentTimeMillis()
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

        val dismissIntent = Intent(context, NotificationDismissReceiver::class.java).apply {
            action = NotificationDismissReceiver.ACTION_NOTIFICATION_DISMISSED
            putExtra(NotificationDismissReceiver.EXTRA_POSTED_TIME, now)
            putExtra(NotificationDismissReceiver.EXTRA_NOTIFICATION_TYPE, "EVENING")
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            3003,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val prompt = tone.eveningPrompt(intention)

        val notification = NotificationCompat.Builder(context, EVENING_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Evening recovery 🌙")
            .setContentText(prompt)
            .setStyle(NotificationCompat.BigTextStyle().bigText(prompt))
            .setContentIntent(pendingIntent)
            .setDeleteIntent(dismissPendingIntent)
            .setAutoCancel(true)
            .setSilent(!soundEnabled)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(context).notify(EVENING_NOTIFICATION_ID, notification)
    }

    /**
     * Zero-friction acknowledgement when user enters Level 2 Dormant Mode (e.g. terse reply "busy")
     */
    fun showDormantAcknowledgementNotification(context: Context) {
        if (!canPostNotifications(context)) return

        val notification = NotificationCompat.Builder(context, CHECK_IN_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("What's the Plan?")
            .setContentText("Got it. Talk tomorrow.")
            .setSilent(true)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        NotificationManagerCompat.from(context).notify(ACKNOWLEDGEMENT_NOTIFICATION_ID, notification)
    }
}
