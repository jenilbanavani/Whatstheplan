package com.example.whatstheplan.ui.screens

import android.app.TimePickerDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.whatstheplan.data.local.repository.SettingsRepository
import com.example.whatstheplan.domain.model.TonePreference
import com.example.whatstheplan.notifications.NotificationHelper
import com.example.whatstheplan.ui.components.AtmospherePhase
import com.example.whatstheplan.ui.components.AtmosphericBackground
import com.example.whatstheplan.ui.components.PrimaryPillButton
import com.example.whatstheplan.ui.components.SectionCard
import com.example.whatstheplan.utils.DateUtils
import kotlinx.coroutines.launch

@Composable
fun SetupScreen(
    settingsRepository: SettingsRepository,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var wakeTimeMinutes by remember { mutableIntStateOf(7 * 60 + 30) } // 7:30 AM
    var sleepTimeMinutes by remember { mutableIntStateOf(23 * 60 + 30) } // 11:30 PM
    var fixedCommitment by remember { mutableStateOf("") }
    var selectedTone by remember { mutableStateOf(TonePreference.CALM) }
    var enableNotifications by remember { mutableStateOf(true) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        enableNotifications = granted
    }

    val wakePicker = remember(wakeTimeMinutes) {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                wakeTimeMinutes = hourOfDay * 60 + minute
            },
            wakeTimeMinutes / 60,
            wakeTimeMinutes % 60,
            false,
        )
    }

    val sleepPicker = remember(sleepTimeMinutes) {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                sleepTimeMinutes = hourOfDay * 60 + minute
            },
            sleepTimeMinutes / 60,
            sleepTimeMinutes % 60,
            false,
        )
    }

    AtmosphericBackground(phase = AtmospherePhase.MORNING) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            Spacer(Modifier.height(8.dp))

            // Title
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "What's the Plan?",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "A quiet, local-first day companion that remembers your rhythm and helps you follow through on one thing.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Privacy Guarantee Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp),
                    )
                    Text(
                        text = "Your routines and plans stay on this phone. No accounts, no cloud sync, no tracking.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            // 1. Name
            SectionCard {
                Text(
                    text = "What should I call you?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Your name (optional)") },
                    placeholder = { Text("e.g. Jenil") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                    ),
                )
            }

            // 2. Daily Rhythm (Wake time & Sleep time)
            SectionCard {
                Text(
                    text = "Your Daily Rhythm",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text("Wake time", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text(DateUtils.formatClock(wakeTimeMinutes), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    }
                    OutlinedButton(onClick = { wakePicker.show() }, shape = RoundedCornerShape(100.dp)) {
                        Icon(Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text(" Set", modifier = Modifier.padding(start = 4.dp))
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text("Sleep time", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text(DateUtils.formatClock(sleepTimeMinutes), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
                    }
                    OutlinedButton(onClick = { sleepPicker.show() }, shape = RoundedCornerShape(100.dp)) {
                        Icon(Icons.Default.Bedtime, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text(" Set", modifier = Modifier.padding(start = 4.dp))
                    }
                }
            }

            // 3. One Fixed Daily Commitment
            SectionCard {
                Text(
                    text = "One Fixed Daily Commitment",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = "A primary daily block so the companion knows when you are busy.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = fixedCommitment,
                    onValueChange = { fixedCommitment = it },
                    label = { Text("e.g. College 9am–3pm, Work 9am–5pm") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                    ),
                )
            }

            // 4. Preferred Companion Tone
            SectionCard {
                Text(
                    text = "Companion Tone",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                )
                TonePreference.entries.forEach { tone ->
                    val isSelected = selectedTone == tone
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedTone = tone },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            if (isSelected) 2.dp else 1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        ),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = tone.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
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

            // 5. Notifications
            SectionCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Calm Daily Prompts",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = "Max 1 morning prompt + 1 follow-up. Zero hourly nagging.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = enableNotifications,
                        onCheckedChange = { isChecked ->
                            enableNotifications = isChecked
                            if (isChecked && !NotificationHelper.canPostNotifications(context)) {
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                    permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                }
                            }
                        },
                    )
                }
            }

            // Finish Button
            PrimaryPillButton(
                text = "Get Started",
                onClick = {
                    scope.launch {
                        settingsRepository.setUserName(name)
                        settingsRepository.setWakeTimeMinutes(wakeTimeMinutes)
                        settingsRepository.setSleepTimeMinutes(sleepTimeMinutes)
                        settingsRepository.setDailyCommitment(fixedCommitment)
                        settingsRepository.setTonePreference(selectedTone)
                        settingsRepository.setMorningReminderEnabled(enableNotifications)
                        settingsRepository.setFollowUpEnabled(enableNotifications)
                        settingsRepository.setEveningReflectionEnabled(enableNotifications)
                        settingsRepository.setSetupComplete(true)
                    }
                },
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}
