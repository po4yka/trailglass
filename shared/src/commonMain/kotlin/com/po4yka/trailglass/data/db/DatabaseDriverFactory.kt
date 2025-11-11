package com.po4yka.trailglass.data.db

import app.cash.sqldelight.db.SqlDriver

/**
 * Factory for creating platform-specific SQL drivers.
 * Implemented using expect/actual pattern.
 */
expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}
