package com.example.whatstheplan.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.whatstheplan.AppContainer
import com.example.whatstheplan.domain.model.UserSettings
import com.example.whatstheplan.ui.components.PrimaryGlowButton
import com.example.whatstheplan.ui.components.SectionCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReflectionScreen(
    container: AppContainer,
    onDone: () -> Unit,
) {
    val existing by container.dailyReflectionRepository.observeToday().collectAsStateWithLifecycle(null)
    val todayPlan by container.dailyPlanRepository.observeToday().collectAsStateWithLifecycle(null)
    val settings by container.settingsRepository.settingsFlow.collectAsStateWithLifecycle(UserSettings())

    val scope = rememberCoroutineScope()
    var completionStatus by remember { mutableStateOf("Done") }
    var mood by remember { mutableStateOf("🙂 Good") }
    var note by remember { mutableStateOf("") }

    val statuses = listOf(
        "Done" to "✓ Accomplished intention",
        "Moved" to "➡️ Shifted to later",
        "Dropped" to "🛑 Consciously dropped",
    )
    val moods = listOf("🌿 Calm", "🙂 Good", "⚡ Productive", "😴 Tired", "🌊 Busy")

    val tone = settings.tonePreference
    val prompt = tone.eveningPrompt()

    LaunchedEffect(existing?.id) {
        existing?.let {
            mood = it.mood
            completionStatus = it.completion
            note = it.note
        } ?: run {
            todayPlan?.let {
                when (it.status) {
                    "DONE" -> completionStatus = "Done"
                    "MOVED" -> completionStatus = "Moved"
                    "DROPPED" -> completionStatus = "Dropped"
                    else -> completionStatus = "Done"
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Spacer(Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.NightlightRound,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(28.dp),
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Evening Recovery",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "Close out today peacefully",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = prompt,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (todayPlan != null && todayPlan!!.text.isNotBlank()) {
            SectionCard {
                Text(
                    text = "TODAY'S INTENTION",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "“${todayPlan!!.text}”",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        // Status: Done, Moved, or Dropped
        Text(
            text = "Outcome",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            statuses.forEach { (statusKey, label) ->
                FilterChip(
                    selected = completionStatus == statusKey,
                    onClick = { completionStatus = statusKey },
                    label = { Text(label, style = MaterialTheme.typography.bodyMedium) },
                    shape = RoundedCornerShape(100.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    ),
                )
            }
        }

        // Tone feedback on outcome
        Text(
            text = tone.statusFeedback(completionStatus.uppercase()),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // Mood
        Text(
            text = "How did you feel overall?",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            moods.forEach { item ->
                FilterChip(
                    selected = mood == item,
                    onClick = { mood = item },
                    label = { Text(item, style = MaterialTheme.typography.bodyMedium) },
                    shape = RoundedCornerShape(100.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    ),
                )
            }
        }

        // Optional Closing Note
        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Closing note (optional)") },
            placeholder = { Text("What did you learn today? What's for tomorrow?") },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            ),
            minLines = 3,
        )

        PrimaryGlowButton(
            text = "Complete Evening Recovery",
            icon = Icons.Default.Check,
            onClick = {
                scope.launch {
                    val dbStatus = when (completionStatus) {
                        "Done" -> "DONE"
                        "Moved" -> "MOVED"
                        "Dropped" -> "DROPPED"
                        else -> "DONE"
                    }
                    container.dailyReflectionRepository.saveReflection(mood, completionStatus, note)
                    container.dailyPlanRepository.updateStatus(dbStatus)
                    container.userCorrectionRepository.addCorrection(
                        category = "RECOVERY",
                        note = "Evening recovery completed: $completionStatus. Mood: $mood. Note: $note",
                    )
                    onDone()
                }
            },
        )

        Spacer(Modifier.height(16.dp))
    }
}
