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

package com.riffdeck.player.ui.component.base

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isGone
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.riffdeck.player.R
import com.riffdeck.player.core.model.MediaEvent
import com.riffdeck.player.extensions.applyWindowInsets
import com.riffdeck.player.extensions.dip
import com.riffdeck.player.extensions.isLandscape
import com.riffdeck.player.ui.screen.MainActivity
import com.riffdeck.player.ui.screen.library.LibraryViewModel
import com.riffdeck.player.ui.screen.player.PlayerViewModel
import com.riffdeck.player.util.OPEN_ON_PLAY
import com.riffdeck.player.util.PLAY_ON_STARTUP_MODE
import com.riffdeck.player.util.PlayOnStartupMode
import com.riffdeck.player.util.PlayOnStartupMode.Companion.WITH_EXPANDED_PLAYER
import com.riffdeck.player.util.Preferences.requireString
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.activityViewModel

/**
 * @author Christians M. A. (mardous)
 */
abstract class AbsMainActivityFragment @JvmOverloads constructor(@LayoutRes layoutRes: Int = 0) :
    Fragment(layoutRes), MenuProvider {

    val playerViewModel: PlayerViewModel by activityViewModel()
    val libraryViewModel: LibraryViewModel by activityViewModel()

    protected val preferences: SharedPreferences by inject()
    protected val mainActivity: MainActivity
        get() = requireActivity() as MainActivity

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.STARTED)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                playerViewModel.mediaEvent.collect {
                    when (it) {
                        MediaEvent.FavoriteContentChanged -> onFavoriteContentChanged()
                        MediaEvent.MediaContentChanged -> onMediaContentChanged()
                        MediaEvent.PlaybackRestored -> {
                            val mode = preferences.requireString(PLAY_ON_STARTUP_MODE, PlayOnStartupMode.NEVER)
                            if (mode == WITH_EXPANDED_PLAYER) {
                                mainActivity.expandPanel()
                            }
                        }
                        MediaEvent.PlaybackStarted -> {
                            if (preferences.getBoolean(OPEN_ON_PLAY, false)) {
                                mainActivity.expandPanel()
                            }
                        }
                    }
                }
            }
        }
    }

    protected open fun onMediaContentChanged() {}

    protected open fun onFavoriteContentChanged() {}

    protected fun applyWindowInsetsFromView(view: View) {
        view.applyWindowInsets(
            left = isLandscape() && mainActivity.navigationView.isGone, right = true, bottom = true
        )
    }

    protected fun checkForMargins(view: View) {
        if (mainActivity.isBottomNavVisible) {
            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = dip(R.dimen.bottom_nav_height)
            }
        }
    }
}