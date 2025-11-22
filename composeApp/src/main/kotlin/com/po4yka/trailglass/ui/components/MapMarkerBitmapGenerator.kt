package com.po4yka.trailglass.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.Typeface
import androidx.core.graphics.toColorInt
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

/**
 * Generates custom bitmap markers for Google Maps.
 *
 * Creates visually distinct markers for:
 * - Individual places with category icons
 * - Clustered markers with count badges
 */
object MapMarkerBitmapGenerator {
    private const val MARKER_WIDTH = 120
    private const val MARKER_HEIGHT = 150
    private const val CLUSTER_SIZE = 120

    /**
     * Generate a custom marker with category icon.
     *
     * @param color Marker color
     * @param isSelected Whether the marker is selected
     * @return BitmapDescriptor for Google Maps
     */
    fun generateMarkerBitmap(
        color: Int,
        isSelected: Boolean
    ): BitmapDescriptor {
        val width = if (isSelected) (MARKER_WIDTH * 1.2f).toInt() else MARKER_WIDTH
        val height = if (isSelected) (MARKER_HEIGHT * 1.2f).toInt() else MARKER_HEIGHT

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Create marker pin shape
        val paint =
            Paint().apply {
                isAntiAlias = true
                style = Paint.Style.FILL
                this.color = color
            }

        val outlinePaint =
            Paint().apply {
                isAntiAlias = true
                style = Paint.Style.STROKE
                strokeWidth = if (isSelected) 8f else 4f
                this.color = if (isSelected) "#FFD700".toColorInt() else "#FFFFFF".toColorInt()
            }

        // Draw marker pin path
        val centerX = width / 2f
        val radius = (width * 0.35f)
        val topMargin = 10f

        val path =
            Path().apply {
                // Circle at top
                addCircle(centerX, topMargin + radius, radius, Path.Direction.CW)

                // Triangular point
                moveTo(centerX - radius * 0.5f, topMargin + radius * 1.5f)
                lineTo(centerX, height - 10f)
                lineTo(centerX + radius * 0.5f, topMargin + radius * 1.5f)
                close()
            }

        // Draw marker
        canvas.drawPath(path, paint)
        canvas.drawPath(path, outlinePaint)

        // Draw inner circle for icon area
        val iconPaint =
            Paint().apply {
                isAntiAlias = true
                style = Paint.Style.FILL
                this.color = "#FFFFFF".toColorInt()
            }

        val iconRadius = radius * 0.6f
        canvas.drawCircle(centerX, topMargin + radius, iconRadius, iconPaint)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    /**
     * Generate a cluster marker with count badge.
     *
     * @param count Number of markers in cluster
     * @param color Cluster color
     * @param isSelected Whether the cluster is selected
     * @return BitmapDescriptor for Google Maps
     */
    fun generateClusterBitmap(
        count: Int,
        color: Int,
        isSelected: Boolean
    ): BitmapDescriptor {
        val size = if (isSelected) (CLUSTER_SIZE * 1.2f).toInt() else CLUSTER_SIZE

        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val centerX = size / 2f
        val centerY = size / 2f

        // Determine circle size based on count
        val baseRadius =
            when {
                count < 10 -> size * 0.3f
                count < 50 -> size * 0.35f
                count < 100 -> size * 0.4f
                else -> size * 0.45f
            }

        // Draw shadow
        val shadowPaint =
            Paint().apply {
                isAntiAlias = true
                style = Paint.Style.FILL
                this.color = "#40000000".toColorInt()
            }
        canvas.drawCircle(centerX + 2f, centerY + 2f, baseRadius, shadowPaint)

        // Draw outer ring (with alpha for depth)
        val outerRingPaint =
            Paint().apply {
                isAntiAlias = true
                style = Paint.Style.FILL
                this.color = adjustAlpha(color, 0.3f)
            }
        canvas.drawCircle(centerX, centerY, baseRadius * 1.15f, outerRingPaint)

        // Draw main circle
        val circlePaint =
            Paint().apply {
                isAntiAlias = true
                style = Paint.Style.FILL
                this.color = color
            }
        canvas.drawCircle(centerX, centerY, baseRadius, circlePaint)

        // Draw border
        val borderPaint =
            Paint().apply {
                isAntiAlias = true
                style = Paint.Style.STROKE
                strokeWidth = if (isSelected) 6f else 3f
                this.color = if (isSelected) "#FFD700".toColorInt() else "#FFFFFF".toColorInt()
            }
        canvas.drawCircle(centerX, centerY, baseRadius, borderPaint)

        // Draw count text
        val countText =
            when {
                count < 1000 -> count.toString()
                count < 10000 -> "${count / 1000}K"
                else -> "${count / 1000}K+"
            }

        val textPaint =
            Paint().apply {
                isAntiAlias = true
                style = Paint.Style.FILL
                this.color = "#FFFFFF".toColorInt()
                textAlign = Paint.Align.CENTER
                textSize =
                    when {
                        count < 10 -> baseRadius * 0.8f
                        count < 100 -> baseRadius * 0.7f
                        count < 1000 -> baseRadius * 0.6f
                        else -> baseRadius * 0.5f
                    }
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

        // Center text vertically
        val textBounds = Rect()
        textPaint.getTextBounds(countText, 0, countText.length, textBounds)
        val textY = centerY + (textBounds.height() / 2f)

        canvas.drawText(countText, centerX, textY, textPaint)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    /**
     * Adjust color alpha channel.
     */
    private fun adjustAlpha(
        color: Int,
        factor: Float
    ): Int {
        val alpha = ((color shr 24 and 0xFF) * factor).toInt()
        val red = color shr 16 and 0xFF
        val green = color shr 8 and 0xFF
        val blue = color and 0xFF
        return (alpha shl 24) or (red shl 16) or (green shl 8) or blue
    }

    /**
     * Generate a route waypoint marker.
     */
    fun generateWaypointBitmap(
        label: String,
        color: Int
    ): BitmapDescriptor {
        val size = 80
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val centerX = size / 2f
        val centerY = size / 2f
        val radius = size * 0.4f

        // Draw circle
        val paint =
            Paint().apply {
                isAntiAlias = true
                style = Paint.Style.FILL
                this.color = color
            }
        canvas.drawCircle(centerX, centerY, radius, paint)

        // Draw border
        val borderPaint =
            Paint().apply {
                isAntiAlias = true
                style = Paint.Style.STROKE
                strokeWidth = 3f
                this.color = "#FFFFFF".toColorInt()
            }
        canvas.drawCircle(centerX, centerY, radius, borderPaint)

        // Draw label text
        val textPaint =
            Paint().apply {
                isAntiAlias = true
                style = Paint.Style.FILL
                this.color = "#FFFFFF".toColorInt()
                textAlign = Paint.Align.CENTER
                textSize = radius * 0.8f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

        val textBounds = Rect()
        textPaint.getTextBounds(label, 0, label.length, textBounds)
        val textY = centerY + (textBounds.height() / 2f)

        canvas.drawText(label, centerX, textY, textPaint)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    /**
     * Cache for generated bitmaps to avoid regenerating identical markers.
     */
    private val bitmapCache = mutableMapOf<String, BitmapDescriptor>()

    /**
     * Get or create a marker bitmap with caching.
     */
    fun getCachedMarkerBitmap(
        color: Int,
        isSelected: Boolean
    ): BitmapDescriptor {
        val key = "marker_${color}_$isSelected"
        return bitmapCache.getOrPut(key) {
            generateMarkerBitmap(color, isSelected)
        }
    }

    /**
     * Get or create a cluster bitmap with caching.
     */
    fun getCachedClusterBitmap(
        count: Int,
        color: Int,
        isSelected: Boolean
    ): BitmapDescriptor {
        val key = "cluster_${count}_${color}_$isSelected"
        return bitmapCache.getOrPut(key) {
            generateClusterBitmap(count, color, isSelected)
        }
    }

    /**
     * Clear the bitmap cache to free memory.
     */
    fun clearCache() {
        bitmapCache.clear()
    }
}
