package com.example.whatstheplan.domain.model

data class ScreenTimeSummary(
    val totalTimeMillis: Long,
    val topApps: List<AppUsageInfo>,
)

data class AppUsageInfo(
    val packageName: String,
    val appLabel: String,
    val totalTimeMillis: Long,
    val category: String?,
)
