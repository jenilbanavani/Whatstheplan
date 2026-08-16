package com.example.whatstheplan.utils

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

object DateUtils {
    private val storageFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val friendlyFormatter: DateTimeFormatter =
        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault())

    fun todayString(): String = LocalDate.now().format(storageFormatter)

    fun friendlyDate(date: String): String =
        runCatching { LocalDate.parse(date, storageFormatter).format(friendlyFormatter) }
            .getOrElse { date }

    fun startOfTodayMillis(): Long =
        LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

    fun formatClock(totalMinutes: Int): String {
        val normalized = ((totalMinutes % (24 * 60)) + (24 * 60)) % (24 * 60)
        val hours = normalized / 60
        val minutes = normalized % 60
        return "%02d:%02d".format(hours, minutes)
    }

    fun formatDuration(millis: Long): String {
        val totalMinutes = (millis / 60_000L).coerceAtLeast(0)
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return when {
            hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
            hours > 0 -> "${hours}h"
            else -> "${minutes}m"
        }
    }

    fun dateFromMillis(millis: Long): String =
        Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(storageFormatter)

    fun formatTime(millis: Long): String =
        Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .toLocalTime()
            .let { "%02d:%02d".format(it.hour, it.minute) }
}
