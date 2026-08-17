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
import com.example.whatstheplan.domain.model.TonePreference
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
    var selectedPrompt by remember { mutableStateOf<String?>(null) }
    var savedFact by remember { mutableStateOf<FunFact?>(null) }

    val prompts = listOf(
        "📚 Study" to "Read first 2 pages",
        "💻 Deep Work" to "Open project & write 1 task",
        "🏃 Exercise" to "Put on workout shoes",
        "🛠 Build something" to "Sketch the initial outline",
        "🧘 Unwind" to "Take 5 deep breaths",
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
                        text = "Daily Intention",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "What's the one thing?",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = greeting,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                // Quick Intention Chips
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    prompts.forEach { (prompt, defaultStep) ->
                        val cleanPrompt = prompt.substringAfter(" ")
                        val isSelected = selectedPrompt == prompt
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (isSelected) {
                                    selectedPrompt = null
                                } else {
                                    selectedPrompt = prompt
                                    if (planText.isBlank()) planText = cleanPrompt
                                    if (firstStep.isBlank()) firstStep = defaultStep
                                }
                            },
                            label = { Text(prompt, style = MaterialTheme.typography.bodyMedium) },
                            shape = RoundedCornerShape(100.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            ),
                        )
                    }
                }

                // 1. One Important Intention
                OutlinedTextField(
                    value = planText,
                    onValueChange = { planText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Today's Main Intention") },
                    placeholder = { Text("e.g. Finish physics problem set, write thesis draft") },
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
                    label = { Text("Smallest First Step") },
                    placeholder = { Text("e.g. Open textbook to page 45, write outline bullets") },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    ),
                    singleLine = true,
                )

                PrimaryGlowButton(
                    text = "Set Today's Intention",
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    enabled = planText.isNotBlank() || selectedPrompt != null,
                    onClick = {
                        val text = planText.ifBlank { selectedPrompt.orEmpty() }
                        val step = firstStep.ifBlank { "Take the first 5 minutes to begin" }
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
                            planRepository.savePlan(text = "", skipped = true)
                            onDone()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text("Take today as it comes")
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
                            text = "💡 Good to know",
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
                        text = "Intention saved locally. Have a wonderful day.",
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
