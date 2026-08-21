package com.riffdeck.player.ui.theme.riffdeck.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.riffdeck.player.ui.theme.riffdeck.visualizer.VISUALIZER_BAND_COUNT
import com.riffdeck.player.ui.theme.riffdeck.visualizer.rememberAudioVisualizerState

/**
 * Classic Winamp-style bar spectrum analyzer: a recessed LCD strip with
 * neon-lime bars reacting to real playback audio via the Visualizer API.
 * Falls back to a flat idle-bar state (rather than nothing) when permission
 * hasn't been granted or no session is attached yet, so the panel never
 * looks broken.
 *
 * @param isPlaying used only to decide whether bars should animate toward
 *   their target height or hold — the actual magnitude data always comes
 *   from [rememberAudioVisualizerState].
 */
@Composable
fun RiffDeckWinampVisualizerPanel(
    audioSessionId: Int,
    hasPermission: Boolean,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val bands by rememberAudioVisualizerState(audioSessionId, hasPermission)
    val barCount = VISUALIZER_BAND_COUNT

    // Smooth per-bar decay so bars fall gracefully instead of snapping
    // instantly on every capture frame — matches classic hardware/software
    // spectrum analyzers, which always have a "fall-off" rate slower than
    // their rise rate. key(i) gives each bar's animateFloatAsState call a
    // stable, unambiguous slot-table identity across recompositions, which
    // is the documented-safe way to call a @Composable per-item in a loop
    // (barCount is a fixed constant here, so identity never shifts).
    val animatedValues = FloatArray(barCount)
    for (i in 0 until barCount) {
        key(i) {
            val target = if (isPlaying) bands.getOrElse(i) { 0f } else 0f
            val animated by animateFloatAsState(
                targetValue = target,
                animationSpec = tween(durationMillis = if (target > 0f) 60 else 260),
                label = "visualizerBar$i"
            )
            animatedValues[i] = animated
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B0C0E))
            .border(1.dp, Color(0xCC000000))
    ) {
        val gap = size.width * 0.01f
        val barWidth = (size.width - gap * (barCount + 1)) / barCount

        for (i in 0 until barCount) {
            val magnitude = animatedValues[i]
            val barHeight = (size.height * 0.9f) * magnitude.coerceIn(0.03f, 1f)
            val x = gap + i * (barWidth + gap)
            val top = size.height - barHeight

            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFEFFFC8), Color(0xFFCFFF3C), Color(0xFF6FA61A)),
                    startY = top,
                    endY = size.height
                ),
                topLeft = Offset(x, top),
                size = Size(barWidth, barHeight)
            )

            // Peak-hold cap, Winamp-signature detail
            if (magnitude > 0.05f) {
                drawRect(
                    color = Color(0xFFF2FFE0),
                    topLeft = Offset(x, (top - 3.dp.toPx()).coerceAtLeast(0f)),
                    size = Size(barWidth, 2.dp.toPx())
                )
            }
        }
    }
}
