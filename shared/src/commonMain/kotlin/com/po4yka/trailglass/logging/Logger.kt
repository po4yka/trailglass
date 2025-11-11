package com.po4yka.trailglass.logging

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * Creates a logger for a class.
 * Usage: private val logger = logger()
 */
inline fun <reified T> T.logger(): KLogger {
    return KotlinLogging.logger(T::class.qualifiedName ?: "UnknownClass")
}

/**
 * Creates a logger with a specific name.
 * Usage: val logger = logger("MyFeature")
 */
fun logger(name: String): KLogger {
    return KotlinLogging.logger(name)
}
