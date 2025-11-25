package com.po4yka.trailglass.logging

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * A single log entry captured for in-app viewing and crash analysis.
 *
 * @property timestamp When the log entry was created
 * @property level The severity level of the log
 * @property tag Category/source of the log message
 * @property message The log message content
 * @property stackTrace Optional stack trace for error logs
 * @property isBreadcrumb Whether this is a breadcrumb for crash analysis
 */
data class LogEntry(
    val timestamp: Instant,
    val level: LogLevel,
    val tag: String,
    val message: String,
    val stackTrace: String? = null,
    val isBreadcrumb: Boolean = false
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
        message: String,
        stackTrace: String? = null,
        isBreadcrumb: Boolean = false
    ) {
        add(
            LogEntry(
                timestamp = Clock.System.now(),
                level = level,
                tag = tag,
                message = message,
                stackTrace = stackTrace,
                isBreadcrumb = isBreadcrumb
            )
        )
    }

    /**
     * Add a breadcrumb entry for crash analysis.
     * Breadcrumbs help understand the sequence of events leading to a crash.
     */
    suspend fun addBreadcrumb(
        tag: String,
        message: String
    ) {
        add(
            level = LogLevel.INFO,
            tag = tag,
            message = message,
            isBreadcrumb = true
        )
    }

    /**
     * Add an error entry with stack trace.
     */
    suspend fun addError(
        tag: String,
        message: String,
        throwable: Throwable? = null
    ) {
        add(
            level = LogLevel.ERROR,
            tag = tag,
            message = message,
            stackTrace = throwable?.stackTraceToString()
        )
    }

    /**
     * Get recent breadcrumbs for crash reports.
     * Returns the last N breadcrumb entries.
     */
    fun getRecentBreadcrumbs(limit: Int = 50): List<LogEntry> {
        return _entries.value
            .filter { it.isBreadcrumb }
            .takeLast(limit)
    }

    /**
     * Get recent errors for crash reports.
     */
    fun getRecentErrors(limit: Int = 10): List<LogEntry> {
        return _entries.value
            .filter { it.level == LogLevel.ERROR }
            .takeLast(limit)
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
                val breadcrumbMarker = if (entry.isBreadcrumb) " [BREADCRUMB]" else ""
                val stackInfo = entry.stackTrace?.let { "\n$it" } ?: ""
                "[${entry.level.name}] [$timestamp] [${entry.tag}]$breadcrumbMarker ${entry.message}$stackInfo"
            }
        }
    }

    /**
     * Export entries in a format suitable for crash reports.
     */
    suspend fun exportForCrashReport(): String {
        return mutex.withLock {
            val breadcrumbs = buffer.filter { it.isBreadcrumb }.takeLast(30)
            val errors = buffer.filter { it.level == LogLevel.ERROR }.takeLast(5)

            buildString {
                appendLine("=== Recent Breadcrumbs (${breadcrumbs.size}) ===")
                breadcrumbs.forEach { entry ->
                    appendLine("[${entry.timestamp}] [${entry.tag}] ${entry.message}")
                }

                appendLine()
                appendLine("=== Recent Errors (${errors.size}) ===")
                errors.forEach { entry ->
                    appendLine("[${entry.timestamp}] [${entry.tag}] ${entry.message}")
                    entry.stackTrace?.let { appendLine(it) }
                }
            }
        }
    }

    fun getEntriesSnapshot(): List<LogEntry> {
        return _entries.value
    }
}
