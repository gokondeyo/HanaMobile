package com.hanamobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hanamobile.data.local.entity.MemoryEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memory_entries WHERE scopeSessionId IS NULL ORDER BY updatedAt DESC")
    fun observeGlobalMemory(): Flow<List<MemoryEntryEntity>>

    @Query("SELECT * FROM memory_entries WHERE scopeSessionId = :sessionId ORDER BY updatedAt DESC")
    fun observeSessionMemory(sessionId: String): Flow<List<MemoryEntryEntity>>

    @Query("SELECT * FROM memory_entries WHERE scopeSessionId IS NULL AND enabled = 1")
    suspend fun getActiveGlobal(): List<MemoryEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: MemoryEntryEntity)

    @Query("DELETE FROM memory_entries WHERE id = :entryId")
    suspend fun delete(entryId: String)

    @Query("UPDATE memory_entries SET enabled = :enabled, updatedAt = :updatedAt WHERE id = :entryId")
    suspend fun setEnabled(entryId: String, enabled: Boolean, updatedAt: Long)
}
