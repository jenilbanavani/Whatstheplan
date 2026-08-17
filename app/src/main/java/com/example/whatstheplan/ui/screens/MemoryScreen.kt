package com.example.whatstheplan.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.whatstheplan.AppContainer
import com.example.whatstheplan.data.local.BackupManager
import com.example.whatstheplan.data.local.database.entities.DailyPlanEntity
import com.example.whatstheplan.data.local.database.entities.UserCorrectionEntity
import com.example.whatstheplan.domain.model.UserSettings
import com.example.whatstheplan.ui.components.CardTitle
import com.example.whatstheplan.ui.components.PillBadge
import com.example.whatstheplan.ui.components.SectionCard
import com.example.whatstheplan.utils.DateUtils
import kotlinx.coroutines.launch

@Composable
fun MemoryScreen(
    container: AppContainer,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val settings by container.settingsRepository.settingsFlow.collectAsStateWithLifecycle(UserSettings())
    val plans by container.dailyPlanRepository.observeAll().collectAsStateWithLifecycle(emptyList())
    val corrections by container.userCorrectionRepository.observeAll().collectAsStateWithLifecycle(emptyList())

    var showAddCorrectionDialog by remember { mutableStateOf(false) }
    var newCorrectionNote by remember { mutableStateOf("") }
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    var editingCorrection by remember { mutableStateOf<UserCorrectionEntity?>(null) }
    var editCorrectionText by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Spacer(Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp),
                    )
                }
                Column {
                    Text(
                        text = "Local Memory",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Transparent on-device records & routines.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // Privacy Guarantee Card
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
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
                        text = "No data ever leaves this phone. You have complete control to view, edit, export, or delete everything here.",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }

        // Active Companion Profile / Preferences Memory
        item {
            SectionCard {
                CardTitle("Remembered Profile", Icons.Default.Psychology)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MemoryItemRow(label = "Name", value = settings.userName.ifBlank { "Not provided" })
                    MemoryItemRow(label = "Wake Time", value = DateUtils.formatClock(settings.wakeTimeMinutes))
                    MemoryItemRow(label = "Fixed Daily Commitment", value = settings.dailyCommitment.ifBlank { "None set" })
                    MemoryItemRow(label = "Tone Preference", value = "${settings.tonePreference.title} (${settings.tonePreference.description})")
                    MemoryItemRow(label = "Quiet Hours", value = "${DateUtils.formatClock(settings.quietHoursStartMinutes)} to ${DateUtils.formatClock(settings.quietHoursEndMinutes)}")
                }
            }
        }

        // User Corrections & Explicit Adjustments
        item {
            SectionCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CardTitle("Explicit Adjustments", Icons.Default.Bookmark)
                    IconButton(onClick = { showAddCorrectionDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add memory adjustment", tint = MaterialTheme.colorScheme.primary)
                    }
                }
                Text(
                    text = "Explicit user adjustments, interaction notes, or corrections the companion remembers.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                if (corrections.isEmpty()) {
                    Text(
                        text = "No explicit adjustments recorded yet. Tap + to add any preference note.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                } else {
                    corrections.forEach { item ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    PillBadge(
                                        text = item.category,
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = item.note,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                    )
                                }
                                Row {
                                    IconButton(
                                        onClick = {
                                            editingCorrection = item
                                            editCorrectionText = item.note
                                        },
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(
                                        onClick = {
                                            scope.launch { container.userCorrectionRepository.deleteCorrection(item.id) }
                                        },
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Stored Daily Intentions History
        item {
            Text(
                text = "Stored Intentions (${plans.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }

        if (plans.isEmpty()) {
            item {
                SectionCard {
                    Text(
                        text = "No daily intentions saved yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            items(plans, key = { it.id }) { plan ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = DateUtils.friendlyDate(plan.date),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                PillBadge(
                                    text = plan.status,
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = if (plan.skipped) "Skipped / relaxed" else plan.text,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            if (plan.firstStep.isNotBlank()) {
                                Text(
                                    text = "Step: ${plan.firstStep}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                scope.launch { container.dailyPlanRepository.deletePlan(plan.id) }
                            },
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

        // Export and Reset Actions
        item {
            SectionCard {
                CardTitle("Data Management", Icons.Default.FileDownload)
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            val json = BackupManager.exportToJson(container)
                            val ok = BackupManager.exportToDownloads(context, json)
                            val msg = if (ok) "Exported memory JSON to Downloads folder" else "Export completed"
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null)
                    Text("Export All Local Memory (JSON)", modifier = Modifier.padding(start = 8.dp))
                }

                Button(
                    onClick = { showClearConfirmDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    ),
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Text("Delete All Remembered Data", modifier = Modifier.padding(start = 8.dp))
                }
            }
        }

        item {
            Spacer(Modifier.height(24.dp))
        }
    }

    // Add Correction Dialog
    if (showAddCorrectionDialog) {
        AlertDialog(
            onDismissRequest = { showAddCorrectionDialog = false },
            title = { Text("Add Memory Adjustment") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Tell the companion an explicit habit or preference to remember:")
                    OutlinedTextField(
                        value = newCorrectionNote,
                        onValueChange = { newCorrectionNote = it },
                        placeholder = { Text("e.g. I prefer writing sessions before 11 AM") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCorrectionNote.isNotBlank()) {
                            scope.launch {
                                container.userCorrectionRepository.addCorrection(
                                    category = "PREFERENCE",
                                    note = newCorrectionNote,
                                )
                                newCorrectionNote = ""
                                showAddCorrectionDialog = false
                            }
                        }
                    },
                ) {
                    Text("Save to Memory")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCorrectionDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }

    // Edit Correction Dialog
    if (editingCorrection != null) {
        val item = editingCorrection!!
        AlertDialog(
            onDismissRequest = { editingCorrection = null },
            title = { Text("Edit Adjustment") },
            text = {
                OutlinedTextField(
                    value = editCorrectionText,
                    onValueChange = { editCorrectionText = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            container.userCorrectionRepository.updateCorrection(
                                item.copy(note = editCorrectionText),
                            )
                            editingCorrection = null
                        }
                    },
                ) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingCorrection = null }) {
                    Text("Cancel")
                }
            },
        )
    }

    // Clear All Confirm Dialog
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("Delete all remembered data?") },
            text = {
                Text("This will permanently clear all intentions, routines, check-in history, and local memory from this device. This cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            container.resetAllLocalData()
                            showClearConfirmDialog = false
                            Toast.makeText(context, "All local memory cleared.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ),
                ) {
                    Text("Clear Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun MemoryItemRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
