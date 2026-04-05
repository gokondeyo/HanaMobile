package com.hanamobile.ui.screen.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hanamobile.ui.viewmodel.ChatUiState

@Composable
fun ChatScreen(
    state: ChatUiState,
    onInputChanged: (String) -> Unit,
    onSend: () -> Unit,
    onOpenVoice: () -> Unit,
    onOpenPrompt: () -> Unit,
    onOpenMemory: () -> Unit,
    onOpenSaved: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(onClick = onOpenVoice) { Text("Voice") }
            Button(onClick = onOpenPrompt) { Text("Prompt") }
            Button(onClick = onOpenMemory) { Text("Memory") }
            Button(onClick = onOpenSaved) { Text("Saved") }
        }

        Text("Active preset: ${state.activePresetName}")

        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(state.messages, key = { it.id }) {
                Card(Modifier.fillMaxWidth()) {
                    Text("${it.role}: ${it.content}", modifier = Modifier.padding(10.dp))
                }
            }
        }

        OutlinedTextField(
            value = state.userInput,
            onValueChange = onInputChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Type message") }
        )
        Button(onClick = onSend, enabled = !state.pending, modifier = Modifier.fillMaxWidth()) {
            Text(if (state.pending) "Thinking..." else "Send")
        }
    }
}
