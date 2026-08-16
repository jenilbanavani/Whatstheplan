package com.example.whatstheplan.ui.theme

import android.app.Activity
import android.os.Build
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
// PALETTE - Deep Indigo & Luminous Lavender
// ==========================================

// Dark Palette
val DarkBg = Color(0xFF0C0C16)
val DarkSurface = Color(0xFF161528)
val DarkSurfaceVariant = Color(0xFF201F38)
val DarkSurfaceContainer = Color(0xFF282744)
val DarkSurfaceContainerHigh = Color(0xFF333155)

val LavenderPrimary = Color(0xFF9E8DFF)
val LavenderPrimaryDark = Color(0xFFBCAEFF)
val LavenderOnPrimary = Color(0xFF13083B)
val LavenderContainer = Color(0xFF2D2566)
val LavenderOnContainer = Color(0xFFEADBFF)

val SkySecondary = Color(0xFF7FA7FF)
val SkyOnSecondary = Color(0xFF002A6A)
val SkySecondaryContainer = Color(0xFF203868)
val SkyOnSecondaryContainer = Color(0xFFD6E3FF)

val CoralTertiary = Color(0xFFFF9E80)
val CoralOnTertiary = Color(0xFF4E1600)
val CoralTertiaryContainer = Color(0xFF6B2B11)
val CoralOnTertiaryContainer = Color(0xFFFFDBCF)

val MintPositive = Color(0xFF6EE7B7)
val MintOnPositive = Color(0xFF003824)

// Light Palette
val LightBg = Color(0xFFF7F6FC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFEEEAF8)
val LightSurfaceContainer = Color(0xFFE5E0F5)
val LightSurfaceContainerHigh = Color(0xFFDBD5F0)

val IndigoPrimaryLight = Color(0xFF5B45D6)
val IndigoOnPrimaryLight = Color(0xFFFFFFFF)
val IndigoPrimaryContainerLight = Color(0xFFE9E4FF)
val IndigoOnPrimaryContainerLight = Color(0xFF1E0A6E)

val SlateSecondaryLight = Color(0xFF455A8A)
val SlateOnSecondaryLight = Color(0xFFFFFFFF)
val SlateSecondaryContainerLight = Color(0xFFDCE4FF)
val SlateOnSecondaryContainerLight = Color(0xFF001848)

val PeachTertiaryLight = Color(0xFFB54C28)
val PeachOnTertiaryLight = Color(0xFFFFFFFF)
val PeachTertiaryContainerLight = Color(0xFFFFDBD0)
val PeachOnTertiaryContainerLight = Color(0xFF3B0F00)

// Category Pastel Colors for Bento cards & graphs
object CategoryColors {
    val Study = Color(0xFFB49EFF)     // Lavender Violet
    val Work = Color(0xFF7EA8F8)      // Sky Blue
    val Building = Color(0xFFFF9F76)  // Muted Coral
    val Social = Color(0xFFF29EC6)    // Pastel Rose
    val Gaming = Color(0xFF70D6CA)    // Pastel Cyan
    val Watching = Color(0xFFF6C878)  // Warm Gold
    val Break = Color(0xFF86D99C)     // Mint Green
    val Other = Color(0xFFA5B4CB)     // Soft Slate
}

private val DarkColorScheme = darkColorScheme(
    primary = LavenderPrimaryDark,
    onPrimary = LavenderOnPrimary,
    primaryContainer = LavenderContainer,
    onPrimaryContainer = LavenderOnContainer,
    secondary = SkySecondary,
    onSecondary = SkyOnSecondary,
    secondaryContainer = SkySecondaryContainer,
    onSecondaryContainer = SkyOnSecondaryContainer,
    tertiary = CoralTertiary,
    onTertiary = CoralOnTertiary,
    tertiaryContainer = CoralTertiaryContainer,
    onTertiaryContainer = CoralOnTertiaryContainer,
    background = DarkBg,
    onBackground = Color(0xFFF1EFF8),
    surface = DarkSurface,
    onSurface = Color(0xFFF1EFF8),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFBBB7D0),
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    outline = Color(0xFF454266),
    outlineVariant = Color(0xFF2E2C4A),
)

private val LightColorScheme = lightColorScheme(
    primary = IndigoPrimaryLight,
    onPrimary = IndigoOnPrimaryLight,
    primaryContainer = IndigoPrimaryContainerLight,
    onPrimaryContainer = IndigoOnPrimaryContainerLight,
    secondary = SlateSecondaryLight,
    onSecondary = SlateOnSecondaryLight,
    secondaryContainer = SlateSecondaryContainerLight,
    onSecondaryContainer = SlateOnSecondaryContainerLight,
    tertiary = PeachTertiaryLight,
    onTertiary = PeachOnTertiaryLight,
    tertiaryContainer = PeachTertiaryContainerLight,
    onTertiaryContainer = PeachOnTertiaryContainerLight,
    background = LightBg,
    onBackground = Color(0xFF181724),
    surface = LightSurface,
    onSurface = Color(0xFF181724),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF58556D),
    surfaceContainer = LightSurfaceContainer,
    surfaceContainerHigh = LightSurfaceContainerHigh,
    outline = Color(0xFFCBC6E2),
    outlineVariant = Color(0xFFE4E0F2),
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
