package com.po4yka.trailglass.crash

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSSetUncaughtExceptionHandler
import kotlin.native.concurrent.ThreadLocal

private val logger = KotlinLogging.logger {}

@ThreadLocal
private object IosExceptionHandler {
    var customHandler: ((Throwable) -> Unit)? = null
}

/**
 * iOS implementation: Get the default uncaught exception handler.
 *
 * Note: iOS/Kotlin Native doesn't provide a built-in way to get the current exception handler,
 * so this returns null. The platform's default crash handler (from iOS SDK) will still be invoked.
 */
actual fun getDefaultExceptionHandler(): ((Throwable) -> Unit)? {
    logger.debug { "iOS getDefaultExceptionHandler called" }
    // iOS doesn't provide a way to get the current handler
    return null
}

/**
 * iOS implementation: Set the default uncaught exception handler.
 *
 * Note: This sets up an NSException handler for Objective-C exceptions.
 * For Kotlin/Native crashes, Firebase Crashlytics iOS SDK will handle them automatically.
 */
@OptIn(ExperimentalForeignApi::class)
actual fun setDefaultExceptionHandler(handler: (Throwable) -> Unit) {
    logger.debug { "iOS setDefaultExceptionHandler called" }

    IosExceptionHandler.customHandler = handler

    // Set up NSException handler for Objective-C/Swift exceptions
    NSSetUncaughtExceptionHandler { exception ->
        logger.error { "Uncaught NSException: ${exception?.reason}" }

        // Convert NSException to Kotlin Throwable
        val kotlinException =
            Exception(
                "NSException: ${exception?.reason ?: "Unknown"}\n" +
                    "Name: ${exception?.name}\n" +
                    "Callstack: ${exception?.callStackSymbols}"
            )

        IosExceptionHandler.customHandler?.invoke(kotlinException)
    }
}
