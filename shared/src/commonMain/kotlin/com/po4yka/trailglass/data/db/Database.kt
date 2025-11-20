package com.po4yka.trailglass.data.db

import app.cash.sqldelight.db.SqlDriver
import com.po4yka.trailglass.db.TrailGlassDatabase

/**
 * Database wrapper providing access to SQLDelight queries.
 * Manages database lifecycle and provides query access.
 */
class Database(driverFactory: DatabaseDriverFactory) {

    private val driver = driverFactory.createDriver()
    private val database = TrailGlassDatabase(driver)

    /**
     * Internal constructor for testing.
     * Allows tests to provide a pre-configured driver directly.
     */
    internal constructor(driver: SqlDriver) : this(
        driverFactory = object : DatabaseDriverFactory {
            override fun createDriver(): SqlDriver = driver
        }
    )

    /**
     * Access to location samples queries.
     */
    val locationSampleQueries get() = database.locationSamplesQueries

    /**
     * Access to place visits queries.
     */
    val placeVisitQueries get() = database.placeVisitsQueries

    /**
     * Access to geocoding cache queries.
     */
    val geocodingCacheQueries get() = database.geocodingCacheQueries

    /**
     * Access to route segments queries.
     */
    val routeSegmentsQueries get() = database.routeSegmentsQueries

    /**
     * Access to trips queries.
     */
    val tripsQueries get() = database.tripsQueries

    /**
     * Access to photos queries.
     */
    val photosQueries get() = database.photosQueries

    /**
     * Access to sync metadata queries.
     */
    val syncMetadataQueries get() = database.syncMetadataQueries

    /**
     * Access to sync conflicts queries.
     */
    val syncConflictsQueries get() = database.syncConflictsQueries

    /**
     * Execute a transaction.
     */
    fun transaction(body: app.cash.sqldelight.TransactionWithoutReturn.() -> Unit) {
        database.transaction(body = body)
    }

    /**
     * Close the database connection.
     */
    fun close() {
        driver.close()
    }
}
