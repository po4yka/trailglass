package com.po4yka.trailglass.logging

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.Level
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Extension function to log and also capture to LogBuffer.
 * Usage: logger.logToBuffer(LogLevel.INFO, "MyTag") { "Message" }
 */
fun KLogger.logToBuffer(
    level: LogLevel,
    tag: String,
    scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    messageProvider: () -> String
) {
    val message = messageProvider()

    // Log to console via kotlin-logging
    when (level) {
        LogLevel.DEBUG -> debug { message }
        LogLevel.INFO -> info { message }
        LogLevel.WARNING -> warn { message }
        LogLevel.ERROR -> error { message }
    }

    // Also capture to LogBuffer
    scope.launch {
        LogBuffer.add(level, tag, message)
    }
}

/**
 * Convert kotlin-logging Level to LogBuffer LogLevel
 */
fun Level.toLogLevel(): LogLevel =
    when (this) {
        Level.TRACE, Level.DEBUG -> LogLevel.DEBUG
        Level.INFO -> LogLevel.INFO
        Level.WARN -> LogLevel.WARNING
        Level.ERROR, Level.OFF -> LogLevel.ERROR
    }
