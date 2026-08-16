package com.example.whatstheplan.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.whatstheplan.domain.model.ActivityType
import com.example.whatstheplan.ui.theme.CategoryColors

data class DayCheckInData(
    val dayLabel: String,
    val count: Int,
    val isToday: Boolean,
)

data class DayScreenTimeData(
    val dayLabel: String,
    val durationMillis: Long,
    val isToday: Boolean,
)

fun getActivityColor(activity: ActivityType): Color = when (activity) {
    ActivityType.STUDYING -> CategoryColors.Study
    ActivityType.WORKING -> CategoryColors.Work
    ActivityType.BUILDING -> CategoryColors.Building
    ActivityType.SCROLLING -> CategoryColors.Social
    ActivityType.GAMING -> CategoryColors.Gaming
    ActivityType.WATCHING -> CategoryColors.Watching
    ActivityType.BREAKING -> CategoryColors.Break
    ActivityType.OUTSIDE -> CategoryColors.Break
    ActivityType.OTHER -> CategoryColors.Other
}

@Composable
fun WeeklyCheckInBarChart(
    data: List<DayCheckInData>,
    modifier: Modifier = Modifier,
) {
    val maxCount = data.maxOfOrNull { it.count }?.coerceAtLeast(1) ?: 1
    val barColor = MaterialTheme.colorScheme.primary
    val barColorBg = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f)
    val todayColor = MaterialTheme.colorScheme.primary

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            data.forEach { item ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    Text(
                        text = if (item.count > 0) item.count.toString() else "",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (item.isToday) FontWeight.Bold else FontWeight.Normal,
                        color = if (item.isToday) todayColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(22.dp)
                            .height(84.dp),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        // Background track
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp))
                                .background(barColorBg),
                        )
                        // Active bar
                        val fillRatio = (item.count.toFloat() / maxCount).coerceIn(0f, 1f)
                        if (fillRatio > 0f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(fillRatio)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (item.isToday) todayColor else barColor.copy(alpha = 0.85f)),
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = item.dayLabel,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (item.isToday) FontWeight.Bold else FontWeight.Normal,
                        color = if (item.isToday) todayColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActivityBreakdownBar(
    counts: Map<ActivityType, Int>,
    modifier: Modifier = Modifier,
) {
    val total = counts.values.sum().coerceAtLeast(1)

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Proportion Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp)),
        ) {
            counts.entries.forEach { entry ->
                val weight = (entry.value.toFloat() / total).coerceAtLeast(0.01f)
                Box(
                    modifier = Modifier
                        .weight(weight)
                        .fillMaxSize()
                        .background(getActivityColor(entry.key)),
                )
            }
        }

        // Legend Grid
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            counts.entries.forEach { (activity, count) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(getActivityColor(activity)),
                    )
                    Text(
                        text = "${activity.displayName}: $count",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@Composable
fun ScreenTimeTrendChart(
    data: List<DayScreenTimeData>,
    modifier: Modifier = Modifier,
) {
    val maxMillis = data.maxOfOrNull { it.durationMillis }?.coerceAtLeast(1L) ?: 1L
    val barColor = MaterialTheme.colorScheme.secondary
    val barColorBg = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            data.forEach { item ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    val labelText = if (item.durationMillis > 0) {
                        val hours = item.durationMillis / (1000 * 60 * 60)
                        val mins = (item.durationMillis / (1000 * 60)) % 60
                        if (hours > 0) "${hours}h" else "${mins}m"
                    } else ""

                    Text(
                        text = labelText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(22.dp)
                            .height(84.dp),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp))
                                .background(barColorBg),
                        )
                        val fillRatio = (item.durationMillis.toFloat() / maxMillis).coerceIn(0f, 1f)
                        if (fillRatio > 0f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(fillRatio)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(barColor),
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = item.dayLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
