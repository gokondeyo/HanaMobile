package com.hanamobile.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.hanamobile.ui.navigation.HanaNavHost
import com.hanamobile.ui.viewmodel.HanaViewModelFactory

class MainActivity : ComponentActivity() {
    private val appContainer by lazy { (application as HanaApplication).container }
    private val factory by lazy {
        HanaViewModelFactory(
            promptRepository = appContainer.promptRepository,
            memoryRepository = appContainer.memoryRepository,
            sessionRepository = appContainer.chatSessionRepository,
            sessionManager = appContainer.sessionManager,
            memoryManager = appContainer.memoryManager,
            stt = appContainer.stt,
            tts = appContainer.tts,
            waveformAnimator = appContainer.waveformAnimator
        )
    }

    private val chatVm by viewModels<com.hanamobile.ui.viewmodel.ChatViewModel> { factory }
    private val promptVm by viewModels<com.hanamobile.ui.viewmodel.PromptSettingsViewModel> { factory }
    private val memoryVm by viewModels<com.hanamobile.ui.viewmodel.MemorySettingsViewModel> { factory }
    private val savedVm by viewModels<com.hanamobile.ui.viewmodel.SavedChatsViewModel> { factory }
    private val voiceVm by viewModels<com.hanamobile.ui.viewmodel.VoiceChatViewModel> { factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    HanaNavHost(chatVm, voiceVm, promptVm, memoryVm, savedVm)
                }
            }
        }
    }
}
