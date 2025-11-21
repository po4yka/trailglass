package com.po4yka.trailglass.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.po4yka.trailglass.ui.util.DrawingView

/**
 * Composable that provides a drawing canvas with motion prediction.
 *
 * This can be used for:
 * - Annotating routes on the map
 * - Adding sketches to journal entries
 * - Any free-form drawing interface
 *
 * The underlying implementation uses AndroidX Input Motion Prediction
 * to reduce perceived latency during drawing.
 *
 * Example usage:
 * ```
 * var showDrawing by remember { mutableStateOf(false) }
 *
 * Box {
 *     MapView() // Your map composable
 *
 *     if (showDrawing) {
 *         DrawingCanvas(
 *             onClear = { drawingView ->
 *                 drawingView.clearDrawing()
 *             }
 *         )
 *     }
 * }
 * ```
 */
@Composable
fun DrawingCanvas(
    modifier: Modifier = Modifier,
    onClear: ((DrawingView) -> Unit)? = null
) {
    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                DrawingView(context).also { view ->
                    onClear?.let { clearCallback ->
                        // Store reference for clear operations if needed
                        // In a real app, you might use a state holder for this
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
