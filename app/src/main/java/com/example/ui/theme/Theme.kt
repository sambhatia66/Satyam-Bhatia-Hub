package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = ChampagneGold,
    onPrimary = ChampagneGoldDark,
    primaryContainer = ChampagneGoldContainer,
    onPrimaryContainer = ChampagneGoldLight,
    secondary = EmeraldTeal,
    onSecondary = EmeraldTealDark,
    secondaryContainer = EmeraldTealContainer,
    onSecondaryContainer = Color(0xFFA7F3D0),
    tertiary = Color(0xFF7DD3FC),
    onTertiary = Color(0xFF082F49),
    background = ObsidianBg,
    onBackground = InkWhite,
    surface = ObsidianSurface,
    onSurface = InkWhite,
    surfaceVariant = ObsidianSurfaceHigh,
    onSurfaceVariant = InkMuted,
    surfaceContainerLowest = ObsidianBg,
    surfaceContainerLow = ObsidianSurface,
    surfaceContainer = ObsidianSurfaceHigh,
    surfaceContainerHigh = ObsidianSurfaceElevated,
    surfaceContainerHighest = Color(0xFF28344D),
    outline = ObsidianOutline,
    outlineVariant = ObsidianOutlineVariant,
    error = SoftCoralError,
    onError = Color(0xFF3E0B12),
    errorContainer = SoftCoralErrorContainer,
    onErrorContainer = Color(0xFFFFDADE)
)

private val LightColorScheme = lightColorScheme(
    primary = RoyalNavyPrimary,
    onPrimary = Color.White,
    primaryContainer = RoyalNavyContainer,
    onPrimaryContainer = Color(0xFF0E1B36),
    secondary = DeepEmeraldSecondary,
    onSecondary = Color.White,
    secondaryContainer = DeepEmeraldContainer,
    onSecondaryContainer = Color(0xFF063827),
    tertiary = WarmAmberTertiary,
    onTertiary = Color.White,
    tertiaryContainer = WarmAmberContainer,
    onTertiaryContainer = Color(0xFF3E2705),
    background = AlabasterBg,
    onBackground = SlateInk,
    surface = AlabasterSurface,
    onSurface = SlateInk,
    surfaceVariant = AlabasterSurfaceHigh,
    onSurfaceVariant = SlateMuted,
    surfaceContainerLowest = AlabasterSurface,
    surfaceContainerLow = Color(0xFFF8FAFD),
    surfaceContainer = AlabasterSurfaceHigh,
    surfaceContainerHigh = AlabasterSurfaceElevated,
    surfaceContainerHighest = Color(0xFFD5DFEE),
    outline = AlabasterOutline,
    outlineVariant = AlabasterOutlineVariant,
    error = CrimsonError,
    onError = Color.White,
    errorContainer = CrimsonErrorContainer,
    onErrorContainer = Color(0xFF5C0D16)
)

val CardVaultShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun CardVaultTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CardVaultTypography,
        shapes = CardVaultShapes,
        content = content
    )
}
