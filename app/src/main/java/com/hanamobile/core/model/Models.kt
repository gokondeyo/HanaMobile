package com.hanamobile.core.model

import java.util.UUID

enum class MessageRole { SYSTEM, MEMORY, USER, ASSISTANT, TOOL }

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sessionId: String,
    val role: MessageRole,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class SessionMetadata(
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val title: String = "New Chat",
    val memorySnapshotOnSave: Boolean = false
)

data class ChatSession(
    val id: String = UUID.randomUUID().toString(),
    val metadata: SessionMetadata = SessionMetadata(),
    val activePromptPresetId: String,
    val messageHistory: List<ChatMessage> = emptyList(),
    val perSessionMemory: List<MemoryEntry> = emptyList(),
    val globalMemoryRefs: List<String> = emptyList()
)

data class PromptPreset(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val systemPrompt: String,
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class MemoryCategory {
    USER_PREFERENCE,
    LONG_TERM_PROJECT_INFO,
    PERSONA_RULE,
    PINNED_FACT,
    CUSTOM
}

data class MemoryEntry(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val category: MemoryCategory,
    val enabled: Boolean = true,
    val scopeSessionId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class MemoryInjectionPreview(
    val globalActiveEntries: List<MemoryEntry>,
    val sessionEntries: List<MemoryEntry>,
    val compiledText: String
)

data class SavedChatSummary(
    val sessionId: String,
    val title: String,
    val updatedAt: Long,
    val messageCount: Int,
    val promptPresetId: String
)

data class ToolResult(
    val toolName: String,
    val outputText: String
)

data class MultimodalPayload(
    val images: List<String> = emptyList(),
    val metadata: Map<String, String> = emptyMap()
)

data class SoulInterventionNote(
    val requestedRetry: Boolean,
    val rationale: String,
    val transformedCandidate: String? = null
)

data class BackendRequest(
    val sessionId: String,
    val systemPrompt: String,
    val memoryBlock: String,
    val history: List<ChatMessage>,
    val userInput: String,
    val toolResults: List<ToolResult> = emptyList(),
    val multimodalPayload: MultimodalPayload? = null,
    val soulInterventionContext: Map<String, String> = emptyMap()
)

data class BackendResponse(
    val text: String,
    val tokensUsed: Int? = null,
    val diagnostics: Map<String, String> = emptyMap()
)
