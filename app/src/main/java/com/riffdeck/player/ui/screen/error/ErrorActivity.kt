package com.riffdeck.player.ui.screen.error

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import androidx.core.content.getSystemService
import cat.ereza.customactivityoncrash.CustomActivityOnCrash
import com.riffdeck.player.R
import com.riffdeck.player.databinding.ActivityErrorBinding
import com.riffdeck.player.extensions.applyWindowInsets
import com.riffdeck.player.extensions.fileProviderAuthority
import com.riffdeck.player.extensions.files.asFormattedFileTime
import com.riffdeck.player.extensions.openUrl
import com.riffdeck.player.extensions.showToast
import com.riffdeck.player.ui.component.base.AbsThemeActivity
import com.riffdeck.player.ui.screen.about.ISSUE_TRACKER_LINK
import java.io.File

/**
 * @author Christians M. A. (mardous)
 */
class ErrorActivity : AbsThemeActivity() {

    private var _binding: ActivityErrorBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val config = CustomActivityOnCrash.getConfigFromIntent(intent)
        if (config == null) {
            finish()
            return
        }
        val errorReport = CustomActivityOnCrash.getAllErrorDetailsFromIntent(this, intent)
        val nameFromTime = System.currentTimeMillis().asFormattedFileTime()
        val errorReportFile = File(filesDir, "Crash_${nameFromTime}.log")
        if (!errorReportFile.exists() || errorReportFile.delete()) {
            errorReportFile.writeText(errorReport)
        }

        _binding = ActivityErrorBinding.inflate(layoutInflater)
        binding.root.applyWindowInsets(top = true, left = true, right = true, bottom = true)
        binding.errorReportText.text = errorReport
        binding.openGithub.setOnClickListener {
            openGithub(errorReport)
        }
        binding.sendReport.setOnClickListener {
            sendFile(errorReportFile)
        }
        binding.restartApp.setOnClickListener {
            CustomActivityOnCrash.restartApplication(this, config)
        }
        setContentView(binding.root)
    }

    private fun openGithub(report: String) {
        val clipboardManager = getSystemService<ClipboardManager>()
        val clipData = ClipData.newPlainText(getString(R.string.uncaught_error_report), report)
        clipboardManager?.setPrimaryClip(clipData)
        openUrl(CREATE_ISSUE_URL)
        showToast(R.string.uncaught_error_report_copied)
    }

    private fun sendFile(file: File) {
        val intent = ShareCompat.IntentBuilder(this)
            .setSubject("${getString(R.string.app_name)} - crash log")
            .setText("Please, add a description of the problem")
            .setType("*/*")
            .setStream(FileProvider.getUriForFile(this, fileProviderAuthority, file))
            .setChooserTitle(R.string.uncaught_error_send)
            .createChooserIntent()

        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        private const val CREATE_ISSUE_URL = "${ISSUE_TRACKER_LINK}/new?template=bug_report.yaml"
    }
}