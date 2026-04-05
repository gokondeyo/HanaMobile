package com.hanamobile.ui.screen.saved

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hanamobile.core.model.SavedChatSummary
import java.text.DateFormat
import java.util.Date

@Composable
fun SavedChatsScreen(
    chats: List<SavedChatSummary>,
    onBack: () -> Unit,
    onCreate: (String) -> Unit,
    onOpen: (String) -> Unit,
    onRename: (String, String) -> Unit,
    onDelete: (String) -> Unit,
    onNewChat: () -> Unit
) {
    val title = remember { mutableStateOf("New Chat") }
    Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = onBack) { Text("Back") }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = title.value, onValueChange = { title.value = it }, label = { Text("Chat title") })
            Button(onClick = { onCreate(title.value) }) { Text("Save") }
        }
        Button(onClick = onNewChat, modifier = Modifier.fillMaxWidth()) { Text("Start New Chat") }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(chats, key = { it.sessionId }) { chat ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(chat.title)
                        Text("Updated: ${DateFormat.getDateTimeInstance().format(Date(chat.updatedAt))}")
                        Text("Messages: ${chat.messageCount} | Prompt: ${chat.promptPresetId}")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { onOpen(chat.sessionId) }) { Text("Open") }
                            Button(onClick = { onRename(chat.sessionId, chat.title + " Renamed") }) { Text("Rename") }
                            Button(onClick = { onDelete(chat.sessionId) }) { Text("Delete") }
                        }
                    }
                }
            }
        }
    }
}
