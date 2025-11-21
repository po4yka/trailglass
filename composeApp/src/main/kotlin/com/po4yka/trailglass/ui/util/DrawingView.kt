package com.po4yka.trailglass.ui.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.view.MotionEvent
import android.view.View

/**
 * Example custom view demonstrating AndroidX Input Motion Prediction usage.
 *
 * This view can be used for:
 * - Drawing route annotations on maps
 * - Sketch-based journal entries
 * - Any free-form drawing with low-latency stylus/touch input
 *
 * The motion prediction reduces perceived latency by predicting where
 * the user's finger/stylus will be in the next frame.
 */
class DrawingView(context: Context) : View(context) {
    private val predictor = MotionPredictionHelper(this)

    private val drawPaint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 5f
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
    }

    private val predictedPaint = Paint().apply {
        color = Color.LTGRAY
        strokeWidth = 3f
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
    }

    private val currentPath = Path()
    private val predictedPath = Path()
    private val completedPaths = mutableListOf<Path>()

    private var lastX = 0f
    private var lastY = 0f
    private var predictedX = 0f
    private var predictedY = 0f

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                currentPath.moveTo(x, y)
                lastX = x
                lastY = y
            }
            MotionEvent.ACTION_MOVE -> {
                // Record actual motion for prediction
                predictor.recordMotionEvent(event)

                // Draw actual path
                currentPath.quadTo(lastX, lastY, (x + lastX) / 2, (y + lastY) / 2)
                lastX = x
                lastY = y

                // Get predicted position
                predictor.predict()?.let { predictedEvent ->
                    val predX = predictedEvent.x
                    val predY = predictedEvent.y

                    // Draw predicted path segment
                    predictedPath.reset()
                    predictedPath.moveTo(x, y)
                    predictedPath.lineTo(predX, predY)

                    predictedX = predX
                    predictedY = predY
                }

                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                currentPath.lineTo(x, y)
                completedPaths.add(Path(currentPath))
                currentPath.reset()
                predictedPath.reset()
                invalidate()
            }
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw completed paths
        completedPaths.forEach { path ->
            canvas.drawPath(path, drawPaint)
        }

        // Draw current path
        canvas.drawPath(currentPath, drawPaint)

        // Draw predicted continuation (in lighter color)
        canvas.drawPath(predictedPath, predictedPaint)
    }

    fun clearDrawing() {
        completedPaths.clear()
        currentPath.reset()
        predictedPath.reset()
        invalidate()
    }
}
