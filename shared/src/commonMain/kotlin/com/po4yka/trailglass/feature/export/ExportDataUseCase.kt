package com.po4yka.trailglass.feature.export

import com.po4yka.trailglass.data.file.ExportManager
import com.po4yka.trailglass.data.repository.PlaceVisitRepository
import com.po4yka.trailglass.data.repository.TripRepository
import com.po4yka.trailglass.domain.model.PlaceVisit
import com.po4yka.trailglass.logging.logger
import kotlinx.datetime.Instant
import me.tatarka.inject.annotations.Inject

/**
 * Use case for exporting data to various formats using ExportManager. Supports exporting trips and visits to CSV, GPX,
 * and JSON formats.
 */
@Inject
class ExportDataUseCase(
    private val exportManager: ExportManager,
    private val tripRepository: TripRepository,
    private val placeVisitRepository: PlaceVisitRepository
) {
    private val logger = logger()

    /** Export format options. */
    enum class Format {
        CSV,
        GPX,
        JSON
    }

    /** Data type to export. */
    sealed class DataType {
        object AllTrips : DataType()

        data class SingleTrip(
            val tripId: String
        ) : DataType()

        data class TripVisits(
            val tripId: String
        ) : DataType()

        data class DateRangeVisits(
            val userId: String,
            val startTime: Instant,
            val endTime: Instant
        ) : DataType()
    }

    /**
     * Export data to the specified format and location.
     *
     * @param dataType Type of data to export
     * @param format Export format
     * @param outputPath Absolute path where the file should be saved
     * @param userId User ID for fetching user-specific data
     * @return Result indicating success or failure
     */
    suspend fun execute(
        dataType: DataType,
        format: Format,
        outputPath: String,
        userId: String
    ): Result<Unit> =
        try {
            logger.info { "Exporting data: type=$dataType, format=$format, path=$outputPath" }

            when (dataType) {
                is DataType.AllTrips -> exportAllTrips(format, outputPath, userId)
                is DataType.SingleTrip -> exportSingleTrip(dataType.tripId, format, outputPath)
                is DataType.TripVisits -> exportTripVisits(dataType.tripId, format, outputPath)
                is DataType.DateRangeVisits ->
                    exportDateRangeVisits(
                        dataType.userId,
                        dataType.startTime,
                        dataType.endTime,
                        format,
                        outputPath
                    )
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to export data" }
            Result.failure(e)
        }

    private suspend fun exportAllTrips(
        format: Format,
        outputPath: String,
        userId: String
    ): Result<Unit> {
        return when (format) {
            Format.CSV -> {
                val trips = tripRepository.getTripsForUser(userId)
                exportManager.exportTripsToCSV(trips, outputPath)
            }

            Format.JSON -> {
                logger.warn { "JSON export for all trips not supported, exporting first trip only" }
                val trips = tripRepository.getTripsForUser(userId)
                if (trips.isEmpty()) {
                    return Result.failure(IllegalStateException("No trips to export"))
                }
                exportManager.exportTripToJSON(trips.first(), outputPath)
            }

            Format.GPX -> {
                logger.warn { "GPX export for trips not supported, use visits instead" }
                Result.failure(IllegalArgumentException("GPX format is only supported for visits"))
            }
        }
    }

    private suspend fun exportSingleTrip(
        tripId: String,
        format: Format,
        outputPath: String
    ): Result<Unit> {
        val trip =
            tripRepository.getTripById(tripId)
                ?: return Result.failure(IllegalArgumentException("Trip not found: $tripId"))

        return when (format) {
            Format.CSV -> {
                exportManager.exportTripsToCSV(listOf(trip), outputPath)
            }

            Format.JSON -> {
                exportManager.exportTripToJSON(trip, outputPath)
            }

            Format.GPX -> {
                logger.warn { "GPX export for trips not supported, use visits instead" }
                Result.failure(IllegalArgumentException("GPX format is only supported for visits"))
            }
        }
    }

    private suspend fun exportTripVisits(
        tripId: String,
        format: Format,
        outputPath: String
    ): Result<Unit> {
        val trip =
            tripRepository.getTripById(tripId)
                ?: return Result.failure(IllegalArgumentException("Trip not found: $tripId"))

        val visits =
            placeVisitRepository.getVisits(
                trip.userId,
                trip.startTime,
                trip.endTime ?: kotlinx.datetime.Clock.System
                    .now()
            )

        return exportVisits(visits, format, outputPath)
    }

    private suspend fun exportDateRangeVisits(
        userId: String,
        startTime: Instant,
        endTime: Instant,
        format: Format,
        outputPath: String
    ): Result<Unit> {
        val visits = placeVisitRepository.getVisits(userId, startTime, endTime)
        return exportVisits(visits, format, outputPath)
    }

    private suspend fun exportVisits(
        visits: List<PlaceVisit>,
        format: Format,
        outputPath: String
    ): Result<Unit> =
        when (format) {
            Format.CSV -> exportManager.exportVisitsToCSV(visits, outputPath)
            Format.GPX -> exportManager.exportVisitsToGPX(visits, outputPath)
            Format.JSON -> {
                logger.warn { "JSON export for visits not fully supported" }
                Result.failure(IllegalArgumentException("JSON format is only supported for trips"))
            }
        }
}
