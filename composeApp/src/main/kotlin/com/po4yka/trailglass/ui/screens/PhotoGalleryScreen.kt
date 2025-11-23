package com.po4yka.trailglass.ui.screens

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.po4yka.trailglass.domain.model.PhotoGroup
import com.po4yka.trailglass.domain.model.PhotoWithMetadata
import com.po4yka.trailglass.ui.components.ErrorView

/** Photo gallery screen showing photos grouped by date. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoGalleryScreen(
    controller: PhotoGalleryController,
    onPhotoClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by controller.state.collectAsState()

    // Load photos on first composition
    LaunchedEffect(Unit) {
        controller.loadGallery()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Photos") },
                actions = {
                    IconButton(onClick = { controller.importPhotos() }) {
                        Icon(Icons.Default.Add, contentDescription = "Import Photos")
                    }
                    IconButton(onClick = { controller.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        when {
            state.isLoading -> {
                Box(
                    modifier =
                        Modifier
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
                    onRetry = { controller.refresh() },
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                )
            }

            state.photoGroups.isNotEmpty() -> {
                PhotoGalleryContent(
                    photoGroups = state.photoGroups,
                    onPhotoClick = onPhotoClick,
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                )
            }

            else -> {
                EmptyGalleryView(
                    onImportClick = { controller.importPhotos() },
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun PhotoGalleryContent(
    photoGroups: List<PhotoGroup>,
    onPhotoClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        items(photoGroups) { group ->
            PhotoGroupSection(
                group = group,
                onPhotoClick = onPhotoClick
            )
        }
    }
}

@Composable
private fun PhotoGroupSection(
    group: PhotoGroup,
    onPhotoClick: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Date header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = group.date.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                group.location?.let { locationText ->
                    Text(
                        text = locationText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = "${group.photoCount} photos",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Photo grid
        PhotoGroupGrid(
            photos = group.photos,
            onPhotoClick = onPhotoClick
        )
    }
}

@Composable
private fun PhotoGroupGrid(
    photos: List<PhotoWithMetadata>,
    onPhotoClick: (String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.heightIn(max = 800.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        userScrollEnabled = false // Disable nested scrolling
    ) {
        items(photos) { photoWithMeta ->
            PhotoGridItem(
                photo = photoWithMeta,
                onClick = { onPhotoClick(photoWithMeta.photo.id) }
            )
        }
    }
}

@Composable
private fun PhotoGridItem(
    photo: PhotoWithMetadata,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier =
            modifier
                .aspectRatio(1f)
                .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box {
            SubcomposeAsyncImage(
                model = Uri.parse(photo.photo.uri),
                contentDescription = "Photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                loading = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
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

            // Attachment indicator
            if (photo.attachments.isNotEmpty()) {
                Surface(
                    modifier =
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp),
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
                ) {
                    Icon(
                        Icons.Default.Place,
                        contentDescription = "Attached to visit",
                        modifier =
                            Modifier
                                .size(16.dp)
                                .padding(2.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyGalleryView(
    onImportClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.PhotoLibrary,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No photos yet",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "Import photos to see your memories",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onImportClick) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Import Photos")
        }
    }
}

/** Controller interface for photo gallery screen. */
interface PhotoGalleryController {
    val state: kotlinx.coroutines.flow.StateFlow<PhotoGalleryState>

    fun loadGallery()

    fun importPhotos()

    fun refresh()
}

data class PhotoGalleryState(
    val photoGroups: List<PhotoGroup> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
