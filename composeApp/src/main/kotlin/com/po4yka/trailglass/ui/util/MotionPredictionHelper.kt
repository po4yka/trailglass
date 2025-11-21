package com.po4yka.trailglass.ui.util

import android.view.MotionEvent
import android.view.View
import androidx.input.motionprediction.MotionEventPredictor

/**
 * Helper class for using AndroidX Input Motion Prediction library to reduce
 * latency in touch and stylus input interactions.
 *
 * Use cases:
 * - Drawing/sketching on maps (route annotations)
 * - Stylus-based journal entries
 * - Any high-frequency touch interactions requiring low latency
 *
 * Example usage with AndroidView:
 * ```
 * AndroidView(
 *     factory = { context ->
 *         MyCustomView(context).apply {
 *             val predictor = MotionPredictionHelper(this)
 *             setOnTouchListener { _, event ->
 *                 predictor.recordMotionEvent(event)
 *                 val predicted = predictor.predict()
 *                 predicted?.let { predictedEvent ->
 *                     // Use predictedEvent for rendering to reduce latency
 *                     drawPredictedPoint(predictedEvent)
 *                 }
 *                 true
 *             }
 *         }
 *     }
 * )
 * ```
 *
 * @param view The View associated with the touch events. Required for motion prediction context.
 */
class MotionPredictionHelper(view: View) {
    private val predictor: MotionEventPredictor = MotionEventPredictor.newInstance(view)

    /**
     * Records a motion event for prediction calculations.
     * Call this for every actual MotionEvent received.
     */
    fun recordMotionEvent(event: MotionEvent) {
        predictor.record(event)
    }

    /**
     * Gets the predicted motion event based on previously recorded events.
     *
     * @return Predicted MotionEvent or null if prediction is not available
     */
    fun predict(): MotionEvent? {
        return predictor.predict()
    }

    /**
     * Cleans up predictor resources.
     * Call this when done using the predictor.
     */
    fun dispose() {
        // MotionEventPredictor doesn't have explicit cleanup in v1.0.0,
        // but keeping this for future compatibility
    }
}
