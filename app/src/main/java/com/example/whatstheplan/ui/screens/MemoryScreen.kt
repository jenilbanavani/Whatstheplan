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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Person
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
    val allCorrections by container.userCorrectionRepository.observeAll().collectAsStateWithLifecycle(emptyList())

    val userAddedMemory = allCorrections.filter { it.source == "USER" }
    val learnedMemory = allCorrections.filter { it.source != "USER" }

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
                        text = "What I Remember",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Transparent on-device records & rhythm.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // Privacy Guarantee
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
                        text = "Your routines and plans stay on this phone. You can inspect, edit, forget, or export everything.",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }

        // 1. ADDED BY YOU: Your Rhythm & Preferences
        item {
            SectionCard {
                CardTitle("Your Rhythm & Setup", Icons.Default.Person)
                Text(
                    text = "Added by you during setup and settings:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MemoryItemRow(label = "Name", value = settings.userName.ifBlank { "Not provided" })
                    MemoryItemRow(label = "Wake time", value = DateUtils.formatClock(settings.wakeTimeMinutes))
                    MemoryItemRow(label = "Sleep time", value = DateUtils.formatClock(settings.sleepTimeMinutes))
                    MemoryItemRow(label = "Fixed commitment", value = settings.dailyCommitment.ifBlank { "None set" })
                    MemoryItemRow(label = "Tone preference", value = "${settings.tonePreference.title} (${settings.tonePreference.description})")
                    MemoryItemRow(
                        label = "Morning check-in",
                        value = if (settings.morningReminderEnabled) "Enabled" else "Disabled",
                    )
                    MemoryItemRow(
                        label = "Follow-up prompt",
                        value = if (settings.followUpEnabled) "Enabled" else "Disabled",
                    )
                    MemoryItemRow(
                        label = "Evening reflection",
                        value = if (settings.eveningReflectionEnabled) "Enabled" else "Disabled",
                    )
                }
            }
        }

        // 2. ADDED BY YOU: Custom Memory Notes
        item {
            SectionCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CardTitle("Added by you", Icons.Default.Bookmark)
                    IconButton(onClick = { showAddCorrectionDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add custom memory", tint = MaterialTheme.colorScheme.primary)
                    }
                }
                Text(
                    text = "Explicit preferences or routine notes you asked the companion to remember.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                if (userAddedMemory.isEmpty()) {
                    Text(
                        text = "No custom notes added yet. Tap + to add an explicit memory note.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                } else {
                    userAddedMemory.forEach { item ->
                        MemoryCardRow(
                            item = item,
                            onEdit = {
                                editingCorrection = item
                                editCorrectionText = item.note
                            },
                            onForget = {
                                scope.launch { container.userCorrectionRepository.deleteCorrection(item.id) }
                            },
                        )
                    }
                }
            }
        }

        // 3. LEARNED FROM YOUR ACTIVITY
        item {
            SectionCard {
                CardTitle("Learned from your activity", Icons.Default.AutoAwesome)
                Text(
                    text = "Logged responses, timing preferences, and intention follow-throughs.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                if (learnedMemory.isEmpty()) {
                    Text(
                        text = "As you respond to prompts (Start, Move, Drop), learned interaction patterns will appear here.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                } else {
                    learnedMemory.take(15).forEach { item ->
                        MemoryCardRow(
                            item = item,
                            onEdit = {
                                editingCorrection = item
                                editCorrectionText = item.note
                            },
                            onForget = {
                                scope.launch { container.userCorrectionRepository.deleteCorrection(item.id) }
                            },
                        )
                    }
                }
            }
        }

        // 4. Stored Daily Intentions
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
                        text = "No daily intentions stored yet.",
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
                                text = if (plan.skipped) "Nothing ambitious today" else plan.text,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            if (plan.firstStep.isNotBlank()) {
                                Text(
                                    text = "Start: ${plan.firstStep}",
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
                            Icon(Icons.Default.Delete, contentDescription = "Forget this", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

        // 5. Actions: Export data & Delete all memory
        item {
            SectionCard {
                CardTitle("Memory Management", Icons.Default.FileDownload)
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
                    Text("Delete All Memory", modifier = Modifier.padding(start = 8.dp))
                }
            }
        }

        item {
            Spacer(Modifier.height(24.dp))
        }
    }

    // Add Memory Dialog
    if (showAddCorrectionDialog) {
        AlertDialog(
            onDismissRequest = { showAddCorrectionDialog = false },
            title = { Text("Add Memory Note") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Tell the companion a preference or routine to remember:")
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
                                    source = "USER",
                                )
                                newCorrectionNote = ""
                                showAddCorrectionDialog = false
                            }
                        }
                    },
                ) {
                    Text("Remember")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCorrectionDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }

    // Edit Memory Dialog
    if (editingCorrection != null) {
        val item = editingCorrection!!
        AlertDialog(
            onDismissRequest = { editingCorrection = null },
            title = { Text("Edit Memory") },
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

    // Delete All Memory Dialog
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("Delete all remembered data?") },
            text = {
                Text("This permanently erases all intentions, rhythm records, interaction history, and local memory from this device.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            container.resetAllLocalData()
                            showClearConfirmDialog = false
                            Toast.makeText(context, "All memory deleted.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ),
                ) {
                    Text("Delete All Memory")
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
private fun MemoryCardRow(
    item: UserCorrectionEntity,
    onEdit: () -> Unit,
    onForget: () -> Unit,
) {
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
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onForget) {
                    Icon(Icons.Default.Delete, contentDescription = "Forget this", modifier = Modifier.size(18.dp))
                }
            }
        }
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
