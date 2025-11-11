package com.po4yka.trailglass.data.db

import app.cash.sqldelight.db.SqlDriver

/**
 * Factory for creating platform-specific SQL drivers.
 * Implemented using expect/actual pattern.
 *
 * NOTE: This expect class intentionally does not declare a constructor,
 * allowing actual implementations to have platform-specific constructors
 * (e.g., Context on Android, no parameters on iOS).
 *
 * Instances should be provided through dependency injection (PlatformModule).
 */
expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}
