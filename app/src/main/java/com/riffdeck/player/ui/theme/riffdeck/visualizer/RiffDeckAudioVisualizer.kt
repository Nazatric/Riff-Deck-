package com.riffdeck.player.ui.theme.riffdeck.visualizer

import android.media.audiofx.Visualizer
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlin.math.abs
import kotlin.math.sqrt

private const val TAG = "RiffDeckVisualizer"

/**
 * Number of bars shown in the Winamp-style spectrum display. The Visualizer
 * FFT capture is downsampled/binned into this many bands.
 */
const val VISUALIZER_BAND_COUNT = 20

/**
 * Wraps [android.media.audiofx.Visualizer] to expose live magnitude data for
 * the currently playing audio session as Compose state. Real audio capture,
 * not a simulated animation — requires RECORD_AUDIO permission (checked by
 * the caller before construction) and a valid, non-zero [audioSessionId].
 *
 * Lifecycle: caller must call [release] when no longer needed (handled
 * automatically when used via [rememberAudioVisualizerState]).
 */
class RiffDeckAudioVisualizer(
    audioSessionId: Int,
    private val onBands: (FloatArray) -> Unit
) {
    private var visualizer: Visualizer? = null

    init {
        try {
            visualizer = Visualizer(audioSessionId).apply {
                captureSize = Visualizer.getCaptureSizeRange()[1] // max supported size
                setDataCaptureListener(
                    object : Visualizer.OnDataCaptureListener {
                        override fun onWaveFormDataCapture(
                            visualizer: Visualizer?,
                            waveform: ByteArray?,
                            samplingRate: Int
                        ) {
                            // Not used — we only care about frequency data.
                        }

                        override fun onFftDataCapture(
                            visualizer: Visualizer?,
                            fft: ByteArray?,
                            samplingRate: Int
                        ) {
                            if (fft != null) {
                                onBands(binFftToBars(fft, VISUALIZER_BAND_COUNT))
                            }
                        }
                    },
                    Visualizer.getMaxCaptureRate() / 2,
                    /* waveform = */ false,
                    /* fft = */ true
                )
                enabled = true
            }
        } catch (e: Exception) {
            // Visualizer can fail to initialize for reasons outside our
            // control (e.g. another app already has an exclusive audio
            // effect session, or the session ID isn't ready yet). Fail
            // silently — the UI falls back to an idle/flat state rather
            // than crashing the player screen over a cosmetic feature.
            Log.w(TAG, "Failed to initialize Visualizer", e)
            visualizer = null
        }
    }

    fun release() {
        try {
            visualizer?.enabled = false
            visualizer?.release()
        } catch (e: Exception) {
            Log.w(TAG, "Error releasing Visualizer", e)
        } finally {
            visualizer = null
        }
    }
}

/**
 * Converts a raw 8-bit magnitude FFT capture into [barCount] normalized
 * (0f..1f) magnitude bands for display, using a logarithmic grouping so
 * bass/mid/treble are represented proportionally (music energy skews
 * heavily toward low frequencies, so a linear grouping would crowd all
 * visible motion into the first couple of bars).
 *
 * Android's Visualizer.getFft() byte layout (see AOSP Visualizer.java /
 * developer.android.com docs) is NOT a simple sequence of real/imaginary
 * pairs starting at index 0. For a capture of size n, with n/2 usable
 * frequency bins:
 *   index 0       = Rf0        (DC component, real part only)
 *   index 1       = Rf(n/2)    (Nyquist component, real part only)
 *   index 2..n-1  = Rf1,If1, Rf2,If2, ... Rf(n/2-1),If(n/2-1)  (real/imaginary pairs)
 */
private fun binFftToBars(fft: ByteArray, barCount: Int): FloatArray {
    val captureSize = fft.size
    val bins = captureSize / 2 // number of usable frequency bins (n/2)
    if (bins <= 1) return FloatArray(barCount)

    val magnitudes = FloatArray(bins + 1)
    magnitudes[0] = abs(fft[0].toInt()).toFloat()       // DC
    magnitudes[bins] = abs(fft[1].toInt()).toFloat()    // Nyquist
    var k = 1
    var i = 2
    while (k < bins && i + 1 < captureSize) {
        val real = fft[i].toInt()
        val imaginary = fft[i + 1].toInt()
        magnitudes[k] = sqrt((real * real + imaginary * imaginary).toFloat())
        k++
        i += 2
    }

    val bars = FloatArray(barCount)
    val n = magnitudes.size
    for (bar in 0 until barCount) {
        val startFrac = bar.toFloat() / barCount
        val endFrac = (bar + 1).toFloat() / barCount
        // Logarithmic bin range: low bars cover few low-frequency bins
        // (bass), high bars cover many high-frequency bins (treble),
        // matching how musical energy and perceived pitch are distributed.
        val startBin = (n.toDouble().pow(startFrac.toDouble())).toInt().coerceIn(0, n - 1)
        val endBin = (n.toDouble().pow(endFrac.toDouble())).toInt().coerceIn(startBin + 1, n)

        var sum = 0f
        for (idx in startBin until endBin) sum += magnitudes[idx]
        val avg = sum / (endBin - startBin).coerceAtLeast(1)

        // Raw magnitude range is roughly 0..~180 in practice for typical
        // music; clamp and scale to 0f..1f for the UI to consume.
        bars[bar] = (avg / 90f).coerceIn(0f, 1f)
    }
    return bars
}

private fun Double.pow(exp: Double): Double = Math.pow(this, exp)

/**
 * Remembers a live [RiffDeckAudioVisualizer] tied to the current composition
 * lifecycle, exposing the latest band magnitudes as Compose [State]. Returns
 * an all-zero array when [audioSessionId] is invalid (0) or permission
 * hasn't been granted — callers should render an idle/flat bar state in
 * that case rather than nothing, so the panel doesn't look broken.
 *
 * @param hasPermission whether RECORD_AUDIO has been granted; checked by the
 *   caller (e.g. via accompanist-permissions or a manual runtime check) since
 *   this composable has no Activity context to request permissions itself.
 */
@Composable
fun rememberAudioVisualizerState(
    audioSessionId: Int,
    hasPermission: Boolean
): State<FloatArray> {
    val bands = remember { mutableStateOf(FloatArray(VISUALIZER_BAND_COUNT)) }

    DisposableEffect(audioSessionId, hasPermission) {
        val visualizer = if (hasPermission && audioSessionId != 0) {
            RiffDeckAudioVisualizer(audioSessionId) { newBands ->
                bands.value = newBands
            }
        } else {
            null
        }

        onDispose {
            visualizer?.release()
            bands.value = FloatArray(VISUALIZER_BAND_COUNT)
        }
    }

    return bands
}
