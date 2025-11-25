package com.po4yka.trailglass.feature.route.export

import com.po4yka.trailglass.domain.error.Result
import com.po4yka.trailglass.domain.error.TrailGlassError
import com.po4yka.trailglass.domain.model.TripRoute
import com.po4yka.trailglass.logging.logger
import me.tatarka.inject.annotations.Inject

/** Export format options. */
enum class ExportFormat {
    GPX,
    KML
}

/** Privacy information for export. */
data class PrivacyInfo(
    val containsGpsData: Boolean,
    val containsPhotoLocations: Boolean,
    val containsTimestamps: Boolean,
    val numberOfPoints: Int,
    val numberOfPhotos: Int,
    val warningMessage: String
)

/** Result of export operation. */
data class ExportResult(
    val fileName: String,
    val content: String,
    val mimeType: String,
    val privacyInfo: PrivacyInfo
)

/** Use case for exporting trip routes to various formats. */
@Inject
class ExportRouteUseCase(
    private val gpxExporter: GpxExporter = GpxExporter(),
    private val kmlExporter: KmlExporter = KmlExporter()
) {
    private val logger = logger()

    /**
     * Export trip route to specified format.
     *
     * @param tripRoute The route to export
     * @param tripName Name for the export file
     * @param format Export format (GPX or KML)
     * @return Result containing export data or error
     */
    fun execute(
        tripRoute: TripRoute,
        tripName: String,
        format: ExportFormat
    ): Result<ExportResult> =
        try {
            logger.info { "Exporting route to $format format" }

            val content =
                when (format) {
                    ExportFormat.GPX -> gpxExporter.export(tripRoute, tripName)
                    ExportFormat.KML -> kmlExporter.export(tripRoute, tripName)
                }

            val sanitizedName = sanitizeFileName(tripName)
            val fileName =
                when (format) {
                    ExportFormat.GPX -> "$sanitizedName.gpx"
                    ExportFormat.KML -> "$sanitizedName.kml"
                }

            val mimeType =
                when (format) {
                    ExportFormat.GPX -> "application/gpx+xml"
                    ExportFormat.KML -> "application/vnd.google-earth.kml+xml"
                }

            // Generate privacy information
            val privacyInfo = generatePrivacyInfo(tripRoute)

            logger.info { "Export successful: $fileName (${content.length} bytes)" }
            logger.warn { "Privacy: ${privacyInfo.warningMessage}" }

            Result.Success(
                ExportResult(
                    fileName = fileName,
                    content = content,
                    mimeType = mimeType,
                    privacyInfo = privacyInfo
                )
            )
        } catch (e: Exception) {
            logger.error(e) { "Failed to export route to $format" }
            Result.Error(
                TrailGlassError.Unknown(
                    technicalMessage = e.message ?: "Failed to export route",
                    cause = e
                )
            )
        }

    /** Generate privacy information for the export. */
    private fun generatePrivacyInfo(tripRoute: TripRoute): PrivacyInfo {
        val hasGps = tripRoute.fullPath.isNotEmpty()
        val hasPhotos = tripRoute.photoMarkers.isNotEmpty()
        val hasTimestamps = true // Always included in exports

        val warningParts =
            buildList {
                if (hasGps) add("precise GPS coordinates (${tripRoute.fullPath.size} points)")
                if (hasPhotos) add("photo locations (${tripRoute.photoMarkers.size} photos)")
                if (hasTimestamps) add("timestamps")
            }

        val warningMessage =
            if (warningParts.isNotEmpty()) {
                "This file contains ${warningParts.joinToString(", ")}. " +
                    "Be careful when sharing, as it reveals your exact movements and locations."
            } else {
                "This file contains location data."
            }

        return PrivacyInfo(
            containsGpsData = hasGps,
            containsPhotoLocations = hasPhotos,
            containsTimestamps = hasTimestamps,
            numberOfPoints = tripRoute.fullPath.size,
            numberOfPhotos = tripRoute.photoMarkers.size,
            warningMessage = warningMessage
        )
    }

    /** Sanitize file name by removing invalid characters. */
    private fun sanitizeFileName(name: String): String =
        name
            .replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
            .take(50) // Limit length
            .ifEmpty { "route" }
}
