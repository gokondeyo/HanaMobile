package com.hanamobile.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hanamobile.ui.screen.chat.ChatScreen
import com.hanamobile.ui.screen.memory.MemorySettingsScreen
import com.hanamobile.ui.screen.prompt.PromptSettingsScreen
import com.hanamobile.ui.screen.saved.SavedChatsScreen
import com.hanamobile.ui.screen.voice.VoiceChatScreen
import com.hanamobile.ui.viewmodel.ChatViewModel
import com.hanamobile.ui.viewmodel.MemorySettingsViewModel
import com.hanamobile.ui.viewmodel.PromptSettingsViewModel
import com.hanamobile.ui.viewmodel.SavedChatsViewModel
import com.hanamobile.ui.viewmodel.VoiceChatViewModel

object Destinations {
    const val CHAT = "chat"
    const val VOICE = "voice"
    const val PROMPT = "prompt"
    const val MEMORY = "memory"
    const val SAVED = "saved"
}

@Composable
fun HanaNavHost(
    chatVm: ChatViewModel,
    voiceVm: VoiceChatViewModel,
    promptVm: PromptSettingsViewModel,
    memoryVm: MemorySettingsViewModel,
    savedVm: SavedChatsViewModel
) {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Destinations.CHAT) {
        composable(Destinations.CHAT) {
            val state = chatVm.state.collectAsState().value
            ChatScreen(
                state = state,
                onInputChanged = chatVm::updateInput,
                onSend = chatVm::send,
                onOpenVoice = { nav.navigate(Destinations.VOICE) },
                onOpenPrompt = { nav.navigate(Destinations.PROMPT) },
                onOpenMemory = { nav.navigate(Destinations.MEMORY) },
                onOpenSaved = { nav.navigate(Destinations.SAVED) }
            )
        }
        composable(Destinations.VOICE) {
            val voiceState = voiceVm.state.collectAsState().value
            val chatState = chatVm.state.collectAsState().value
            VoiceChatScreen(
                state = voiceState,
                onBack = { nav.popBackStack() },
                onStartListening = voiceVm::startListening,
                onStopListening = { voiceVm.stopAndProcess(chatState.sessionId, chatState.messages) },
                onStopPlayback = voiceVm::stopPlayback
            )
        }
        composable(Destinations.PROMPT) {
            val state = promptVm.state.collectAsState().value
            PromptSettingsScreen(
                state = state,
                onBack = { nav.popBackStack() },
                onEditorChange = promptVm::updateEditor,
                onSave = promptVm::saveCurrentPreset,
                onReset = promptVm::resetCurrentPreset,
                onCreatePreset = promptVm::createPreset,
                onRenamePreset = promptVm::renamePreset,
                onDeletePreset = promptVm::deletePreset,
                onApplyPreset = promptVm::setActivePreset
            )
        }
        composable(Destinations.MEMORY) {
            val state = memoryVm.state.collectAsState().value
            MemorySettingsScreen(
                state = state,
                onBack = { nav.popBackStack() },
                onUpsert = memoryVm::upsert,
                onToggle = memoryVm::toggle,
                onDelete = memoryVm::delete
            )
        }
        composable(Destinations.SAVED) {
            val state = savedVm.chats.collectAsState().value
            SavedChatsScreen(
                chats = state,
                onBack = { nav.popBackStack() },
                onCreate = savedVm::create,
                onOpen = {
                    chatVm.loadSession(it)
                    nav.popBackStack(Destinations.CHAT, false)
                },
                onRename = savedVm::rename,
                onDelete = savedVm::delete,
                onNewChat = {
                    chatVm.startNewSession()
                    nav.popBackStack(Destinations.CHAT, false)
                }
            )
        }
    }
}
