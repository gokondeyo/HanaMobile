package com.hanamobile.ui.screen.memory

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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hanamobile.core.model.MemoryCategory
import com.hanamobile.ui.viewmodel.MemoryUiState

@Composable
fun MemorySettingsScreen(
    state: MemoryUiState,
    onBack: () -> Unit,
    onUpsert: (String, String, MemoryCategory, String?) -> Unit,
    onToggle: (String, Boolean) -> Unit,
    onDelete: (String) -> Unit
) {
    val title = remember { mutableStateOf("") }
    val content = remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = onBack) { Text("Back") }
        OutlinedTextField(title.value, { title.value = it }, label = { Text("Memory title") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(content.value, { content.value = it }, label = { Text("Memory content") }, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { onUpsert(title.value, content.value, MemoryCategory.CUSTOM, null) }) { Text("Add") }
        }
        Text("Active Injection Preview:")
        Card(Modifier.fillMaxWidth()) { Text(state.injectionPreview, Modifier.padding(8.dp)) }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(state.entries, key = { it.id }) { entry ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Checkbox(checked = entry.enabled, onCheckedChange = { onToggle(entry.id, it) })
                            Text("[${entry.category}] ${entry.title}")
                        }
                        Text(entry.content)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = {
                                onUpsert(entry.title + "*", entry.content, entry.category, entry.id)
                            }) { Text("Edit") }
                            Button(onClick = { onDelete(entry.id) }) { Text("Delete") }
                        }
                    }
                }
            }
        }
    }
}
