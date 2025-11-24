package com.po4yka.trailglass.di

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.po4yka.trailglass.data.db.DatabaseDriverFactory
import com.po4yka.trailglass.db.TrailGlassDatabase

/** Android unit test driver factory using the JVM JDBC driver. */
actual class TestDatabaseDriverFactory : DatabaseDriverFactory {
    override fun createDriver(): app.cash.sqldelight.db.SqlDriver {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        TrailGlassDatabase.Schema.create(driver)
        return driver
    }
}
