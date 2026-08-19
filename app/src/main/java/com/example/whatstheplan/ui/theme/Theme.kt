package com.example.whatstheplan.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.whatstheplan.domain.model.ThemeMode

// ==========================================
// PALETTE - Morning Check-in Visual Language
// ==========================================

// Core Brand Tones
val DeepNavy = Color(0xFF181E34)
val OffWhiteBg = Color(0xFFF8FAFA)
val TextPrimary = Color(0xFF191C1D)
val TextSecondary = Color(0xFF46464D)

// Warm Sunrise Accents
val WarmSunrise = Color(0xFFFFB783)
val WarmSunriseFixed = Color(0xFFFFDCC5)
val WarmSunriseText = Color(0xFFEA8126)
val WarmSunriseContainer = Color(0xFF552800)

// Cool Sky Accents
val CoolSky = Color(0xFFBAC3FF)
val CoolSkySecondary = Color(0xFF4858AB)
val CoolSkyFixed = Color(0xFFDEE0FF)
val CoolSkyContainer = Color(0xFF96A5FF)

// Dark Midnight Palette
val DarkBg = Color(0xFF0F1322)
val DarkSurface = Color(0xFF181E34)
val DarkSurfaceVariant = Color(0xFF242A44)
val DarkSurfaceContainer = Color(0xFF2D334A)
val DarkSurfaceContainerHigh = Color(0xFF383F59)
val DarkOnSurface = Color(0xFFEFF1F1)
val DarkOnSurfaceVariant = Color(0xFFA2A5B8)
val DarkPrimary = Color(0xFFC0C5E2)

// Category Colors
object CategoryColors {
    val Study = Color(0xFF96A5FF)     // Sky Cool
    val Project = Color(0xFFFFB783)   // Warm Sunrise
    val Work = Color(0xFF7EA8F8)      // Sky Blue
    val Building = Color(0xFFFF9F76)  // Muted Coral
    val Social = Color(0xFFF29EC6)    // Pastel Rose
    val Gaming = Color(0xFF70D6CA)    // Pastel Cyan
    val Watching = Color(0xFFF6C878)  // Warm Gold
    val Exercise = Color(0xFF6EE7B7)  // Mint Fresh
    val Break = Color(0xFF86D99C)     // Soft Green
    val LifeAdmin = Color(0xFFBAC3FF) // Soft Lavender
    val Rest = Color(0xFFFFDCC5)      // Warm Peach
    val Other = Color(0xFFC6C6CD)     // Neutral Slate
}

private val LightColorScheme = lightColorScheme(
    primary = DeepNavy,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF2D334A),
    onPrimaryContainer = Color(0xFFC0C5E2),

    secondary = CoolSkySecondary,
    onSecondary = Color.White,
    secondaryContainer = CoolSkyFixed,
    onSecondaryContainer = Color(0xFF27378A),

    tertiary = WarmSunriseText,
    onTertiary = Color.White,
    tertiaryContainer = WarmSunriseFixed,
    onTertiaryContainer = WarmSunriseContainer,

    background = OffWhiteBg,
    onBackground = TextPrimary,

    surface = OffWhiteBg,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFFE1E3E3),
    onSurfaceVariant = TextSecondary,
    surfaceContainer = Color(0xFFECEEEE),
    surfaceContainerHigh = Color(0xFFE6E8E9),

    outline = Color(0xFF76767E),
    outlineVariant = Color(0xFFC6C6CD),
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DeepNavy,
    primaryContainer = DarkSurfaceContainer,
    onPrimaryContainer = Color(0xFFDEE0FF),

    secondary = CoolSky,
    onSecondary = DeepNavy,
    secondaryContainer = Color(0xFF2A3464),
    onSecondaryContainer = Color(0xFFDEE0FF),

    tertiary = WarmSunrise,
    onTertiary = Color(0xFF301400),
    tertiaryContainer = Color(0xFF4A2500),
    onTertiaryContainer = WarmSunriseFixed,

    background = DarkBg,
    onBackground = DarkOnSurface,

    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceContainerHigh,

    outline = Color(0xFF5A5E75),
    outlineVariant = Color(0xFF3B3F56),
)

@Composable
fun WhatsThePlanTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content,
    )
}
