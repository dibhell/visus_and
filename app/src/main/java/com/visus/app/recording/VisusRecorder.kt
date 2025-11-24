package com.visus.app.recording

import android.content.Context
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Environment
import android.view.Surface
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VisusRecorder {
    private var mediaCodec: MediaCodec? = null
    private var mediaMuxer: MediaMuxer? = null
    private var trackIndex: Int = -1
    private var muxerStarted = false
    private var recordingSurface: Surface? = null
    private var isRecording = false

    fun start(context: Context, width: Int, height: Int): Surface? {
        if (isRecording) return recordingSurface

        val format = MediaFormat.createVideoFormat(VIDEO_MIME, width, height).apply {
            setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
            setInteger(MediaFormat.KEY_BIT_RATE, width * height * 8) // rough placeholder bitrate
            setInteger(MediaFormat.KEY_FRAME_RATE, 30)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
        }

        val outputFile = createOutputFile(context)
        mediaCodec = MediaCodec.createEncoderByType(VIDEO_MIME).apply {
            configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            recordingSurface = createInputSurface()
            start()
        }
        mediaMuxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        isRecording = true
        drainEncoderAsync()
        return recordingSurface
    }

    fun stop() {
        if (!isRecording) return
        isRecording = false
        drainEncoder()
        try {
            mediaCodec?.stop()
        } catch (_: Exception) { }
        try {
            mediaCodec?.release()
        } catch (_: Exception) { }
        try {
            if (muxerStarted) mediaMuxer?.stop()
        } catch (_: Exception) { }
        mediaMuxer?.release()
        mediaCodec = null
        mediaMuxer = null
        recordingSurface = null
        trackIndex = -1
        muxerStarted = false
    }

    fun isRecording(): Boolean = isRecording
    fun surface(): Surface? = recordingSurface

    private fun drainEncoderAsync() {
        // Placeholder: would move draining to a background thread/handler.
    }

    private fun drainEncoder() {
        val codec = mediaCodec ?: return
        val muxer = mediaMuxer ?: return
        val bufferInfo = MediaCodec.BufferInfo()

        while (true) {
            val outIndex = codec.dequeueOutputBuffer(bufferInfo, 10_000)
            when {
                outIndex == MediaCodec.INFO_TRY_AGAIN_LATER -> if (!isRecording) break
                outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                    if (muxerStarted) throw IllegalStateException("Format changed twice")
                    trackIndex = muxer.addTrack(codec.outputFormat)
                    muxer.start()
                    muxerStarted = true
                }
                outIndex >= 0 -> {
                    val encodedData = codec.getOutputBuffer(outIndex) ?: continue
                    if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                        bufferInfo.size = 0
                    }
                    if (bufferInfo.size != 0 && muxerStarted) {
                        encodedData.position(bufferInfo.offset)
                        encodedData.limit(bufferInfo.offset + bufferInfo.size)
                        muxer.writeSampleData(trackIndex, encodedData, bufferInfo)
                    }
                    codec.releaseOutputBuffer(outIndex, false)
                    if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) break
                }
            }
        }
    }

    private fun createOutputFile(context: Context): File {
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES) ?: context.filesDir
        val ts = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return File(dir, "VISUS_$ts.mp4")
    }

    companion object {
        private const val VIDEO_MIME = "video/avc"
    }
}
