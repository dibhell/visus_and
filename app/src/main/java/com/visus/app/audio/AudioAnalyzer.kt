package com.visus.app.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.jtransforms.fft.DoubleFFT_1D
import kotlin.math.abs
import kotlin.math.hypot

class AudioAnalyzer {
    private val sampleRate = 44100
    private val fftSize = 1024
    private val bufferSize = AudioRecord.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    ).coerceAtLeast(fftSize * 2)

    private var audioRecord: AudioRecord? = null
    private val scope = CoroutineScope(Dispatchers.Default + Job())
    private val _bands = MutableStateFlow(Triple(0f, 0f, 0f))
    val bands: StateFlow<Triple<Float, Float, Float>> = _bands

    @SuppressLint("MissingPermission")
    fun start() {
        if (audioRecord != null) return
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        ).apply {
            startRecording()
        }
        scope.launch { readLoop() }
    }

    fun stop() {
        audioRecord?.apply {
            stop()
            release()
        }
        audioRecord = null
        scope.cancel()
    }

    private suspend fun readLoop() {
        val ar = audioRecord ?: return
        val shortBuffer = ShortArray(fftSize)
        val fftBuffer = DoubleArray(fftSize * 2)
        val fft = DoubleFFT_1D(fftSize.toLong())

        while (scope.isActive) {
            val read = ar.read(shortBuffer, 0, shortBuffer.size)
            if (read <= 0) continue
            // Copy to double buffer with zeroed imaginary parts.
            for (i in 0 until fftSize) {
                val sample = if (i < read) shortBuffer[i].toDouble() / Short.MAX_VALUE else 0.0
                fftBuffer[2 * i] = sample
                fftBuffer[2 * i + 1] = 0.0
            }
            fft.complexForward(fftBuffer)
            computeBands(fftBuffer)
        }
    }

    private fun computeBands(fftBuffer: DoubleArray) {
        var bassEnergy = 0.0
        var midEnergy = 0.0
        var highEnergy = 0.0
        val binSizeHz = sampleRate.toDouble() / fftSize.toDouble()

        for (i in 0 until fftSize / 2) {
            val re = fftBuffer[2 * i]
            val im = fftBuffer[2 * i + 1]
            val mag = hypot(re, im)
            val freq = i * binSizeHz
            when {
                freq < 140 -> bassEnergy += mag
                freq < 2000 -> midEnergy += mag
                else -> highEnergy += mag
            }
        }

        val normBass = bassEnergy.toFloat().coerceIn(0f, 5f) / 5f
        val normMid = midEnergy.toFloat().coerceIn(0f, 8f) / 8f
        val normHigh = highEnergy.toFloat().coerceIn(0f, 8f) / 8f
        _bands.value = Triple(abs(normBass), abs(normMid), abs(normHigh))
    }
}
