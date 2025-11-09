package com.po4yka.trailglass.feature.route.export

import com.po4yka.trailglass.domain.model.TripRoute
import com.po4yka.trailglass.logging.logger
import me.tatarka.inject.annotations.Inject

/**
 * Export format options.
 */
enum class ExportFormat {
    GPX,
    KML
}

/**
 * Result of export operation.
 */
data class ExportResult(
    val fileName: String,
    val content: String,
    val mimeType: String
)

/**
 * Use case for exporting trip routes to various formats.
 */
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
    ): Result<ExportResult> {
        return try {
            logger.info { "Exporting route to $format format" }

            val content = when (format) {
                ExportFormat.GPX -> gpxExporter.export(tripRoute, tripName)
                ExportFormat.KML -> kmlExporter.export(tripRoute, tripName)
            }

            val sanitizedName = sanitizeFileName(tripName)
            val fileName = when (format) {
                ExportFormat.GPX -> "$sanitizedName.gpx"
                ExportFormat.KML -> "$sanitizedName.kml"
            }

            val mimeType = when (format) {
                ExportFormat.GPX -> "application/gpx+xml"
                ExportFormat.KML -> "application/vnd.google-earth.kml+xml"
            }

            logger.info { "Export successful: $fileName (${content.length} bytes)" }

            Result.success(
                ExportResult(
                    fileName = fileName,
                    content = content,
                    mimeType = mimeType
                )
            )
        } catch (e: Exception) {
            logger.error(e) { "Failed to export route to $format" }
            Result.failure(e)
        }
    }

    /**
     * Sanitize file name by removing invalid characters.
     */
    private fun sanitizeFileName(name: String): String {
        return name
            .replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
            .take(50) // Limit length
            .ifEmpty { "route" }
    }
}
