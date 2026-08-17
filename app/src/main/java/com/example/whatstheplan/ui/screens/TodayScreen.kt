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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
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
import com.example.whatstheplan.domain.model.TonePreference
import com.example.whatstheplan.domain.model.UserSettings
import com.example.whatstheplan.ui.components.CardTitle
import com.example.whatstheplan.ui.components.PillBadge
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
    val tone = settings.tonePreference

    var showMoveDialog by remember { mutableStateOf(false) }
    var moveNote by remember { mutableStateOf("") }
    var showNotTodayDialog by remember { mutableStateOf(false) }

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

    val formattedDate = runCatching {
        LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d"))
    }.getOrDefault("Today")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Spacer(Modifier.height(4.dp))

        // 1. Header (Greeting & Date)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = formattedDate.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.2.sp,
                )
                Text(
                    text = if (settings.userName.isNotBlank()) "Hey, ${settings.userName}" else "Today",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            // Pause status pill
            if (isPausedToday) {
                PillBadge(
                    text = "🔕 Paused for today",
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // 2. Today's Intention Card (Hero Element)
        if (todayPlan == null || (todayPlan?.text.isNullOrBlank() && todayPlan?.skipped == false)) {
            // Empty state: Prompt to set intention
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        RoundedCornerShape(24.dp),
                    )
                    .clickable { onNavigateMorning() },
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.WbSunny,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    Text(
                        text = "What is your plan for today?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "Take 10 seconds to choose one main intention before you begin.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Button(
                        onClick = onNavigateMorning,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        ),
                    ) {
                        Text("Set Intention", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // Intention Active Card
            val plan = todayPlan!!
            val statusColor = when (plan.status) {
                "DONE" -> MaterialTheme.colorScheme.primary
                "IN_PROGRESS" -> Color(0xFF6EE7B7) // Mint green
                "MOVED" -> Color(0xFF7FA7FF) // Soft sky
                "DROPPED" -> MaterialTheme.colorScheme.onSurfaceVariant
                else -> MaterialTheme.colorScheme.primary
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        statusColor.copy(alpha = 0.4f),
                        RoundedCornerShape(24.dp),
                    ),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "TODAY'S INTENTION",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp,
                        )
                        PillBadge(
                            text = when (plan.status) {
                                "DONE" -> "✓ Done"
                                "IN_PROGRESS" -> "⏱️ In Progress"
                                "MOVED" -> "➡️ Moved"
                                "DROPPED" -> "🛑 Not Today"
                                else -> "Active"
                            },
                            containerColor = statusColor.copy(alpha = 0.2f),
                            contentColor = statusColor,
                        )
                    }

                    Text(
                        text = if (plan.skipped) "Taking today as it comes" else "“${plan.text}”",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    // Smallest First Step
                    if (plan.firstStep.isNotBlank() && !plan.skipped) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
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
                                        text = "Smallest first step:",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        text = plan.firstStep,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                            }
                        }
                    }

                    // 10-Minute Active Countdown Banner
                    if (plan.status == "IN_PROGRESS" && remainingSeconds > 0) {
                        val mins = remainingSeconds / 60
                        val secs = remainingSeconds % 60
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF6EE7B7).copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, Color(0xFF6EE7B7).copy(alpha = 0.5f)),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
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
                                        tint = Color(0xFF6EE7B7),
                                        modifier = Modifier.size(20.dp),
                                    )
                                    Text(
                                        text = "10-minute focus burst",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                                Text(
                                    text = "%02d:%02d".format(mins, secs),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF6EE7B7),
                                )
                            }
                        }
                    }

                    // Status feedback in selected tone
                    if (plan.status != "ACTIVE" && plan.status != "IN_PROGRESS") {
                        Text(
                            text = tone.statusFeedback(plan.status),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    // 3 PRIMARY ACTIONS: Start, Move, Not Today
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        // Action 1: Start (or Mark Done)
                        if (plan.status == "IN_PROGRESS") {
                            Button(
                                onClick = {
                                    scope.launch {
                                        container.dailyPlanRepository.updateStatus("DONE")
                                        container.userCorrectionRepository.addCorrection(
                                            category = "INTENTION",
                                            note = "Marked intention as DONE: ${plan.text}",
                                        )
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                ),
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Done", fontWeight = FontWeight.Bold)
                            }
                        } else if (plan.status != "DONE") {
                            Button(
                                onClick = {
                                    scope.launch {
                                        container.dailyPlanRepository.startTimer()
                                        container.userCorrectionRepository.addCorrection(
                                            category = "INTENTION",
                                            note = "Started 10 min on: ${plan.text}",
                                        )
                                    }
                                },
                                modifier = Modifier.weight(1.2f),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                ),
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Start 10m", fontWeight = FontWeight.Bold)
                            }
                        }

                        // Action 2: Move it
                        if (plan.status != "MOVED") {
                            OutlinedButton(
                                onClick = { showMoveDialog = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                            ) {
                                Icon(Icons.Default.Forward, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Move")
                            }
                        }

                        // Action 3: Not today
                        if (plan.status != "DROPPED" && plan.status != "DONE") {
                            OutlinedButton(
                                onClick = { showNotTodayDialog = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Not today")
                            }
                        }
                    }
                }
            }
        }

        // 3. Pause for Today Control
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
                            text = if (isPausedToday) "Notifications paused today" else "Pause prompts for today",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = if (isPausedToday) "Resumes automatically tomorrow morning" else "Need uninterrupted focus or rest?",
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
                        }
                    },
                ) {
                    Text(
                        text = if (isPausedToday) "Resume" else "Pause",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }

        // 4. Evening Recovery Prompt
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
                            text = "Evening Recovery",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = if (todayReflection != null) "Reflected: ${todayReflection?.completion}" else "Reflect on how today unfolded",
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

        Spacer(Modifier.height(16.dp))
    }

    // Move Dialog
    if (showMoveDialog) {
        AlertDialog(
            onDismissRequest = { showMoveDialog = false },
            title = { Text("Move this intention?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Priorities shift. That's completely normal. You can move this intention to later.")
                    OutlinedTextField(
                        value = moveNote,
                        onValueChange = { moveNote = it },
                        placeholder = { Text("Optional note (e.g. Moving to tomorrow 10am)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            container.dailyPlanRepository.updateStatus("MOVED")
                            container.userCorrectionRepository.addCorrection(
                                category = "INTENTION",
                                note = "Moved intention: ${todayPlan?.text}. Note: $moveNote",
                            )
                            showMoveDialog = false
                        }
                    },
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
            title = { Text("Drop for today?") },
            text = {
                Text("Letting go of a plan when life requires your attention elsewhere is a conscious, healthy decision. No guilt.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            container.dailyPlanRepository.updateStatus("DROPPED")
                            container.userCorrectionRepository.addCorrection(
                                category = "INTENTION",
                                note = "Dropped intention for today: ${todayPlan?.text}",
                            )
                            showNotTodayDialog = false
                        }
                    },
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
