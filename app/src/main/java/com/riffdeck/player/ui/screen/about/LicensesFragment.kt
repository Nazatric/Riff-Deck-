package com.riffdeck.player.ui.screen.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.riffdeck.player.extensions.getOnBackPressedDispatcher
import com.riffdeck.player.extensions.materialSharedAxis
import com.riffdeck.player.ui.theme.RiffDeckAppTheme

class LicensesFragment : Fragment() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return ComposeView(requireContext()).apply {
            setContent {
                RiffDeckAppTheme {
                    OSSLicensesScreen {
                        getOnBackPressedDispatcher().onBackPressed()
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        materialSharedAxis(view)
    }
}