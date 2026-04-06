package com.hanamobile.domain.service.inference

internal object LiteRtLmModelSupport {
    val supportedExtensions: Set<String> = setOf("litertlm")

    // Keep this restrictive: plain filename only (no traversal, no separators).
    private val fileNamePattern = Regex("^[A-Za-z0-9._-]+$")

    fun isSafeFileName(fileName: String): Boolean =
        fileNamePattern.matches(fileName) &&
            !fileName.startsWith(".") &&
            '/' !in fileName &&
            '\\' !in fileName

    fun isSupportedExtension(fileName: String): Boolean =
        fileName.substringAfterLast('.', missingDelimiterValue = "")
            .lowercase() in supportedExtensions

    fun supportedExtensionsLabel(): String =
        supportedExtensions.joinToString(prefix = ".", separator = ", .")
}
