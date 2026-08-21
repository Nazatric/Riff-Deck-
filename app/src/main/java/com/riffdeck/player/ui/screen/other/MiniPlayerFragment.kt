/*
 * Copyright (c) 2024 Christians Martínez Alvarado
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

package com.riffdeck.player.ui.screen.other

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.style.ForegroundColorSpan
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import coil3.request.Disposable
import com.riffdeck.player.R
import com.riffdeck.player.coil.songImage
import com.riffdeck.player.core.model.player.ProgressState
import com.riffdeck.player.core.model.theme.NowPlayingButtonStyle
import com.riffdeck.player.databinding.FragmentMiniPlayerBinding
import com.riffdeck.player.extensions.isTablet
import com.riffdeck.player.extensions.launchAndRepeatWithViewLifecycle
import com.riffdeck.player.extensions.media.displayArtistName
import com.riffdeck.player.extensions.resources.*
import com.riffdeck.player.ui.component.base.SkipButtonTouchHandler
import com.riffdeck.player.ui.component.base.SkipButtonTouchHandler.Companion.DIRECTION_NEXT
import com.riffdeck.player.ui.component.base.SkipButtonTouchHandler.Companion.DIRECTION_PREVIOUS
import com.riffdeck.player.ui.screen.player.PlayerViewModel
import com.riffdeck.player.util.Preferences
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import kotlin.math.abs

class MiniPlayerFragment : Fragment(R.layout.fragment_mini_player),
    View.OnClickListener, SkipButtonTouchHandler.Callback {

    private val playerViewModel: PlayerViewModel by activityViewModel()

    private var _binding: FragmentMiniPlayerBinding? = null
    private val binding get() = _binding!!

    private lateinit var primaryColorSpan: ForegroundColorSpan
    private lateinit var secondaryColorSpan: ForegroundColorSpan

    private val buttonStyle: NowPlayingButtonStyle
        get() = if (Preferences.adaptiveControls) {
            Preferences.nowPlayingScreen.buttonStyle
        } else {
            NowPlayingButtonStyle.Normal
        }

    private var disposable: Disposable? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMiniPlayerBinding.bind(view)
        binding.progressBar.installWavyAnimatorCleanup()
        viewLifecycleOwner.launchAndRepeatWithViewLifecycle {
            playerViewModel.currentSongFlow.collect { currentSong ->
                disposable = binding.image.songImage(currentSong)
                binding.songTitle.isSelected = true
                binding.songTitle.text = currentSong.title
                binding.songArtist.isSelected = true
                binding.songArtist.text = currentSong.displayArtistName()
            }
        }
        viewLifecycleOwner.launchAndRepeatWithViewLifecycle {
            combine(
                playerViewModel.progressFlow,
                playerViewModel.durationFlow
            ) { progress, duration -> ProgressState(progress, duration) }
                .filter { progress -> progress.mayUpdateUI }
                .collectLatest {
                    binding.progressBar.max = it.total.toInt()
                    binding.progressBar.setProgressCompat(it.progress.toInt(), true)
                }
        }
        viewLifecycleOwner.launchAndRepeatWithViewLifecycle {
            playerViewModel.isPlayingFlow.collect { isPlaying ->
                updatePlayPause(isPlaying, buttonStyle)
            }
        }
        primaryColorSpan = textColorPrimary().toForegroundColorSpan()
        secondaryColorSpan = textColorSecondary().toForegroundColorSpan()
        setupImageStyle()
        setUpButtons()
        setUpProgressStyle()
        applyShadeMode(Preferences.miniPlayerShadeMode)
        view.setOnTouchListener { _, event -> flingPlayBackController.onTouchEvent(event) }
        view.setOnLongClickListener {
            Preferences.miniPlayerShadeMode = !Preferences.miniPlayerShadeMode
            applyShadeMode(Preferences.miniPlayerShadeMode)
            true
        }
    }

    /**
     * Winamp-style "shade mode": long-press the mini-player to collapse it
     * to just the LCD title/artist strip and transport controls, hiding the
     * album art and progress bar for a denser, hardware-player-like compact
     * view. Persisted so it stays collapsed across app restarts.
     */
    private fun applyShadeMode(collapsed: Boolean) {
        _binding?.let { nonNullBinding ->
            nonNullBinding.image.isVisible = !collapsed
            nonNullBinding.progressBar.isVisible = !collapsed
        }
    }

    fun setupImageStyle() {
        val cornerRadius = Preferences.getNowPlayingImageCornerRadius(requireContext())
        binding.image.setCornerRadius((cornerRadius / 2).toFloat())
    }

    private fun setUpButtons() {
        setupButtonStyle()
        setupExtraControls()
        binding.actionNext.setOnTouchListener(SkipButtonTouchHandler(DIRECTION_NEXT, this))
        binding.actionPrevious.setOnTouchListener(SkipButtonTouchHandler(DIRECTION_PREVIOUS, this))
        binding.actionPlayPause.setOnClickListener(this)
    }

    fun setUpProgressStyle() {
        val isWavy = playerViewModel.isPlaying && Preferences.squigglySeekBar
        binding.progressBar.setAnimatedWave(isWavy)
        binding.progressBar.setWavy(isWavy)
    }

    fun setupButtonStyle() {
        val buttonStyle = this.buttonStyle
        binding.actionNext.setIconResource(buttonStyle.skipNext)
        binding.actionPrevious.setIconResource(buttonStyle.skipPrevious)
        updatePlayPause(playerViewModel.isPlaying, buttonStyle)
    }

    fun setupExtraControls() {
        if (resources.isTablet) {
            binding.actionNext.show()
            binding.actionPrevious.show()
        } else {
            binding.actionNext.isVisible = Preferences.extraControls
            binding.actionPrevious.isVisible = Preferences.extraControls
        }
    }

    override fun onSkipButtonHold(direction: Int) {
        when (direction) {
            DIRECTION_NEXT -> playerViewModel.seekForward()
            DIRECTION_PREVIOUS -> playerViewModel.seekBack()
        }
    }

    override fun onSkipButtonTap(direction: Int) {
        when (direction) {
            DIRECTION_NEXT -> playerViewModel.seekToNext()
            DIRECTION_PREVIOUS -> playerViewModel.seekToPrevious()
        }
    }

    override fun onClick(view: View) {
        when (view) {
            binding.actionPlayPause -> playerViewModel.togglePlayPause()
        }
    }

    override fun onDestroyView() {
        disposable?.dispose()
        super.onDestroyView()
        _binding = null
    }

    private fun updatePlayPause(isPlaying: Boolean, buttonStyle: NowPlayingButtonStyle) {
        if (isPlaying) {
            binding.actionPlayPause.setIconResource(buttonStyle.pause)
        } else {
            binding.actionPlayPause.setIconResource(buttonStyle.play)
        }
        setUpProgressStyle()
    }

    private var flingPlayBackController = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                if (abs(velocityX) > abs(velocityY)) {
                    if (velocityX < 0) {
                        playerViewModel.seekToNext()
                        return true
                    } else if (velocityX > 0) {
                        playerViewModel.seekToPrevious()
                        return true
                    }
                }
                return false
            }
        })
}