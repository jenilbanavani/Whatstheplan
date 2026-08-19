package com.example.whatstheplan.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.whatstheplan.AppContainer
import com.example.whatstheplan.data.local.BackupManager
import com.example.whatstheplan.data.local.database.entities.UserCorrectionEntity
import com.example.whatstheplan.domain.model.UserSettings
import com.example.whatstheplan.ui.components.AtmospherePhase
import com.example.whatstheplan.ui.components.AtmosphericBackground
import com.example.whatstheplan.ui.components.CardTitle
import com.example.whatstheplan.ui.components.PillBadge
import com.example.whatstheplan.ui.components.PrimaryPillButton
import com.example.whatstheplan.ui.components.SecondaryPillButton
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
    val allCorrections by container.userCorrectionRepository.observeAll().collectAsStateWithLifecycle(emptyList())

    val userAddedMemory = allCorrections.filter { it.source == "USER" }
    val learnedMemory = allCorrections.filter { it.source != "USER" }

    var showAddCorrectionDialog by remember { mutableStateOf(false) }
    var newCorrectionNote by remember { mutableStateOf("") }
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    var editingCorrection by remember { mutableStateOf<UserCorrectionEntity?>(null) }
    var editCorrectionText by remember { mutableStateOf("") }

    AtmosphericBackground(phase = AtmospherePhase.AUTO) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item {
                Spacer(Modifier.height(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "What I Remember",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "Transparent on-device records & daily rhythm.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Privacy Guarantee
            item {
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
                            text = "Your routines and plans stay on this phone. You can inspect, edit, forget, or export everything.",
                            style = MaterialTheme.typography.bodySmall,
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
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
                        text = "Logged responses, timing preferences, and engagement signals.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    // Current Engagement State Pill
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "Current State",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                PillBadge(
                                    text = settings.engagementLevel.title,
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = settings.engagementLevel.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    if (learnedMemory.isEmpty()) {
                        Text(
                            text = "As you respond to prompts (Start, Move, Drop), learned interaction patterns will appear here.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        )
                    } else {
                        learnedMemory.forEach { item ->
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

            // 4. Data Controls: Export & Delete All Memory
            item {
                SectionCard {
                    Text(
                        text = "DATA & MEMORY CONTROLS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp,
                    )

                    PrimaryPillButton(
                        text = "Export All Local Memory (JSON)",
                        icon = Icons.Default.Download,
                        onClick = {
                            scope.launch {
                                try {
                                    val json = BackupManager.exportToJson(container)
                                    val sendIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                        putExtra(android.content.Intent.EXTRA_TEXT, json)
                                        type = "application/json"
                                    }
                                    val shareIntent = android.content.Intent.createChooser(sendIntent, "Export Local Memory JSON")
                                    shareIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(shareIntent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                    )

                    SecondaryPillButton(
                        text = "Delete All Memory & Learned Data",
                        icon = Icons.Default.DeleteOutline,
                        onClick = { showClearConfirmDialog = true },
                    )
                }
            }

            item {
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    // Add Memory Dialog
    if (showAddCorrectionDialog) {
        AlertDialog(
            onDismissRequest = { showAddCorrectionDialog = false },
            title = { Text("Add Memory Note", style = MaterialTheme.typography.titleLarge) },
            text = {
                OutlinedTextField(
                    value = newCorrectionNote,
                    onValueChange = { newCorrectionNote = it },
                    placeholder = { Text("e.g. Always keep Friday mornings for research") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCorrectionNote.isNotBlank()) {
                            scope.launch {
                                container.userCorrectionRepository.addCorrection(
                                    category = "CUSTOM",
                                    note = newCorrectionNote.trim(),
                                    source = "USER",
                                )
                                newCorrectionNote = ""
                                showAddCorrectionDialog = false
                            }
                        }
                    },
                    shape = RoundedCornerShape(100.dp),
                ) {
                    Text("Add Memory")
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
        AlertDialog(
            onDismissRequest = { editingCorrection = null },
            title = { Text("Edit Memory Note", style = MaterialTheme.typography.titleLarge) },
            text = {
                OutlinedTextField(
                    value = editCorrectionText,
                    onValueChange = { editCorrectionText = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        editingCorrection?.let { correction ->
                            scope.launch {
                                container.userCorrectionRepository.updateCorrection(
                                    correction.copy(note = editCorrectionText.trim()),
                                )
                                editingCorrection = null
                            }
                        }
                    },
                    shape = RoundedCornerShape(100.dp),
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingCorrection = null }) {
                    Text("Cancel")
                }
            },
        )
    }

    // Delete All Confirmation Dialog
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("Delete All Memory?", style = MaterialTheme.typography.titleLarge) },
            text = {
                Text("This permanently deletes all logged routines, intentions, and learned preferences from this device. This cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            container.userCorrectionRepository.deleteAll()
                            showClearConfirmDialog = false
                            Toast.makeText(context, "All memory deleted", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(100.dp),
                ) {
                    Text("Delete Everything")
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
private fun MemoryItemRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
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
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.note,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = item.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onForget, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Forget", modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
