package com.po4yka.trailglass.feature.trips

import com.po4yka.trailglass.data.repository.LocationRepository
import com.po4yka.trailglass.data.repository.PlaceVisitRepository
import com.po4yka.trailglass.data.repository.TripRepository
import com.po4yka.trailglass.feature.trips.export.GpxExporter
import com.po4yka.trailglass.feature.trips.export.KmlExporter
import com.po4yka.trailglass.logging.logger
import me.tatarka.inject.annotations.Inject

/**
 * Use case for exporting trips to various formats.
 */
@Inject
class ExportTripUseCase(
    private val tripRepository: TripRepository,
    private val locationRepository: LocationRepository,
    private val placeVisitRepository: PlaceVisitRepository,
    private val gpxExporter: GpxExporter,
    private val kmlExporter: KmlExporter
) {
    private val logger = logger()

    enum class ExportFormat {
        GPX,
        KML
    }

    /**
     * Export a trip to the specified format.
     *
     * @param tripId Trip ID to export
     * @param format Export format (GPX or KML)
     * @param includeWaypoints Whether to include place visits as waypoints
     * @return Result with export string or error
     */
    suspend fun execute(
        tripId: String,
        format: ExportFormat,
        includeWaypoints: Boolean = true
    ): Result<String> {
        return try {
            // Get trip
            val trip =
                tripRepository.getTripById(tripId)
                    ?: return Result.failure(IllegalArgumentException("Trip not found: $tripId"))

            // Get location samples for the trip
            val locationsResult = locationRepository.getSamplesForTrip(tripId)
            val locationSamples = locationsResult.getOrNull() ?: emptyList()

            logger.info { "Exporting trip $tripId with ${locationSamples.size} location samples" }

            // Get place visits if needed
            val placeVisits =
                if (includeWaypoints) {
                    placeVisitRepository.getVisits(
                        trip.userId,
                        trip.startTime,
                        trip.endTime ?: kotlinx.datetime.Clock.System
                            .now()
                    )
                } else {
                    emptyList()
                }

            // Export based on format
            val exportedData =
                when (format) {
                    ExportFormat.GPX -> gpxExporter.export(trip, locationSamples, placeVisits)
                    ExportFormat.KML -> kmlExporter.export(trip, locationSamples, placeVisits)
                }

            logger.info { "Successfully exported trip $tripId as ${format.name}" }

            Result.success(exportedData)
        } catch (e: Exception) {
            logger.error(e) { "Failed to export trip $tripId" }
            Result.failure(e)
        }
    }
}
