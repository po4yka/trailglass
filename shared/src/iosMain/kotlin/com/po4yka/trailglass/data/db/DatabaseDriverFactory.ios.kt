package com.po4yka.trailglass.data.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.po4yka.trailglass.db.TrailGlassDatabase

/**
 * iOS implementation of DatabaseDriverFactory.
 * Uses NativeSqliteDriver for database access.
 */
actual interface DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver
}

/**
 * iOS implementation.
 */
class IosDatabaseDriverFactory : DatabaseDriverFactory {
    override fun createDriver(): SqlDriver {
        return NativeSqliteDriver(
            schema = TrailGlassDatabase.Schema,
            name = "trailglass.db"
        )
    }
}
