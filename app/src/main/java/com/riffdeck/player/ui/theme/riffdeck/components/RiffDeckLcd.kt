package com.riffdeck.player.ui.theme.riffdeck.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.riffdeck.player.ui.theme.riffdeck.RiffDeckColor
import com.riffdeck.player.ui.theme.riffdeck.RiffDeckType

/**
 * A recessed "LCD screen" readout — used for elapsed/remaining time,
 * track index (e.g. "03/12"), EQ band values, bitrate/sample-rate info.
 * Carved-in look via dark inset background + glowing monospace text.
 */
@Composable
fun RiffDeckLcdReadout(
    text: String,
    modifier: Modifier = Modifier,
    glowColor: Color = RiffDeckColor.LcdGreen
) {
    val shape = RoundedCornerShape(6.dp)
    Text(
        text = text,
        style = RiffDeckType.lcdReadout,
        color = glowColor,
        modifier = modifier
            .background(RiffDeckColor.SurfaceInset, shape)
            .border(1.dp, RiffDeckColor.RivetShadow, shape)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}
