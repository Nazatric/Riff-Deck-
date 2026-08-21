package com.riffdeck.player.ui.theme.riffdeck

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val riffDeckColorScheme = darkColorScheme(
    primary = RiffDeckColor.NeonRed,
    onPrimary = RiffDeckColor.TextOnNeon,
    primaryContainer = RiffDeckColor.GunmetalMid,
    onPrimaryContainer = RiffDeckColor.TextPrimary,
    secondary = RiffDeckColor.AmpAmber,
    onSecondary = RiffDeckColor.TextOnNeon,
    secondaryContainer = RiffDeckColor.SurfaceRaised,
    onSecondaryContainer = RiffDeckColor.TextPrimary,
    tertiary = RiffDeckColor.LcdGreen,
    onTertiary = RiffDeckColor.TextOnNeon,
    background = RiffDeckColor.SurfaceDeep,
    onBackground = RiffDeckColor.TextPrimary,
    surface = RiffDeckColor.SurfacePanel,
    onSurface = RiffDeckColor.TextPrimary,
    surfaceVariant = RiffDeckColor.SurfaceRaised,
    onSurfaceVariant = RiffDeckColor.TextSecondary,
    outline = RiffDeckColor.BrushedSteelLight,
    outlineVariant = RiffDeckColor.BevelInsetLight,
    error = RiffDeckColor.Danger,
    onError = RiffDeckColor.TextPrimary,
    surfaceContainerLowest = RiffDeckColor.SurfaceInset,
    surfaceContainerLow = RiffDeckColor.GunmetalBase,
    surfaceContainer = RiffDeckColor.SurfacePanel,
    surfaceContainerHigh = RiffDeckColor.SurfaceRaised,
    surfaceContainerHighest = RiffDeckColor.BrushedSteel,
)

/**
 * Root theme for the RiffDeck skin. Always dark — brushed metal and neon
 * accents are designed for a dark chassis, matching the rock/metal/Y2K
 * hardware aesthetic (amps, car stereos, MP3 players never had a "light mode").
 */
@Composable
fun RiffDeckTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = riffDeckColorScheme,
        typography = RiffDeckType.typography,
        content = content
    )
}
