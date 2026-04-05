package com.hanamobile.domain.service

import com.hanamobile.core.model.MemoryEntry
import com.hanamobile.core.model.MemoryInjectionPreview

class MemoryManager {
    fun buildPreview(global: List<MemoryEntry>, session: List<MemoryEntry>): MemoryInjectionPreview {
        val activeGlobal = global.filter { it.enabled }
        val compiled = buildString {
            if (activeGlobal.isNotEmpty()) {
                appendLine("# Global Memory")
                activeGlobal.forEach { appendLine("- [${it.category}] ${it.title}: ${it.content}") }
            }
            if (session.isNotEmpty()) {
                appendLine("# Session Memory")
                session.forEach { appendLine("- [${it.category}] ${it.title}: ${it.content}") }
            }
        }.trim()
        return MemoryInjectionPreview(activeGlobalEntries = activeGlobal, sessionEntries = session, compiledText = compiled)
    }
}
