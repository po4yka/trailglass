package com.po4yka.trailglass.data.db

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.po4yka.trailglass.db.TrailGlassDatabase

/**
 * Android implementation of DatabaseDriverFactory.
 * Uses AndroidSqliteDriver for database access.
 */
actual class DatabaseDriverFactory(private val context: Context) {

    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = TrailGlassDatabase.Schema,
            context = context,
            name = "trailglass.db"
        )
    }
}
