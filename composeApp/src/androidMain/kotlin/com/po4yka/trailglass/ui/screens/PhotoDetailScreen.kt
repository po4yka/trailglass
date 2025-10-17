package com.po4yka.trailglass.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.po4yka.trailglass.domain.model.PhotoWithMetadata
import com.po4yka.trailglass.ui.components.ErrorView

/**
 * Photo detail screen showing full photo with metadata and actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoDetailScreen(
    photoId: String,
    controller: PhotoDetailController,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by controller.state.collectAsState()

    // Load photo on first composition
    LaunchedEffect(photoId) {
        controller.loadPhoto(photoId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Photo Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { controller.sharePhoto() }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                    IconButton(onClick = { controller.deletePhoto() }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            state.error != null -> {
                ErrorView(
                    error = state.error!!,
                    onRetry = { controller.loadPhoto(photoId) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
            state.photo != null -> {
                PhotoDetailContent(
                    photoWithMetadata = state.photo!!,
                    onAttachToVisit = { controller.showAttachmentDialog() },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun PhotoDetailContent(
    photoWithMetadata: PhotoWithMetadata,
    onAttachToVisit: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Photo image
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
            ) {
                SubcomposeAsyncImage(
                    model = Uri.parse(photoWithMetadata.photo.uri),
                    contentDescription = "Photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                    loading = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.BrokenImage,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )
            }
        }

        // Metadata section
        item {
            MetadataSection(photoWithMetadata)
        }

        // EXIF section
        if (photoWithMetadata.metadata != null) {
            item {
                ExifSection(photoWithMetadata.metadata!!)
            }
        }

        // Attachments section
        if (photoWithMetadata.attachments.isNotEmpty()) {
            item {
                AttachmentsSection(photoWithMetadata.attachments)
            }
        } else {
            item {
                Button(
                    onClick = onAttachToVisit,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Attach to Visit")
                }
            }
        }
    }
}

@Composable
private fun MetadataSection(photoWithMetadata: PhotoWithMetadata) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Photo Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            InfoRow("Taken", photoWithMetadata.photo.timestamp.toString())

            if (photoWithMetadata.photo.latitude != null && photoWithMetadata.photo.longitude != null) {
                InfoRow(
                    "Location",
                    "${photoWithMetadata.photo.latitude}, ${photoWithMetadata.photo.longitude}"
                )
            }

            if (photoWithMetadata.photo.width != null && photoWithMetadata.photo.height != null) {
                InfoRow(
                    "Dimensions",
                    "${photoWithMetadata.photo.width} × ${photoWithMetadata.photo.height}"
                )
            }

            if (photoWithMetadata.photo.sizeBytes != null) {
                val sizeMb = photoWithMetadata.photo.sizeBytes / (1024.0 * 1024.0)
                InfoRow("Size", "%.2f MB".format(sizeMb))
            }
        }
    }
}

@Composable
private fun ExifSection(metadata: com.po4yka.trailglass.domain.model.PhotoMetadata) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Camera Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            metadata.cameraMake?.let { InfoRow("Make", it) }
            metadata.cameraModel?.let { InfoRow("Model", it) }
            metadata.lens?.let { InfoRow("Lens", it) }

            // Camera settings
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                metadata.focalLength?.let {
                    SettingChip("${it.toInt()}mm")
                }
                metadata.aperture?.let {
                    SettingChip("f/${it}")
                }
                metadata.iso?.let {
                    SettingChip("ISO $it")
                }
                metadata.shutterSpeed?.let {
                    SettingChip(it)
                }
            }

            if (metadata.exifLatitude != null && metadata.exifLongitude != null) {
                InfoRow(
                    "GPS Coordinates",
                    "${metadata.exifLatitude}, ${metadata.exifLongitude}"
                )
                metadata.exifAltitude?.let {
                    InfoRow("Altitude", "${it.toInt()}m")
                }
            }
        }
    }
}

@Composable
private fun AttachmentsSection(attachments: List<com.po4yka.trailglass.domain.model.PhotoAttachment>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Attached to Visits",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            attachments.forEach { attachment ->
                ListItem(
                    headlineContent = { Text("Visit ${attachment.placeVisitId.take(8)}") },
                    supportingContent = attachment.caption?.let { { Text(it) } },
                    leadingContent = {
                        Icon(Icons.Default.Place, contentDescription = null)
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = Color.Transparent
                    )
                )
            }
        }
    }
}

@Composable
private fun SettingChip(text: String) {
    SuggestionChip(
        onClick = { },
        label = { Text(text, style = MaterialTheme.typography.labelSmall) }
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Controller interface for photo detail screen.
 */
interface PhotoDetailController {
    val state: kotlinx.coroutines.flow.StateFlow<PhotoDetailState>
    fun loadPhoto(photoId: String)
    fun sharePhoto()
    fun deletePhoto()
    fun showAttachmentDialog()
}

data class PhotoDetailState(
    val photo: PhotoWithMetadata? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
