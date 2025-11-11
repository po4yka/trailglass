package com.po4yka.trailglass.data.db

import com.po4yka.trailglass.db.TrailGlassDatabase

/**
 * Database wrapper providing access to SQLDelight queries.
 * Manages database lifecycle and provides query access.
 */
class Database(driverFactory: DatabaseDriverFactory) {

    private val driver = driverFactory.createDriver()
    private val database = TrailGlassDatabase(driver)

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
     * Close the database connection.
     */
    fun close() {
        driver.close()
    }
}
