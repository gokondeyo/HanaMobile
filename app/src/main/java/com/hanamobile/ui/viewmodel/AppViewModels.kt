package com.hanamobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hanamobile.core.extensions.SpeechToTextEngine
import com.hanamobile.core.extensions.TextToSpeechEngine
import com.hanamobile.core.extensions.WaveformAnimator
import com.hanamobile.core.model.ChatMessage
import com.hanamobile.core.model.MemoryCategory
import com.hanamobile.core.model.MemoryEntry
import com.hanamobile.core.model.MessageRole
import com.hanamobile.core.model.PromptPreset
import com.hanamobile.core.model.SavedChatSummary
import com.hanamobile.core.session.SessionManager
import com.hanamobile.data.repository.PromptRepositoryImpl
import com.hanamobile.domain.repository.ChatSessionRepository
import com.hanamobile.domain.repository.MemoryRepository
import com.hanamobile.domain.repository.PromptRepository
import com.hanamobile.domain.service.MemoryManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

data class ChatUiState(
    val sessionId: String = UUID.randomUUID().toString(),
    val messages: List<ChatMessage> = emptyList(),
    val userInput: String = "",
    val pending: Boolean = false,
    val activePresetName: String = "Default",
    val memoryPreview: String = ""
)

class ChatViewModel(
    private val promptRepository: PromptRepository,
    private val memoryRepository: MemoryRepository,
    private val sessionRepository: ChatSessionRepository,
    private val sessionManager: SessionManager,
    private val memoryManager: MemoryManager
) : ViewModel() {

    private val messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    private val input = MutableStateFlow("")
    private val pending = MutableStateFlow(false)
    private val presetName = MutableStateFlow("Default")
    private val memoryPreview = MutableStateFlow("")
    private val sessionId = MutableStateFlow(UUID.randomUUID().toString())

    val state: StateFlow<ChatUiState> = combine(
        messages, input, pending, presetName, memoryPreview, sessionId
    ) { m, i, p, n, mem, sid ->
        ChatUiState(sessionId = sid, messages = m, userInput = i, pending = p, activePresetName = n, memoryPreview = mem)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChatUiState())

    fun updateInput(value: String) { input.value = value }

    fun startNewSession() {
        messages.value = emptyList()
        sessionId.value = UUID.randomUUID().toString()
    }

    fun loadSession(sessionId: String) {
        viewModelScope.launch {
            val loaded = sessionRepository.loadSession(sessionId)
            this@ChatViewModel.sessionId.value = loaded.id
            messages.value = loaded.messageHistory
            input.value = ""
        }
    }

    fun send() {
        val userInput = input.value.trim()
        if (userInput.isEmpty() || pending.value) return

        viewModelScope.launch {
            pending.value = true
            val sid = sessionId.value
            val userMessage = ChatMessage(sessionId = sid, role = MessageRole.USER, content = userInput)
            messages.value = messages.value + userMessage
            sessionRepository.appendMessage(userMessage)
            input.value = ""

            val activePreset = promptRepository.getActivePreset()
            presetName.value = activePreset.name
            val globalMemory = memoryRepository.getActiveGlobal()
            val preview = memoryManager.buildPreview(globalMemory, emptyList())
            memoryPreview.value = preview.compiledText

            val (_, response) = sessionManager.runTurn(
                sessionId = sid,
                preset = activePreset,
                history = messages.value,
                userInput = userInput,
                globalMemory = globalMemory,
                sessionMemory = emptyList()
            )
            if (preview.compiledText.isNotBlank()) {
                val memMessage = sessionManager.toMemoryInjectionMessage(sid, preview.compiledText)
                messages.value = messages.value + memMessage
            }
            val assistant = ChatMessage(sessionId = sid, role = MessageRole.ASSISTANT, content = response.text)
            messages.value = messages.value + assistant
            sessionRepository.appendMessage(assistant)
            pending.value = false
        }
    }
}

data class PromptUiState(
    val presets: List<PromptPreset> = emptyList(),
    val activePresetId: String = PromptRepositoryImpl.DEFAULT_PRESET_ID,
    val editorText: String = ""
)

class PromptSettingsViewModel(
    private val repository: PromptRepository
) : ViewModel() {
    private val editorText = MutableStateFlow("")

    val state: StateFlow<PromptUiState> = combine(
        repository.observePresets(),
        repository.observeActivePresetId(),
        editorText
    ) { presets, active, editor ->
        PromptUiState(
            presets = presets,
            activePresetId = active,
            editorText = if (editor.isBlank()) presets.firstOrNull { it.id == active }?.systemPrompt ?: "" else editor
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PromptUiState())

    fun updateEditor(text: String) { editorText.value = text }

    fun saveCurrentPreset() {
        viewModelScope.launch {
            val s = state.value
            val current = s.presets.firstOrNull { it.id == s.activePresetId } ?: return@launch
            repository.upsertPreset(current.copy(systemPrompt = s.editorText, updatedAt = System.currentTimeMillis()))
        }
    }

    fun resetCurrentPreset() { viewModelScope.launch { repository.resetPresetToDefault(state.value.activePresetId) } }
    fun setActivePreset(id: String) { viewModelScope.launch { repository.setActivePreset(id); editorText.value = "" } }
    fun createPreset(name: String) { viewModelScope.launch { repository.upsertPreset(PromptPreset(name = name, systemPrompt = "")) } }
    fun renamePreset(id: String, newName: String) { viewModelScope.launch { repository.renamePreset(id, newName) } }
    fun deletePreset(id: String) { viewModelScope.launch { repository.deletePreset(id) } }
}

data class MemoryUiState(
    val entries: List<MemoryEntry> = emptyList(),
    val injectionPreview: String = ""
)

class MemorySettingsViewModel(
    private val repository: MemoryRepository,
    private val memoryManager: MemoryManager
) : ViewModel() {
    val state: StateFlow<MemoryUiState> = repository.observeGlobalMemory().combine(MutableStateFlow(Unit)) { entries, _ ->
        val preview = memoryManager.buildPreview(entries, emptyList()).compiledText
        MemoryUiState(entries = entries, injectionPreview = preview)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MemoryUiState())

    fun upsert(title: String, content: String, category: MemoryCategory, id: String? = null) {
        viewModelScope.launch {
            repository.upsert(
                MemoryEntry(
                    id = id ?: UUID.randomUUID().toString(),
                    title = title,
                    content = content,
                    category = category
                )
            )
        }
    }

    fun toggle(id: String, enabled: Boolean) { viewModelScope.launch { repository.setEnabled(id, enabled) } }
    fun delete(id: String) { viewModelScope.launch { repository.delete(id) } }
}

class SavedChatsViewModel(
    private val repository: ChatSessionRepository,
    private val promptRepository: PromptRepository,
) : ViewModel() {
    val chats: StateFlow<List<SavedChatSummary>> = repository.observeSavedChats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun create(title: String) {
        viewModelScope.launch {
            val active = promptRepository.observeActivePresetId().first()
            repository.createSession(title, active)
        }
    }

    fun rename(id: String, title: String) { viewModelScope.launch { repository.renameSession(id, title) } }
    fun delete(id: String) { viewModelScope.launch { repository.deleteSession(id) } }
}

enum class VoicePipelineState { IDLE, LISTENING, THINKING, SPEAKING, ERROR }

data class VoiceUiState(
    val state: VoicePipelineState = VoicePipelineState.IDLE,
    val transcript: String = "",
    val response: String = "",
    val waveform: List<Float> = List(20) { 0f }
)

class VoiceChatViewModel(
    private val stt: SpeechToTextEngine,
    private val tts: TextToSpeechEngine,
    private val sessionManager: SessionManager,
    private val promptRepository: PromptRepository,
    private val memoryRepository: MemoryRepository,
    waveformAnimator: WaveformAnimator
) : ViewModel() {
    private val ui = MutableStateFlow(VoiceUiState())
    val state: StateFlow<VoiceUiState> = ui

    init {
        viewModelScope.launch {
            waveformAnimator.bindAmplitude(tts.amplitudeFlow()).collect {
                ui.value = ui.value.copy(waveform = it)
            }
        }
    }

    fun startListening() {
        if (ui.value.state == VoicePipelineState.SPEAKING) return
        viewModelScope.launch {
            ui.value = ui.value.copy(state = VoicePipelineState.LISTENING, transcript = "")
            stt.startListening { partial -> ui.value = ui.value.copy(transcript = partial) }
        }
    }

    fun stopAndProcess(sessionId: String, history: List<ChatMessage>) {
        if (ui.value.state != VoicePipelineState.LISTENING) return
        viewModelScope.launch {
            ui.value = ui.value.copy(state = VoicePipelineState.THINKING)
            val text = stt.stopListening()
            val preset = promptRepository.getActivePreset()
            val globalMemory = memoryRepository.getActiveGlobal()
            val (_, response) = sessionManager.runTurn(
                sessionId = sessionId,
                preset = preset,
                history = history,
                userInput = text,
                globalMemory = globalMemory,
                sessionMemory = emptyList()
            )
            ui.value = ui.value.copy(state = VoicePipelineState.SPEAKING, response = response.text)
            tts.speak(response.text)
            ui.value = ui.value.copy(state = VoicePipelineState.IDLE)
        }
    }

    fun stopPlayback() {
        viewModelScope.launch {
            tts.stop()
            ui.value = ui.value.copy(state = VoicePipelineState.IDLE)
        }
    }
}

class HanaViewModelFactory(
    private val promptRepository: PromptRepository,
    private val memoryRepository: MemoryRepository,
    private val sessionRepository: ChatSessionRepository,
    private val sessionManager: SessionManager,
    private val memoryManager: MemoryManager,
    private val stt: SpeechToTextEngine,
    private val tts: TextToSpeechEngine,
    private val waveformAnimator: WaveformAnimator
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return when (modelClass) {
            ChatViewModel::class.java -> ChatViewModel(promptRepository, memoryRepository, sessionRepository, sessionManager, memoryManager)
            PromptSettingsViewModel::class.java -> PromptSettingsViewModel(promptRepository)
            MemorySettingsViewModel::class.java -> MemorySettingsViewModel(memoryRepository, memoryManager)
            SavedChatsViewModel::class.java -> SavedChatsViewModel(sessionRepository, promptRepository)
            VoiceChatViewModel::class.java -> VoiceChatViewModel(stt, tts, sessionManager, promptRepository, memoryRepository, waveformAnimator)
            else -> error("Unknown ViewModel: ${modelClass.simpleName}")
        } as T
    }
}
