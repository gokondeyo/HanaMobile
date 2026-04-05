package com.hanamobile.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.hanamobile.core.model.MemoryCategory
import com.hanamobile.core.model.MessageRole
import com.hanamobile.data.local.dao.ChatDao
import com.hanamobile.data.local.dao.MemoryDao
import com.hanamobile.data.local.dao.PromptDao
import com.hanamobile.data.local.entity.AppSettingEntity
import com.hanamobile.data.local.entity.ChatMessageEntity
import com.hanamobile.data.local.entity.ChatSessionEntity
import com.hanamobile.data.local.entity.MemoryEntryEntity
import com.hanamobile.data.local.entity.PromptPresetEntity

@Database(
    entities = [
        PromptPresetEntity::class,
        AppSettingEntity::class,
        MemoryEntryEntity::class,
        ChatSessionEntity::class,
        ChatMessageEntity::class
    ],
    version = 1
)
@TypeConverters(EnumConverters::class)
abstract class HanaDatabase : RoomDatabase() {
    abstract fun promptDao(): PromptDao
    abstract fun memoryDao(): MemoryDao
    abstract fun chatDao(): ChatDao
}

class EnumConverters {
    @TypeConverter
    fun fromMemoryCategory(value: MemoryCategory): String = value.name

    @TypeConverter
    fun toMemoryCategory(value: String): MemoryCategory = MemoryCategory.valueOf(value)

    @TypeConverter
    fun fromMessageRole(value: MessageRole): String = value.name

    @TypeConverter
    fun toMessageRole(value: String): MessageRole = MessageRole.valueOf(value)
}
