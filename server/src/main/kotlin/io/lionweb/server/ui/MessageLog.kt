package io.lionweb.server.ui

import java.util.concurrent.CopyOnWriteArrayList

data class MessageLogEntry(
    val timestamp: Long,
    val direction: String,
    val category: String,
    val messageKind: String,
    val clientId: String? = null,
    val participationId: String? = null,
    val json: String,
)

class MessageLog(
    private val maxEntries: Int = 500,
) {
    private val entries = CopyOnWriteArrayList<MessageLogEntry>()

    fun add(entry: MessageLogEntry) {
        entries.add(entry)
        while (entries.size > maxEntries) entries.removeAt(0)
    }

    fun getAll(): List<MessageLogEntry> = entries.toList()
}
