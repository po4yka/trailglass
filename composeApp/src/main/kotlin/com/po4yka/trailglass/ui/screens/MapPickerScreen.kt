package com.po4yka.trailglass.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import com.po4yka.trailglass.ui.navigation.MapPickerComponent
import kotlinx.coroutines.launch

/**
 * Map Picker screen allows users to select a location by dragging a marker on Google Maps.
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun MapPickerScreen(
    component: MapPickerComponent,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedLatLng by remember {
        mutableStateOf(
            component.initialLat?.let { lat ->
                component.initialLon?.let { lon ->
                    LatLng(lat, lon)
                }
            }
        )
    }
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var googleMap by remember { mutableStateOf<GoogleMap?>(null) }

    val cameraPositionState = rememberCameraPositionState()
    val markerState = rememberUpdatedMarkerState(
        position = selectedLatLng ?: LatLng(37.7749, -122.4194) // Default to SF
    )

    // Initialize map position
    LaunchedEffect(selectedLatLng) {
        selectedLatLng?.let { latLng ->
            cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
            markerState.position = latLng
        }
    }

    // Handle marker drag
    LaunchedEffect(markerState.position) {
        selectedLatLng = markerState.position
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pick Location") },
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = component.onBack) {
                        androidx.compose.material3.Icon(
                            androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    selectedLatLng?.let { latLng ->
                        androidx.compose.material3.Button(
                            onClick = {
                                component.onLocationSelected(
                                    latLng.latitude,
                                    latLng.longitude,
                                    component.initialRadius ?: 50.0
                                )
                            }
                        ) {
                            Text("Confirm")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search places") },
                    placeholder = { Text("Enter address or place name") },
                    modifier = Modifier.weight(1f),
                    trailingIcon = {
                        if (isSearching) {
                            androidx.compose.material3.CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            androidx.compose.material3.IconButton(
                                onClick = {
                                    // TODO: Implement place search
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Place search coming soon")
                                    }
                                }
                            ) {
                                androidx.compose.material3.Icon(
                                    Icons.Default.Search,
                                    contentDescription = "Search",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.width(8.dp))

                androidx.compose.material3.FilledIconButton(
                    onClick = {
                        // TODO: Get current location and center map
                        scope.launch {
                            snackbarHostState.showSnackbar("Current location centering coming soon")
                        }
                    }
                ) {
                    androidx.compose.material3.Icon(
                        Icons.Default.MyLocation,
                        contentDescription = "My Location",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Map view
            Box(modifier = Modifier.weight(1f)) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(
                        isMyLocationEnabled = true,
                        mapType = com.google.maps.android.compose.MapType.NORMAL
                    ),
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = true,
                        myLocationButtonEnabled = false,
                        compassEnabled = true
                    ),
                    onMapLoaded = { /* Map loaded */ },
                    onMapClick = { latLng ->
                        selectedLatLng = latLng
                        markerState.position = latLng
                    }
                ) {
                    // Draggable marker
                    selectedLatLng?.let { latLng ->
                        Marker(
                            state = markerState,
                            title = "Selected Location",
                            snippet = "${latLng.latitude}, ${latLng.longitude}",
                            draggable = true,
                            onClick = {
                                // Keep default behavior
                                false
                            }
                        )
                    }
                }
            }

            // Location info card
            selectedLatLng?.let { latLng ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Selected Location",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Latitude",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "%.6f".format(latLng.latitude),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Longitude",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "%.6f".format(latLng.longitude),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }

                        if (component.initialRadius != null) {
                            Text(
                                "Radius: ${component.initialRadius}m",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Initial position hint
            if (selectedLatLng == null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = androidx.compose.material3.CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.Icon(
                            Icons.Default.MyLocation,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            "Tap on the map or drag the marker to select a location",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
