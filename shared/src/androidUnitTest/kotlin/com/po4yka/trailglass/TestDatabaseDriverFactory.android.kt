package com.po4yka.trailglass

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.po4yka.trailglass.data.db.Database
import com.po4yka.trailglass.db.TrailGlassDatabase

/**
 * Android unit test implementation to create test database.
 * Uses JDBC SQLite driver since Android unit tests run on JVM.
 */
actual fun createTestDatabaseImpl(): Database {
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    TrailGlassDatabase.Schema.create(driver)
    return Database(driver)
}
