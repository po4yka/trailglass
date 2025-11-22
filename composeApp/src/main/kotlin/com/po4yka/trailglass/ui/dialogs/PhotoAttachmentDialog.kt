package com.po4yka.trailglass.ui.dialogs

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.po4yka.trailglass.feature.photo.AttachPhotoToVisitUseCase
import kotlinx.coroutines.launch

/**
 * Photo attachment dialog for attaching photos to place visits.
 * Uses Android photo picker to select images.
 */
@Composable
fun PhotoAttachmentHandler(
    placeVisitId: String,
    attachPhotoUseCase: AttachPhotoToVisitUseCase,
    onDismiss: () -> Unit,
    onPhotoAttached: () -> Unit
) {
    var showCaptionDialog by remember { mutableStateOf(false) }
    var selectedPhotoUri by remember { mutableStateOf<String?>(null) }
    var caption by remember { mutableStateOf("") }
    var isAttaching by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    // Photo picker launcher
    val photoPicker =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            if (uri != null) {
                selectedPhotoUri = uri.toString()
                showCaptionDialog = true
            } else {
                onDismiss()
            }
        }

    // Launch photo picker on first composition
    LaunchedEffect(Unit) {
        photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    // Caption dialog
    if (showCaptionDialog && selectedPhotoUri != null) {
        AlertDialog(
            onDismissRequest = {
                if (!isAttaching) {
                    showCaptionDialog = false
                    onDismiss()
                }
            },
            title = { Text("Add Caption") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Add an optional caption for this photo")

                    OutlinedTextField(
                        value = caption,
                        onValueChange = { caption = it },
                        label = { Text("Caption") },
                        placeholder = { Text("Enter caption (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isAttaching,
                        singleLine = true
                    )

                    if (error != null) {
                        Text(
                            text = error ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            isAttaching = true
                            error = null

                            // TODO: First need to import/upload the photo to get photoId
                            // For now, this is a placeholder - the actual flow would be:
                            // 1. Upload photo to server -> get photoId
                            // 2. Attach photoId to placeVisitId with caption

                            // Placeholder implementation showing the intended flow:
                            // val uploadResult = photoRepository.uploadPhoto(selectedPhotoUri!!)
                            // if (uploadResult is Success) {
                            //     val photoId = uploadResult.photoId
                            //     when (val result = attachPhotoUseCase.execute(photoId, placeVisitId, caption.ifBlank { null })) {
                            //         is AttachPhotoToVisitUseCase.Result.Success -> {
                            //             onPhotoAttached()
                            //             showCaptionDialog = false
                            //             onDismiss()
                            //         }
                            //         else -> {
                            //             error = "Failed to attach photo"
                            //         }
                            //     }
                            // }

                            // For now, show error explaining this needs photo upload implementation
                            error =
                                "Photo upload not yet implemented. Please use the Photos tab to import photos first."
                            isAttaching = false
                        }
                    },
                    enabled = !isAttaching
                ) {
                    if (isAttaching) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Attach")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showCaptionDialog = false
                        onDismiss()
                    },
                    enabled = !isAttaching
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Simple info dialog explaining photo attachment feature status.
 */
@Composable
fun PhotoAttachmentInfoDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.PhotoLibrary,
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
        },
        title = { Text("Photo Attachment") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("To attach photos to this place visit:")

                Text("1. Go to the Photos tab")
                Text("2. Import your photos")
                Text("3. Select a photo to view details")
                Text("4. Use the 'Attach to Visit' button")

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "Direct photo capture and attachment from the map will be available in a future update.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Got it")
            }
        }
    )
}
