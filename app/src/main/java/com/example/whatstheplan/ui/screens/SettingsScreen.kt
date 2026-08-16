package com.example.whatstheplan.ui.screens

import android.os.Build
import android.os.PowerManager
import android.provider.Settings as AndroidSettings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.whatstheplan.AppContainer
import com.example.whatstheplan.data.local.BackupManager
import com.example.whatstheplan.domain.model.ThemeMode
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
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settings by container.settingsRepository.settingsFlow.collectAsStateWithLifecycle(UserSettings())
    var showResetDialog by remember { mutableStateOf(false) }
    val usageAccess = container.usageStatsReader.hasUsageAccess()
    val notificationsEnabled = NotificationHelper.canPostNotifications(context)

    val powerManager = context.getSystemService(android.content.Context.POWER_SERVICE) as? PowerManager
    val isIgnoringBatteryOptimizations = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        powerManager?.isIgnoringBatteryOptimizations(context.packageName) == true
    } else true

    // Export file launcher
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val json = BackupManager.exportToJson(container)
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(json.toByteArray())
                }
                Toast.makeText(context, "Backup exported successfully", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Import file launcher
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val json = context.contentResolver.openInputStream(uri)?.use { stream ->
                    stream.bufferedReader().readText()
                }
                if (json != null) {
                    val result = BackupManager.importFromJson(container, json)
                    if (result.isSuccess) {
                        Toast.makeText(
                            context,
                            "Imported ${result.getOrDefault(0)} records",
                            Toast.LENGTH_SHORT,
                        ).show()
                    } else {
                        Toast.makeText(context, "Failed to import backup file", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(Modifier.height(4.dp))
        Text("Settings", style = MaterialTheme.typography.headlineLarge)
        Text(
            text = "Configure reminders, appearance, and local storage.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // Privacy Promise Hero
        SectionCard(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
        ) {
            CardTitle("100% Offline & Private", Icons.Default.Security)
            Text(
                text = "Your data never leaves this device. No cloud sync, no tracking, no external servers.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        // Section 1: Notifications & Reminders
        SectionCard {
            CardTitle("Notifications & Reminders", Icons.Default.Notifications)
            SettingSwitchRow(
                title = "Morning reminder",
                subtitle = "Prompts you on your first unlock of the morning.",
                checked = settings.morningReminderEnabled,
                onCheckedChange = {
                    scope.launch { container.settingsRepository.setMorningReminderEnabled(it) }
                },
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            SettingSwitchRow(
                title = "Daytime check-ins",
                subtitle = if (notificationsEnabled) {
                    "Gentle prompts during active hours."
                } else {
                    "System notifications are blocked."
                },
                checked = settings.checkInsEnabled,
                onCheckedChange = {
                    scope.launch { container.settingsRepository.setCheckInsEnabled(it) }
                },
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            SettingSwitchRow(
                title = "Strict / Exact Alarms",
                subtitle = "Uses exact device alarms for to-the-minute intervals.",
                checked = settings.exactAlarmsEnabled,
                onCheckedChange = {
                    scope.launch { container.settingsRepository.setExactAlarmsEnabled(it) }
                },
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            StepperRow(
                title = "Check-in interval",
                value = "${settings.checkInIntervalMinutes} min",
                onDecrease = {
                    scope.launch {
                        container.settingsRepository.setCheckInIntervalMinutes(
                            settings.checkInIntervalMinutes - 15,
                        )
                    }
                },
                onIncrease = {
                    scope.launch {
                        container.settingsRepository.setCheckInIntervalMinutes(
                            settings.checkInIntervalMinutes + 15,
                        )
                    }
                },
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            StepperRow(
                title = "Active start",
                value = DateUtils.formatClock(settings.activeStartMinutes),
                onDecrease = {
                    scope.launch {
                        container.settingsRepository.setActiveStartMinutes(
                            settings.activeStartMinutes - 30,
                        )
                    }
                },
                onIncrease = {
                    scope.launch {
                        container.settingsRepository.setActiveStartMinutes(
                            settings.activeStartMinutes + 30,
                        )
                    }
                },
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            StepperRow(
                title = "Active end",
                value = DateUtils.formatClock(settings.activeEndMinutes),
                onDecrease = {
                    scope.launch {
                        container.settingsRepository.setActiveEndMinutes(
                            settings.activeEndMinutes - 30,
                        )
                    }
                },
                onIncrease = {
                    scope.launch {
                        container.settingsRepository.setActiveEndMinutes(
                            settings.activeEndMinutes + 30,
                        )
                    }
                },
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            SettingSwitchRow(
                title = "Notification sound",
                subtitle = "Play default sound on check-in arrival.",
                checked = settings.notificationSound,
                onCheckedChange = {
                    scope.launch { container.settingsRepository.setNotificationSound(it) }
                },
            )
        }

        // Section 2: Reliability & Battery Optimization
        SectionCard {
            CardTitle("Background Reliability", Icons.Default.BatterySaver)
            Text(
                text = if (isIgnoringBatteryOptimizations) {
                    "✓ Battery optimizations are disabled for this app. Check-ins will run reliably."
                } else {
                    "Some Android phones (Samsung, Xiaomi, OnePlus) aggressively stop background reminders. Allow unrestricted battery usage for exact timing."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (!isIgnoringBatteryOptimizations && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                OutlinedButton(
                    onClick = {
                        val intent = android.content.Intent(AndroidSettings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(Icons.Default.BatterySaver, contentDescription = null)
                    Text("Disable Battery Restrictions", modifier = Modifier.padding(start = 8.dp))
                }
            }
        }

        // Section 3: Appearance & Feel
        SectionCard {
            CardTitle("Appearance", Icons.Default.Palette)
            Text("Color theme", style = MaterialTheme.typography.titleMedium)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ThemeMode.entries.forEach { mode ->
                    FilterChip(
                        selected = settings.themeMode == mode,
                        onClick = { scope.launch { container.settingsRepository.setThemeMode(mode) } },
                        label = { Text(mode.label) },
                        shape = RoundedCornerShape(100.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    )
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            SettingSwitchRow(
                title = "Fun facts",
                subtitle = "Show calm interesting facts after logging.",
                checked = settings.funFactsEnabled,
                onCheckedChange = {
                    scope.launch { container.settingsRepository.setFunFactsEnabled(it) }
                },
            )
        }

        // Section 4: Screen Time Insights
        SectionCard {
            CardTitle("Screen Time", Icons.Default.PhoneAndroid)
            Text(
                text = if (usageAccess) {
                    "Usage Access granted. Daily screen summaries are computed and saved locally."
                } else {
                    "Enable Usage Access to view approximate on-device screen time."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            SettingSwitchRow(
                title = "Screen-time insights",
                subtitle = "Read local app usage statistics.",
                checked = settings.screenTimeInsightsEnabled,
                onCheckedChange = {
                    scope.launch { container.settingsRepository.setScreenTimeInsightsEnabled(it) }
                    if (it && !usageAccess) {
                        context.startActivity(container.usageStatsReader.usageAccessIntent())
                    }
                },
            )
            if (!usageAccess) {
                OutlinedButton(
                    onClick = { context.startActivity(container.usageStatsReader.usageAccessIntent()) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text("Open Usage Access Settings")
                }
            }
        }

        // Section 5: Local Data Backup & Restore
        SectionCard {
            CardTitle("Data & Backup", Icons.Default.SaveAlt)
            Text(
                text = "Export your entire offline history to a JSON file or restore from a previous backup.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedButton(
                onClick = { exportLauncher.launch("whatstheplan_backup.json") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            ) {
                Icon(Icons.Default.SaveAlt, contentDescription = null)
                Text("Export Backup (JSON)", modifier = Modifier.padding(start = 8.dp))
            }
            OutlinedButton(
                onClick = { importLauncher.launch(arrayOf("application/json", "text/*")) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            ) {
                Icon(Icons.Default.Download, contentDescription = null)
                Text("Restore Backup (JSON)", modifier = Modifier.padding(start = 8.dp))
            }
        }

        // Section 6: About & Privacy
        SectionCard {
            CardTitle("About", Icons.Default.Info)
            Text(
                text = "What's the Plan? v1.1.0\nA quiet digital-wellbeing companion.",
                style = MaterialTheme.typography.bodyMedium,
            )
            OutlinedButton(
                onClick = onPrivacy,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            ) {
                Icon(Icons.Default.PrivacyTip, contentDescription = null)
                Text("Privacy Details", modifier = Modifier.padding(start = 8.dp))
            }
            OutlinedButton(
                onClick = { showResetDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Icon(Icons.Default.RestartAlt, contentDescription = null)
                Text("Reset All Data", modifier = Modifier.padding(start = 8.dp))
            }
        }

        Spacer(Modifier.height(16.dp))
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset all data?") },
            text = { Text("This will permanently clear all local plans, check-ins, reflections, and settings on this phone.") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            container.resetAllLocalData()
                            showResetDialog = false
                            Toast.makeText(context, "All data reset", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
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

@Composable
private fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun StepperRow(
    title: String,
    value: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
            Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDecrease) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease $title")
            }
            IconButton(onClick = onIncrease) {
                Icon(Icons.Default.Add, contentDescription = "Increase $title")
            }
        }
    }
}
