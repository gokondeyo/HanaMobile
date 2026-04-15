package com.hanamobile.domain.service.inference

import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.hanamobile.core.extensions.StreamingLocalInferenceBackend
import com.hanamobile.core.model.BackendConfig
import com.hanamobile.core.model.BackendError
import com.hanamobile.core.model.BackendException
import com.hanamobile.core.model.BackendRequest
import com.hanamobile.core.model.BackendResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
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
    private val modelLoader: LiteRtLmModelLoader = LiteRtLmModelLoader(config),
    private val engineFactory: LiteRtLmEngineFactory = LiteRtLmEngineFactory.Default,
    private val workerDispatcher: CoroutineDispatcher = Dispatchers.Default
) : StreamingLocalInferenceBackend {

    private val initLock = Mutex()
    private val engineSession = ModelEngineSession<Engine>()

    override suspend fun generate(request: BackendRequest): BackendResponse {
        val chunks = mutableListOf<String>()
        generateStream(request).collect { chunk -> chunks += chunk }
        val output = chunks.joinToString(separator = "").trim()
        if (output.isBlank()) {
            throw BackendException(BackendError.GenerationFailure("Empty response from LiteRT-LM runtime"))
        }

        val modelFile = currentModelFile()
        return BackendResponse(
            text = output,
            diagnostics = mapOf(
                "backend" to config.backendId,
                "modelPath" to modelFile.absolutePath,
                "modelFile" to modelFile.name
            )
        )
    }

    override fun generateStream(request: BackendRequest): Flow<String> = flow {
        val modelFile = currentModelFile()
        val engine = ensureEngineInitialized(modelFile.absolutePath)
        val prompt = promptFormatter.format(request)

        val conversationConfig = ConversationConfig(
            samplerConfig = GenerationConfigValidator.toSamplerConfig(config.generation)
        )

        try {
            engine.createConversation(conversationConfig).use { conversation ->
                conversation.sendMessageAsync(prompt)
                    .map { it.text }
                    .collect { chunk ->
                        if (chunk.isNotEmpty()) emit(chunk)
                    }
            }
        } catch (e: BackendException) {
            throw e
        } catch (e: Throwable) {
            throw mapGenerationError(e)
        }
    }.catch { throwable ->
        throw if (throwable is BackendException) throwable else mapGenerationError(throwable)
    }

    suspend fun close() {
        initLock.withLock {
            runCatching { engineSession.closeCurrent() }
                .getOrElse { throw mapLifecycleError(it) }
        }
    }

    private suspend fun currentModelFile() = withContext(workerDispatcher) {
        val selectedModel = selectedModelProvider()
        modelLoader.resolveModelFile(selectedModel)
    }

    private suspend fun ensureEngineInitialized(modelPath: String): Engine {
        // Re-init only when there is no engine yet, or when selected canonical model path changed.
        engineSession.currentOrNull(modelPath)?.let { return it }

        return initLock.withLock {
            engineSession.currentOrNull(modelPath)?.let { return@withLock it }

            withContext(workerDispatcher) {
                try {
                    GenerationConfigValidator.validate(config.generation)
                    val engine = engineFactory.create(createEngineConfig(modelPath))
                    engine.initialize()
                    engineSession.swap(modelPath = modelPath, newEngine = engine)
                } catch (e: BackendException) {
                    throw e
                } catch (e: Throwable) {
                    throw mapInitializationError(e)
                }
            }
        }
    }

    private fun createEngineConfig(modelPath: String): EngineConfig {
        // Keep CPU-compatible default while retaining an explicit switch for future GPU/NPU rollout.
        // Official LiteRT-LM runtime target preference is kept in config.runtime.executionTarget.
        return EngineConfig(modelPath = modelPath)
    }

    private fun mapInitializationError(e: Throwable): BackendException =
        BackendException(
            BackendError.ModelInitializationFailure(e.message ?: "Unknown initialization error", e)
        )

    private fun mapGenerationError(e: Throwable): BackendException =
        BackendException(BackendError.GenerationFailure(e.message ?: "Unknown generation error", e))

    private fun mapLifecycleError(e: Throwable): BackendException =
        BackendException(BackendError.EngineLifecycleFailure(e.message ?: "Unknown lifecycle error", e))
}

fun interface LiteRtLmEngineFactory {
    fun create(config: EngineConfig): Engine

    object Default : LiteRtLmEngineFactory {
        override fun create(config: EngineConfig): Engine = Engine(config)
    }
}
