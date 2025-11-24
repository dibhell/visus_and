package com.visus.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import com.visus.app.audio.AudioAnalyzer
import com.visus.app.engine.CameraController
import com.visus.app.engine.VisusRenderer
import com.visus.app.model.VisualizerState
import com.visus.app.ui.components.GlPreview
import com.visus.app.recording.VisusRecorder
import kotlinx.coroutines.delay

@Composable
fun VisusScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val renderer = remember { VisusRenderer(context) }
    val cameraController = remember { CameraController(context) }
    val audioAnalyzer = remember { AudioAnalyzer() }
    val recorder = remember { VisusRecorder() }
    var uiState by remember {
        mutableStateOf(
            VisualizerState(
                fps = 60,
                bass = 0.2f,
                mid = 0.35f,
                high = 0.15f,
                isRecording = false,
                aspectRatio = 16f / 9f
            )
        )
    }
    val bandLevels by audioAnalyzer.bands.collectAsState()

    LaunchedEffect(bandLevels) {
        renderer.updateBands(bandLevels.first, bandLevels.second, bandLevels.third)
        uiState = uiState.copy(
            bass = bandLevels.first,
            mid = bandLevels.second,
            high = bandLevels.third
        )
    }

    LaunchedEffect(renderer, lifecycleOwner) {
        while (renderer.getCameraSurfaceTexture() == null) {
            delay(50)
        }
        renderer.getCameraSurfaceTexture()?.let { surfaceTexture ->
            cameraController.startCamera(lifecycleOwner, surfaceTexture)
        }
    }

    DisposableEffect(Unit) {
        audioAnalyzer.start()
        onDispose {
            audioAnalyzer.stop()
            cameraController.stopCamera()
            cameraController.shutdown()
            renderer.detachRecordingSurface()
            recorder.stop()
        }
    }

    val startRecording: () -> Unit = {
        val (w, h) = renderer.getViewportSize().let { size ->
            if (size.first <= 0 || size.second <= 0) 1080 to 1920 else size
        }
        val surface = recorder.start(context, w, h)
        if (surface != null) {
            renderer.attachRecordingSurface(surface, w, h)
            uiState = uiState.copy(isRecording = true)
        }
    }

    val stopRecording: () -> Unit = {
        renderer.detachRecordingSurface()
        recorder.stop()
        uiState = uiState.copy(isRecording = false)
    }

    val gradient = Brush.verticalGradient(
        listOf(
            Color(0xFF0B1224),
            Color(0xFF0B1224),
            Color(0xFF020617)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TopStatusBar(uiState)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(uiState.aspectRatio),
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.25f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                GlPreview(renderer = renderer)
            }

            SpectrumBars(uiState)

            ControlPanel(
                uiState = uiState,
                onToggleRecording = {
                    if (recorder.isRecording()) stopRecording() else startRecording()
                },
                onAspectChanged = { ratio -> uiState = uiState.copy(aspectRatio = ratio) }
            )
        }
    }
}

@Composable
private fun TopStatusBar(state: VisualizerState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "VISUS Native", style = MaterialTheme.typography.labelLarge)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatusChip(text = "FPS ${state.fps}", tint = MaterialTheme.colorScheme.primary)
                StatusChip(text = if (state.isRecording) "REC" else "LIVE", tint = if (state.isRecording) Color.Red else MaterialTheme.colorScheme.secondary)
            }
        }
    }
}

@Composable
private fun StatusChip(text: String, tint: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(color = tint.copy(alpha = 0.18f), shape = RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color = tint, shape = RoundedCornerShape(50))
        )
        Spacer(modifier = Modifier.size(6.dp))
        Text(text = text, color = Color.White, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun SpectrumBars(state: VisualizerState) {
    val bars = listOf(state.bass, state.mid, state.high)
    val barColors = listOf(
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.primary
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            val barWidth = size.width / (bars.size * 2)
            val maxHeight = size.height
            bars.forEachIndexed { index, value ->
                val height = value.coerceIn(0f, 1f) * maxHeight
                val x = barWidth + index * barWidth * 2
                drawRoundRect(
                    color = barColors.getOrElse(index) { barColors.last() },
                    topLeft = androidx.compose.ui.geometry.Offset(x, maxHeight - height),
                    size = androidx.compose.ui.geometry.Size(barWidth, height),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
                )
            }
        }
    }
}

@Composable
private fun ControlPanel(
    uiState: VisualizerState,
    onToggleRecording: () -> Unit,
    onAspectChanged: (Float) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onToggleRecording) {
                    Icon(
                        imageVector = if (uiState.isRecording) Icons.Default.Stop else Icons.Default.FiberManualRecord,
                        contentDescription = null,
                        tint = if (uiState.isRecording) Color.Red else MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(text = if (uiState.isRecording) "Stop" else "Record")
                }
                TextButton(onClick = { /* TODO: open shader picker */ }) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Load Shader")
                }
            }

            Text(text = "Aspect ratio", style = MaterialTheme.typography.labelMedium)
            AspectSlider(current = uiState.aspectRatio, onAspectChanged = onAspectChanged)
        }
    }
}

@Composable
private fun AspectSlider(current: Float, onAspectChanged: (Float) -> Unit) {
    var sliderValue by remember { mutableStateOf(current) }
    Column {
        Slider(
            value = sliderValue,
            onValueChange = {
                sliderValue = it
                onAspectChanged(it)
            },
            valueRange = 0.5f..2.5f
        )
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(text = "9:16")
            Text(text = "1:1")
            Text(text = "16:9")
        }
    }
}
