package com.example.whatstheplan.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.whatstheplan.AppContainer
import com.example.whatstheplan.data.local.database.entities.CheckInEntity
import com.example.whatstheplan.data.local.database.entities.DailyPlanEntity
import com.example.whatstheplan.data.local.database.entities.DailyReflectionEntity
import com.example.whatstheplan.data.local.database.entities.ScreenTimeSnapshotEntity
import com.example.whatstheplan.domain.model.ActivityType
import com.example.whatstheplan.ui.components.ActivityBreakdownBar
import com.example.whatstheplan.ui.components.CardTitle
import com.example.whatstheplan.ui.components.DayCheckInData
import com.example.whatstheplan.ui.components.DayScreenTimeData
import com.example.whatstheplan.ui.components.ScreenTimeTrendChart
import com.example.whatstheplan.ui.components.SectionCard
import com.example.whatstheplan.ui.components.WeeklyCapsuleTracker
import com.example.whatstheplan.ui.components.WeeklyCheckInBarChart
import com.example.whatstheplan.utils.DateUtils
import java.time.LocalDate

@Composable
fun InsightsScreen(
    container: AppContainer,
) {
    val checkIns by container.checkInRepository.observeAll().collectAsStateWithLifecycle(emptyList())
    val plans by container.dailyPlanRepository.observeAll().collectAsStateWithLifecycle(emptyList())
    val reflections by container.dailyReflectionRepository.observeAll().collectAsStateWithLifecycle(emptyList())
    val screenSnapshots by container.screenTimeRepository.observeAll().collectAsStateWithLifecycle(emptyList())

    val today = LocalDate.now()
    val last7Days = (6 downTo 0).map { today.minusDays(it.toLong()) }
    val dayOfWeekLabels = listOf("M", "T", "W", "T", "F", "S", "S")

    // 1. Weekly Check-In Bar Data
    val checkInsByDate = checkIns.groupBy { it.date }
    val weeklyCheckInData = last7Days.map { date ->
        val dateStr = date.toString()
        val count = checkInsByDate[dateStr]?.size ?: 0
        val labelIndex = date.dayOfWeek.value - 1
        DayCheckInData(
            dayLabel = dayOfWeekLabels.getOrElse(labelIndex) { "?" },
            count = count,
            isToday = date == today,
        )
    }

    // 2. Activity Distribution (All check-ins from past 7 days)
    val past7Dates = last7Days.map { it.toString() }.toSet()
    val past7CheckIns = checkIns.filter { it.date in past7Dates }
    val activityCounts: Map<ActivityType, Int> = past7CheckIns
        .map { ActivityType.fromCode(it.activity) }
        .groupBy { it }
        .mapValues { it.value.size }

    val topActivity = activityCounts.maxByOrNull { it.value }?.key

    // 3. Plan Follow-Through (Past 7 days)
    val past7Plans = plans.filter { it.date in past7Dates }
    val past7Reflections = reflections.filter { it.date in past7Dates }
    val completedCount = past7Reflections.count { it.completion.equals("Yes", ignoreCase = true) }
    val totalPlannedDays = past7Plans.size

    // 4. Screen Time Trend
    val screenMap = screenSnapshots.associateBy { it.date }
    val weeklyScreenData = last7Days.map { date ->
        val dateStr = date.toString()
        val duration = screenMap[dateStr]?.totalMillis ?: 0L
        val labelIndex = date.dayOfWeek.value - 1
        DayScreenTimeData(
            dayLabel = dayOfWeekLabels.getOrElse(labelIndex) { "?" },
            durationMillis = duration,
            isToday = date == today,
        )
    }

    val totalWeekScreenMillis = weeklyScreenData.sumOf { it.durationMillis }
    val activeScreenDays = weeklyScreenData.count { it.durationMillis > 0 }.coerceAtLeast(1)
    val avgScreenMillis = totalWeekScreenMillis / activeScreenDays
    val avgHours = avgScreenMillis / (1000 * 60 * 60)
    val avgMins = (avgScreenMillis / (1000 * 60)) % 60

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Visual Insights",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Understand your phone relationship and weekly intentions.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // Section 1: Weekly Check-Ins Bar Chart
        SectionCard {
            CardTitle("Weekly Check-Ins", Icons.Default.BarChart, "${past7CheckIns.size} logged")
            WeeklyCheckInBarChart(data = weeklyCheckInData)
            Text(
                text = if (past7CheckIns.isNotEmpty()) {
                    "You completed ${past7CheckIns.size} check-ins across the last 7 days."
                } else {
                    "Log regular check-ins to see your weekly rhythm emerge."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Section 2: Activity Distribution
        SectionCard {
            CardTitle("Activity Distribution", Icons.Default.PieChart)
            if (activityCounts.isNotEmpty()) {
                ActivityBreakdownBar(counts = activityCounts)
                Text(
                    text = topActivity?.let {
                        "${it.displayName} was your most frequent check-in this week."
                    } ?: "Activity breakdown is updated as you check in.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Text(
                    text = "No activities logged yet this week. Check-in during the day to view your activity breakdown.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // Section 3: Plan Follow-Through
        SectionCard {
            CardTitle("Plan Follow-Through", Icons.Default.CheckCircle)
            WeeklyCapsuleTracker(
                completedDays = past7Reflections
                    .filter { it.completion.equals("Yes", ignoreCase = true) }
                    .mapNotNull {
                        runCatching { LocalDate.parse(it.date).dayOfWeek.value }.getOrNull()
                    }.toSet(),
            )
            Text(
                text = when {
                    totalPlannedDays > 0 -> "You followed through on $completedCount of $totalPlannedDays planned days this week."
                    else -> "Create daily morning plans to track your weekly follow-through."
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        // Section 4: Screen Time Trend (If available)
        if (weeklyScreenData.any { it.durationMillis > 0 }) {
            SectionCard {
                CardTitle("Screen Time Trend", Icons.Default.Timeline)
                ScreenTimeTrendChart(data = weeklyScreenData)
                Text(
                    text = "Your average screen time was ${avgHours}h ${avgMins}m across active days.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}
