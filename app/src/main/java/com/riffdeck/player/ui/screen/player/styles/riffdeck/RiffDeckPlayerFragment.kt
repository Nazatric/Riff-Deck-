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

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsCompat.Type
import androidx.core.view.updatePadding
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.riffdeck.player.R
import com.riffdeck.player.core.model.player.PlayerColorScheme
import com.riffdeck.player.core.model.player.PlayerColorSchemeMode
import com.riffdeck.player.core.model.player.PlayerTintTarget
import com.riffdeck.player.core.model.player.surfaceTintTarget
import com.riffdeck.player.core.model.player.tintTarget
import com.riffdeck.player.core.model.theme.NowPlayingScreen
import com.riffdeck.player.databinding.FragmentRiffdeckPlayerBinding
import com.riffdeck.player.extensions.getOnBackPressedDispatcher
import com.riffdeck.player.extensions.launchAndRepeatWithViewLifecycle
import com.riffdeck.player.extensions.whichFragment
import com.riffdeck.player.ui.component.base.AbsPlayerControlsFragment
import com.riffdeck.player.ui.component.base.AbsPlayerFragment
import com.riffdeck.player.ui.theme.riffdeck.components.RiffDeckWinampVisualizerPanel
import com.riffdeck.player.util.Preferences

/**
 * The RiffDeck player style: brushed-metal/chrome/Y2K skeuomorphic Now
 * Playing screen. Structurally mirrors [com.riffdeck.player.ui.screen.player.styles.plainstyle.PlainPlayerFragment]
 * so it plugs into the same tint/menu/gesture system as every other player
 * style, just with a different visual skin.
 */
class RiffDeckPlayerFragment : AbsPlayerFragment(R.layout.fragment_riffdeck_player) {

    private var _binding: FragmentRiffdeckPlayerBinding? = null
    private val binding get() = _binding!!

    private lateinit var controlsFragment: RiffDeckPlayerControlsFragment

    // RECORD_AUDIO is required by the Visualizer API used for the real
    // audio-reactive spectrum panel. Per Fragment Activity Result API
    // rules, this MUST be registered at class initialization / before the
    // fragment reaches CREATED — registering it later (e.g. in
    // onViewCreated) throws IllegalStateException at runtime.
    private val hasRecordAudioPermissionState = mutableStateOf(false)

    private val recordAudioPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasRecordAudioPermissionState.value = granted
    }

    private fun hasRecordAudioPermissionGranted(): Boolean =
        ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

    override val colorSchemeMode: PlayerColorSchemeMode
        get() = Preferences.getNowPlayingColorSchemeMode(NowPlayingScreen.RiffDeck)

    override val playerControlsFragment: AbsPlayerControlsFragment
        get() = controlsFragment

    override val playerToolbar: Toolbar
        get() = binding.toolbar

    override val blurView: ImageView
        get() = binding.blur

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRiffdeckPlayerBinding.bind(view)
        setupToolbar()
        inflateMenuInView(playerToolbar)
        setupVisualizer()
        ViewCompat.setOnApplyWindowInsetsListener(view) { v: View, insets: WindowInsetsCompat ->
            val systemBars = insets.getInsets(Type.systemBars())
            v.updatePadding(top = systemBars.top, bottom = systemBars.bottom)
            val displayCutout = insets.getInsets(Type.displayCutout())
            v.updatePadding(left = displayCutout.left, right = displayCutout.right)
            WindowInsetsCompat.CONSUMED
        }
        viewLifecycleOwner.launchAndRepeatWithViewLifecycle {
            playerViewModel.currentSongFlow.collect { currentSong ->
                _binding?.let { nonNullBinding ->
                    nonNullBinding.title.text = currentSong.title
                    nonNullBinding.text.text = getSongArtist(currentSong)
                }
            }
        }
    }

    private fun setupVisualizer() {
        hasRecordAudioPermissionState.value = hasRecordAudioPermissionGranted()
        if (!hasRecordAudioPermissionState.value) {
            recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }

        binding.visualizerPanel.apply {
            // Per official Android guidance, ComposeView used inside a
            // Fragment should tie its Composition lifecycle to the
            // Fragment's own view lifecycle rather than the default
            // (dispose-on-window-detach), which can fire prematurely
            // during transitions and would tear down the Visualizer mid-
            // animation unnecessarily.
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            setContent {
                val hasPermission by hasRecordAudioPermissionState
                val audioSessionId by playerViewModel.audioSessionIdFlow.collectAsStateWithLifecycle()
                val isPlaying by playerViewModel.isPlayingFlow.collectAsStateWithLifecycle()

                RiffDeckWinampVisualizerPanel(
                    audioSessionId = audioSessionId,
                    hasPermission = hasPermission,
                    isPlaying = isPlaying
                )
            }
        }
    }

    private fun setupToolbar() {
        if (playerToolbar.navigationIcon == null)
            return

        playerToolbar.setNavigationOnClickListener {
            getOnBackPressedDispatcher().onBackPressed()
        }
    }

    override fun getTintTargets(scheme: PlayerColorScheme): List<PlayerTintTarget> {
        val oldPrimaryTextColor = binding.title.currentTextColor
        val oldSecondaryTextColor = binding.text.currentTextColor
        return mutableListOf(
            binding.root.surfaceTintTarget(scheme.surfaceColor),
            binding.toolbar.tintTarget(oldPrimaryTextColor, scheme.onSurfaceColor),
            binding.title.tintTarget(oldPrimaryTextColor, scheme.onSurfaceColor),
            binding.text.tintTarget(oldSecondaryTextColor, scheme.onSurfaceVariantColor)
        ).also {
            it.addAll(playerControlsFragment.getTintTargets(scheme))
        }
    }

    override fun onMenuInflated(menu: Menu) {
        super.onMenuInflated(menu)
        menu.setShowAsAction(R.id.action_playing_queue, mode = MenuItem.SHOW_AS_ACTION_ALWAYS)
        menu.setShowAsAction(R.id.action_favorite, mode = MenuItem.SHOW_AS_ACTION_ALWAYS)
        menu.setShowAsAction(R.id.action_sleep_timer, mode = MenuItem.SHOW_AS_ACTION_ALWAYS)
        menu.setShowAsAction(R.id.action_show_lyrics, mode = MenuItem.SHOW_AS_ACTION_ALWAYS)
    }

    override fun onCreateChildFragments() {
        super.onCreateChildFragments()
        controlsFragment = whichFragment(R.id.playbackControlsFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
