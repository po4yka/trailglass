package com.po4yka.trailglass.di

import app.cash.sqldelight.driver.native.inMemoryDriver
import com.po4yka.trailglass.data.db.DatabaseDriverFactory
import com.po4yka.trailglass.db.TrailGlassDatabase

/** iOS test driver factory backed by SQLDelight in-memory driver. */
actual class TestDatabaseDriverFactory : DatabaseDriverFactory {
    override fun createDriver(): app.cash.sqldelight.db.SqlDriver = inMemoryDriver(TrailGlassDatabase.Schema)
}
