package com.po4yka.trailglass.ui.components

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.po4yka.trailglass.domain.model.Photo

/**
 * Horizontal carousel of photo thumbnails.
 */
@Composable
fun PhotoCarousel(
    photos: List<Photo>,
    onPhotoClick: (Photo) -> Unit,
    modifier: Modifier = Modifier
) {
    if (photos.isEmpty()) return

    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(photos) { photo ->
            PhotoThumbnail(
                photo = photo,
                onClick = { onPhotoClick(photo) }
            )
        }
    }
}

@Composable
private fun PhotoThumbnail(
    photo: Photo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier =
            modifier
                .size(80.dp)
                .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp)
    ) {
        SubcomposeAsyncImage(
            model = Uri.parse(photo.uri),
            contentDescription = "Photo thumbnail",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            loading = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                }
            },
            error = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.BrokenImage,
                        contentDescription = "Error",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        )
    }
}

/**
 * Compact photo indicator showing photo count.
 */
@Composable
fun PhotoCountIndicator(
    photoCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (photoCount == 0) return

    AssistChip(
        onClick = onClick,
        label = { Text("$photoCount photo${if (photoCount != 1) "s" else ""}") },
        leadingIcon = {
            Icon(
                Icons.Default.BrokenImage,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        },
        modifier = modifier
    )
}
