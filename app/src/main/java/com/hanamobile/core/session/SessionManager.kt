package com.hanamobile.core.session

import com.hanamobile.core.extensions.LocalInferenceBackend
import com.hanamobile.core.extensions.SoulEngineIntervention
import com.hanamobile.core.model.BackendRequest
import com.hanamobile.core.model.BackendResponse
import com.hanamobile.core.model.ChatMessage
import com.hanamobile.core.model.MessageRole
import com.hanamobile.core.model.MultimodalPayload
import com.hanamobile.core.model.PromptPreset
import com.hanamobile.core.model.ToolResult
import com.hanamobile.domain.service.MemoryManager

class SessionManager(
    private val backend: LocalInferenceBackend,
    private val memoryManager: MemoryManager,
    private val soulEngine: SoulEngineIntervention? = null
) {
    suspend fun runTurn(
        sessionId: String,
        preset: PromptPreset,
        history: List<ChatMessage>,
        userInput: String,
        globalMemory: List<com.hanamobile.core.model.MemoryEntry>,
        sessionMemory: List<com.hanamobile.core.model.MemoryEntry>,
        tools: List<ToolResult> = emptyList(),
        multimodalPayload: MultimodalPayload? = null
    ): Pair<BackendRequest, BackendResponse> {
        val memoryPreview = memoryManager.buildPreview(globalMemory, sessionMemory)
        val request = BackendRequest(
            sessionId = sessionId,
            systemPrompt = preset.systemPrompt,
            memoryBlock = memoryPreview.compiledText,
            history = history,
            userInput = userInput,
            toolResults = tools,
            multimodalPayload = multimodalPayload,
            soulInterventionContext = mapOf("activePreset" to preset.name)
        )

        val preprocessed = soulEngine?.beforeGeneration(request) ?: request
        val firstResponse = backend.generate(preprocessed)
        val review = soulEngine?.reviewResponse(preprocessed, firstResponse)
        val finalResponse = if (review?.requestedRetry == true && !review.transformedCandidate.isNullOrBlank()) {
            BackendResponse(text = review.transformedCandidate)
        } else {
            firstResponse
        }
        return preprocessed to finalResponse
    }

    fun toMemoryInjectionMessage(sessionId: String, text: String): ChatMessage =
        ChatMessage(sessionId = sessionId, role = MessageRole.MEMORY, content = text)
}
