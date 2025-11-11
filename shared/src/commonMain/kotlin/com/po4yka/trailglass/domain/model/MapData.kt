package com.po4yka.trailglass.domain.model

/**
 * Geographic coordinate.
 */
data class Coordinate(
    val latitude: Double,
    val longitude: Double
) {
    init {
        require(latitude in -90.0..90.0) { "Latitude must be between -90 and 90" }
        require(longitude in -180.0..180.0) { "Longitude must be between -180 and 180" }
    }
}

/**
 * Geographic region (bounding box).
 */
data class MapRegion(
    val center: Coordinate,
    val latitudeDelta: Double,
    val longitudeDelta: Double
) {
    val northEast: Coordinate
        get() = Coordinate(
            latitude = center.latitude + latitudeDelta / 2,
            longitude = center.longitude + longitudeDelta / 2
        )

    val southWest: Coordinate
        get() = Coordinate(
            latitude = center.latitude - latitudeDelta / 2,
            longitude = center.longitude - longitudeDelta / 2
        )
}

/**
 * Map marker representing a place visit.
 */
data class MapMarker(
    val id: String,
    val coordinate: Coordinate,
    val title: String?,
    val snippet: String?,
    val placeVisitId: String
)

/**
 * Map route representing movement.
 */
data class MapRoute(
    val id: String,
    val coordinates: List<Coordinate>,
    val transportType: TransportType,
    val color: Int? = null,
    val routeSegmentId: String
)

/**
 * Complete map data for display.
 */
data class MapDisplayData(
    val markers: List<MapMarker> = emptyList(),
    val routes: List<MapRoute> = emptyList(),
    val region: MapRegion? = null
)

/**
 * Map camera position.
 */
data class CameraPosition(
    val target: Coordinate,
    val zoom: Float = 15f,
    val tilt: Float = 0f,
    val bearing: Float = 0f
)
