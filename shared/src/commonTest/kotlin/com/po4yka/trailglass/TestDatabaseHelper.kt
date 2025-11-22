package com.po4yka.trailglass

import com.po4yka.trailglass.data.db.Database

/**
 * Helper for creating in-memory test databases.
 * Uses expect/actual pattern for platform-specific drivers.
 */
object TestDatabaseHelper {
    /**
     * Creates an in-memory Database for testing.
     * Uses platform-specific driver (JDBC for JVM, Native for iOS).
     */
    fun createTestDatabase(): Database = createTestDatabaseImpl()

    /**
     * Clears all tables in the database that have deleteAll methods.
     */
    fun clearDatabase(database: Database) {
        database.transaction {
            database.photosQueries.deleteAll()
            database.photosQueries.deleteAllAttachments()
            database.routeSegmentsQueries.deleteAll()
            database.placeVisitQueries.deleteAll()
            database.locationSampleQueries.deleteAll()
            database.geocodingCacheQueries.clearAll()
            database.syncConflictsQueries.deleteAll()
        }
    }
}

/**
 * Platform-specific implementation to create test database.
 */
expect fun createTestDatabaseImpl(): Database
