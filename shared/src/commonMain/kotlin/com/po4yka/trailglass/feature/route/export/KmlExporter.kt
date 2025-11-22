package com.po4yka.trailglass.feature.route.export

import com.po4yka.trailglass.domain.model.TripRoute

/**
 * KML (Keyhole Markup Language) exporter for trip routes.
 * Exports route data to KML format for Google Earth and other mapping applications.
 */
class KmlExporter {
    /**
     * Export trip route to KML format.
     *
     * @param tripRoute The route to export
     * @param tripName Optional trip name for metadata
     * @return KML XML string
     */
    fun export(
        tripRoute: TripRoute,
        tripName: String? = null
    ): String =
        buildString {
            // XML header
            appendLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
            appendLine("<kml xmlns=\"http://www.opengis.net/kml/2.2\">")
            appendLine("  <Document>")
            appendLine("    <name>${escapeXml(tripName ?: "Trip Route")}</name>")
            appendLine("    <description>Route exported from TrailGlass</description>")

            // Styles for different transport types
            appendTransportStyles()

            // Folder for place visits
            if (tripRoute.visits.isNotEmpty()) {
                appendLine("    <Folder>")
                appendLine("      <name>Places Visited</name>")
                tripRoute.visits.forEach { visit ->
                    appendLine("      <Placemark>")
                    appendLine("        <name>${escapeXml(visit.poiName ?: visit.city ?: "Place")}</name>")
                    if (visit.approximateAddress != null) {
                        appendLine("        <description>${escapeXml(visit.approximateAddress)}</description>")
                    }
                    appendLine("        <Point>")
                    appendLine(
                        "          <coordinates>${visit.centerLongitude},${visit.centerLatitude},0</coordinates>"
                    )
                    appendLine("        </Point>")
                    appendLine("      </Placemark>")
                }
                appendLine("    </Folder>")
            }

            // Folder for photos
            if (tripRoute.photoMarkers.isNotEmpty()) {
                appendLine("    <Folder>")
                appendLine("      <name>Photos</name>")
                tripRoute.photoMarkers.forEach { photo ->
                    appendLine("      <Placemark>")
                    appendLine("        <name>Photo</name>")
                    if (photo.caption != null) {
                        appendLine("        <description>${escapeXml(photo.caption)}</description>")
                    }
                    appendLine("        <Point>")
                    appendLine("          <coordinates>${photo.longitude},${photo.latitude},0</coordinates>")
                    appendLine("        </Point>")
                    appendLine("      </Placemark>")
                }
                appendLine("    </Folder>")
            }

            // Folder for route segments
            appendLine("    <Folder>")
            appendLine("      <name>Route</name>")

            // Group by transport type for colored segments
            val segmentsByType = tripRoute.segments.groupBy { it.transportType }

            segmentsByType.forEach { (transportType, segments) ->
                segments.forEach { segment ->
                    if (segment.simplifiedPath.isNotEmpty()) {
                        appendLine("      <Placemark>")
                        appendLine(
                            "        <name>${transportType.name.lowercase().replaceFirstChar {
                                it.uppercase()
                            }} - ${formatDistance(
                                segment.distanceMeters
                            )}</name>"
                        )
                        appendLine(
                            "        <description>Distance: ${formatDistance(
                                segment.distanceMeters
                            )}, Duration: ${formatDuration(
                                segment.endTime.toEpochMilliseconds() - segment.startTime.toEpochMilliseconds()
                            )}</description>"
                        )
                        appendLine("        <styleUrl>#${transportType.name.lowercase()}</styleUrl>")
                        appendLine("        <LineString>")
                        appendLine("          <tessellate>1</tessellate>")
                        appendLine("          <coordinates>")
                        segment.simplifiedPath.forEach { coord ->
                            appendLine("            ${coord.longitude},${coord.latitude},0")
                        }
                        appendLine("          </coordinates>")
                        appendLine("        </LineString>")
                        appendLine("      </Placemark>")
                    }
                }
            }

            appendLine("    </Folder>")
            appendLine("  </Document>")
            appendLine("</kml>")
        }

    /**
     * Append KML styles for different transport types.
     */
    private fun StringBuilder.appendTransportStyles() {
        val styles =
            mapOf(
                "walk" to "ff00ff00", // Green
                "bike" to "ffff0000", // Blue
                "car" to "ff0099ff", // Orange
                "train" to "ffff00ff", // Purple
                "plane" to "ff0000ff", // Red
                "boat" to "ffffff00", // Cyan
                "unknown" to "ff999999" // Gray
            )

        styles.forEach { (type, color) ->
            appendLine("    <Style id=\"$type\">")
            appendLine("      <LineStyle>")
            appendLine("        <color>$color</color>")
            appendLine("        <width>4</width>")
            appendLine("      </LineStyle>")
            appendLine("    </Style>")
        }
    }

    /**
     * Format distance for display.
     */
    private fun formatDistance(meters: Double): String =
        when {
            meters < 1000 -> "${meters.toInt()} m"
            else -> {
                val km = meters / 1000
                val rounded = (km * 10).toInt() / 10.0
                "$rounded km"
            }
        }

    /**
     * Format duration for display.
     */
    private fun formatDuration(millis: Long): String {
        val seconds = millis / 1000
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60

        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m"
            else -> "${seconds}s"
        }
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
