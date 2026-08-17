package com.example.whatstheplan.ui.screens

import android.app.TimePickerDialog
import android.os.Build
import android.os.PowerManager
import android.provider.Settings as AndroidSettings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.whatstheplan.AppContainer
import com.example.whatstheplan.data.local.BackupManager
import com.example.whatstheplan.domain.model.ThemeMode
import com.example.whatstheplan.domain.model.TonePreference
import com.example.whatstheplan.domain.model.UserSettings
import com.example.whatstheplan.notifications.NotificationHelper
import com.example.whatstheplan.ui.components.CardTitle
import com.example.whatstheplan.ui.components.PillBadge
import com.example.whatstheplan.ui.components.SectionCard
import com.example.whatstheplan.utils.DateUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    container: AppContainer,
    onPrivacy: () -> Unit,
    onNavigateMemory: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settings by container.settingsRepository.settingsFlow.collectAsStateWithLifecycle(UserSettings())
    var showResetDialog by remember { mutableStateOf(false) }
    var editingName by remember { mutableStateOf(false) }
    var tempName by remember { mutableStateOf("") }
    var editingCommitment by remember { mutableStateOf(false) }
    var tempCommitment by remember { mutableStateOf("") }

    val notificationsEnabled = NotificationHelper.canPostNotifications(context)
    val powerManager = context.getSystemService(android.content.Context.POWER_SERVICE) as? PowerManager
    val isIgnoringBatteryOptimizations = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        powerManager?.isIgnoringBatteryOptimizations(context.packageName) == true
    } else true

    val wakeTimePickerDialog = remember(settings.wakeTimeMinutes) {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                scope.launch { container.settingsRepository.setWakeTimeMinutes(hourOfDay * 60 + minute) }
            },
            settings.wakeTimeMinutes / 60,
            settings.wakeTimeMinutes % 60,
            false,
        )
    }

    val sleepTimePickerDialog = remember(settings.sleepTimeMinutes) {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                scope.launch { container.settingsRepository.setSleepTimeMinutes(hourOfDay * 60 + minute) }
            },
            settings.sleepTimeMinutes / 60,
            settings.sleepTimeMinutes % 60,
            false,
        )
    }

    val quietStartPickerDialog = remember(settings.quietHoursStartMinutes) {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                scope.launch {
                    container.settingsRepository.setQuietHours(
                        hourOfDay * 60 + minute,
                        settings.quietHoursEndMinutes,
                    )
                }
            },
            settings.quietHoursStartMinutes / 60,
            settings.quietHoursStartMinutes % 60,
            false,
        )
    }

    val quietEndPickerDialog = remember(settings.quietHoursEndMinutes) {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                scope.launch {
                    container.settingsRepository.setQuietHours(
                        settings.quietHoursStartMinutes,
                        hourOfDay * 60 + minute,
                    )
                }
            },
            settings.quietHoursEndMinutes / 60,
            settings.quietHoursEndMinutes % 60,
            false,
        )
    }

    val feedbackOptions = listOf("Useful", "Too early", "Too late", "Not relevant", "Too much")

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val json = BackupManager.exportToJson(container)
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(json.toByteArray())
                }
                Toast.makeText(context, "Memory exported successfully", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val content = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                if (content != null) {
                    val success = BackupManager.importFromJson(container, content)
                    if (success) {
                        Toast.makeText(context, "Memory restored successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Failed to import JSON", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Fine-tune companion tone, rhythm, and notification timing.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // 1. Companion Tone
        SectionCard {
            CardTitle("Companion Tone", Icons.Default.ChatBubbleOutline)
            Text(
                text = "Voice of notifications, intention cards, and reflections.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TonePreference.entries.forEach { tone ->
                val isSelected = tone == settings.tonePreference
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { scope.launch { container.settingsRepository.setTonePreference(tone) } },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        if (isSelected) 2.dp else 1.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    ),
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = tone.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = tone.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        // 2. Schedule & Rhythm
        SectionCard {
            CardTitle("Schedule & Rhythm", Icons.Default.Schedule)

            // Name
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Name", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(
                        text = settings.userName.ifBlank { "Not provided" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                TextButton(onClick = {
                    tempName = settings.userName
                    editingName = true
                }) {
                    Text("Edit")
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // Wake Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Wake Time", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(
                        text = DateUtils.formatClock(settings.wakeTimeMinutes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                TextButton(onClick = { wakeTimePickerDialog.show() }) {
                    Text("Change")
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // Sleep Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Sleep Time", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(
                        text = DateUtils.formatClock(settings.sleepTimeMinutes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                TextButton(onClick = { sleepTimePickerDialog.show() }) {
                    Text("Change")
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // Daily Commitment
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Fixed Daily Commitment", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(
                        text = settings.dailyCommitment.ifBlank { "None set" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                TextButton(onClick = {
                    tempCommitment = settings.dailyCommitment
                    editingCommitment = true
                }) {
                    Text("Edit")
                }
            }
        }

        // 3. Notification Controls (Morning, Follow-Up, Evening)
        SectionCard {
            CardTitle("Notification Controls", Icons.Default.Notifications)

            // Morning Prompt Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Morning Check-In", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(
                        text = "Prompts for your one intention at wake time",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = settings.morningReminderEnabled,
                    onCheckedChange = { scope.launch { container.settingsRepository.setMorningReminderEnabled(it) } },
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // Follow-Up Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Context-Aware Follow-Up", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(
                        text = "Max 1 follow-up midday with Start, Move, Not today",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = settings.followUpEnabled,
                    onCheckedChange = { scope.launch { container.settingsRepository.setFollowUpEnabled(it) } },
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // Evening Reflection Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Evening Reflection", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(
                        text = "Prompts for neutral recovery before sleep time",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = settings.eveningReflectionEnabled,
                    onCheckedChange = { scope.launch { container.settingsRepository.setEveningReflectionEnabled(it) } },
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // Quiet Hours
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Quiet Hours", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(
                        text = "${DateUtils.formatClock(settings.quietHoursStartMinutes)} to ${DateUtils.formatClock(settings.quietHoursEndMinutes)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Row {
                    TextButton(onClick = { quietStartPickerDialog.show() }) { Text("Start") }
                    TextButton(onClick = { quietEndPickerDialog.show() }) { Text("End") }
                }
            }
        }

        // 4. Notification Timing Feedback
        SectionCard {
            CardTitle("Notification Feedback", Icons.Default.Feedback)
            Text(
                text = "Help the companion tune its notification timing for your rhythm:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                feedbackOptions.forEach { feedback ->
                    val isSelected = settings.notificationFeedback == feedback
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            scope.launch {
                                container.settingsRepository.setNotificationFeedback(if (isSelected) "" else feedback)
                                container.userCorrectionRepository.addCorrection(
                                    category = "FEEDBACK",
                                    note = "Notification timing feedback: $feedback",
                                    source = "LEARNED",
                                )
                                Toast.makeText(context, "Feedback recorded locally.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        label = { Text(feedback) },
                        shape = RoundedCornerShape(100.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        ),
                    )
                }
            }
        }

        // 5. Future Calendar Integration Note
        SectionCard {
            CardTitle("Calendar Integration (Planned)", Icons.Default.CalendarMonth)
            Text(
                text = "Optional read-only local calendar sync is architected for a future update. No cloud connection will ever be required.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // 6. Memory & Data Transparency
        SectionCard {
            CardTitle("Memory & Privacy", Icons.Default.Memory)
            Text(
                text = "All memory is 100% offline on this device. View, edit, export, or clear memory at any time.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (onNavigateMemory != null) {
                OutlinedButton(
                    onClick = onNavigateMemory,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Icon(Icons.Default.Memory, contentDescription = null)
                    Text("Open Local Memory Inspector", modifier = Modifier.padding(start = 8.dp))
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedButton(
                    onClick = { exportLauncher.launch("whats_the_plan_memory.json") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(Icons.Default.SaveAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text(" Export JSON", style = MaterialTheme.typography.labelMedium)
                }
                OutlinedButton(
                    onClick = { importLauncher.launch(arrayOf("application/json", "text/*")) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text(" Import JSON", style = MaterialTheme.typography.labelMedium)
                }
            }

            TextButton(
                onClick = onPrivacy,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.PrivacyTip, contentDescription = null, modifier = Modifier.size(16.dp))
                Text(" Privacy Architecture Guarantee", modifier = Modifier.padding(start = 6.dp))
            }
        }

        // 7. Theme Mode
        SectionCard {
            CardTitle("Appearance", Icons.Default.Palette)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ThemeMode.entries.forEach { mode ->
                    FilterChip(
                        selected = settings.themeMode == mode,
                        onClick = { scope.launch { container.settingsRepository.setThemeMode(mode) } },
                        label = { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        shape = RoundedCornerShape(100.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        ),
                    )
                }
            }
        }

        // 8. Reset App Data
        SectionCard {
            CardTitle("Reset", Icons.Default.RestartAlt)
            Button(
                onClick = { showResetDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                ),
            ) {
                Text("Reset All Local Data & Settings")
            }
        }

        Spacer(Modifier.height(24.dp))
    }

    // Edit Name Dialog
    if (editingName) {
        AlertDialog(
            onDismissRequest = { editingName = false },
            title = { Text("Edit Name") },
            text = {
                OutlinedTextField(
                    value = tempName,
                    onValueChange = { tempName = it },
                    placeholder = { Text("Your name (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            container.settingsRepository.setUserName(tempName)
                            editingName = false
                        }
                    },
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingName = false }) {
                    Text("Cancel")
                }
            },
        )
    }

    // Edit Commitment Dialog
    if (editingCommitment) {
        AlertDialog(
            onDismissRequest = { editingCommitment = false },
            title = { Text("Edit Daily Commitment") },
            text = {
                OutlinedTextField(
                    value = tempCommitment,
                    onValueChange = { tempCommitment = it },
                    placeholder = { Text("e.g. Work 9am–5pm, College") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            container.settingsRepository.setDailyCommitment(tempCommitment)
                            editingCommitment = false
                        }
                    },
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingCommitment = false }) {
                    Text("Cancel")
                }
            },
        )
    }

    // Reset Confirm Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset What's the Plan?") },
            text = {
                Text("This will delete all local plans, check-ins, reflections, and settings on this device. This cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            container.resetAllLocalData()
                            showResetDialog = false
                            Toast.makeText(context, "App reset to default state.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ),
                ) {
                    Text("Reset Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}
