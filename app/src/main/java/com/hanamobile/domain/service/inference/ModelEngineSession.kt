package com.hanamobile.domain.service.inference

/**
 * Reuses a single engine per model path and safely recreates on model switch.
 */
internal class ModelEngineSession<T : AutoCloseable> {
    @Volatile
    private var holder: Holder<T>? = null

    fun currentOrNull(modelPath: String): T? = holder?.takeIf { it.modelPath == modelPath }?.engine

    fun swap(modelPath: String, newEngine: T): T {
        val previous = holder
        holder = Holder(modelPath = modelPath, engine = newEngine)
        previous?.engine?.close()
        return newEngine
    }

    fun closeCurrent() {
        val existing = holder ?: return
        holder = null
        existing.engine.close()
    }

    private data class Holder<T : AutoCloseable>(
        val modelPath: String,
        val engine: T
    )
}
