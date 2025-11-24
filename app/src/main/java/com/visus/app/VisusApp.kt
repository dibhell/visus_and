package com.visus.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.visus.app.ui.VisusScreen

private val visusColors = darkColorScheme(
    primary = Color(0xFFA78BFA),
    secondary = Color(0xFF38BDF8),
    tertiary = Color(0xFFFBBF24),
    background = Color(0xFF020617),
    surface = Color(0xFF0B1224)
)

@Composable
fun VisusApp() {
    MaterialTheme(
        colorScheme = visusColors
    ) {
        VisusScreen()
    }
}
