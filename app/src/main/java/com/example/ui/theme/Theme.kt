package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Immutable
data class KhataExtendedColors(
    val isDark: Boolean,
    val positiveAccent: Color,
    val positiveContainer: Color,
    val negativeAccent: Color,
    val negativeSoft: Color,
    val negativeContainer: Color,
    val settledAccent: Color,
    val settledContainer: Color,
    val textMuted: Color,
    val limeButton: Color,
    val onLimeButton: Color,
    val heroTextPrimary: Color,
    val heroTextSecondary: Color,
    val heroPositiveAccent: Color,
    val heroPositiveContainer: Color,
    val heroNegativeAccent: Color,
    val heroNegativeContainer: Color,
    val heroSettledAccent: Color,
    val heroChipBackground: Color
)

private val DarkKhataExtendedColors = KhataExtendedColors(
    isDark = true,
    positiveAccent = AccentLime,
    positiveContainer = LimeTintContainer,
    negativeAccent = DarkDebtRose,
    negativeSoft = DarkDebtRoseSoft,
    negativeContainer = DarkDebtRoseContainer,
    settledAccent = DarkSettledLavender,
    settledContainer = DarkSettledContainer,
    textMuted = DarkTextMuted,
    limeButton = AccentLime,
    onLimeButton = OnAccentLime,
    heroTextPrimary = DarkTextPrimary,
    heroTextSecondary = DarkTextSecondary,
    heroPositiveAccent = AccentLime,
    heroPositiveContainer = LimeTintContainer.copy(alpha = 0.7f),
    heroNegativeAccent = DarkDebtRose,
    heroNegativeContainer = DarkDebtRoseContainer.copy(alpha = 0.75f),
    heroSettledAccent = DarkSettledLavender,
    heroChipBackground = BgDeepPurple.copy(alpha = 0.55f)
)

private val LightKhataExtendedColors = KhataExtendedColors(
    isDark = false,
    positiveAccent = LightPositiveText,
    positiveContainer = LightPositiveContainer,
    negativeAccent = LightDebtRose,
    negativeSoft = LightDebtRoseSoft,
    negativeContainer = LightDebtRoseContainer,
    settledAccent = LightSettledAccent,
    settledContainer = LightSettledContainer,
    textMuted = LightTextMuted,
    limeButton = AccentLime,
    onLimeButton = OnAccentLime,
    heroTextPrimary = Color.White,
    heroTextSecondary = Color(0xFFE9D5FF),
    heroPositiveAccent = AccentLime,
    heroPositiveContainer = Color(0xFF1F0A4A).copy(alpha = 0.45f),
    heroNegativeAccent = Color(0xFFFDA4AF),
    heroNegativeContainer = Color(0xFF4C0519).copy(alpha = 0.45f),
    heroSettledAccent = Color(0xFFE9D5FF),
    heroChipBackground = Color(0xFF2E1065).copy(alpha = 0.42f)
)

val LocalKhataColors = staticCompositionLocalOf { DarkKhataExtendedColors }

private val DarkKhataColorScheme = darkColorScheme(
    primary = PrimaryPurple,
    onPrimary = DarkTextPrimary,
    primaryContainer = SurfacePurpleElevated,
    onPrimaryContainer = DarkTextPrimary,
    secondary = AccentLime,
    onSecondary = OnAccentLime,
    secondaryContainer = LimeTintContainer,
    onSecondaryContainer = AccentLime,
    tertiary = PrimaryPurpleSoft,
    onTertiary = BgDeepPurpleDarker,
    background = BgDeepPurple,
    onBackground = DarkTextPrimary,
    surface = SurfacePurple,
    onSurface = DarkTextPrimary,
    surfaceVariant = SurfacePurpleElevated,
    onSurfaceVariant = DarkTextSecondary,
    surfaceContainerLowest = BgDeepPurpleDarker,
    surfaceContainerLow = BgDeepPurple,
    surfaceContainer = SurfacePurple,
    surfaceContainerHigh = SurfacePurpleElevated,
    surfaceContainerHighest = SurfacePurpleHighest,
    outline = DarkOutlinePurple,
    outlineVariant = DarkOutlineSubtle,
    error = DarkDebtRose,
    onError = BgDeepPurpleDarker,
    errorContainer = DarkDebtRoseContainer,
    onErrorContainer = DarkDebtRoseSoft
)

private val LightKhataColorScheme = lightColorScheme(
    primary = PrimaryPurple,
    onPrimary = Color.White,
    primaryContainer = LightSurfaceElevated,
    onPrimaryContainer = LightTextPrimary,
    secondary = AccentLime,
    onSecondary = OnAccentLime,
    secondaryContainer = LightPositiveContainer,
    onSecondaryContainer = LightPositiveText,
    tertiary = PrimaryPurple,
    onTertiary = Color.White,
    background = LightBgLavender,
    onBackground = LightTextPrimary,
    surface = LightSurfaceCard,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceElevated,
    onSurfaceVariant = LightTextSecondary,
    surfaceContainerLowest = LightSurfaceCard,
    surfaceContainerLow = LightBgLavender,
    surfaceContainer = LightSurfaceCard,
    surfaceContainerHigh = LightSurfaceElevated,
    surfaceContainerHighest = LightSurfaceHighest,
    outline = LightOutlinePurple,
    outlineVariant = LightOutlineSubtle,
    error = LightDebtRose,
    onError = Color.White,
    errorContainer = LightDebtRoseContainer,
    onErrorContainer = LightDebtRoseSoft
)

private val ZamKhataShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun ZamKhataTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkKhataColorScheme else LightKhataColorScheme
    val extendedColors = if (darkTheme) DarkKhataExtendedColors else LightKhataExtendedColors

    CompositionLocalProvider(LocalKhataColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = ZamKhataTypography,
            shapes = ZamKhataShapes,
            content = content
        )
    }
}
