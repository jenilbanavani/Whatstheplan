package com.example.whatstheplan.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.whatstheplan.AppContainer
import com.example.whatstheplan.data.local.database.entities.CheckInEntity
import com.example.whatstheplan.data.local.database.entities.DailyPlanEntity
import com.example.whatstheplan.data.local.database.entities.DailyReflectionEntity
import com.example.whatstheplan.data.local.database.entities.ScreenTimeSnapshotEntity
import com.example.whatstheplan.domain.model.ActivityType
import com.example.whatstheplan.ui.components.CardTitle
import com.example.whatstheplan.ui.components.PillBadge
import com.example.whatstheplan.ui.components.SectionCard
import com.example.whatstheplan.ui.components.getActivityColor
import com.example.whatstheplan.utils.DateUtils
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun HistoryScreen(
    container: AppContainer,
) {
    val checkIns by container.checkInRepository.observeAll().collectAsStateWithLifecycle(emptyList())
    val plans by container.dailyPlanRepository.observeAll().collectAsStateWithLifecycle(emptyList())
    val reflections by container.dailyReflectionRepository.observeAll().collectAsStateWithLifecycle(emptyList())
    val screenSnapshots by container.screenTimeRepository.observeAll().collectAsStateWithLifecycle(emptyList())

    val allDates: List<String> = (
        plans.map { it.date } +
            checkIns.map { it.date } +
            reflections.map { it.date } +
            screenSnapshots.map { it.date }
    ).distinct().sortedDescending()

    val plansByDate = plans.associateBy { it.date }
    val checkInsByDate = checkIns.groupBy { it.date }
    val reflectionsByDate = reflections.associateBy { it.date }
    val screensByDate = screenSnapshots.associateBy { it.date }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Spacer(Modifier.height(16.dp))
            Text(
                text = "History & Journal",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "A quiet chronological record of your intentions, logs, and reflections.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
        }

        if (allDates.isEmpty()) {
            item {
                SectionCard {
                    Text(
                        text = "No history recorded yet.",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Your daily plans, check-ins, and evening reflections will be saved here completely offline.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            items(allDates, key = { it }) { date ->
                DayJournalCard(
                    date = date,
                    plan = plansByDate[date],
                    checkIns = checkInsByDate[date].orEmpty(),
                    reflection = reflectionsByDate[date],
                    screenTime = screensByDate[date],
                )
            }
        }

        item {
            Spacer(Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DayJournalCard(
    date: String,
    plan: DailyPlanEntity?,
    checkIns: List<CheckInEntity>,
    reflection: DailyReflectionEntity?,
    screenTime: ScreenTimeSnapshotEntity?,
) {
    var expanded by remember { mutableStateOf(false) }

    val formattedDate = runCatching {
        val parsed = LocalDate.parse(date)
        parsed.format(DateTimeFormatter.ofPattern("EEE • MMM d, yyyy"))
    }.getOrDefault(date)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                RoundedCornerShape(22.dp),
            )
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = formattedDate.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.0.sp,
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }

            // Plan text
            if (plan != null && plan.text.isNotBlank()) {
                Text(
                    text = "“${plan.text}”",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            } else {
                Text(
                    text = "No explicit plan set",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Pill badges row
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                PillBadge(
                    text = "${checkIns.size} check-ins",
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                if (screenTime != null && screenTime.totalMillis > 0) {
                    val hours = screenTime.totalMillis / (1000 * 60 * 60)
                    val mins = (screenTime.totalMillis / (1000 * 60)) % 60
                    PillBadge(
                        text = if (hours > 0) "${hours}h ${mins}m screen" else "${mins}m screen",
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }

                if (reflection != null) {
                    PillBadge(
                        text = "${reflection.mood} • ${reflection.completion}",
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                }
            }

            // Expandable details
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Reflection note
                    if (reflection != null && reflection.note.isNotBlank()) {
                        Text(
                            text = "Reflection: “${reflection.note}”",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }

                    // Check-in timeline
                    if (checkIns.isNotEmpty()) {
                        Text(
                            text = "Check-ins log:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        checkIns.forEach { checkIn ->
                            val activity = ActivityType.fromCode(checkIn.activity)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(getActivityColor(activity)),
                                    )
                                    Text(
                                        text = "${activity.emoji} ${activity.displayName}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                    )
                                    if (checkIn.note.isNotBlank()) {
                                        Text(
                                            text = "— ${checkIn.note}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                                Text(
                                    text = DateUtils.formatTime(checkIn.timestamp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
