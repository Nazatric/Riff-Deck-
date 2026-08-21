package com.riffdeck.player.ui.screen.sleeptimer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.riffdeck.player.ui.theme.RiffDeckAppTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class SleepTimerFragment: BottomSheetDialogFragment() {

    private val viewModel: SleepTimerViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                RiffDeckAppTheme {
                    SleepTimerBottomSheet(
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}