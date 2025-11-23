package com.po4yka.trailglass.data.db

import app.cash.sqldelight.db.SqlDriver

/**
 * Factory for creating platform-specific SQL drivers. Implemented using expect/actual pattern.
 *
 * Platform-specific implementations should be provided through dependency injection (PlatformModule). On Android, the
 * implementation requires a Context parameter. On iOS, the implementation has no parameters.
 */
expect interface DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}
