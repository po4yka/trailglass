package com.po4yka.trailglass.crash

import kotlin.concurrent.Volatile

private object AndroidExceptionHandler {
    @Volatile
    private var defaultHandler: Thread.UncaughtExceptionHandler? = null

    @Volatile
    private var customHandlerWrapper: Thread.UncaughtExceptionHandler? = null

    fun getOriginalHandler(): Thread.UncaughtExceptionHandler? {
        if (defaultHandler == null) {
            defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        }
        return defaultHandler
    }

    fun setHandler(handler: (Throwable) -> Unit) {
        customHandlerWrapper =
            Thread.UncaughtExceptionHandler { thread, throwable ->
                handler(throwable)
            }
        Thread.setDefaultUncaughtExceptionHandler(customHandlerWrapper)
    }
}

/**
 * Android implementation: Get the default uncaught exception handler.
 */
actual fun getDefaultExceptionHandler(): ((Throwable) -> Unit)? {
    val originalHandler = AndroidExceptionHandler.getOriginalHandler()
    return originalHandler?.let { handler ->
        { throwable: Throwable ->
            handler.uncaughtException(Thread.currentThread(), throwable)
        }
    }
}

/**
 * Android implementation: Set the default uncaught exception handler.
 */
actual fun setDefaultExceptionHandler(handler: (Throwable) -> Unit) {
    AndroidExceptionHandler.setHandler(handler)
}
