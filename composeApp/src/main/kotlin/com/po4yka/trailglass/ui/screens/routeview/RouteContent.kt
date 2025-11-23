package com.po4yka.trailglass.ui.screens.routeview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.po4yka.trailglass.domain.model.PhotoMarker
import com.po4yka.trailglass.domain.model.TripRoute
import com.po4yka.trailglass.feature.route.MapStyle
import com.po4yka.trailglass.ui.components.RouteMapView
import com.po4yka.trailglass.ui.components.RouteSummaryCard

/** Main content showing the route map and summary card. */
@Composable
internal fun RouteViewContent(
    tripRoute: TripRoute,
    mapStyle: MapStyle,
    shouldRecenterCamera: Boolean,
    selectedPhotoMarker: PhotoMarker?,
    onPhotoMarkerClick: (PhotoMarker) -> Unit,
    onCameraRecentered: () -> Unit,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        // Map view
        RouteMapView(
            tripRoute = tripRoute,
            mapStyle = mapStyle,
            shouldRecenterCamera = shouldRecenterCamera,
            selectedPhotoMarker = selectedPhotoMarker,
            onPhotoMarkerClick = onPhotoMarkerClick,
            onCameraRecentered = onCameraRecentered,
            modifier = Modifier.fillMaxSize()
        )

        // Summary card at the bottom
        RouteSummaryCard(
            tripRoute = tripRoute,
            onPlayClick = onPlayClick,
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
        )
    }
}
