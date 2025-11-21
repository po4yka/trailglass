package com.po4yka.trailglass.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.po4yka.trailglass.domain.model.Trip
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.UUID

/**
 * Dialog for creating a new trip.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTripDialog(
    userId: String,
    onDismiss: () -> Unit,
    onConfirm: (Trip) -> Unit
) {
    var tripName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isOngoing by remember { mutableStateOf(true) }
    var tagInput by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf(listOf<String>()) }

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title
                Text(
                    text = "Create New Trip",
                    style = MaterialTheme.typography.headlineSmall
                )

                // Trip name
                OutlinedTextField(
                    value = tripName,
                    onValueChange = { tripName = it },
                    label = { Text("Trip Name *") },
                    placeholder = { Text("e.g., Summer Vacation 2024") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    placeholder = { Text("Optional description of your trip") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )

                // Ongoing trip checkbox
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Currently traveling",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Uncheck if this is a past trip",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isOngoing,
                        onCheckedChange = { isOngoing = it }
                    )
                }

                // Tags section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Tags",
                        style = MaterialTheme.typography.labelLarge
                    )

                    // Tag input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = tagInput,
                            onValueChange = { tagInput = it },
                            label = { Text("Add tag") },
                            placeholder = { Text("vacation, business, etc.") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        IconButton(
                            onClick = {
                                if (tagInput.isNotBlank() && tagInput !in tags) {
                                    tags = tags + tagInput.trim()
                                    tagInput = ""
                                }
                            },
                            enabled = tagInput.isNotBlank()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add tag")
                        }
                    }

                    // Tag chips
                    if (tags.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            tags.forEach { tag ->
                                InputChip(
                                    selected = false,
                                    onClick = { },
                                    label = { Text(tag) },
                                    trailingIcon = {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Remove",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    modifier = Modifier
                                        .clickable {
                                            tags = tags.filter { it != tag }
                                        }
                                )
                            }
                        }
                    }
                }

                HorizontalDivider()

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val now = Clock.System.now()
                            val trip = Trip(
                                id = UUID.randomUUID().toString(),
                                name = tripName.trim(),
                                startTime = now,
                                endTime = if (isOngoing) null else now, // For past trips, use now as placeholder
                                isOngoing = isOngoing,
                                userId = userId,
                                description = description.trim().ifBlank { null },
                                tags = tags,
                                isAutoDetected = false,
                                detectionConfidence = 1.0f, // Manual creation
                                createdAt = now
                            )
                            onConfirm(trip)
                        },
                        enabled = tripName.isNotBlank(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Create")
                    }
                }
            }
        }
    }
}
