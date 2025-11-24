package com.po4yka.trailglass.ui.components

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.DragState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.po4yka.trailglass.location.CurrentLocationProvider
import kotlinx.coroutines.launch

/**
 * Full-screen map picker for selecting a region location.
 * Shows a draggable marker and a circle overlay representing the region radius.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegionMapPicker(
    initialLatitude: Double = 0.0,
    initialLongitude: Double = 0.0,
    initialRadius: Double = 200.0,
    currentLocationProvider: CurrentLocationProvider,
    onLocationSelected: (Double, Double) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Camera position state
    val cameraPositionState =
        rememberCameraPositionState {
            position =
                CameraPosition.fromLatLngZoom(
                    LatLng(
                        if (initialLatitude != 0.0) initialLatitude else 37.7749,
                        if (initialLongitude != 0.0) initialLongitude else -122.4194
                    ),
                    15f
                )
        }

    // Marker position (draggable)
    var markerPosition by remember {
        mutableStateOf(
            LatLng(
                if (initialLatitude != 0.0) initialLatitude else 37.7749,
                if (initialLongitude != 0.0) initialLongitude else -122.4194
            )
        )
    }

    // Location permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            // Permissions granted, get current location
            coroutineScope.launch {
                currentLocationProvider.getCurrentLocation()
                    .onSuccess { locationData ->
                        val newPosition = LatLng(locationData.latitude, locationData.longitude)
                        markerPosition = newPosition
                        cameraPositionState.animate(
                            update = com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(
                                newPosition,
                                15f
                            )
                        )
                    }
                    .onFailure {
                        snackbarHostState.showSnackbar("Failed to get current location")
                    }
            }
        } else {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Location permission required")
            }
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Pick Location") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            onLocationSelected(markerPosition.latitude, markerPosition.longitude)
                            onNavigateBack()
                        }
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Confirm")
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
            )
        }
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings =
                    MapUiSettings(
                        zoomControlsEnabled = true,
                        myLocationButtonEnabled = false,
                        compassEnabled = true,
                        mapToolbarEnabled = false
                    )
            ) {
                // Draggable marker
                val markerState = remember(markerPosition) {
                    MarkerState(position = markerPosition)
                }

                // Update marker position when dragged
                LaunchedEffect(markerState.position) {
                    if (markerState.position != markerPosition && markerState.dragState == com.google.maps.android.compose.DragState.END) {
                        markerPosition = markerState.position
                    }
                }

                Marker(
                    state = markerState,
                    title = "Region Center",
                    draggable = true
                )

                // Circle overlay showing radius
                Circle(
                    center = markerPosition,
                    radius = initialRadius,
                    strokeColor = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2f,
                    fillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                )
            }

            // Current location button
            FloatingActionButton(
                onClick = {
                    // Request location permission and get current location
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                },
                containerColor = MaterialTheme.colorScheme.surface,
                modifier =
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
            ) {
                Icon(
                    Icons.Default.MyLocation,
                    contentDescription = "Current Location",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
