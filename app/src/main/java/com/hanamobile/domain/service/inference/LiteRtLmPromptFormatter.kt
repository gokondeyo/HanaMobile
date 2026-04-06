package com.hanamobile.domain.service.inference

import com.hanamobile.core.model.BackendRequest
import com.hanamobile.core.model.MessageRole

/**
 * Converts assembled session context into a single text prompt for LiteRT-LM.
 * This keeps session assembly concerns outside backend runtime code.
 */
class LiteRtLmPromptFormatter {
    fun format(request: BackendRequest): String {
        val parts = mutableListOf<String>()

        if (request.systemPrompt.isNotBlank()) {
            parts += "<system>\n${request.systemPrompt.trim()}\n</system>"
        }

        if (request.memoryBlock.isNotBlank()) {
            parts += "<memory>\n${request.memoryBlock.trim()}\n</memory>"
        }

        if (request.toolResults.isNotEmpty()) {
            val toolBlock = request.toolResults.joinToString("\n") {
                "- ${it.toolName}: ${it.outputText}"
            }
            parts += "<tools>\n$toolBlock\n</tools>"
        }

        request.history.forEach { message ->
            val role = when (message.role) {
                MessageRole.SYSTEM -> "system"
                MessageRole.MEMORY -> "memory"
                MessageRole.USER -> "user"
                MessageRole.ASSISTANT -> "assistant"
                MessageRole.TOOL -> "tool"
            }
            parts += "<$role>\n${message.content.trim()}\n</$role>"
        }

        parts += "<user>\n${request.userInput.trim()}\n</user>"
        parts += "<assistant>"

        return parts.joinToString(separator = "\n\n")
    }
}
