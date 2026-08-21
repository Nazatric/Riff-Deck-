package com.riffdeck.player.ui.theme.riffdeck.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.riffdeck.player.ui.theme.riffdeck.RiffDeckColor

/**
 * Draws four small "screwed-in" rivets at the corners of a panel — a
 * recurring skeuomorphic detail (amp chassis, pedal boards, road cases).
 * Wrap any composable with this to add the hardware-panel feel.
 */
@Composable
fun RiffDeckRiveted(
    modifier: Modifier = Modifier,
    inset: Dp = 10.dp,
    rivetDiameter: Dp = 8.dp,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        content()
        Box(Modifier.align(Alignment.TopStart).padding(inset)) { RivetDot(rivetDiameter) }
        Box(Modifier.align(Alignment.TopEnd).padding(inset)) { RivetDot(rivetDiameter) }
        Box(Modifier.align(Alignment.BottomStart).padding(inset)) { RivetDot(rivetDiameter) }
        Box(Modifier.align(Alignment.BottomEnd).padding(inset)) { RivetDot(rivetDiameter) }
    }
}

@Composable
private fun RivetDot(diameter: Dp) {
    Canvas(modifier = Modifier.size(diameter)) {
        // `this` here is a DrawScope, which carries the Density needed for .toPx()
        drawRivet(this)
    }
}

private fun drawRivet(scope: DrawScope) = with(scope) {
    // radius computed inside the DrawScope receiver, so Density is in scope
    val radius = size.minDimension / 2f
    val center = Offset(radius, radius)

    // Dark base (recessed shadow the cap sits in)
    drawCircle(
        color = RiffDeckColor.GunmetalDark,
        radius = radius,
        center = center
    )
    // Metallic gradient cap (the screw head)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(RiffDeckColor.ChromeSpecular, RiffDeckColor.ChromeHighlight, RiffDeckColor.GunmetalMid),
            center = Offset(radius * 0.7f, radius * 0.7f),
            radius = radius
        ),
        radius = radius * 0.75f,
        center = center
    )
}
