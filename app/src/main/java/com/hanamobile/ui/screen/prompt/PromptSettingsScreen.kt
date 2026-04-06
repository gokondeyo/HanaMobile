package com.hanamobile.ui.screen.prompt

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
import com.hanamobile.ui.viewmodel.PromptUiState

@Composable
fun PromptSettingsScreen(
    state: PromptUiState,
    onBack: () -> Unit,
    onEditorChange: (String) -> Unit,
    onSave: () -> Unit,
    onReset: () -> Unit,
    onCreatePreset: (String) -> Unit,
    onRenamePreset: (String, String) -> Unit,
    onDeletePreset: (String) -> Unit,
    onApplyPreset: (String) -> Unit,
    onRefreshModels: () -> Unit,
    onApplyModel: (String) -> Unit
) {
    val newName = remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = onBack) { Text("Back") }
        Text("Prompt changes apply to new turns. Use New Chat for full reset.")
        OutlinedTextField(state.editorText, onEditorChange, Modifier.fillMaxWidth(), label = { Text("System Prompt") })
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onSave) { Text("Save") }
            Button(onClick = onReset) { Text("Reset") }
        }

        OutlinedTextField(newName.value, { newName.value = it }, label = { Text("Preset name") })
        Button(onClick = { if (newName.value.isNotBlank()) onCreatePreset(newName.value) }) { Text("Create Preset") }

        Text("Model Directory: ${state.modelDirectoryPath}")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onRefreshModels) { Text("Refresh Models") }
        }

        if (state.availableModelFiles.isEmpty()) {
            Text("No model files found in models directory.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(state.availableModelFiles, key = { it }) { modelFile ->
                    Card(Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("$modelFile${if (modelFile == state.activeModelFile) " (Active)" else ""}")
                            Button(onClick = { onApplyModel(modelFile) }) { Text("Use") }
                        }
                    }
                }
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(state.presets, key = { it.id }) { preset ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("${preset.name}${if (preset.id == state.activePresetId) " (Active)" else ""}")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { onApplyPreset(preset.id) }) { Text("Apply") }
                            Button(onClick = { onRenamePreset(preset.id, preset.name + " Renamed") }) { Text("Rename") }
                            Button(onClick = { onDeletePreset(preset.id) }) { Text("Delete") }
                        }
                    }
                }
            }
        }
    }
}
