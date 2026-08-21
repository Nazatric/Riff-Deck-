package com.riffdeck.player.ui.theme.riffdeck.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.riffdeck.player.ui.theme.riffdeck.RiffDeckColor

/**
 * Spinning vinyl-record style album art holder for the Now Playing screen —
 * the centerpiece skeuomorphic element. Album art sits on a black vinyl disc
 * with a chrome spindle; the whole thing rotates while playing, like a real
 * turntable, echoing the "cover flow / album art everywhere" request while
 * staying skeuomorphic rather than flat.
 */
@Composable
fun RiffDeckVinylArt(
    artworkUrl: Any?,
    isSpinning: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "vinylSpin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "vinylRotation"
    )

    val appliedRotation = if (isSpinning) rotation else 0f

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .rotate(appliedRotation)
            .shadow(16.dp, CircleShape, ambientColor = Color.Black, spotColor = Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // Black vinyl disc base with grooves
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawVinylGrooves(this)
        }

        // Album art label in the center of the "record"
        AsyncImage(
            model = artworkUrl,
            contentDescription = "Album art",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(RiffDeckColor.SurfaceRaised)
        )

        // Chrome spindle hole in the very center
        Box(
            modifier = Modifier
                .fillMaxSize(0.09f)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(RiffDeckColor.ChromeSpecular, RiffDeckColor.GunmetalDark)
                    )
                )
                .border(1.dp, RiffDeckColor.RivetShadow, CircleShape)
        )
    }
}

private fun drawVinylGrooves(scope: androidx.compose.ui.graphics.drawscope.DrawScope) {
    with(scope) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxRadius = size.minDimension / 2f
        drawCircle(color = Color(0xFF0B0B0D), radius = maxRadius, center = center)
        var r = maxRadius * 0.32f
        while (r < maxRadius * 0.98f) {
            drawCircle(
                color = Color(0xFF1C1C20),
                radius = r,
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.2f)
            )
            r += maxRadius * 0.035f
        }
    }
}

/**
 * Brushed-metal scrubber track — an inset "slot" the thumb slides through,
 * like a physical volume/seek slider on a stereo faceplate.
 */
@Composable
fun RiffDeckScrubber(
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    Slider(
        value = value,
        onValueChange = onValueChange,
        onValueChangeFinished = onValueChangeFinished,
        modifier = modifier,
        colors = SliderDefaults.colors(
            thumbColor = RiffDeckColor.ChromeSpecular,
            activeTrackColor = RiffDeckColor.NeonRed,
            inactiveTrackColor = RiffDeckColor.SurfaceInset
        )
    )
}
