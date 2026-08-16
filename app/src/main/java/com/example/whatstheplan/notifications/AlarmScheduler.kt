package com.example.whatstheplan.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.whatstheplan.domain.model.UserSettings
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

object AlarmScheduler {
    private const val ALARM_REQUEST_CODE = 2001

    fun scheduleNextExactCheckIn(context: Context, settings: UserSettings) {
        if (!settings.checkInsEnabled) {
            cancelExactCheckIn(context)
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, CheckInAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val now = LocalDateTime.now()
        val start = LocalTime.of(
            (settings.activeStartMinutes / 60).coerceIn(0, 23),
            (settings.activeStartMinutes % 60).coerceIn(0, 59),
        )
        val end = LocalTime.of(
            (settings.activeEndMinutes / 60).coerceIn(0, 23),
            (settings.activeEndMinutes % 60).coerceIn(0, 59),
        )
        val current = now.toLocalTime()
        val inWindow = if (start <= end) current in start..end else current >= start || current <= end

        val nextRun = when {
            inWindow -> now.plusMinutes(settings.checkInIntervalMinutes.toLong())
            start <= end && current < start -> LocalDateTime.of(LocalDate.now(), start)
            else -> LocalDateTime.of(LocalDate.now().plusDays(1), start)
        }

        val triggerAtMillis = System.currentTimeMillis() + Duration.between(now, nextRun).toMillis().coerceAtLeast(60_000L)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent,
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent,
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent,
                )
            }
        } else {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent,
            )
        }
    }

    fun cancelExactCheckIn(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, CheckInAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        alarmManager.cancel(pendingIntent)
    }
}
