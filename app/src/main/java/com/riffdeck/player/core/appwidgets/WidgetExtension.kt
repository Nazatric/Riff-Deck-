package com.riffdeck.player.core.appwidgets

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.TypedValue
import android.view.KeyEvent
import androidx.annotation.ColorInt
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.glance.action.Action
import androidx.glance.appwidget.action.actionStartService
import androidx.glance.color.ColorProvider
import androidx.glance.color.ColorProviders
import androidx.glance.color.colorProviders
import com.riffdeck.player.core.appwidgets.state.WidgetTheme
import com.riffdeck.player.playback.PlaybackService
import com.riffdeck.player.ui.theme.PaletteStyle
import com.riffdeck.player.ui.theme.dynamicColorSchemes

fun Dp.toPx(context: Context): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.value,
        context.resources.displayMetrics
    ).toInt()
}

fun WidgetTheme(
    @ColorInt
    sourceColor: Int,
    style: PaletteStyle = PaletteStyle.Fidelity
): WidgetTheme {
    val colorSchemes = dynamicColorSchemes(
        keyColor = Color(sourceColor),
        style = style,
        contrastLevel = 0.75
    )
    return WidgetTheme(
        lightSurfaceColor = colorSchemes.lightColorScheme.surface.toArgb(),
        lightOnSurfaceColor = colorSchemes.lightColorScheme.onSurface.toArgb(),
        lightOnSurfaceVariantColor = colorSchemes.lightColorScheme.onSurfaceVariant.toArgb(),
        lightPrimaryColor = colorSchemes.lightColorScheme.primary.toArgb(),
        lightOnPrimaryColor = colorSchemes.lightColorScheme.onPrimary.toArgb(),
        lightPrimaryContainerColor = colorSchemes.lightColorScheme.primaryContainer.toArgb(),
        lightOnPrimaryContainerColor = colorSchemes.lightColorScheme.onPrimaryContainer.toArgb(),
        lightTertiaryContainerColor = colorSchemes.lightColorScheme.tertiaryContainer.toArgb(),
        lightOnTertiaryContainerColor = colorSchemes.lightColorScheme.onTertiaryContainer.toArgb(),
        darkSurfaceColor = colorSchemes.darkColorScheme.surface.toArgb(),
        darkOnSurfaceColor = colorSchemes.darkColorScheme.onSurface.toArgb(),
        darkOnSurfaceVariantColor = colorSchemes.darkColorScheme.onSurfaceVariant.toArgb(),
        darkPrimaryColor = colorSchemes.darkColorScheme.primary.toArgb(),
        darkOnPrimaryColor = colorSchemes.darkColorScheme.onPrimary.toArgb(),
        darkPrimaryContainerColor = colorSchemes.darkColorScheme.primaryContainer.toArgb(),
        darkOnPrimaryContainerColor = colorSchemes.darkColorScheme.onPrimaryContainer.toArgb(),
        darkTertiaryContainerColor = colorSchemes.darkColorScheme.tertiaryContainer.toArgb(),
        darkOnTertiaryContainerColor = colorSchemes.lightColorScheme.onTertiaryContainer.toArgb()
    )
}

@Composable
fun WidgetTheme?.getColors(): ColorProviders {
    // RiffDeck: when there's no album-art-derived theme (nothing playing, or
    // color extraction failed), fall back to a fixed RiffDeck palette rather
    // than Android's default GlanceTheme.colors, which is wallpaper-derived
    // dynamic color on API 31+ — that would silently break the widget's
    // rock/metal/Y2K identity whenever no song is playing.
    if (this == null) {
        return colorProviders(
            primary = ColorProvider(day = Color(0xFFB3122A), night = Color(0xFFFF5A6E)),
            onPrimary = ColorProvider(day = Color(0xFFFFFFFF), night = Color(0xFF3A0008)),
            primaryContainer = ColorProvider(day = Color(0xFFFFD9DC), night = Color(0xFF7A1220)),
            onPrimaryContainer = ColorProvider(day = Color(0xFF3A0008), night = Color(0xFFFFD9DC)),
            secondary = ColorProvider(day = Color(0xFF565D68), night = Color(0xFFAEB6C2)),
            onSecondary = ColorProvider(day = Color(0xFFFFFFFF), night = Color(0xFF1E2126)),
            secondaryContainer = ColorProvider(day = Color(0xFFEFF3F8), night = Color(0xFF3B4048)),
            onSecondaryContainer = ColorProvider(day = Color(0xFF1E2126), night = Color(0xFFEFF3F8)),
            tertiary = ColorProvider(day = Color(0xFF6B4A00), night = Color(0xFFFFB020)),
            onTertiary = ColorProvider(day = Color(0xFFFFFFFF), night = Color(0xFF3A2600)),
            tertiaryContainer = ColorProvider(day = Color(0xFFFFE4B0), night = Color(0xFF6B4A00)),
            onTertiaryContainer = ColorProvider(day = Color(0xFF3A2600), night = Color(0xFFFFE4B0)),
            error = ColorProvider(day = Color(0xFF93000A), night = Color(0xFFFF6E7A)),
            errorContainer = ColorProvider(day = Color(0xFFFFDAD6), night = Color(0xFF93000A)),
            onError = ColorProvider(day = Color(0xFFFFFFFF), night = Color(0xFF3A0008)),
            onErrorContainer = ColorProvider(day = Color(0xFF3A0008), night = Color(0xFFFFDAD6)),
            background = ColorProvider(day = Color(0xFFF2F3F5), night = Color(0xFF15171B)),
            onBackground = ColorProvider(day = Color(0xFF15171B), night = Color(0xFFF2F3F5)),
            surface = ColorProvider(day = Color(0xFFF2F3F5), night = Color(0xFF15171B)),
            onSurface = ColorProvider(day = Color(0xFF15171B), night = Color(0xFFF2F3F5)),
            surfaceVariant = ColorProvider(day = Color(0xFFEFF3F8), night = Color(0xFF2A2E35)),
            onSurfaceVariant = ColorProvider(day = Color(0xFF3B4048), night = Color(0xFFA7ADB8)),
            outline = ColorProvider(day = Color(0xFF565D68), night = Color(0xFF565D68)),
            inverseOnSurface = ColorProvider(day = Color(0xFFF2F3F5), night = Color(0xFF15171B)),
            inverseSurface = ColorProvider(day = Color(0xFF15171B), night = Color(0xFFF2F3F5)),
            inversePrimary = ColorProvider(day = Color(0xFFFF5A6E), night = Color(0xFFB3122A)),
            widgetBackground = ColorProvider(day = Color(0xFFF2F3F5), night = Color(0xFF15171B))
        )
    }

    // Fields not present in WidgetTheme's extracted palette use fixed
    // RiffDeck colors rather than system GlanceTheme.colors (dynamic/
    // wallpaper-derived), so the widget never silently drifts from the
    // app's rock/metal/Y2K identity even for the album-art-tinted branch.
    return run {
        colorProviders(
            primary = ColorProvider(
                day = Color(lightPrimaryColor),
                night = Color(darkPrimaryColor)
            ),
            onPrimary = ColorProvider(
                day = Color(lightOnPrimaryColor),
                night = Color(darkOnPrimaryColor)
            ),
            primaryContainer = ColorProvider(
                day = Color(lightPrimaryContainerColor),
                night = Color(darkPrimaryContainerColor)
            ),
            onPrimaryContainer = ColorProvider(
                day = Color(lightOnPrimaryContainerColor),
                night = Color(darkOnPrimaryContainerColor)
            ),
            secondary = ColorProvider(day = Color(0xFF565D68), night = Color(0xFFAEB6C2)),
            onSecondary = ColorProvider(day = Color(0xFFFFFFFF), night = Color(0xFF1E2126)),
            secondaryContainer = ColorProvider(day = Color(0xFFEFF3F8), night = Color(0xFF3B4048)),
            onSecondaryContainer = ColorProvider(day = Color(0xFF1E2126), night = Color(0xFFEFF3F8)),
            tertiary = ColorProvider(day = Color(0xFF6B4A00), night = Color(0xFFFFB020)),
            onTertiary = ColorProvider(day = Color(0xFFFFFFFF), night = Color(0xFF3A2600)),
            tertiaryContainer = ColorProvider(
                day = Color(lightTertiaryContainerColor),
                night = Color(darkTertiaryContainerColor)
            ),
            onTertiaryContainer = ColorProvider(
                day = Color(lightOnTertiaryContainerColor),
                night = Color(darkOnTertiaryContainerColor)
            ),
            error = ColorProvider(day = Color(0xFF93000A), night = Color(0xFFFF6E7A)),
            errorContainer = ColorProvider(day = Color(0xFFFFDAD6), night = Color(0xFF93000A)),
            onError = ColorProvider(day = Color(0xFFFFFFFF), night = Color(0xFF3A0008)),
            onErrorContainer = ColorProvider(day = Color(0xFF3A0008), night = Color(0xFFFFDAD6)),
            background = ColorProvider(day = Color(0xFFF2F3F5), night = Color(0xFF15171B)),
            onBackground = ColorProvider(day = Color(0xFF15171B), night = Color(0xFFF2F3F5)),
            surface = ColorProvider(
                day = Color(lightSurfaceColor),
                night = Color(darkSurfaceColor)
            ),
            onSurface = ColorProvider(
                day = Color(lightOnSurfaceColor),
                night = Color(darkOnSurfaceColor)
            ),
            surfaceVariant = ColorProvider(day = Color(0xFFEFF3F8), night = Color(0xFF2A2E35)),
            onSurfaceVariant = ColorProvider(
                day = Color(lightOnSurfaceVariantColor),
                night = Color(darkOnSurfaceVariantColor)
            ),
            outline = ColorProvider(day = Color(0xFF565D68), night = Color(0xFF565D68)),
            inverseOnSurface = ColorProvider(day = Color(0xFFF2F3F5), night = Color(0xFF15171B)),
            inverseSurface = ColorProvider(day = Color(0xFF15171B), night = Color(0xFFF2F3F5)),
            inversePrimary = ColorProvider(day = Color(0xFFFF5A6E), night = Color(0xFFB3122A)),
            widgetBackground = ColorProvider(day = Color(0xFFF2F3F5), night = Color(0xFF15171B))
        )
    }
}

fun playbackAction(context: Context, mediaKeyCode: Int): Action {
    val intent = Intent(Intent.ACTION_MEDIA_BUTTON)
    intent.setComponent(ComponentName(context, PlaybackService::class.java))
    intent.putExtra(Intent.EXTRA_KEY_EVENT, KeyEvent(KeyEvent.ACTION_DOWN, mediaKeyCode))
    return actionStartService(intent, true)
}

fun toggleShuffleAction(context: Context): Action {
    val intent = Intent(PlaybackService.ACTION_TOGGLE_SHUFFLE)
    intent.setComponent(ComponentName(context, PlaybackService::class.java))
    return actionStartService(intent)
}

fun cycleRepeatAction(context: Context): Action {
    val intent = Intent(PlaybackService.ACTION_CYCLE_REPEAT)
    intent.setComponent(ComponentName(context, PlaybackService::class.java))
    return actionStartService(intent)
}

fun toggleFavoriteAction(context: Context): Action {
    val intent = Intent(PlaybackService.ACTION_TOGGLE_FAVORITE)
    intent.setComponent(ComponentName(context, PlaybackService::class.java))
    return actionStartService(intent)
}