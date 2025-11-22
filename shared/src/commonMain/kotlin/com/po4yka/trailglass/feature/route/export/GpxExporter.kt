package com.po4yka.trailglass.feature.route.export

import com.po4yka.trailglass.domain.model.TripRoute

/**
 * GPX (GPS Exchange Format) exporter for trip routes.
 * Exports route data to standard GPX 1.1 XML format.
 */
class GpxExporter {
    /**
     * Export trip route to GPX format.
     *
     * @param tripRoute The route to export
     * @param tripName Optional trip name for metadata
     * @return GPX XML string
     */
    fun export(
        tripRoute: TripRoute,
        tripName: String? = null
    ): String =
        buildString {
            // XML header
            appendLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
            appendLine("<gpx version=\"1.1\" creator=\"TrailGlass\"")
            appendLine("  xmlns=\"http://www.topografix.com/GPX/1/1\"")
            appendLine("  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"")
            appendLine(
                "  xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">"
            )

            // Metadata
            appendLine("  <metadata>")
            appendLine("    <name>${escapeXml(tripName ?: "Trip Route")}</name>")
            appendLine("    <time>${formatGpxTime(tripRoute.startTime)}</time>")
            appendLine(
                "    <bounds minlat=\"${tripRoute.bounds.minLatitude}\" minlon=\"${tripRoute.bounds.minLongitude}\""
            )
            appendLine(
                "            maxlat=\"${tripRoute.bounds.maxLatitude}\" maxlon=\"${tripRoute.bounds.maxLongitude}\"/>"
            )
            appendLine("  </metadata>")

            // Waypoints for place visits
            tripRoute.visits.forEach { visit ->
                appendLine("  <wpt lat=\"${visit.centerLatitude}\" lon=\"${visit.centerLongitude}\">")
                appendLine("    <time>${formatGpxTime(visit.startTime)}</time>")
                if (visit.poiName != null) {
                    appendLine("    <name>${escapeXml(visit.poiName)}</name>")
                }
                if (visit.approximateAddress != null) {
                    appendLine("    <desc>${escapeXml(visit.approximateAddress)}</desc>")
                }
                if (visit.city != null || visit.countryCode != null) {
                    val location = listOfNotNull(visit.city, visit.countryCode).joinToString(", ")
                    appendLine("    <cmt>${escapeXml(location)}</cmt>")
                }
                appendLine("  </wpt>")
            }

            // Waypoints for photos
            tripRoute.photoMarkers.forEach { photo ->
                appendLine("  <wpt lat=\"${photo.latitude}\" lon=\"${photo.longitude}\">")
                appendLine("    <time>${formatGpxTime(photo.timestamp)}</time>")
                appendLine("    <name>Photo</name>")
                if (photo.caption != null) {
                    appendLine("    <desc>${escapeXml(photo.caption)}</desc>")
                }
                appendLine("    <type>photo</type>")
                appendLine("  </wpt>")
            }

            // Track with route segments
            appendLine("  <trk>")
            appendLine("    <name>${escapeXml(tripName ?: "Route")}</name>")
            appendLine("    <type>trip</type>")

            // Create track segments by transport type
            val segmentsByType =
                tripRoute.fullPath
                    .groupBy { it.transportType }
                    .entries
                    .sortedBy { tripRoute.fullPath.indexOf(it.value.first()) }

            segmentsByType.forEach { (transportType, points) ->
                if (points.isNotEmpty()) {
                    appendLine("    <trkseg>")
                    points.forEach { point ->
                        append("      <trkpt lat=\"${point.latitude}\" lon=\"${point.longitude}\">")
                        append("<time>${formatGpxTime(point.timestamp)}</time>")
                        if (point.speed != null) {
                            append("<speed>${point.speed}</speed>")
                        }
                        appendLine("</trkpt>")
                    }
                    appendLine("    </trkseg>")
                }
            }

            appendLine("  </trk>")
            appendLine("</gpx>")
        }

    /**
     * Format instant to GPX time format (ISO 8601).
     */
    private fun formatGpxTime(instant: kotlinx.datetime.Instant): String {
        return instant.toString() // Already ISO 8601 format
    }

    /**
     * Escape XML special characters.
     */
    private fun escapeXml(text: String): String =
        text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
}
