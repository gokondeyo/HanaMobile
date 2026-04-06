package com.hanamobile.domain.service.inference

import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.SamplerConfig
import com.hanamobile.core.extensions.LocalInferenceBackend
import com.hanamobile.core.model.BackendConfig
import com.hanamobile.core.model.BackendError
import com.hanamobile.core.model.BackendException
import com.hanamobile.core.model.BackendRequest
import com.hanamobile.core.model.BackendResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Real on-device backend using official LiteRT-LM Android Kotlin APIs.
 */
class LiteRtLmLocalInferenceBackend(
    private val config: BackendConfig,
    private val selectedModelProvider: suspend () -> String?,
    private val promptFormatter: LiteRtLmPromptFormatter = LiteRtLmPromptFormatter(),
    private val modelLoader: LiteRtLmModelLoader = LiteRtLmModelLoader(config)
) : LocalInferenceBackend {

    private val initLock = Mutex()

    @Volatile
    private var engineHolder: EngineHolder? = null

    override suspend fun generate(request: BackendRequest): BackendResponse = withContext(Dispatchers.Default) {
        val prompt = promptFormatter.format(request)
        val selectedModel = selectedModelProvider()
        val modelFile = modelLoader.resolveModelFile(selectedModel)
        val engine = ensureEngineInitialized(modelFile.absolutePath)

        val generation = config.generation
        val conversationConfig = ConversationConfig(
            samplerConfig = SamplerConfig(
                topK = generation.topK,
                topP = generation.topP,
                temperature = generation.temperature,
                maxTokens = generation.maxTokens,
                randomSeed = generation.randomSeed.toLong()
            )
        )

        try {
            val output = engine.createConversation(conversationConfig).use { conversation ->
                conversation.sendMessage(prompt).text.trim()
            }

            if (output.isBlank()) {
                throw BackendException(BackendError.GenerationFailure("Empty response from LiteRT-LM runtime"))
            }

            BackendResponse(
                text = output,
                diagnostics = mapOf(
                    "backend" to config.backendId,
                    "modelPath" to modelFile.absolutePath
                )
            )
        } catch (e: BackendException) {
            throw e
        } catch (e: Throwable) {
            throw BackendException(BackendError.GenerationFailure(e.message ?: "Unknown generation error", e))
        }
    }

    private suspend fun ensureEngineInitialized(modelPath: String): Engine {
        engineHolder?.takeIf { it.modelPath == modelPath }?.let { return it.engine }

        return initLock.withLock {
            engineHolder?.takeIf { it.modelPath == modelPath }?.let { return@withLock it.engine }

            val generation = config.generation
            if (generation.maxTokens <= 0) throw BackendException(BackendError.UnsupportedSetting("maxTokens must be > 0"))
            if (generation.topK <= 0) throw BackendException(BackendError.UnsupportedSetting("topK must be > 0"))
            if (generation.topP !in 0f..1f) throw BackendException(BackendError.UnsupportedSetting("topP must be in [0, 1]"))
            if (generation.temperature < 0f) throw BackendException(BackendError.UnsupportedSetting("temperature must be >= 0"))

            closeEngineIfAny()

            try {
                val engineConfig = EngineConfig(modelPath = modelPath)
                val engine = Engine(engineConfig)
                engine.initialize()
                engineHolder = EngineHolder(modelPath = modelPath, engine = engine)
                engine
            } catch (e: BackendException) {
                throw e
            } catch (e: Throwable) {
                throw BackendException(
                    BackendError.ModelInitializationFailure(e.message ?: "Unknown initialization error", e)
                )
            }
        }
    }

    private fun closeEngineIfAny() {
        val existing = engineHolder ?: return
        runCatching { existing.engine.close() }
        engineHolder = null
    }

    private data class EngineHolder(
        val modelPath: String,
        val engine: Engine
    )
}
