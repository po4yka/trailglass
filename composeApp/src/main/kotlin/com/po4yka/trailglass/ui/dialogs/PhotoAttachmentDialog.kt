package com.po4yka.trailglass.ui.dialogs

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.po4yka.trailglass.feature.photo.AttachPhotoToVisitUseCase
import kotlinx.coroutines.launch

/**
 * Photo attachment dialog for attaching photos to place visits. Allows importing a new photo and immediately attaching
 * it to a visit.
 */
@Composable
fun PhotoAttachmentHandler(
    placeVisitId: String,
    attachPhotoUseCase: AttachPhotoToVisitUseCase,
    importPhotoUseCase: com.po4yka.trailglass.feature.photo.ImportPhotoUseCase,
    userId: String,
    onDismiss: () -> Unit,
    onPhotoAttached: () -> Unit
) {
    var showCaptionDialog by remember { mutableStateOf(false) }
    var selectedPhotoUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var caption by remember { mutableStateOf("") }
    var isAttaching by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    // Photo picker launcher
    val photoPicker =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            if (uri != null) {
                selectedPhotoUri = uri
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

                            try {
                                val uri = selectedPhotoUri!!
                                val photoData =
                                    context.contentResolver.openInputStream(uri)?.use {
                                        it.readBytes()
                                    }

                                if (photoData == null) {
                                    error = "Failed to read photo data"
                                    isAttaching = false
                                    return@launch
                                }

                                val importResult =
                                    importPhotoUseCase.execute(
                                        uri = uri.toString(),
                                        photoData = photoData,
                                        userId = userId
                                    )

                                when (importResult) {
                                    is com.po4yka.trailglass.feature.photo.ImportPhotoUseCase.ImportResult.Success -> {
                                        val photoId = importResult.photo.id

                                        when (val result = attachPhotoUseCase.execute(
                                            photoId,
                                            placeVisitId,
                                            caption.ifBlank { null })) {
                                            is AttachPhotoToVisitUseCase.Result.Success -> {
                                                onPhotoAttached()
                                                showCaptionDialog = false
                                                onDismiss()
                                            }

                                            is AttachPhotoToVisitUseCase.Result.AlreadyAttached -> {
                                                error = "Photo already attached to this visit"
                                                isAttaching = false
                                            }

                                            is AttachPhotoToVisitUseCase.Result.Error -> {
                                                error = result.message
                                                isAttaching = false
                                            }
                                        }
                                    }

                                    is com.po4yka.trailglass.feature.photo.ImportPhotoUseCase.ImportResult.Error -> {
                                        error = importResult.message
                                        isAttaching = false
                                    }
                                }
                            } catch (e: java.io.IOException) {
                                error = "Failed to read photo: ${e.message}"
                                isAttaching = false
                            } catch (e: SecurityException) {
                                error = "Permission denied to read photo"
                                isAttaching = false
                            }
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

/** Simple info dialog explaining photo attachment feature status. */
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
