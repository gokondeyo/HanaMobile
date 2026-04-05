package com.hanamobile.data.repository

import com.hanamobile.core.model.MemoryEntry
import com.hanamobile.data.local.dao.MemoryDao
import com.hanamobile.domain.repository.MemoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MemoryRepositoryImpl(
    private val dao: MemoryDao
) : MemoryRepository {
    override fun observeGlobalMemory(): Flow<List<MemoryEntry>> =
        dao.observeGlobalMemory().map { list -> list.map { it.toModel() } }

    override fun observeSessionMemory(sessionId: String): Flow<List<MemoryEntry>> =
        dao.observeSessionMemory(sessionId).map { list -> list.map { it.toModel() } }

    override suspend fun upsert(entry: MemoryEntry) = dao.upsert(entry.toEntity())

    override suspend fun delete(entryId: String) = dao.delete(entryId)

    override suspend fun setEnabled(entryId: String, enabled: Boolean) {
        dao.setEnabled(entryId, enabled, System.currentTimeMillis())
    }

    override suspend fun getActiveGlobal(): List<MemoryEntry> =
        dao.getActiveGlobal().map { it.toModel() }
}
