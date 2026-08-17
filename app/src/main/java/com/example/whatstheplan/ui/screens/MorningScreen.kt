package com.example.whatstheplan.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.whatstheplan.data.local.repository.DailyPlanRepository
import com.example.whatstheplan.data.local.repository.FunFactRepository
import com.example.whatstheplan.data.local.repository.SettingsRepository
import com.example.whatstheplan.domain.model.FunFact
import com.example.whatstheplan.domain.model.UserSettings
import com.example.whatstheplan.ui.components.PillBadge
import com.example.whatstheplan.ui.components.PrimaryGlowButton
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
    var selectedSuggestion by remember { mutableStateOf<String?>(null) }
    var savedFact by remember { mutableStateOf<FunFact?>(null) }

    val suggestions = listOf(
        "Study" to "Open textbook / notes",
        "Project" to "Write three bullet points",
        "Exercise" to "Put on shoes & stretch",
        "Life admin" to "Clear one pending email / task",
        "Rest" to "Take 10 minutes of quiet screen-free time",
        "Something else" to "Decide the very first 2 minutes",
    )

    val tone = settings.tonePreference
    val greeting = tone.morningGreeting(settings.userName)

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
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Spacer(Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .size(52.dp)
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

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Your one thing today",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = greeting,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "Pick one realistic intention. Low friction > many features.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                // Suggestions Chips
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    suggestions.forEach { (category, defaultStep) ->
                        val isSelected = selectedSuggestion == category
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (isSelected) {
                                    selectedSuggestion = null
                                } else {
                                    selectedSuggestion = category
                                    if (planText.isBlank() && category != "Something else") {
                                        planText = category
                                    }
                                    if (firstStep.isBlank()) {
                                        firstStep = defaultStep
                                    }
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

                // 1. Natural Intention Text Input
                OutlinedTextField(
                    value = planText,
                    onValueChange = { planText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Today's one intention") },
                    placeholder = { Text("e.g. Finish project outline, read chapter 3") },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    ),
                    minLines = 2,
                )

                // 2. Smallest First Step
                OutlinedTextField(
                    value = firstStep,
                    onValueChange = { firstStep = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Smallest start (first 2 minutes)") },
                    placeholder = { Text("e.g. Write three bullet points") },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    ),
                    singleLine = true,
                )

                PrimaryGlowButton(
                    text = "Set Intention",
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    enabled = planText.isNotBlank() || selectedSuggestion != null,
                    onClick = {
                        val text = planText.ifBlank { selectedSuggestion.orEmpty() }
                        val step = firstStep.ifBlank { "Take 2 minutes to start" }
                        scope.launch {
                            planRepository.savePlan(text = text, firstStep = step)
                            val nextFact = funFactRepository.nextFactOrNull()
                            if (nextFact == null) onDone() else savedFact = nextFact
                        }
                    },
                )

                OutlinedButton(
                    onClick = {
                        scope.launch {
                            planRepository.savePlan(text = "Nothing ambitious today", skipped = true)
                            onDone()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text("Nothing ambitious today")
                }
            }
        } else {
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
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Normal),
                        textAlign = TextAlign.Start,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Text(
                        text = "Your intention is set. Go make it happen.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Button(
                        onClick = onDone,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        ),
                    ) {
                        Text("Open Today's Plan", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}
