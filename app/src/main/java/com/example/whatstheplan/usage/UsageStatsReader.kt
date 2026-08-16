package com.example.whatstheplan.usage

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Build
import android.provider.Settings
import com.example.whatstheplan.domain.model.AppUsageInfo
import com.example.whatstheplan.domain.model.ScreenTimeSummary
import com.example.whatstheplan.utils.DateUtils

class UsageStatsReader(
    private val context: Context,
) {
    fun hasUsageAccess(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName,
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName,
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun usageAccessIntent(): Intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)

    fun readToday(): ScreenTimeSummary? {
        if (!hasUsageAccess()) return null

        val usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val now = System.currentTimeMillis()
        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            DateUtils.startOfTodayMillis(),
            now,
        ).orEmpty()

        val packageManager = context.packageManager
        val apps = stats
            .filter { it.totalTimeInForeground > 0 }
            .groupBy { it.packageName }
            .mapNotNull { (packageName, packageStats) ->
                val total = packageStats.sumOf { it.totalTimeInForeground }
                val appInfo = runCatching {
                    packageManager.getApplicationInfo(packageName, 0)
                }.getOrNull()
                val label = appInfo?.loadLabel(packageManager)?.toString() ?: packageName
                AppUsageInfo(
                    packageName = packageName,
                    appLabel = label,
                    totalTimeMillis = total,
                    category = appInfo?.categoryLabel(),
                )
            }
            .sortedByDescending { it.totalTimeMillis }

        return ScreenTimeSummary(
            totalTimeMillis = apps.sumOf { it.totalTimeMillis },
            topApps = apps.take(5),
        )
    }

    private fun ApplicationInfo.categoryLabel(): String? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return null
        return when (category) {
            ApplicationInfo.CATEGORY_AUDIO -> "Audio"
            ApplicationInfo.CATEGORY_GAME -> "Game"
            ApplicationInfo.CATEGORY_IMAGE -> "Image"
            ApplicationInfo.CATEGORY_MAPS -> "Maps"
            ApplicationInfo.CATEGORY_NEWS -> "News"
            ApplicationInfo.CATEGORY_PRODUCTIVITY -> "Productivity"
            ApplicationInfo.CATEGORY_SOCIAL -> "Social"
            ApplicationInfo.CATEGORY_VIDEO -> "Video"
            else -> null
        }
    }
}
