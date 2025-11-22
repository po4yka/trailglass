package com.po4yka.trailglass.feature.trips.export

import com.po4yka.trailglass.domain.model.LocationSample
import com.po4yka.trailglass.domain.model.PlaceVisit
import com.po4yka.trailglass.domain.model.Trip

/**
 * Exports trip data to KML (Keyhole Markup Language) format.
 * KML is used by Google Earth and other mapping applications.
 */
class KmlExporter {
    /**
     * Export a trip to KML format.
     *
     * @param trip The trip to export
     * @param locationSamples Location track points
     * @param placeVisits Optional placemarks for significant places
     * @return KML XML string
     */
    fun export(
        trip: Trip,
        locationSamples: List<LocationSample>,
        placeVisits: List<PlaceVisit> = emptyList()
    ): String =
        buildString {
            // XML declaration
            appendLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")

            // KML root element
            appendLine("<kml xmlns=\"http://www.opengis.net/kml/2.2\">")
            appendLine("  <Document>")

            // Document name and description
            appendLine("    <name>${escapeXml(trip.displayName)}</name>")
            if (trip.description != null) {
                appendLine("    <description>${escapeXml(trip.description)}</description>")
            }

            // Styles for placemarks
            appendLine("    <Style id=\"visitStyle\">")
            appendLine("      <IconStyle>")
            appendLine("        <color>ff0000ff</color>")
            appendLine("        <scale>1.2</scale>")
            appendLine("        <Icon>")
            appendLine("          <href>http://maps.google.com/mapfiles/kml/pushpin/red-pushpin.png</href>")
            appendLine("        </Icon>")
            appendLine("      </IconStyle>")
            appendLine("    </Style>")

            // Style for track line
            appendLine("    <Style id=\"trackStyle\">")
            appendLine("      <LineStyle>")
            appendLine("        <color>ff0000ff</color>")
            appendLine("        <width>4</width>")
            appendLine("      </LineStyle>")
            appendLine("    </Style>")

            // Placemarks for visits
            placeVisits.forEach { visit ->
                appendLine("    <Placemark>")
                appendLine("      <name>${escapeXml(visit.displayName)}</name>")
                if (visit.approximateAddress != null) {
                    appendLine("      <description>${escapeXml(visit.approximateAddress)}</description>")
                }
                appendLine("      <styleUrl>#visitStyle</styleUrl>")
                appendLine("      <Point>")
                appendLine("        <coordinates>${visit.centerLongitude},${visit.centerLatitude},0</coordinates>")
                appendLine("      </Point>")
                appendLine("    </Placemark>")
            }

            // Track line
            if (locationSamples.isNotEmpty()) {
                appendLine("    <Placemark>")
                appendLine("      <name>${escapeXml(trip.displayName)} Track</name>")
                appendLine("      <styleUrl>#trackStyle</styleUrl>")
                appendLine("      <LineString>")
                appendLine("        <extrude>1</extrude>")
                appendLine("        <tessellate>1</tessellate>")
                appendLine("        <altitudeMode>clampToGround</altitudeMode>")
                appendLine("        <coordinates>")

                locationSamples.sortedBy { it.timestamp }.forEach { sample ->
                    val altitude = sample.altitude ?: 0.0
                    append("          ${sample.longitude},${sample.latitude},$altitude")
                    appendLine()
                }

                appendLine("        </coordinates>")
                appendLine("      </LineString>")
                appendLine("    </Placemark>")
            }

            appendLine("  </Document>")
            appendLine("</kml>")
        }

    /**
     * Escape special XML characters.
     */
    private fun escapeXml(text: String): String =
        text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
}
