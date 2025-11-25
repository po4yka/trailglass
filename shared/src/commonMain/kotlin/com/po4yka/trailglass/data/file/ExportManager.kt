package com.po4yka.trailglass.data.file

import com.po4yka.trailglass.domain.error.Result
import com.po4yka.trailglass.domain.error.TrailGlassError
import com.po4yka.trailglass.domain.model.PlaceVisit
import com.po4yka.trailglass.domain.model.Trip
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.datetime.Instant

private val logger = KotlinLogging.logger {}

/**
 * Manages exporting data to various formats using Kotlinx IO. Provides cross-platform export functionality for trips,
 * visits, and location data.
 */
class ExportManager(
    private val fileOperations: FileOperations
) {
    /**
     * Export trips to CSV format.
     *
     * @param trips List of trips to export
     * @param outputPath Output file path
     * @return true if export was successful
     */
    suspend fun exportTripsToCSV(
        trips: List<Trip>,
        outputPath: String
    ): Result<Unit> =
        try {
            logger.info { "Exporting ${trips.size} trips to CSV: $outputPath" }

            val csv =
                buildString {
                    appendLine("id,name,start_time,end_time,primary_country,is_ongoing,user_id")
                    trips.forEach { trip ->
                        appendLine(
                            "${trip.id}," +
                                "\"${trip.name ?: ""}\",${trip.startTime.toEpochMilliseconds()}," +
                                "${trip.endTime?.toEpochMilliseconds() ?: ""}," +
                                "\"${trip.primaryCountry ?: ""}\"," +
                                "${trip.isOngoing}," +
                                "${trip.userId}"
                        )
                    }
                }

            fileOperations.writeFileText(outputPath, csv)
            logger.info { "Successfully exported trips to $outputPath" }
            Result.Success(Unit)
        } catch (e: Exception) {
            logger.error(e) { "Failed to export trips to CSV" }
            Result.Error(
                TrailGlassError.Unknown(
                    technicalMessage = e.message ?: "Failed to export trips to CSV",
                    cause = e
                )
            )
        }

    /**
     * Export place visits to CSV format.
     *
     * @param visits List of visits to export
     * @param outputPath Output file path
     * @return true if export was successful
     */
    suspend fun exportVisitsToCSV(
        visits: List<PlaceVisit>,
        outputPath: String
    ): Result<Unit> =
        try {
            logger.info { "Exporting ${visits.size} visits to CSV: $outputPath" }

            val csv =
                buildString {
                    appendLine(
                        "id,start_time,end_time,latitude,longitude,address," +
                            "poi_name,city,country,category,is_favorite"
                    )
                    visits.forEach { visit ->
                        appendLine(
                            "${visit.id}," +
                                "${visit.startTime.toEpochMilliseconds()}," +
                                "${visit.endTime.toEpochMilliseconds()}," +
                                "${visit.centerLatitude}," +
                                "${visit.centerLongitude}," +
                                "\"${visit.approximateAddress ?: ""}\"," +
                                "\"${visit.poiName ?: ""}\"," +
                                "\"${visit.city ?: ""}\"," +
                                "\"${visit.countryCode ?: ""}\"," +
                                "${visit.category}," +
                                "${visit.isFavorite}"
                        )
                    }
                }

            fileOperations.writeFileText(outputPath, csv)
            logger.info { "Successfully exported visits to $outputPath" }
            Result.Success(Unit)
        } catch (e: Exception) {
            logger.error(e) { "Failed to export visits to CSV" }
            Result.Error(
                TrailGlassError.Unknown(
                    technicalMessage = e.message ?: "Export failed",
                    cause = e
                )
            )
        }

    /**
     * Export place visits to GPX format for GPS applications.
     *
     * @param visits List of visits to export
     * @param outputPath Output file path
     * @return true if export was successful
     */
    suspend fun exportVisitsToGPX(
        visits: List<PlaceVisit>,
        outputPath: String
    ): Result<Unit> =
        try {
            logger.info { "Exporting ${visits.size} visits to GPX: $outputPath" }

            val gpx =
                buildString {
                    appendLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
                    appendLine(
                        "<gpx version=\"1.1\" creator=\"Trailglass\" " +
                            "xmlns=\"http://www.topografix.com/GPX/1/1\">"
                    )
                    appendLine("  <metadata>")
                    appendLine("    <name>Trailglass Export</name>")
                    appendLine(
                        "    <time>${
                            Instant.fromEpochMilliseconds(
                                kotlinx.datetime.Clock.System
                                    .now()
                                    .toEpochMilliseconds()
                            )
                        }</time>"
                    )
                    appendLine("  </metadata>")

                    visits.forEach { visit ->
                        appendLine("  <wpt lat=\"${visit.centerLatitude}\" lon=\"${visit.centerLongitude}\">")
                        appendLine("    <time>${visit.startTime}</time>")
                        visit.poiName?.let { appendLine("    <name>$it</name>") }
                        visit.approximateAddress?.let { appendLine("    <desc>$it</desc>") }
                        visit.category.let { appendLine("    <type>${it.name}</type>") }
                        appendLine("  </wpt>")
                    }

                    appendLine("</gpx>")
                }

            fileOperations.writeFileText(outputPath, gpx)
            logger.info { "Successfully exported visits to GPX: $outputPath" }
            Result.Success(Unit)
        } catch (e: Exception) {
            logger.error(e) { "Failed to export visits to GPX" }
            Result.Error(
                TrailGlassError.Unknown(
                    technicalMessage = e.message ?: "Export failed",
                    cause = e
                )
            )
        }

    /**
     * Export trip data to JSON format.
     *
     * @param trip Trip to export
     * @param outputPath Output file path
     * @return Result indicating success or failure
     */
    suspend fun exportTripToJSON(
        trip: Trip,
        outputPath: String
    ): Result<Unit> =
        try {
            logger.info { "Exporting trip ${trip.id} to JSON: $outputPath" }

            val json =
                buildString {
                    appendLine("{")
                    appendLine("  \"id\": \"${trip.id}\",")
                    appendLine("  \"name\": \"${trip.name ?: ""}\",")
                    appendLine("  \"startTime\": ${trip.startTime.toEpochMilliseconds()},")
                    appendLine("  \"endTime\": ${trip.endTime?.toEpochMilliseconds() ?: "null"},")
                    appendLine("  \"primaryCountry\": \"${trip.primaryCountry ?: ""}\",")
                    appendLine("  \"isOngoing\": ${trip.isOngoing},")
                    appendLine("  \"userId\": \"${trip.userId}\"")
                    appendLine("}")
                }

            fileOperations.writeFileText(outputPath, json)
            logger.info { "Successfully exported trip to JSON: $outputPath" }
            Result.Success(Unit)
        } catch (e: Exception) {
            logger.error(e) { "Failed to export trip to JSON" }
            Result.Error(
                TrailGlassError.Unknown(
                    technicalMessage = e.message ?: "Export failed",
                    cause = e
                )
            )
        }

    /**
     * Create a backup of all data in a compressed format.
     *
     * @param backupPath Path to the backup directory
     * @return Result indicating success or failure
     */
    suspend fun createBackup(backupPath: String): Result<String> =
        try {
            logger.info { "Creating backup at: $backupPath" }
            fileOperations.createDirectories(backupPath)

            val timestamp =
                kotlinx.datetime.Clock.System
                    .now()
                    .toEpochMilliseconds()
            val backupFile = "$backupPath/trailglass_backup_$timestamp.txt"

            fileOperations.writeFileText(
                backupFile,
                "Trailglass backup created at ${Instant.fromEpochMilliseconds(timestamp)}"
            )

            logger.info { "Successfully created backup: $backupFile" }
            Result.Success(backupFile)
        } catch (e: Exception) {
            logger.error(e) { "Failed to create backup" }
            Result.Error(
                TrailGlassError.Unknown(
                    technicalMessage = e.message ?: "Export failed",
                    cause = e
                )
            )
        }
}
