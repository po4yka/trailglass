package com.po4yka.trailglass.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Photo upload loading indicator with progress bar. Optimized for displaying upload status with file count.
 *
 * @param isUploading Whether upload is in progress
 * @param currentFile Current file being uploaded (1-indexed)
 * @param totalFiles Total number of files to upload
 * @param modifier Modifier for the component
 */
@Composable
fun PhotoUploadIndicator(
    isUploading: Boolean,
    currentFile: Int,
    totalFiles: Int,
    modifier: Modifier = Modifier
) {
    val progress = if (totalFiles > 0) currentFile.toFloat() / totalFiles.toFloat() else 0f

    ContainedLoadingIndicator(
        visible = isUploading,
        message = "Uploading photo $currentFile of $totalFiles",
        progress = progress,
        modifier = modifier
    )
}

/**
 * Route processing indicator for trip analysis and route building.
 *
 * @param isProcessing Whether processing is active
 * @param modifier Modifier for the component
 */
@Composable
fun RouteProcessingIndicator(
    isProcessing: Boolean,
    modifier: Modifier = Modifier
) {
    SyncOperationIndicator(
        isActive = isProcessing,
        message = "Processing route data...",
        modifier = modifier
    )
}

/**
 * Sync loading indicator with shape morphing and visibility animation.
 *
 * This is a wrapper around MorphingLoadingIndicator that adds visibility animations.
 * Use this when you need a standalone morphing indicator with enter/exit animations.
 *
 * @param visible Whether the indicator is visible
 * @param modifier Modifier for the component
 */
@Composable
fun SyncLoadingIndicator(
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut(),
        modifier = modifier
    ) {
        MorphingLoadingIndicator()
    }
}
