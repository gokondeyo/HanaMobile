package com.hanamobile.data.repository

import com.hanamobile.core.model.ChatMessage
import com.hanamobile.core.model.ChatSession
import com.hanamobile.core.model.MemoryEntry
import com.hanamobile.core.model.PromptPreset
import com.hanamobile.core.model.SavedChatSummary
import com.hanamobile.core.model.SessionMetadata
import com.hanamobile.data.local.entity.ChatMessageEntity
import com.hanamobile.data.local.entity.ChatSessionEntity
import com.hanamobile.data.local.entity.MemoryEntryEntity
import com.hanamobile.data.local.entity.PromptPresetEntity

internal fun PromptPresetEntity.toModel() = PromptPreset(
    id = id,
    name = name,
    systemPrompt = systemPrompt,
    isDefault = isDefault,
    createdAt = createdAt,
    updatedAt = updatedAt
)

internal fun PromptPreset.toEntity() = PromptPresetEntity(
    id = id,
    name = name,
    systemPrompt = systemPrompt,
    isDefault = isDefault,
    createdAt = createdAt,
    updatedAt = updatedAt
)

internal fun MemoryEntryEntity.toModel() = MemoryEntry(
    id = id,
    title = title,
    content = content,
    category = category,
    enabled = enabled,
    scopeSessionId = scopeSessionId,
    createdAt = createdAt,
    updatedAt = updatedAt
)

internal fun MemoryEntry.toEntity() = MemoryEntryEntity(
    id = id,
    title = title,
    content = content,
    category = category,
    enabled = enabled,
    scopeSessionId = scopeSessionId,
    createdAt = createdAt,
    updatedAt = updatedAt
)

internal fun ChatMessageEntity.toModel() = ChatMessage(
    id = id,
    sessionId = sessionId,
    role = role,
    content = content,
    timestamp = timestamp
)

internal fun ChatMessage.toEntity() = ChatMessageEntity(
    id = id,
    sessionId = sessionId,
    role = role,
    content = content,
    timestamp = timestamp
)

internal fun ChatSessionEntity.toSummary(messageCount: Int) = SavedChatSummary(
    sessionId = id,
    title = title,
    updatedAt = updatedAt,
    messageCount = messageCount,
    promptPresetId = activePromptPresetId
)

internal fun ChatSessionEntity.toModel(messages: List<ChatMessage>) = ChatSession(
    id = id,
    metadata = SessionMetadata(
        createdAt = createdAt,
        updatedAt = updatedAt,
        title = title,
        memorySnapshotOnSave = memorySnapshotOnSave
    ),
    activePromptPresetId = activePromptPresetId,
    messageHistory = messages
)
