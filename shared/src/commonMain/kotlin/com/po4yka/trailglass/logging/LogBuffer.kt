package com.po4yka.trailglass.logging

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

data class LogEntry(
    val timestamp: Instant,
    val level: LogLevel,
    val tag: String,
    val message: String
)

enum class LogLevel {
    DEBUG,
    INFO,
    WARNING,
    ERROR
}

object LogBuffer {
    private const val MAX_ENTRIES = 500
    private val mutex = Mutex()
    private val buffer = ArrayDeque<LogEntry>(MAX_ENTRIES)
    private val _entries = MutableStateFlow<List<LogEntry>>(emptyList())
    val entries: StateFlow<List<LogEntry>> = _entries.asStateFlow()

    suspend fun add(entry: LogEntry) {
        mutex.withLock {
            // Add to buffer
            buffer.addLast(entry)

            // Remove oldest entry if buffer is full
            if (buffer.size > MAX_ENTRIES) {
                buffer.removeFirst()
            }

            // Update flow with current buffer state
            _entries.value = buffer.toList()
        }
    }

    suspend fun add(
        level: LogLevel,
        tag: String,
        message: String
    ) {
        add(
            LogEntry(
                timestamp = Clock.System.now(),
                level = level,
                tag = tag,
                message = message
            )
        )
    }

    suspend fun clear() {
        mutex.withLock {
            buffer.clear()
            _entries.value = emptyList()
        }
    }

    suspend fun export(): String {
        return mutex.withLock {
            buffer.joinToString("\n") { entry ->
                val timestamp = entry.timestamp.toString()
                "[${entry.level.name}] [$timestamp] [${entry.tag}] ${entry.message}"
            }
        }
    }

    fun getEntriesSnapshot(): List<LogEntry> {
        return _entries.value
    }
}
