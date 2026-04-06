package com.hanamobile.domain.service.inference

import com.hanamobile.core.model.BackendConfig
import com.hanamobile.core.model.BackendError
import com.hanamobile.core.model.BackendException
import java.io.File

class LiteRtLmModelLoader(
    private val config: BackendConfig
) {
    private val supportedExtensions = setOf("litertlm", "task")

    fun resolveModelFile(selectedModelFileName: String?): File {
        val modelDir = File(config.modelDirectoryPath)
        if (!modelDir.exists()) {
            modelDir.mkdirs()
        }
        if (!modelDir.isDirectory) {
            throw BackendException(BackendError.InvalidModelPath(modelDir.absolutePath))
        }

        val fileName = selectedModelFileName
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: config.defaultModelFileName

        if (fileName.contains('/') || fileName.contains('\\')) {
            throw BackendException(BackendError.InvalidModelPath(fileName))
        }

        val file = File(modelDir, fileName)
        if (!file.exists()) {
            throw BackendException(BackendError.ModelFileMissing(file.absolutePath))
        }
        if (!file.isFile) {
            throw BackendException(BackendError.InvalidModelPath(file.absolutePath))
        }

        val extension = file.extension.lowercase()
        if (extension !in supportedExtensions) {
            throw BackendException(BackendError.UnsupportedModelFile(file.absolutePath))
        }

        return file
    }
}
