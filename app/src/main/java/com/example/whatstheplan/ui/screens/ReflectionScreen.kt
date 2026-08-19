package com.example.whatstheplan.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.whatstheplan.AppContainer
import com.example.whatstheplan.domain.model.UserSettings
import com.example.whatstheplan.ui.components.AtmospherePhase
import com.example.whatstheplan.ui.components.AtmosphericBackground
import com.example.whatstheplan.ui.components.PrimaryPillButton
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
    var mood by remember { mutableStateOf("Good") }
    var outcome by remember { mutableStateOf("Done") }
    var note by remember { mutableStateOf("") }

    val moodOptions = listOf("Good", "Okay", "Chaotic", "Could've been better")
    val outcomes = listOf(
        "Done" to "✓ Done",
        "Move to tomorrow" to "➡️ Move to tomorrow",
        "Make it smaller" to "🤏 Make it smaller",
        "Drop it" to "🛑 Drop it",
    )

    LaunchedEffect(existing?.id) {
        existing?.let {
            mood = it.mood.ifBlank { "Good" }
            outcome = it.completion.ifBlank { "Done" }
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

    AtmosphericBackground(phase = AtmospherePhase.EVENING) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            Spacer(Modifier.height(8.dp))

            // Conversational Headline
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "You made it.",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "How did today feel?",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Feeling Pill Selector
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                moodOptions.forEach { option ->
                    val isSelected = mood == option
                    FilterChip(
                        selected = isSelected,
                        onClick = { mood = option },
                        label = { Text(option, style = MaterialTheme.typography.bodyMedium) },
                        shape = RoundedCornerShape(100.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                    )
                }
            }

            // Today's Intention Card
            if (todayPlan != null && todayPlan!!.text.isNotBlank()) {
                SectionCard {
                    Text(
                        text = "TODAY'S INTENTION",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp,
                    )
                    Text(
                        text = "“${todayPlan!!.text}”",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            // Recovery / Outcome Selection
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Status & Adjustment",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
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
                                containerColor = MaterialTheme.colorScheme.surface,
                            ),
                        )
                    }
                }

                Text(
                    text = when (outcome) {
                        "Done" -> "Great follow-through today."
                        "Move to tomorrow" -> "Priorities adjust naturally. Tomorrow is a fresh slate."
                        "Make it smaller" -> "Shrinking an intention makes starting frictionless next time."
                        "Drop it" -> "Consciously letting go leaves mental space for what matters."
                        else -> "Rest well tonight."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Closing note
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Closing note (optional)") },
                placeholder = { Text("What did you notice about today?") },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                ),
                minLines = 2,
            )

            PrimaryPillButton(
                text = "Close Day Peacefully",
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
                            mood = mood,
                            completion = outcome,
                            note = note,
                        )
                        container.dailyPlanRepository.updateStatus(dbStatus)
                        container.userCorrectionRepository.addCorrection(
                            category = "RECOVERY",
                            note = "Evening reflection: $mood | Outcome: $outcome",
                            source = "LEARNED",
                        )
                        onDone()
                    }
                },
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}
