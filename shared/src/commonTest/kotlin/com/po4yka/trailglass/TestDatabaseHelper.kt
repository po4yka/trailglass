package com.po4yka.trailglass

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.po4yka.trailglass.db.TrailGlassDatabase

/**
 * Helper for creating in-memory test databases.
 */
object TestDatabaseHelper {

    /**
     * Creates an in-memory SQLDelight database for testing.
     */
    fun createTestDatabase(): TrailGlassDatabase {
        val driver: SqlDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        TrailGlassDatabase.Schema.create(driver)
        return TrailGlassDatabase(driver)
    }

    /**
     * Clears all tables in the database.
     */
    fun clearDatabase(database: TrailGlassDatabase) {
        database.transaction {
            database.photoAttachmentsQueries.deleteAll()
            database.photosQueries.deleteAll()
            database.routeSegmentsQueries.deleteAll()
            database.placeVisitsQueries.deleteAll()
            database.locationSamplesQueries.deleteAll()
            database.tripsQueries.deleteAll()
        }
    }
}
