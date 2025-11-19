package com.po4yka.trailglass.feature.settings

import com.po4yka.trailglass.data.repository.SettingsRepository
import com.po4yka.trailglass.data.storage.SettingsStorage
import com.po4yka.trailglass.data.db.Database
import com.po4yka.trailglass.logging.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

/**
 * Use case for clearing all app data including settings, database, and caches.
 */
@Inject
class ClearDataUseCase(
    private val database: Database,
    private val settingsStorage: SettingsStorage
) {
    private val logger = logger()

    /**
     * Clear all application data:
     * - All database tables (trips, locations, photos, etc.)
     * - Settings and preferences
     * - Cached data
     */
    suspend fun execute() = withContext(Dispatchers.IO) {
        logger.info { "Starting to clear all application data" }

        try {
            // Clear all database tables
            logger.debug { "Clearing database tables" }
            database.transaction {
                database.locationSamplesQueries.deleteAll()
                database.placeVisitsQueries.deleteAll()
                database.routeSegmentsQueries.deleteAll()
                database.tripsQueries.deleteAll()
                database.photosQueries.deleteAll()
                database.photosQueries.deleteAllAttachments()
                database.geocodingCacheQueries.clearAll()
                database.syncMetadataQueries.deleteAll()
                database.syncConflictsQueries.deleteAll()
            }
            logger.debug { "Database tables cleared successfully" }

            // Clear settings storage
            logger.debug { "Clearing settings storage" }
            settingsStorage.clearSettings()
            logger.debug { "Settings storage cleared successfully" }

            logger.info { "All application data cleared successfully" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to clear application data" }
            throw e
        }
    }
}
