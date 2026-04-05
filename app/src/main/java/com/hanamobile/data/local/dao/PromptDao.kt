package com.hanamobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hanamobile.data.local.entity.AppSettingEntity
import com.hanamobile.data.local.entity.PromptPresetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PromptDao {
    @Query("SELECT * FROM prompt_presets ORDER BY updatedAt DESC")
    fun observePresets(): Flow<List<PromptPresetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPreset(entity: PromptPresetEntity)

    @Query("UPDATE prompt_presets SET name = :newName, updatedAt = :updatedAt WHERE id = :presetId")
    suspend fun renamePreset(presetId: String, newName: String, updatedAt: Long)

    @Query("DELETE FROM prompt_presets WHERE id = :presetId")
    suspend fun deletePreset(presetId: String)

    @Query("SELECT * FROM prompt_presets WHERE id = :presetId")
    suspend fun getPresetById(presetId: String): PromptPresetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun putSetting(setting: AppSettingEntity)

    @Query("SELECT value FROM app_settings WHERE `key` = :key")
    fun observeSettingValue(key: String): Flow<String?>
}
