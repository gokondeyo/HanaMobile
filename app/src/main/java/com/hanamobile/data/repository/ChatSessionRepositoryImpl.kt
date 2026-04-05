package com.hanamobile.data.repository

import com.hanamobile.core.model.ChatMessage
import com.hanamobile.core.model.ChatSession
import com.hanamobile.core.model.SavedChatSummary
import com.hanamobile.core.model.SessionMetadata
import com.hanamobile.data.local.dao.ChatDao
import com.hanamobile.domain.repository.ChatSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class ChatSessionRepositoryImpl(
    private val dao: ChatDao
) : ChatSessionRepository {
    override fun observeMessages(sessionId: String): Flow<List<ChatMessage>> =
        dao.observeMessages(sessionId).map { list -> list.map { it.toModel() } }

    override fun observeSavedChats(): Flow<List<SavedChatSummary>> =
        dao.observeSessions().map { sessions ->
            sessions.map { session ->
                val count = dao.observeMessages(session.id).first().size
                session.toSummary(count)
            }
        }

    override suspend fun createSession(title: String, promptPresetId: String): ChatSession {
        val session = ChatSession(
            metadata = SessionMetadata(title = title),
            activePromptPresetId = promptPresetId
        )
        dao.upsertSession(
            com.hanamobile.data.local.entity.ChatSessionEntity(
                id = session.id,
                title = title,
                activePromptPresetId = promptPresetId,
                createdAt = session.metadata.createdAt,
                updatedAt = session.metadata.updatedAt,
                memorySnapshotOnSave = false
            )
        )
        return session
    }

    override suspend fun appendMessage(message: ChatMessage) {
        dao.appendMessage(message.toEntity())
        dao.getSession(message.sessionId)?.let {
            dao.upsertSession(it.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    override suspend fun loadSession(sessionId: String): ChatSession {
        val session = dao.getSession(sessionId)
            ?: error("Session not found: $sessionId")
        val messages = dao.getMessages(sessionId).map { it.toModel() }
        return session.toModel(messages)
    }

    override suspend fun renameSession(sessionId: String, newName: String) {
        dao.renameSession(sessionId, newName, System.currentTimeMillis())
    }

    override suspend fun deleteSession(sessionId: String) {
        dao.deleteMessagesForSession(sessionId)
        dao.deleteSession(sessionId)
    }

    override suspend fun saveSession(session: ChatSession) {
        dao.upsertSession(
            com.hanamobile.data.local.entity.ChatSessionEntity(
                id = session.id,
                title = session.metadata.title,
                activePromptPresetId = session.activePromptPresetId,
                createdAt = session.metadata.createdAt,
                updatedAt = System.currentTimeMillis(),
                memorySnapshotOnSave = session.metadata.memorySnapshotOnSave
            )
        )
        session.messageHistory.forEach { dao.appendMessage(it.toEntity()) }
    }
}
