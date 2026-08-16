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
import com.example.whatstheplan.domain.model.ActivityType

object NotificationHelper {
    const val CHECK_IN_CHANNEL_ID = "hourly_check_ins"
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
            "Hourly check-ins",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Gentle reminders to decide what you are using your phone for."
        }

        val morningChannel = NotificationChannel(
            MORNING_CHANNEL_ID,
            "Morning planning",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Start your day with a clear plan."
        }

        val eveningChannel = NotificationChannel(
            EVENING_CHANNEL_ID,
            "Evening reflection",
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

    fun showCheckInNotification(context: Context, soundEnabled: Boolean) {
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

        val builder = NotificationCompat.Builder(context, CHECK_IN_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Hey 👋")
            .setContentText("What are you doing right now?")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Before you keep going, take five seconds: what are you doing right now?"),
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setSilent(!soundEnabled)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        // Quick 1-tap notification actions
        listOf(
            ActivityType.STUDYING to "📚 Study",
            ActivityType.WORKING to "💻 Work",
            ActivityType.BREAKING to "😴 Break",
        ).forEachIndexed { index, (activity, label) ->
            val actionIntent = Intent(context, CheckInActionReceiver::class.java).apply {
                action = CheckInActionReceiver.ACTION_QUICK_CHECK_IN
                putExtra(CheckInActionReceiver.EXTRA_ACTIVITY, activity.name)
            }
            val actionPendingIntent = PendingIntent.getBroadcast(
                context,
                CHECK_IN_NOTIFICATION_ID + 10 + index,
                actionIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            builder.addAction(0, label, actionPendingIntent)
        }

        NotificationManagerCompat.from(context).notify(CHECK_IN_NOTIFICATION_ID, builder.build())
    }

    fun showMorningReminderNotification(context: Context, soundEnabled: Boolean) {
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

        val notification = NotificationCompat.Builder(context, MORNING_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Good morning ☀️")
            .setContentText("What's the plan today?")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Take a moment before the day starts: what do you actually want to do today?"),
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setSilent(!soundEnabled)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(context).notify(MORNING_NOTIFICATION_ID, notification)
    }

    fun showEveningReflectionNotification(context: Context, soundEnabled: Boolean) {
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

        val notification = NotificationCompat.Builder(context, EVENING_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Evening reflection 🌙")
            .setContentText("How did today go?")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Close the loop on today. Take a quick moment to reflect."),
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setSilent(!soundEnabled)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(context).notify(EVENING_NOTIFICATION_ID, notification)
    }
}
