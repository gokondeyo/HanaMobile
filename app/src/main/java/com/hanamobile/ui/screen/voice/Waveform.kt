package com.hanamobile.ui.screen.voice

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun WaveformView(bars: List<Float>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxWidth().height(96.dp)) {
        val barWidth = size.width / (bars.size * 1.8f)
        var x = barWidth
        bars.forEach { amp ->
            val h = (size.height * amp).coerceAtLeast(size.height * 0.08f)
            drawRoundRect(
                color = Color(0xFF43A047),
                topLeft = androidx.compose.ui.geometry.Offset(x, (size.height - h) / 2),
                size = androidx.compose.ui.geometry.Size(barWidth, h),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
            )
            x += barWidth * 1.8f
        }
    }
}
