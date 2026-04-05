package com.hanamobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.hanamobile.core.model.MemoryCategory
import com.hanamobile.core.model.MessageRole

@Entity(tableName = "prompt_presets")
data class PromptPresetEntity(
    @PrimaryKey val id: String,
    val name: String,
    val systemPrompt: String,
    val isDefault: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "app_settings")
data class AppSettingEntity(
    @PrimaryKey val key: String,
    val value: String
)

@Entity(tableName = "memory_entries")
data class MemoryEntryEntity(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val category: MemoryCategory,
    val enabled: Boolean,
    val scopeSessionId: String?,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "chat_sessions")
data class ChatSessionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val activePromptPresetId: String,
    val createdAt: Long,
    val updatedAt: Long,
    val memorySnapshotOnSave: Boolean
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val role: MessageRole,
    val content: String,
    val timestamp: Long
)
