package com.po4yka.trailglass

import app.cash.sqldelight.driver.native.inMemoryDriver
import com.po4yka.trailglass.data.db.Database
import com.po4yka.trailglass.db.TrailGlassDatabase

/**
 * iOS implementation to create test database.
 */
actual fun createTestDatabaseImpl(): Database {
    val driver = inMemoryDriver(TrailGlassDatabase.Schema)
    return Database(driver)
}
