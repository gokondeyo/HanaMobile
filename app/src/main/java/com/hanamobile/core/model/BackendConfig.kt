package com.hanamobile.core.model

/**
 * Runtime configuration for a local inference backend.
 */
data class BackendConfig(
    val backendId: String = "litert-lm",
    val modelDirectoryPath: String,
    val defaultModelFileName: String = "model.litertlm",
    val generation: GenerationConfig = GenerationConfig()
)

data class GenerationConfig(
    val maxTokens: Int = 256,
    val topK: Int = 40,
    val topP: Float = 0.95f,
    val temperature: Float = 0.7f,
    val randomSeed: Int = 0
)

sealed class BackendError(val message: String, val cause: Throwable? = null) {
    class ModelFileMissing(path: String) : BackendError("Model file was not found at: $path")
    class InvalidModelPath(path: String) : BackendError("Model path is invalid: $path")
    class UnsupportedModelFile(path: String) :
        BackendError("Unsupported model file type: $path. Expected .litertlm")

    class InvalidModelSelection(fileName: String) :
        BackendError("Invalid model selection: $fileName")

    class ModelInitializationFailure(details: String, cause: Throwable? = null) :
        BackendError("Failed to initialize LiteRT-LM backend: $details", cause)

    class UnsupportedSetting(details: String) : BackendError("Unsupported backend setting: $details")
    class GenerationFailure(details: String, cause: Throwable? = null) :
        BackendError("Text generation failed: $details", cause)
}

class BackendException(val error: BackendError) : RuntimeException(error.message, error.cause)
