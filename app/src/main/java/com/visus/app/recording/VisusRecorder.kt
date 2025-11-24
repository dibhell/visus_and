package com.visus.app.recording

import android.view.Surface

class VisusRecorder {
    private var isRecording = false

    fun start(surfaceProvider: () -> Surface) {
        // TODO: build MediaCodec video encoder, create input Surface, and render GL scene into it.
        isRecording = true
    }

    fun stop() {
        // TODO: stop muxer/codec and finalize MP4 file.
        isRecording = false
    }

    fun isRecording(): Boolean = isRecording
}
