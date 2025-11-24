package com.visus.app.model

data class VisualizerState(
    val fps: Int,
    val bass: Float,
    val mid: Float,
    val high: Float,
    val isRecording: Boolean,
    val aspectRatio: Float
)
