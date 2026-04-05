package com.hanamobile.domain.repository

import com.hanamobile.core.model.ChatMessage
import com.hanamobile.core.model.ChatSession
import com.hanamobile.core.model.MemoryEntry
import com.hanamobile.core.model.PromptPreset
import com.hanamobile.core.model.SavedChatSummary
import kotlinx.coroutines.flow.Flow

interface PromptRepository {
    fun observePresets(): Flow<List<PromptPreset>>
    fun observeActivePresetId(): Flow<String>
    suspend fun getActivePreset(): PromptPreset
    suspend fun upsertPreset(preset: PromptPreset)
    suspend fun renamePreset(presetId: String, newName: String)
    suspend fun deletePreset(presetId: String)
    suspend fun setActivePreset(presetId: String)
    suspend fun resetPresetToDefault(presetId: String)
}

interface MemoryRepository {
    fun observeGlobalMemory(): Flow<List<MemoryEntry>>
    fun observeSessionMemory(sessionId: String): Flow<List<MemoryEntry>>
    suspend fun upsert(entry: MemoryEntry)
    suspend fun delete(entryId: String)
    suspend fun setEnabled(entryId: String, enabled: Boolean)
    suspend fun getActiveGlobal(): List<MemoryEntry>
}

interface ChatSessionRepository {
    fun observeMessages(sessionId: String): Flow<List<ChatMessage>>
    fun observeSavedChats(): Flow<List<SavedChatSummary>>
    suspend fun createSession(title: String, promptPresetId: String): ChatSession
    suspend fun appendMessage(message: ChatMessage)
    suspend fun loadSession(sessionId: String): ChatSession
    suspend fun renameSession(sessionId: String, newName: String)
    suspend fun deleteSession(sessionId: String)
    suspend fun saveSession(session: ChatSession)
}
