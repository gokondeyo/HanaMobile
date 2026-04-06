package com.hanamobile.domain.service.inference

import android.content.Context
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
 * Real on-device backend using Google LiteRT-LM runtime through the Android LLM inference API.
 */
class LiteRtLmLocalInferenceBackend(
    private val context: Context,
    private val config: BackendConfig,
    private val selectedModelProvider: suspend () -> String?,
    private val promptFormatter: LiteRtLmPromptFormatter = LiteRtLmPromptFormatter(),
    private val modelLoader: LiteRtLmModelLoader = LiteRtLmModelLoader(config)
) : LocalInferenceBackend {

    private val initLock = Mutex()
    @Volatile private var llmInstance: Any? = null
    @Volatile private var loadedModelPath: String? = null

    override suspend fun generate(request: BackendRequest): BackendResponse = withContext(Dispatchers.Default) {
        val prompt = promptFormatter.format(request)
        val selectedModel = selectedModelProvider()
        val modelFile = modelLoader.resolveModelFile(selectedModel)
        val engine = ensureEngineInitialized(modelFile.absolutePath)

        try {
            val output = engine.javaClass
                .getMethod("generateResponse", String::class.java)
                .invoke(engine, prompt) as? String
                ?: throw BackendException(BackendError.GenerationFailure("Empty response from LiteRT-LM runtime"))

            BackendResponse(
                text = output.trim(),
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

    private suspend fun ensureEngineInitialized(modelPath: String): Any {
        val currentPath = loadedModelPath
        if (llmInstance != null && currentPath == modelPath) return llmInstance as Any

        return initLock.withLock {
            val lockedCurrentPath = loadedModelPath
            if (llmInstance != null && lockedCurrentPath == modelPath) {
                return@withLock llmInstance as Any
            }

            val generation = config.generation
            if (generation.maxTokens <= 0) throw BackendException(BackendError.UnsupportedSetting("maxTokens must be > 0"))
            if (generation.topK <= 0) throw BackendException(BackendError.UnsupportedSetting("topK must be > 0"))
            if (generation.temperature < 0f) throw BackendException(BackendError.UnsupportedSetting("temperature must be >= 0"))

            closeIfPossible(llmInstance)

            try {
                val llmInferenceClass = Class.forName("com.google.mediapipe.tasks.genai.llminference.LlmInference")
                val optionsClass = Class.forName("com.google.mediapipe.tasks.genai.llminference.LlmInference$LlmInferenceOptions")
                val builderClass = Class.forName("com.google.mediapipe.tasks.genai.llminference.LlmInference$LlmInferenceOptions$Builder")

                val builder = optionsClass.getMethod("builder").invoke(null)
                invokeBuilder(builderClass, builder, "setModelPath", modelPath)
                invokeBuilderIfPresent(builderClass, builder, "setMaxTokens", generation.maxTokens)
                invokeBuilderIfPresent(builderClass, builder, "setTopK", generation.topK)
                invokeBuilderIfPresent(builderClass, builder, "setMaxTopK", generation.topK)
                invokeBuilderIfPresent(builderClass, builder, "setTemperature", generation.temperature)
                invokeBuilderIfPresent(builderClass, builder, "setRandomSeed", generation.randomSeed)

                val options = builderClass.getMethod("build").invoke(builder)
                val created = llmInferenceClass
                    .getMethod("createFromOptions", Context::class.java, optionsClass)
                    .invoke(null, context, options)

                llmInstance = created
                loadedModelPath = modelPath
                created
            } catch (e: BackendException) {
                throw e
            } catch (e: Throwable) {
                throw BackendException(
                    BackendError.ModelInitializationFailure(e.message ?: "Unknown initialization error", e)
                )
            }
        }
    }

    private fun closeIfPossible(instance: Any?) {
        if (instance == null) return
        runCatching {
            instance.javaClass.methods.firstOrNull { it.name == "close" && it.parameterCount == 0 }?.invoke(instance)
        }
    }

    private fun invokeBuilder(builderClass: Class<*>, builder: Any, method: String, value: Any) {
        val parameterType = when (value) {
            is Int -> Int::class.javaPrimitiveType
            is Float -> Float::class.javaPrimitiveType
            is Boolean -> Boolean::class.javaPrimitiveType
            else -> value::class.java
        } ?: value::class.java

        val m = builderClass.getMethod(method, parameterType)
        m.invoke(builder, value)
    }

    private fun invokeBuilderIfPresent(builderClass: Class<*>, builder: Any, method: String, value: Any) {
        runCatching { invokeBuilder(builderClass, builder, method, value) }
    }
}
