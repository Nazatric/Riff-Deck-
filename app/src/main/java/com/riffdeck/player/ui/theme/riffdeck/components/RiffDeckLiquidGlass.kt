package com.riffdeck.player.ui.theme.riffdeck.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * "Liquid glass" frosted panel — layers a soft white-to-transparent diagonal
 * sheen over a translucent dark base with a bright top-edge highlight
 * border, giving the impression of light catching a curved glass surface.
 * Used behind the lyrics view and other overlay panels in the RiffDeck skin.
 *
 * Note: this does not blur the content *behind* it (Compose has no stable
 * backdrop-blur API without an extra dependency); it approximates the
 * liquid-glass look via translucency + gradient sheen + edge highlight,
 * which reads correctly over the existing blurred/gradient backgrounds
 * already used on the Lyrics and player screens.
 */
@Composable
fun Modifier.riffDeckLiquidGlass(
    shape: Shape = RoundedCornerShape(24.dp),
    tint: Color = Color(0xFF15171B)
): Modifier = this
    .background(tint.copy(alpha = 0.35f), shape)
    .background(
        Brush.linearGradient(
            colors = listOf(
                Color(0x3DFFFFFF),
                Color(0x0DFFFFFF),
                Color(0x00FFFFFF)
            )
        ),
        shape
    )
    .border(
        width = 1.dp,
        brush = Brush.linearGradient(
            colors = listOf(Color(0x66FFFFFF), Color(0x00FFFFFF))
        ),
        shape = shape
    )
