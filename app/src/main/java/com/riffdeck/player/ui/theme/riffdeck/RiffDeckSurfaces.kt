package com.riffdeck.player.ui.theme.riffdeck

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Central library of skeuomorphic surface treatments used across every
 * screen so the "brushed metal / rock-metal / Y2K" look stays consistent
 * without every screen re-inventing gradients by hand.
 */
object RiffDeckSurfaces {

    /** Vertical brushed-steel gradient, like an amp faceplate. */
    fun brushedMetalBrush(
        base: Color = RiffDeckColor.BrushedSteel,
        light: Color = RiffDeckColor.BrushedSteelLight,
        dark: Color = RiffDeckColor.GunmetalMid
    ) = Brush.verticalGradient(
        colors = listOf(light, base, base, dark)
    )

    /** Glossy diagonal sheen for jewel-case / plastic elements. */
    fun jewelGlossBrush() = Brush.linearGradient(
        colors = listOf(
            RiffDeckColor.JewelGlossTop,
            RiffDeckColor.JewelGlossBottom
        )
    )

    /** Radial glow used behind neon/LCD elements (VU meters, play button). */
    fun neonGlowBrush(color: Color) = Brush.radialGradient(
        colors = listOf(color.copy(alpha = 0.55f), color.copy(alpha = 0f))
    )
}

/** Raised, beveled panel — like a physical control panel screwed onto a chassis. */
@Composable
fun Modifier.riffDeckPanel(
    shape: Shape = RoundedCornerShape(18.dp),
    color: Color = RiffDeckColor.SurfacePanel
): Modifier = this
    .shadow(elevation = 10.dp, shape = shape, ambientColor = Color.Black, spotColor = Color.Black)
    .background(RiffDeckSurfaces.brushedMetalBrush(base = color), shape)
    .border(1.dp, RiffDeckColor.BevelInsetLight, shape)

/** Inset "carved into the metal" panel — used for LCD screens, scrubbers, list wells. */
@Composable
fun Modifier.riffDeckInset(
    shape: Shape = RoundedCornerShape(12.dp),
    color: Color = RiffDeckColor.SurfaceInset
): Modifier = this
    .background(color, shape)
    .border(1.dp, RiffDeckColor.RivetShadow, shape)

/** Simple corner-rivet size token so components stay visually consistent. */
val RiffDeckRivetSize: Dp = 6.dp
