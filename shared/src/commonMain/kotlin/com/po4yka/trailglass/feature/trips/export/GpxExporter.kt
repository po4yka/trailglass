package com.po4yka.trailglass.feature.trips.export

import com.po4yka.trailglass.domain.model.LocationSample
import com.po4yka.trailglass.domain.model.PlaceVisit
import com.po4yka.trailglass.domain.model.Trip

/** Exports trip data to GPX (GPS Exchange Format) format. GPX is an XML-based format for GPS data exchange. */
class GpxExporter {
    /**
     * Export a trip to GPX format.
     *
     * @param trip The trip to export
     * @param locationSamples Location track points
     * @param placeVisits Optional waypoints for significant places
     * @return GPX XML string
     */
    fun export(
        trip: Trip,
        locationSamples: List<LocationSample>,
        placeVisits: List<PlaceVisit> = emptyList()
    ): String =
        buildString {
            // XML declaration
            appendLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")

            // GPX root element
            appendLine("<gpx version=\"1.1\" creator=\"TrailGlass\"")
            appendLine("  xmlns=\"http://www.topografix.com/GPX/1/1\"")
            appendLine("  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"")
            appendLine(
                "  xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">"
            )

            // Metadata
            appendLine("  <metadata>")
            appendLine("    <name>${escapeXml(trip.displayName)}</name>")
            if (trip.description != null) {
                appendLine("    <desc>${escapeXml(trip.description)}</desc>")
            }
            appendLine("    <time>${trip.startTime}</time>")
            appendLine("  </metadata>")

            // Waypoints (place visits)
            placeVisits.forEach { visit ->
                appendLine("  <wpt lat=\"${visit.centerLatitude}\" lon=\"${visit.centerLongitude}\">")
                appendLine("    <time>${visit.startTime}</time>")
                appendLine("    <name>${escapeXml(visit.displayName)}</name>")
                if (visit.approximateAddress != null) {
                    appendLine("    <desc>${escapeXml(visit.approximateAddress)}</desc>")
                }
                appendLine("  </wpt>")
            }

            // Track (location samples)
            if (locationSamples.isNotEmpty()) {
                appendLine("  <trk>")
                appendLine("    <name>${escapeXml(trip.displayName)} Track</name>")
                appendLine("    <trkseg>")

                locationSamples.sortedBy { it.timestamp }.forEach { sample ->
                    appendLine("      <trkpt lat=\"${sample.latitude}\" lon=\"${sample.longitude}\">")
                    appendLine("        <time>${sample.timestamp}</time>")
                    if (sample.altitude != null) {
                        appendLine("        <ele>${sample.altitude}</ele>")
                    }
                    appendLine("      </trkpt>")
                }

                appendLine("    </trkseg>")
                appendLine("  </trk>")
            }

            appendLine("</gpx>")
        }

    /** Escape special XML characters. */
    private fun escapeXml(text: String): String =
        text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
}
