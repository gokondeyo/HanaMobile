package com.hanamobile.app

import android.app.Application
import androidx.room.Room
import com.hanamobile.BuildConfig
import com.hanamobile.core.model.BackendConfig
import com.hanamobile.core.model.GenerationConfig
import com.hanamobile.core.session.SessionManager
import com.hanamobile.data.local.HanaDatabase
import com.hanamobile.data.repository.ChatSessionRepositoryImpl
import com.hanamobile.data.repository.MemoryRepositoryImpl
import com.hanamobile.data.repository.PromptRepositoryImpl
import com.hanamobile.domain.service.MemoryManager
import com.hanamobile.domain.service.MockSpeechToTextEngine
import com.hanamobile.domain.service.MockTextToSpeechEngine
import com.hanamobile.domain.service.SimpleWaveformAnimator
import com.hanamobile.domain.service.inference.LiteRtLmLocalInferenceBackend
import com.hanamobile.domain.service.inference.LocalModelCatalog
import kotlinx.coroutines.flow.first

class HanaApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        val db = Room.databaseBuilder(this, HanaDatabase::class.java, "hana.db").build()
        val promptRepository = PromptRepositoryImpl(db.promptDao())
        val memoryRepository = MemoryRepositoryImpl(db.memoryDao())
        val sessionRepository = ChatSessionRepositoryImpl(db.chatDao())
        val memoryManager = MemoryManager()
        val modelCatalog = LocalModelCatalog(applicationContext)

        val backendConfig = BackendConfig(
            backendId = "litert-lm",
            modelDirectoryPath = modelCatalog.modelDirectory().absolutePath,
            defaultModelFileName = BuildConfig.LITERT_DEFAULT_MODEL_FILE,
            generation = GenerationConfig(
                maxTokens = BuildConfig.LITERT_MAX_TOKENS,
                topK = BuildConfig.LITERT_TOP_K,
                topP = BuildConfig.LITERT_TOP_P,
                temperature = BuildConfig.LITERT_TEMPERATURE,
                randomSeed = BuildConfig.LITERT_RANDOM_SEED
            )
        )

        val localInferenceBackend = LiteRtLmLocalInferenceBackend(
            config = backendConfig,
            selectedModelProvider = {
                promptRepository.observeActiveModelFileName().first()
            }
        )

        container = AppContainer(
            promptRepository = promptRepository,
            memoryRepository = memoryRepository,
            chatSessionRepository = sessionRepository,
            sessionManager = SessionManager(localInferenceBackend, memoryManager),
            memoryManager = memoryManager,
            stt = MockSpeechToTextEngine(),
            tts = MockTextToSpeechEngine(),
            waveformAnimator = SimpleWaveformAnimator(),
            modelCatalog = modelCatalog
        )
    }
}
