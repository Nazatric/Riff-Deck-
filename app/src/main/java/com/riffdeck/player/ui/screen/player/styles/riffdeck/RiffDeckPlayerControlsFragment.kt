/*
 * Copyright (c) 2025 Christians Martínez Alvarado
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.riffdeck.player.ui.screen.player.styles.riffdeck

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.riffdeck.player.R
import com.riffdeck.player.core.model.player.PlayerColorScheme
import com.riffdeck.player.core.model.player.PlayerTintTarget
import com.riffdeck.player.core.model.player.iconButtonTintTarget
import com.riffdeck.player.core.model.player.tintTarget
import com.riffdeck.player.data.model.Song
import com.riffdeck.player.databinding.FragmentRiffdeckPlayerPlaybackControlsBinding
import com.riffdeck.player.ui.component.base.AbsPlayerControlsFragment
import com.riffdeck.player.ui.component.base.SkipButtonTouchHandler.Companion.DIRECTION_NEXT
import com.riffdeck.player.ui.component.base.SkipButtonTouchHandler.Companion.DIRECTION_PREVIOUS
import com.riffdeck.player.ui.component.views.MusicSlider

/**
 * Transport controls for the RiffDeck player style: a riveted brushed-metal
 * panel with a chrome convex play/pause button and LCD-style time readouts.
 */
class RiffDeckPlayerControlsFragment :
    AbsPlayerControlsFragment(R.layout.fragment_riffdeck_player_playback_controls) {

    private var _binding: FragmentRiffdeckPlayerPlaybackControlsBinding? = null
    private val binding get() = _binding!!

    override val playPauseFab: FloatingActionButton
        get() = binding.playPauseButton

    override val repeatButton: MaterialButton
        get() = binding.repeatButton

    override val shuffleButton: MaterialButton
        get() = binding.shuffleButton

    override val musicSlider: MusicSlider?
        get() = binding.progressSlider

    override val songCurrentProgress: TextView
        get() = binding.songCurrentProgress

    override val songTotalTime: TextView
        get() = binding.songTotalTime

    override val songInfoView: TextView?
        get() = binding.songInfo

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRiffdeckPlayerPlaybackControlsBinding.bind(view)
        binding.playPauseButton.setOnClickListener(this)
        binding.shuffleButton.setOnClickListener(this)
        binding.repeatButton.setOnClickListener(this)
        binding.nextButton.setOnTouchListener(getSkipButtonTouchHandler(DIRECTION_NEXT))
        binding.previousButton.setOnTouchListener(getSkipButtonTouchHandler(DIRECTION_PREVIOUS))
    }

    override fun getTintTargets(scheme: PlayerColorScheme): List<PlayerTintTarget> {
        // nextButton/previousButton are AppCompatImageButton (not
        // MaterialButton, which has no `iconTint`), so read the current
        // tint via imageTintList — the real property ImageView/
        // AppCompatImageButton actually exposes, and the one
        // animateTintColor() (ViewExt.kt) itself writes to for this widget
        // type via ImageViewCompat.setImageTintList.
        val oldControlColor = binding.nextButton.imageTintList?.defaultColor
            ?: android.graphics.Color.WHITE
        val oldSliderColor = binding.progressSlider.currentColor
        val oldSecondaryTextColor = binding.songInfo.currentTextColor
        val oldShuffleColor = getPlaybackControlsColor(isShuffleModeOn)
        val newShuffleColor = getPlaybackControlsColor(
            isShuffleModeOn,
            scheme.onSurfaceColor,
            scheme.onSurfaceVariantColor
        )
        val oldRepeatColor = getPlaybackControlsColor(isRepeatModeOn)
        val newRepeatColor = getPlaybackControlsColor(
            isRepeatModeOn,
            scheme.onSurfaceColor,
            scheme.onSurfaceVariantColor
        )
        return listOfNotNull(
            binding.progressSlider.progressView?.tintTarget(oldSliderColor, scheme.primaryColor),
            binding.songInfo.tintTarget(oldSecondaryTextColor, scheme.onSurfaceVariantColor),
            binding.nextButton.iconButtonTintTarget(oldControlColor, scheme.onSurfaceColor),
            binding.previousButton.iconButtonTintTarget(oldControlColor, scheme.onSurfaceColor),
            binding.shuffleButton.iconButtonTintTarget(oldShuffleColor, newShuffleColor),
            binding.repeatButton.iconButtonTintTarget(oldRepeatColor, newRepeatColor)
        )
        // Note: songCurrentProgress/songTotalTime (LCD readouts) and the chrome
        // playPauseButton are deliberately NOT tinted by the dynamic color scheme —
        // they keep their fixed LCD-green/amber and chrome look regardless of
        // theme/album-art color, which is the point of the skeuomorphic skin.
    }

    override fun onSongInfoChanged(currentSong: Song, nextSong: Song) {}

    override fun onExtraInfoChanged(extraInfo: String?) {
        _binding?.let { nonNullBinding ->
            if (isExtraInfoEnabled()) {
                nonNullBinding.songInfo.text = extraInfo
                nonNullBinding.songInfo.isVisible = true
            } else {
                nonNullBinding.songInfo.isVisible = false
            }
        }
    }

    override fun onUpdatePlayPause(isPlaying: Boolean) {
        if (isPlaying) {
            _binding?.playPauseButton?.setImageResource(R.drawable.ic_pause_24dp)
        } else {
            _binding?.playPauseButton?.setImageResource(R.drawable.ic_play_24dp)
        }
    }

    override fun onClick(view: View) {
        super.onClick(view)
        when (view) {
            binding.repeatButton -> playerViewModel.cycleRepeatMode()
            binding.shuffleButton -> playerViewModel.toggleShuffleMode()
            binding.playPauseButton -> playerViewModel.togglePlayPause()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
