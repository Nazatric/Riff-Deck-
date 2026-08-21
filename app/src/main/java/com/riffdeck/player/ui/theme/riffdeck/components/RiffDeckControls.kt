package com.riffdeck.player.ui.theme.riffdeck.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.riffdeck.player.R
import com.riffdeck.player.ui.theme.riffdeck.RiffDeckColor

/**
 * Small circular chrome retry button — shown over displayed lyrics so a
 * user can re-fetch if the matched lyrics are wrong, and used as the
 * "Add lyrics" action when none were found at all.
 */
@Composable
fun RiffDeckRetryLyricsButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(36.dp)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(RiffDeckColor.ChromeSpecular, RiffDeckColor.ChromeHighlight, RiffDeckColor.BrushedSteel)
                ),
                shape = CircleShape
            )
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_refresh_24dp),
            contentDescription = stringResource(R.string.action_add_lyrics),
            tint = RiffDeckColor.GunmetalDark,
            modifier = Modifier.size(18.dp)
        )
    }
}

/**
 * The big chrome transport button used for Play/Pause across the player
 * screen and mini-player. Convex chrome look with a pressed/inset state,
 * like a physical button on a car stereo or amp.
 */
@Composable
fun RiffDeckChromeButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 72.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = 0.4f),
        label = "chromeButtonScale"
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .shadow(if (pressed) 2.dp else 8.dp, CircleShape, ambientColor = RiffDeckColor.NeonRed, spotColor = RiffDeckColor.NeonRed)
            .background(
                brush = Brush.radialGradient(
                    colors = if (pressed)
                        listOf(RiffDeckColor.GunmetalMid, RiffDeckColor.ChromeHighlight)
                    else
                        listOf(RiffDeckColor.ChromeSpecular, RiffDeckColor.ChromeHighlight, RiffDeckColor.BrushedSteel)
                ),
                shape = CircleShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
            contentDescription = if (isPlaying) "Pause" else "Play",
            tint = RiffDeckColor.GunmetalDark,
            modifier = Modifier.size(size * 0.5f)
        )
    }
}
