package com.example.whatstheplan.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.whatstheplan.data.local.repository.DailyPlanRepository
import com.example.whatstheplan.data.local.repository.FunFactRepository
import com.example.whatstheplan.data.local.repository.SettingsRepository
import com.example.whatstheplan.domain.model.FunFact
import com.example.whatstheplan.domain.model.UserSettings
import com.example.whatstheplan.ui.components.AtmospherePhase
import com.example.whatstheplan.ui.components.AtmosphericBackground
import com.example.whatstheplan.ui.components.PillBadge
import com.example.whatstheplan.ui.components.PrimaryPillButton
import com.example.whatstheplan.ui.components.SecondaryPillButton
import com.example.whatstheplan.ui.components.SectionCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MorningScreen(
    planRepository: DailyPlanRepository,
    funFactRepository: FunFactRepository,
    settingsRepository: SettingsRepository? = null,
    onDone: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val settings by settingsRepository?.settingsFlow?.collectAsStateWithLifecycle(UserSettings())
        ?: remember { mutableStateOf(UserSettings()) }

    var planText by remember { mutableStateOf("") }
    var firstStep by remember { mutableStateOf("") }
    var selectedSuggestion by remember { mutableStateOf<String?>("Study") }
    var isCustomizing by remember { mutableStateOf(false) }
    var savedFact by remember { mutableStateOf<FunFact?>(null) }

    val suggestions = listOf(
        "Study" to "Open textbook / notes (Physics Chapter 4)",
        "Project" to "Write 3 key bullet points",
        "Exercise" to "Put on workout shoes & stretch",
        "Life admin" to "Clear 1 pending email",
        "Rest" to "Take 10 minutes screen-free",
        "Something else" to "Decide the very first 2 minutes",
    )

    val userName = settings.userName.ifBlank { "there" }
    val headlineGreeting = "Morning, $userName 👋"

    AtmosphericBackground(phase = AtmospherePhase.MORNING) {
        AnimatedContent(
            targetState = savedFact,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "morning_fact_transition",
        ) { fact ->
            if (fact == null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Spacer(Modifier.height(16.dp))

                    // Conversational Content Block (Direct Match to HTML Reference)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text(
                            text = headlineGreeting,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )

                        val activeIntention = planText.ifBlank { selectedSuggestion ?: "studying" }
                        Text(
                            text = "You've got 1 key focus planned today. Your biggest priority is $activeIntention.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 26.sp,
                        )

                        Text(
                            text = "Want me to build the day around it?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )

                        // Option to expand intention customization
                        AnimatedVisibility(visible = isCustomizing) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                            ) {
                                Text(
                                    text = "Choose your focus:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                )

                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    suggestions.forEach { (category, defaultStep) ->
                                        val isSelected = selectedSuggestion == category
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = {
                                                selectedSuggestion = category
                                                if (planText.isBlank() && category != "Something else") {
                                                    planText = category
                                                }
                                                if (firstStep.isBlank()) {
                                                    firstStep = defaultStep
                                                }
                                            },
                                            label = { Text(category, style = MaterialTheme.typography.bodyMedium) },
                                            shape = RoundedCornerShape(100.dp),
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            ),
                                        )
                                    }
                                }

                                OutlinedTextField(
                                    value = planText,
                                    onValueChange = { planText = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Specific intention") },
                                    placeholder = { Text("e.g. Study Physics Chapter 4") },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                                    ),
                                    singleLine = true,
                                )

                                OutlinedTextField(
                                    value = firstStep,
                                    onValueChange = { firstStep = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Smallest start (first 2 min)") },
                                    placeholder = { Text("e.g. Open textbook and notes") },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                                    ),
                                    singleLine = true,
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(32.dp))

                    // Action Controls (Pill Action Buttons Zone)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        PrimaryPillButton(
                            text = "Let's do it",
                            onClick = {
                                val text = planText.ifBlank { selectedSuggestion ?: "Study" }
                                val step = firstStep.ifBlank { "Open textbook / notes" }
                                scope.launch {
                                    planRepository.savePlan(text = text, firstStep = step)
                                    val nextFact = funFactRepository.nextFactOrNull()
                                    if (nextFact == null) onDone() else savedFact = nextFact
                                }
                            },
                        )

                        SecondaryPillButton(
                            text = if (isCustomizing) "Nothing ambitious today" else "I'll plan myself",
                            onClick = {
                                if (!isCustomizing) {
                                    isCustomizing = true
                                } else {
                                    scope.launch {
                                        planRepository.savePlan(text = "Nothing ambitious today", skipped = true)
                                        onDone()
                                    }
                                }
                            },
                        )
                    }
                }
            } else {
                // Post-Intention Moment Card
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(28.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    SectionCard(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = MaterialTheme.colorScheme.surface,
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            PillBadge(
                                text = "💡 Quick thought",
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                            Text(
                                text = fact.category,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        Text(
                            text = fact.text,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 26.sp,
                        )

                        Text(
                            text = "Your intention is set. Go make it happen.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        PrimaryPillButton(
                            text = "Open Today's Plan",
                            onClick = onDone,
                        )
                    }
                }
            }
        }
    }
}
