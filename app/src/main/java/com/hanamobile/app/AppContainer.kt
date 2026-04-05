package com.hanamobile.app

import com.hanamobile.core.extensions.SpeechToTextEngine
import com.hanamobile.core.extensions.TextToSpeechEngine
import com.hanamobile.core.extensions.WaveformAnimator
import com.hanamobile.core.session.SessionManager
import com.hanamobile.domain.repository.ChatSessionRepository
import com.hanamobile.domain.repository.MemoryRepository
import com.hanamobile.domain.repository.PromptRepository
import com.hanamobile.domain.service.MemoryManager

class AppContainer(
    val promptRepository: PromptRepository,
    val memoryRepository: MemoryRepository,
    val chatSessionRepository: ChatSessionRepository,
    val sessionManager: SessionManager,
    val memoryManager: MemoryManager,
    val stt: SpeechToTextEngine,
    val tts: TextToSpeechEngine,
    val waveformAnimator: WaveformAnimator
)
