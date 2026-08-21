package com.riffdeck.player.ui.screen.player.cover

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.riffdeck.player.R
import com.riffdeck.player.extensions.currentFragment
import com.riffdeck.player.ui.component.base.goToDestination
import com.riffdeck.player.ui.screen.MainActivity
import com.riffdeck.player.ui.screen.lyrics.CoverLyricsScreen
import com.riffdeck.player.ui.screen.lyrics.LyricsFragment
import com.riffdeck.player.ui.screen.lyrics.LyricsViewModel
import com.riffdeck.player.ui.screen.player.PlayerViewModel
import com.riffdeck.player.ui.theme.RiffDeckAppTheme
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class CoverLyricsFragment : Fragment() {

    private val lyricsViewModel: LyricsViewModel by activityViewModel()
    private val playerViewModel: PlayerViewModel by activityViewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            setContent {
                RiffDeckAppTheme {
                    CoverLyricsScreen(
                        lyricsViewModel,
                        playerViewModel,
                        onExpandClick = {
                            if (currentFragment(R.id.fragment_container) is LyricsFragment) {
                                (activity as? MainActivity)?.collapsePanel()
                            } else {
                                goToDestination(requireActivity(), R.id.nav_lyrics)
                            }
                        })
                }
            }
        }
    }
}