package com.example.whatstheplan.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.whatstheplan.data.local.repository.SettingsRepository
import com.example.whatstheplan.notifications.NotificationHelper
import com.example.whatstheplan.ui.components.CardTitle
import com.example.whatstheplan.ui.components.PillBadge
import com.example.whatstheplan.ui.components.PrimaryIconButton
import com.example.whatstheplan.ui.components.SectionCard
import com.example.whatstheplan.usage.UsageStatsReader
import kotlinx.coroutines.launch

@Composable
fun SetupScreen(
    settingsRepository: SettingsRepository,
    usageStatsReader: UsageStatsReader,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var notificationState by remember {
        mutableStateOf(NotificationHelper.canPostNotifications(context))
    }
    var usageAccess by remember { mutableStateOf(usageStatsReader.hasUsageAccess()) }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        notificationState = granted
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.WbSunny,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(28.dp),
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Welcome.",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Let's make phone use intentional.",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "Before you use your phone, decide what you want to use it for.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        SectionCard {
            CardTitle("How it works", Icons.Default.Shield)
            SetupRow(icon = Icons.Default.WbSunny, text = "Set an intentional plan every morning.")
            SetupRow(icon = Icons.Default.Schedule, text = "Gentle check-ins during your active day.")
            SetupRow(icon = Icons.Default.BarChart, text = "100% offline, local insights and history.")
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            PillBadge(text = "🔒 100% Offline")
            PillBadge(text = "🛡️ Zero Cloud")
            PillBadge(text = "✨ Pure Privacy")
        }

        SectionCard {
            CardTitle("Permissions", Icons.Default.Notifications)
            Text(
                text = "Notifications power hourly check-ins. Usage Access is optional for local screen-time graphs.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedButton(
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        notificationState = NotificationHelper.canPostNotifications(context)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            ) {
                Icon(Icons.Default.Notifications, contentDescription = null)
                Text(
                    text = if (notificationState) "✓ Notifications ready" else "Allow notifications",
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
            OutlinedButton(
                onClick = {
                    context.startActivity(usageStatsReader.usageAccessIntent())
                    usageAccess = usageStatsReader.hasUsageAccess()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            ) {
                Icon(Icons.Default.BarChart, contentDescription = null)
                Text(
                    text = if (usageAccess) "✓ Usage Access enabled" else "Optional: Usage Access settings",
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }

        PrimaryIconButton(
            text = "Start planning",
            icon = Icons.Default.ArrowForward,
            onClick = {
                scope.launch {
                    settingsRepository.setScreenTimeInsightsEnabled(usageStatsReader.hasUsageAccess())
                    settingsRepository.setSetupComplete(true)
                }
            },
        )
    }
}

@Composable
private fun SetupRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(16.dp),
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
