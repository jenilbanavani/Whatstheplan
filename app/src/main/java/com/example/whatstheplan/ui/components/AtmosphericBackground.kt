package com.example.whatstheplan.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.whatstheplan.ui.theme.CoolSky
import com.example.whatstheplan.ui.theme.CoolSkyFixed
import com.example.whatstheplan.ui.theme.DarkBg
import com.example.whatstheplan.ui.theme.DarkSurface
import com.example.whatstheplan.ui.theme.WarmSunrise
import com.example.whatstheplan.ui.theme.WarmSunriseFixed
import java.time.LocalTime

enum class AtmospherePhase {
    MORNING,
    AFTERNOON,
    EVENING,
    NIGHT,
    AUTO;

    companion object {
        fun current(): AtmospherePhase {
            val hour = LocalTime.now().hour
            return when (hour) {
                in 5..11 -> MORNING
                in 12..17 -> AFTERNOON
                in 18..21 -> EVENING
                else -> NIGHT
            }
        }
    }
}

@Composable
fun AtmosphericBackground(
    modifier: Modifier = Modifier,
    phase: AtmospherePhase = AtmospherePhase.AUTO,
    content: @Composable BoxScope.() -> Unit,
) {
    val isDark = isSystemInDarkTheme()
    val activePhase = if (phase == AtmospherePhase.AUTO) AtmospherePhase.current() else phase

    val infiniteTransition = rememberInfiniteTransition(label = "ambient_glow_pulse")
    val pulse1 by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse_orb_1",
    )
    val pulse2 by infiniteTransition.animateFloat(
        initialValue = 0.50f,
        targetValue = 0.30f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse_orb_2",
    )

    val (gradientColors, orb1Color, orb2Color) = when {
        isDark -> {
            Triple(
                listOf(DarkBg, DarkSurface, Color(0xFF141828)),
                WarmSunrise.copy(alpha = 0.12f * pulse1),
                CoolSky.copy(alpha = 0.15f * pulse2),
            )
        }
        activePhase == AtmospherePhase.MORNING -> {
            Triple(
                listOf(
                    MaterialTheme.colorScheme.background,
                    WarmSunriseFixed.copy(alpha = 0.30f),
                    CoolSkyFixed.copy(alpha = 0.20f),
                ),
                WarmSunrise.copy(alpha = 0.35f * pulse1),
                CoolSky.copy(alpha = 0.30f * pulse2),
            )
        }
        activePhase == AtmospherePhase.AFTERNOON -> {
            Triple(
                listOf(
                    MaterialTheme.colorScheme.background,
                    CoolSkyFixed.copy(alpha = 0.30f),
                    Color(0xFFECEEEE).copy(alpha = 0.40f),
                ),
                CoolSky.copy(alpha = 0.30f * pulse1),
                WarmSunriseFixed.copy(alpha = 0.20f * pulse2),
            )
        }
        activePhase == AtmospherePhase.EVENING -> {
            Triple(
                listOf(
                    MaterialTheme.colorScheme.background,
                    WarmSunriseFixed.copy(alpha = 0.25f),
                    Color(0xFFD8DADA).copy(alpha = 0.30f),
                ),
                WarmSunriseText_alpha(pulse1),
                CoolSky.copy(alpha = 0.25f * pulse2),
            )
        }
        else -> {
            Triple(
                listOf(
                    MaterialTheme.colorScheme.background,
                    Color(0xFFE6E8E9),
                    CoolSkyFixed.copy(alpha = 0.15f),
                ),
                CoolSky.copy(alpha = 0.20f * pulse1),
                WarmSunrise.copy(alpha = 0.15f * pulse2),
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = gradientColors,
                    start = Offset.Zero,
                    end = Offset.Infinite,
                ),
            ),
    ) {
        // Atmospheric Glowing Orbs Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Top-right glowing sunrise orb
            drawCircle(
                color = orb1Color,
                radius = width * 0.65f,
                center = Offset(width * 0.95f, height * 0.05f),
            )

            // Mid-left cool sky ambient orb
            drawCircle(
                color = orb2Color,
                radius = width * 0.55f,
                center = Offset(width * 0.05f, height * 0.45f),
            )
        }

        // Foreground Content
        content()
    }
}

private fun WarmSunriseText_alpha(pulse: Float): Color =
    WarmSunrise.copy(alpha = 0.28f * pulse)
