package com.hanamobile.ui.screen.voice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hanamobile.ui.viewmodel.VoiceUiState

@Composable
fun VoiceChatScreen(
    state: VoiceUiState,
    onBack: () -> Unit,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onStopPlayback: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(onClick = onBack) { Text("Back to Chat") }
        Text("State: ${state.state}")
        Text("Transcript: ${state.transcript}")
        Text("Assistant: ${state.response}")
        WaveformView(bars = state.waveform, modifier = Modifier.fillMaxWidth())
        Button(onClick = onStartListening, modifier = Modifier.fillMaxWidth()) { Text("Start Listening") }
        Button(onClick = onStopListening, modifier = Modifier.fillMaxWidth()) { Text("Stop + Process") }
        Button(onClick = onStopPlayback, modifier = Modifier.fillMaxWidth()) { Text("Stop Playback") }
    }
}
