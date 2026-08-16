package com.example.whatstheplan.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flare
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.whatstheplan.AppContainer
import com.example.whatstheplan.data.local.database.dao.ActivityCount
import com.example.whatstheplan.data.local.database.entities.DailyPlanEntity
import com.example.whatstheplan.data.local.database.entities.DailyReflectionEntity
import com.example.whatstheplan.data.local.database.entities.ScreenTimeSnapshotEntity
import com.example.whatstheplan.domain.model.ActivityType
import com.example.whatstheplan.domain.model.UserSettings
import com.example.whatstheplan.ui.components.BentoStatCard
import com.example.whatstheplan.ui.components.CardTitle
import com.example.whatstheplan.ui.components.PillBadge
import com.example.whatstheplan.ui.components.PrimaryGlowButton
import com.example.whatstheplan.ui.components.SectionCard
import com.example.whatstheplan.ui.components.WeeklyCapsuleTracker
import com.example.whatstheplan.ui.components.getActivityColor
import com.example.whatstheplan.utils.DateUtils
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TodayScreen(
    container: AppContainer,
    onNavigateMorning: () -> Unit,
    onNavigateCheckIn: () -> Unit,
    onNavigateReflection: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val todayPlan by container.dailyPlanRepository.observeToday().collectAsStateWithLifecycle(null)
    val checkIns by container.checkInRepository.observeToday().collectAsStateWithLifecycle(emptyList())
    val reflection by container.dailyReflectionRepository.observeToday().collectAsStateWithLifecycle(null)
    val activityCounts by container.checkInRepository.observeTodayActivityCounts().collectAsStateWithLifecycle(emptyList())
    val screenTime by container.screenTimeRepository.observeToday().collectAsStateWithLifecycle(null)
    val settings by container.settingsRepository.settingsFlow.collectAsStateWithLifecycle(UserSettings())

    val now = LocalTime.now()
    val isMorning = now.hour in 5..11
    val isAfternoon = now.hour in 12..16

    val greeting = when {
        isMorning -> "Good morning ☀️"
        isAfternoon -> "Good afternoon 🌤"
        else -> "Good evening 🌙"
    }

    val formattedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d"))
    val isFocusActive = System.currentTimeMillis() < settings.focusModeUntilMillis
    val focusRemainingMinutes = if (isFocusActive) {
        ((settings.focusModeUntilMillis - System.currentTimeMillis()) / (1000 * 60)).coerceAtLeast(1)
    } else 0L

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Spacer(Modifier.height(4.dp))

        // 1. Personal Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (isFocusActive) {
                PillBadge(
                    text = "Focus: ${focusRemainingMinutes}m",
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Spa,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
        }

        // 2. HERO CARD — Today's Plan
        HeroPlanCard(
            plan = todayPlan,
            checkInCount = checkIns.size,
            onSetPlan = onNavigateMorning,
        )

        // 3. DAILY SNAPSHOT — Bento Stat Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            BentoStatCard(
                title = "Check-ins",
                value = "${checkIns.size}",
                subtitle = if (checkIns.isNotEmpty()) "Logged today" else "Tap to log",
                icon = Icons.Default.CheckCircle,
                accentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
                onClick = onNavigateCheckIn,
            )

            val screenTimeText = screenTime?.let {
                val hours = it.totalMillis / (1000 * 60 * 60)
                val mins = (it.totalMillis / (1000 * 60)) % 60
                if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
            } ?: if (container.usageStatsReader.hasUsageAccess()) "0m" else "Off"

            BentoStatCard(
                title = "Screen Time",
                value = screenTimeText,
                subtitle = if (container.usageStatsReader.hasUsageAccess()) "Today's usage" else "Tap to enable",
                icon = Icons.Default.PhoneAndroid,
                accentColor = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f),
            )
        }

        // 4. QUICK CHECK-IN ACTION BAR
        PrimaryGlowButton(
            text = "⚡ Quick Check-In",
            onClick = onNavigateCheckIn,
            icon = Icons.Default.ArrowForward,
        )

        // 5. ACTIVITY BREAKDOWN (If user has logged check-ins today)
        if (activityCounts.isNotEmpty()) {
            SectionCard {
                CardTitle("Today's Activities", Icons.Default.Flare, "${checkIns.size} total")
                ActivityDistributionView(counts = activityCounts)
            }
        }

        // 6. FOCUS MODE / QUICK PAUSE
        SectionCard {
            CardTitle("Focus Mode", Icons.Default.NotificationsOff)
            Text(
                text = if (isFocusActive) {
                    "🔕 Check-in notifications are paused for another $focusRemainingMinutes min."
                } else {
                    "Temporarily pause check-in reminders for focused work or rest:"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(30 to "30m", 60 to "1h", 120 to "2h", 240 to "4h").forEach { (minutes, label) ->
                    FilterChip(
                        selected = false,
                        onClick = {
                            scope.launch {
                                val until = System.currentTimeMillis() + (minutes * 60 * 1000L)
                                container.settingsRepository.setFocusModeUntil(until)
                            }
                        },
                        label = { Text("Pause $label") },
                        shape = RoundedCornerShape(100.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    )
                }

                if (isFocusActive) {
                    FilterChip(
                        selected = true,
                        onClick = {
                            scope.launch { container.settingsRepository.clearFocusMode() }
                        },
                        label = { Text("Resume Now") },
                        shape = RoundedCornerShape(100.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer,
                        ),
                    )
                }
            }
        }

        // 7. EVENING REFLECTION PROMPT
        EveningReflectionCard(
            reflection = reflection,
            onReflect = onNavigateReflection,
        )

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun HeroPlanCard(
    plan: DailyPlanEntity?,
    checkInCount: Int,
    onSetPlan: () -> Unit,
) {
    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                RoundedCornerShape(26.dp),
            ),
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .background(gradientBrush)
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.WbSunny,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Text(
                        text = "TODAY'S PLAN",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.2.sp,
                    )
                }

                if (plan != null) {
                    PillBadge(
                        text = "$checkInCount check-ins",
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }

            if (plan != null && plan.text.isNotBlank()) {
                Text(
                    text = "“${plan.text}”",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (plan.skipped) {
                    Text(
                        text = "Taking today as it comes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                // 7-day capsule streak preview
                WeeklyCapsuleTracker(
                    completedDays = setOf(LocalDate.now().dayOfWeek.value),
                )
            } else {
                Text(
                    text = "What do you want to accomplish today?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Take ten seconds before diving in to decide what today is for.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Button(
                    onClick = onSetPlan,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Text("Make today's plan →", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ActivityDistributionView(counts: List<ActivityCount>) {
    val total = counts.sumOf { it.total }.coerceAtLeast(1)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Proportional Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(6.dp)),
        ) {
            counts.forEach { item ->
                val weight = (item.total.toFloat() / total).coerceAtLeast(0.01f)
                val activity = ActivityType.fromCode(item.activity)
                Box(
                    modifier = Modifier
                        .weight(weight)
                        .fillMaxSize()
                        .background(getActivityColor(activity)),
                )
            }
        }
        // Chips
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            counts.forEach { item ->
                val activity = ActivityType.fromCode(item.activity)
                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(getActivityColor(activity)),
                        )
                        Text(
                            text = "${activity.displayName} (${item.total})",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EveningReflectionCard(
    reflection: DailyReflectionEntity?,
    onReflect: () -> Unit,
) {
    SectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Nightlight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Text(
                    text = "Evening Reflection",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            if (reflection != null) {
                PillBadge(
                    text = "Completed ✓",
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }

        if (reflection == null) {
            Text(
                text = "Close the loop on today. Take a quick moment to reflect on what went well.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedButton(
                onClick = onReflect,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text("Reflect on today →")
            }
        } else {
            Text(
                text = "Mood: ${reflection.mood} • Follow-through: ${reflection.completion}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
            )
            if (reflection.note.isNotBlank()) {
                Text(
                    text = "“${reflection.note}”",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
