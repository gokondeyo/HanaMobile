package com.hanamobile.data.repository

import com.hanamobile.core.model.PromptPreset
import com.hanamobile.data.local.dao.PromptDao
import com.hanamobile.data.local.entity.AppSettingEntity
import com.hanamobile.domain.repository.PromptRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class PromptRepositoryImpl(
    private val dao: PromptDao
) : PromptRepository {

    override fun observePresets(): Flow<List<PromptPreset>> =
        dao.observePresets().map { items -> items.map { it.toModel() } }

    override fun observeActivePresetId(): Flow<String> =
        dao.observeSettingValue(ACTIVE_PROMPT_KEY).map { it ?: DEFAULT_PRESET_ID }

    override suspend fun getActivePreset(): PromptPreset {
        val activeId = dao.observeSettingValue(ACTIVE_PROMPT_KEY).first() ?: DEFAULT_PRESET_ID
        return dao.getPresetById(activeId)?.toModel()
            ?: PromptPreset(
                id = DEFAULT_PRESET_ID,
                name = "Default",
                systemPrompt = DEFAULT_PROMPT,
                isDefault = true
            )
    }

    override suspend fun upsertPreset(preset: PromptPreset) = dao.upsertPreset(preset.toEntity())

    override suspend fun renamePreset(presetId: String, newName: String) {
        dao.renamePreset(presetId, newName, System.currentTimeMillis())
    }

    override suspend fun deletePreset(presetId: String) = dao.deletePreset(presetId)

    override suspend fun setActivePreset(presetId: String) {
        dao.putSetting(AppSettingEntity(ACTIVE_PROMPT_KEY, presetId))
    }

    override suspend fun resetPresetToDefault(presetId: String) {
        val preset = dao.getPresetById(presetId) ?: return
        dao.upsertPreset(preset.copy(systemPrompt = DEFAULT_PROMPT, updatedAt = System.currentTimeMillis()))
    }

    override fun observeActiveModelFileName(): Flow<String?> =
        dao.observeSettingValue(ACTIVE_MODEL_FILE_KEY)

    override suspend fun setActiveModelFileName(fileName: String) {
        dao.putSetting(AppSettingEntity(ACTIVE_MODEL_FILE_KEY, fileName))
    }

    companion object {
        const val ACTIVE_PROMPT_KEY = "active_prompt_id"
        const val DEFAULT_PRESET_ID = "default"
        const val ACTIVE_MODEL_FILE_KEY = "active_model_file"
        const val DEFAULT_PROMPT = "You are Hana, a local-first assistant. Be clear, safe, and concise."
    }
}
