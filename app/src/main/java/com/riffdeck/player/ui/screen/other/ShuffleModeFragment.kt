package com.riffdeck.player.ui.screen.other

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.riffdeck.player.ui.screen.library.LibraryViewModel
import com.riffdeck.player.ui.screen.player.PlayerViewModel
import com.riffdeck.player.ui.theme.RiffDeckAppTheme
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class ShuffleModeFragment : BottomSheetDialogFragment() {

    private val libraryViewModel: LibraryViewModel by activityViewModel()
    private val playerViewModel: PlayerViewModel by activityViewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return ComposeView(requireContext()).apply {
            setContent {
                RiffDeckAppTheme {
                    ShuffleModeBottomSheet(
                        libraryViewModel = libraryViewModel,
                        playerViewModel = playerViewModel
                    )
                }
            }
        }
    }
}