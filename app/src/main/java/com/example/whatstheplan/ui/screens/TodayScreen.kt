package com.example.whatstheplan.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Forward
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.whatstheplan.domain.model.EngagementLevel
import com.example.whatstheplan.domain.model.UserSettings
import com.example.whatstheplan.ui.components.AtmospherePhase
import com.example.whatstheplan.ui.components.AtmosphericBackground
import com.example.whatstheplan.ui.components.PillBadge
import com.example.whatstheplan.ui.components.PrimaryPillButton
import com.example.whatstheplan.ui.components.SecondaryPillButton
import com.example.whatstheplan.ui.components.SectionCard
import com.example.whatstheplan.utils.DateUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun TodayScreen(
    container: AppContainer,
    onNavigateMorning: () -> Unit,
    onNavigateCheckIn: () -> Unit,
    onNavigateReflection: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val settings by container.settingsRepository.settingsFlow.collectAsStateWithLifecycle(UserSettings())
    val todayPlan by container.dailyPlanRepository.observeToday().collectAsStateWithLifecycle(null)
    val todayReflection by container.dailyReflectionRepository.observeToday().collectAsStateWithLifecycle(null)

    val todayDate = DateUtils.todayString()
    val isPausedToday = settings.pausedTodayDate == todayDate
    val isDormantToday = settings.dormantUntilDate == todayDate || settings.engagementLevel == EngagementLevel.LEVEL_2_STRESSED
    val isGhosting = settings.engagementLevel == EngagementLevel.LEVEL_3_GHOSTING
    val tone = settings.tonePreference

    var showMoveDialog by remember { mutableStateOf(false) }
    var moveNote by remember { mutableStateOf("") }
    var showNotTodayDialog by remember { mutableStateOf(false) }

    // Record interaction on app open
    LaunchedEffect(Unit) {
        container.settingsRepository.recordInteraction()
    }

    // 10-minute timer ticker
    var remainingSeconds by remember { mutableLongStateOf(0L) }
    LaunchedEffect(todayPlan?.status, todayPlan?.startedAt) {
        if (todayPlan?.status == "IN_PROGRESS" && todayPlan?.startedAt != null) {
            val elapsed = (System.currentTimeMillis() - todayPlan!!.startedAt!!) / 1000L
            val target = 10 * 60L // 10 minutes
            remainingSeconds = (target - elapsed).coerceAtLeast(0L)
            while (remainingSeconds > 0) {
                delay(1000L)
                remainingSeconds -= 1
            }
        }
    }

    val userGreeting = if (settings.userName.isNotBlank()) {
        "Morning, ${settings.userName} 👋"
    } else {
        "Morning 👋"
    }

    AtmosphericBackground(phase = AtmospherePhase.AUTO) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            Spacer(Modifier.height(4.dp))

            // 1. Conversational Header (Sora Typography & Breathing Spacing)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = userGreeting,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = if (todayPlan?.status == "DONE") "You've already accomplished your main priority today." else "Your day is shaped around your one intention.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                if (isPausedToday) {
                    PillBadge(
                        text = "🔕 Paused",
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else if (isDormantToday) {
                    PillBadge(
                        text = "🌙 Dormant",
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f),
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                }
            }

            // 2. Fresh Slate Re-engagement Banner (Level 3 Ghosting Re-engagement)
            if (isGhosting) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp),
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Fresh Slate",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = "No guilt, no backlog. Let's decide today's plan.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        TextButton(
                            onClick = {
                                scope.launch {
                                    container.settingsRepository.setEngagementLevel(EngagementLevel.LEVEL_0_NORMAL)
                                    container.settingsRepository.recordInteraction()
                                }
                            },
                        ) {
                            Text("Ready", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 3. RIGHT NOW HERO FOCUS SECTION
            if (todayPlan == null || (todayPlan?.text.isNullOrBlank() && todayPlan?.skipped == false)) {
                // Empty State
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                            RoundedCornerShape(32.dp),
                        )
                        .clickable { onNavigateMorning() },
                    shape = RoundedCornerShape(32.dp),
                    color = MaterialTheme.colorScheme.surface,
                ) {
                    Column(
                        modifier = Modifier.padding(26.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Text(
                            text = "RIGHT NOW",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp,
                        )
                        Text(
                            text = "What's your priority today?",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Before the day pulls you in every direction, choose your one thing.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        PrimaryPillButton(
                            text = "Set Intention",
                            onClick = onNavigateMorning,
                        )
                    }
                }
            } else {
                // Active Intention Card
                val plan = todayPlan!!
                val isDone = plan.status == "DONE"
                val isInProgress = plan.status == "IN_PROGRESS"

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (isDone) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                            RoundedCornerShape(32.dp),
                        ),
                    shape = RoundedCornerShape(32.dp),
                    color = MaterialTheme.colorScheme.surface,
                ) {
                    Column(
                        modifier = Modifier.padding(26.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "RIGHT NOW",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.2.sp,
                            )
                            PillBadge(
                                text = when (plan.status) {
                                    "DONE" -> "✓ Done"
                                    "IN_PROGRESS" -> "⏱️ Focus burst"
                                    "MOVED" -> "➡️ Moved"
                                    "DROPPED" -> "🛑 Dropped"
                                    else -> "Priority"
                                },
                                containerColor = if (isDone) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isDone) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        // Intention title
                        Text(
                            text = if (plan.skipped) "Nothing ambitious today" else plan.text,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )

                        // Smallest start
                        if (plan.firstStep.isNotBlank() && !plan.skipped) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp),
                                )
                                Column {
                                    Text(
                                        text = "Smallest start:",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        text = plan.firstStep,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                            }
                        }

                        // 10-Minute Countdown
                        if (isInProgress && remainingSeconds > 0) {
                            val mins = remainingSeconds / 60
                            val secs = remainingSeconds % 60
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFFBAC3FF).copy(alpha = 0.25f))
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp),
                                    )
                                    Text(
                                        text = "10-minute burst",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Medium,
                                    )
                                }
                                Text(
                                    text = "%02d:%02d".format(mins, secs),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }

                        // Status / Emotional Tone Feedback
                        if (isDone) {
                            Text(
                                text = "Nice. That's done. You've got one less thing on your mind.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        } else if (plan.status != "ACTIVE" && plan.status != "IN_PROGRESS") {
                            Text(
                                text = tone.statusFeedback(plan.status),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        // Action Buttons (Pill Buttons)
                        if (isInProgress) {
                            PrimaryPillButton(
                                text = "Complete (Done)",
                                icon = Icons.Default.Check,
                                onClick = {
                                    scope.launch {
                                        container.dailyPlanRepository.updateStatus("DONE")
                                        container.settingsRepository.setEngagementLevel(EngagementLevel.LEVEL_0_NORMAL)
                                        container.settingsRepository.recordInteraction()
                                    }
                                },
                            )
                        } else if (!isDone) {
                            PrimaryPillButton(
                                text = "Start 10 min",
                                icon = Icons.Default.PlayArrow,
                                onClick = {
                                    scope.launch {
                                        container.dailyPlanRepository.startTimer()
                                        container.settingsRepository.setEngagementLevel(EngagementLevel.LEVEL_0_NORMAL)
                                        container.settingsRepository.recordInteraction()
                                    }
                                },
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                SecondaryPillButton(
                                    text = "Move it",
                                    icon = Icons.Default.Forward,
                                    onClick = { showMoveDialog = true },
                                    modifier = Modifier.weight(1f),
                                )
                                SecondaryPillButton(
                                    text = "Not today",
                                    icon = Icons.Default.Close,
                                    onClick = { showNotTodayDialog = true },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                }
            }

            // 4. UP NEXT & LIGHTWEIGHT VERTICAL TIMELINE
            SectionCard {
                Text(
                    text = "UP NEXT & TODAY'S RHYTHM",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp,
                )

                val wakeFormatted = DateUtils.formatClock(settings.wakeTimeMinutes)
                val sleepFormatted = DateUtils.formatClock(settings.sleepTimeMinutes)
                val commitment = settings.dailyCommitment.ifBlank { "Core Focus & Work" }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    TimelineItem(time = wakeFormatted, title = "Wake Up & Morning Check-in", isActive = false, isDone = true)
                    TimelineItem(time = "09:30", title = todayPlan?.text?.ifBlank { "Daily Intention" } ?: "Main Intention", isActive = todayPlan?.status != "DONE", isDone = todayPlan?.status == "DONE")
                    TimelineItem(time = "14:00", title = commitment, isActive = false, isDone = false)
                    TimelineItem(time = sleepFormatted, title = "Evening Reflection & Rest", isActive = false, isDone = false)
                }
            }

            // 5. Pause Notifications Control
            SectionCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsOff,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Column {
                            Text(
                                text = if (isPausedToday) "Notifications paused today" else "Pause notifications today",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium,
                            )
                            Text(
                                text = if (isPausedToday) "Muted until tomorrow morning" else "Mute all prompts for rest",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    TextButton(
                        onClick = {
                            scope.launch {
                                val newPausedDate = if (isPausedToday) "" else todayDate
                                container.settingsRepository.setPausedTodayDate(newPausedDate)
                                container.settingsRepository.recordInteraction()
                            }
                        },
                    ) {
                        Text(
                            text = if (isPausedToday) "Resume" else "Pause",
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            // 6. Evening Reflection Card
            SectionCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.DarkMode,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            )
                        }
                        Column {
                            Text(
                                text = "Evening Reflection",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium,
                            )
                            Text(
                                text = if (todayReflection != null) "Reflected: ${todayReflection?.completion}" else "Close out today peacefully without guilt",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    TextButton(onClick = onNavigateReflection) {
                        Text(
                            text = if (todayReflection != null) "Edit" else "Reflect",
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
        }
    }

    // Move It Dialog (Non-guilt)
    if (showMoveDialog) {
        AlertDialog(
            onDismissRequest = { showMoveDialog = false },
            title = { Text("Move this intention?", style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Priorities shift. Move this intention to later or tomorrow with zero penalty.")
                    OutlinedTextField(
                        value = moveNote,
                        onValueChange = { moveNote = it },
                        placeholder = { Text("Optional note (e.g. Move to tomorrow morning)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            container.dailyPlanRepository.updateStatus("MOVED")
                            container.settingsRepository.setEngagementLevel(EngagementLevel.LEVEL_0_NORMAL)
                            container.settingsRepository.recordInteraction()
                            showMoveDialog = false
                        }
                    },
                    shape = RoundedCornerShape(100.dp),
                ) {
                    Text("Move It")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMoveDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }

    // Not Today Dialog (Zero Guilt)
    if (showNotTodayDialog) {
        AlertDialog(
            onDismissRequest = { showNotTodayDialog = false },
            title = { Text("Drop for today?", style = MaterialTheme.typography.titleLarge) },
            text = {
                Text("Letting go of a plan when life requires your attention elsewhere is a conscious, healthy decision. No guilt.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            container.dailyPlanRepository.updateStatus("DROPPED")
                            container.settingsRepository.setEngagementLevel(EngagementLevel.LEVEL_0_NORMAL)
                            container.settingsRepository.recordInteraction()
                            showNotTodayDialog = false
                        }
                    },
                    shape = RoundedCornerShape(100.dp),
                ) {
                    Text("Drop for Today")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNotTodayDialog = false }) {
                    Text("Keep Plan")
                }
            },
        )
    }
}

@Composable
private fun TimelineItem(
    time: String,
    title: String,
    isActive: Boolean,
    isDone: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = time,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(50.dp),
        )

        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isDone -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        isActive -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.outlineVariant
                    },
                ),
        )

        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
    }
}
