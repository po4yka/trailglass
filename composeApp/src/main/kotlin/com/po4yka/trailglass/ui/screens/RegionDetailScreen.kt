package com.po4yka.trailglass.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.po4yka.trailglass.domain.model.Region
import com.po4yka.trailglass.feature.regions.RegionsController
import kotlinx.coroutines.launch

/**
 * Screen for creating or editing a region.
 * Mode is determined by regionId: null = CREATE, non-null = EDIT.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegionDetailScreen(
    controller: RegionsController,
    regionId: String? = null,
    onNavigateBack: () -> Unit,
    onNavigateToMapPicker: (Double, Double, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val isEditMode = regionId != null

    // Form state
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var latitude by remember { mutableDoubleStateOf(0.0) }
    var longitude by remember { mutableDoubleStateOf(0.0) }
    var radiusMeters by remember { mutableStateOf(Region.DEFAULT_RADIUS_METERS) }
    var notificationsEnabled by remember { mutableStateOf(true) }

    // Validation errors
    var nameError by remember { mutableStateOf<String?>(null) }
    var locationError by remember { mutableStateOf<String?>(null) }

    // Load existing region if in edit mode
    LaunchedEffect(regionId) {
        if (regionId != null) {
            val region = controller.getRegionById(regionId)
            if (region != null) {
                name = region.name
                description = region.description ?: ""
                latitude = region.latitude
                longitude = region.longitude
                radiusMeters = region.radiusMeters
                notificationsEnabled = region.notificationsEnabled
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Place" else "Create Place") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isEditMode) {
                        IconButton(
                            onClick = {
                                regionId?.let { id ->
                                    controller.deleteRegion(id)
                                    onNavigateBack()
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    IconButton(
                        onClick = {
                            // Validate
                            var hasError = false
                            if (name.isBlank()) {
                                nameError = "Name is required"
                                hasError = true
                            } else {
                                nameError = null
                            }

                            if (latitude == 0.0 && longitude == 0.0) {
                                locationError = "Please pick a location on the map"
                                hasError = true
                            } else {
                                locationError = null
                            }

                            if (!hasError) {
                                scope.launch {
                                    if (isEditMode && regionId != null) {
                                        controller.updateRegion(
                                            regionId = regionId,
                                            name = name.trim(),
                                            description = description.trim().ifBlank { null },
                                            latitude = latitude,
                                            longitude = longitude,
                                            radiusMeters = radiusMeters,
                                            notificationsEnabled = notificationsEnabled
                                        )
                                    } else {
                                        controller.createRegion(
                                            name = name.trim(),
                                            description = description.trim().ifBlank { null },
                                            latitude = latitude,
                                            longitude = longitude,
                                            radiusMeters = radiusMeters,
                                            notificationsEnabled = notificationsEnabled
                                        )
                                    }
                                    onNavigateBack()
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
            )
        }
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Name field
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = null
                },
                label = { Text("Name") },
                placeholder = { Text("e.g., Home, Office, Gym") },
                isError = nameError != null,
                supportingText = nameError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Description field
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (optional)") },
                placeholder = { Text("Add details about this place...") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                minLines = 2
            )

            // Location section
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Location",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                if (latitude != 0.0 || longitude != 0.0) {
                    Text(
                        text = "Latitude: ${"%.6f".format(latitude)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Longitude: ${"%.6f".format(longitude)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "No location selected",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                locationError?.let { error ->
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                OutlinedButton(
                    onClick = {
                        onNavigateToMapPicker(latitude, longitude, radiusMeters.toDouble())
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Map, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pick on Map")
                }
            }

            // Radius slider
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Radius: $radiusMeters meters",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "You'll be notified when you enter this area",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = radiusMeters.toFloat(),
                    onValueChange = { radiusMeters = it.toInt() },
                    valueRange = Region.MIN_RADIUS_METERS.toFloat()..Region.MAX_RADIUS_METERS.toFloat(),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Notifications toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Notifications",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Get notified when you arrive at this place",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save button (alternative to top bar action)
            Button(
                onClick = {
                    // Validate
                    var hasError = false
                    if (name.isBlank()) {
                        nameError = "Name is required"
                        hasError = true
                    } else {
                        nameError = null
                    }

                    if (latitude == 0.0 && longitude == 0.0) {
                        locationError = "Please pick a location on the map"
                        hasError = true
                    } else {
                        locationError = null
                    }

                    if (!hasError) {
                        scope.launch {
                            if (isEditMode && regionId != null) {
                                controller.updateRegion(
                                    regionId = regionId,
                                    name = name.trim(),
                                    description = description.trim().ifBlank { null },
                                    latitude = latitude,
                                    longitude = longitude,
                                    radiusMeters = radiusMeters,
                                    notificationsEnabled = notificationsEnabled
                                )
                            } else {
                                controller.createRegion(
                                    name = name.trim(),
                                    description = description.trim().ifBlank { null },
                                    latitude = latitude,
                                    longitude = longitude,
                                    radiusMeters = radiusMeters,
                                    notificationsEnabled = notificationsEnabled
                                )
                            }
                            onNavigateBack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isEditMode) "Save Changes" else "Create Place")
            }
        }
    }
}
