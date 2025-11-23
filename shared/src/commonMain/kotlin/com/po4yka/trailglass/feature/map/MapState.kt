package com.po4yka.trailglass.feature.map

import com.po4yka.trailglass.domain.model.CameraMove
import com.po4yka.trailglass.domain.model.MapDisplayData
import com.po4yka.trailglass.domain.model.MapMarker
import com.po4yka.trailglass.domain.model.MapRoute

/**
 * Map UI state.
 *
 * @property mapData The map display data containing markers and routes
 * @property cameraMove The current camera movement command
 * @property selectedMarker The currently selected marker
 * @property selectedRoute The currently selected route
 * @property isFollowModeEnabled Whether follow mode is enabled
 * @property isLoading Whether the map is loading data
 * @property error Error message if any
 * @property hasLocationPermission Whether location permission is granted
 */
data class MapState(
    val mapData: MapDisplayData = MapDisplayData(),
    val cameraMove: CameraMove? = null,
    val selectedMarker: MapMarker? = null,
    val selectedRoute: MapRoute? = null,
    val isFollowModeEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val hasLocationPermission: Boolean = false
)

/**
 * Follow mode parameters.
 *
 * @property zoom Zoom level for following
 * @property tilt Camera tilt angle in degrees
 * @property bearing Camera bearing in degrees
 */
internal data class FollowModeParams(
    val zoom: Float,
    val tilt: Float,
    val bearing: Float
)
