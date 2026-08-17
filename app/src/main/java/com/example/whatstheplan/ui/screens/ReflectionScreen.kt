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
    var outcome by remember { mutableStateOf("Done") }
    var note by remember { mutableStateOf("") }

    val outcomes = listOf(
        "Done" to "✓ Done",
        "Move to tomorrow" to "➡️ Move to tomorrow",
        "Make it smaller" to "🤏 Make it smaller",
        "Drop it" to "🛑 Drop it",
    )

    val tone = settings.tonePreference
    val intentionText = todayPlan?.text.orEmpty()
    val prompt = tone.eveningPrompt(intentionText)

    LaunchedEffect(existing?.id) {
        existing?.let {
            outcome = it.completion
            note = it.note
        } ?: run {
            todayPlan?.let {
                when (it.status) {
                    "DONE" -> outcome = "Done"
                    "MOVED" -> outcome = "Move to tomorrow"
                    "DROPPED" -> outcome = "Drop it"
                    else -> outcome = "Done"
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
                text = "Evening Reflection",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "Close out today",
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

        // Recovery Actions: Done, Move to tomorrow, Make it smaller, Drop it
        Text(
            text = "Outcome",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            outcomes.forEach { (key, label) ->
                FilterChip(
                    selected = outcome == key,
                    onClick = { outcome = key },
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

        // Low-guilt recovery guidance
        Text(
            text = when (outcome) {
                "Done" -> "Great follow-through today."
                "Move to tomorrow" -> "Priorities adjust naturally. Tomorrow is a fresh start."
                "Make it smaller" -> "Shrinking an intention makes starting frictionless next time."
                "Drop it" -> "Consciously letting go leaves mental space for what matters."
                else -> ""
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // Optional Closing Note
        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Closing note (optional)") },
            placeholder = { Text("What did you notice today?") },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            ),
            minLines = 3,
        )

        PrimaryGlowButton(
            text = "Save Reflection",
            icon = Icons.Default.Check,
            onClick = {
                scope.launch {
                    val dbStatus = when (outcome) {
                        "Done" -> "DONE"
                        "Move to tomorrow" -> "MOVED"
                        "Make it smaller" -> "ACTIVE"
                        "Drop it" -> "DROPPED"
                        else -> "DONE"
                    }
                    container.dailyReflectionRepository.saveReflection(
                        mood = outcome,
                        completion = outcome,
                        note = note,
                    )
                    container.dailyPlanRepository.updateStatus(dbStatus)
                    container.userCorrectionRepository.addCorrection(
                        category = "RECOVERY",
                        note = "Evening recovery: $outcome. Note: $note",
                        source = "LEARNED",
                    )
                    onDone()
                }
            },
        )

        Spacer(Modifier.height(16.dp))
    }
}
