package com.po4yka.trailglass.feature.map

import com.po4yka.trailglass.domain.model.PlaceCategory

/**
 * Provides marker icon information based on place category.
 * Platform-specific implementations will use this to render appropriate icons.
 */
object MarkerIconProvider {
    /**
     * Marker icon descriptor for a place category.
     */
    data class MarkerIcon(
        val iconName: String, // Icon identifier (system icon name)
        val color: Int, // Primary color
        val isDefault: Boolean = false
    )

    /**
     * Get marker icon for a place category.
     */
    fun getIcon(
        category: PlaceCategory,
        isFavorite: Boolean = false
    ): MarkerIcon =
        if (isFavorite) {
            MarkerIcon(
                iconName = "star.fill",
                color = 0xFFFFD700.toInt(), // Gold
                isDefault = false
            )
        } else {
            when (category) {
                PlaceCategory.HOME ->
                    MarkerIcon(
                        iconName = "house.fill",
                        color = 0xFF4CAF50.toInt(), // Green
                        isDefault = false
                    )
                PlaceCategory.WORK ->
                    MarkerIcon(
                        iconName = "briefcase.fill",
                        color = 0xFF2196F3.toInt(), // Blue
                        isDefault = false
                    )
                PlaceCategory.FOOD ->
                    MarkerIcon(
                        iconName = "fork.knife",
                        color = 0xFFFF9800.toInt(), // Orange
                        isDefault = false
                    )
                PlaceCategory.SHOPPING ->
                    MarkerIcon(
                        iconName = "cart.fill",
                        color = 0xFFE91E63.toInt(), // Pink
                        isDefault = false
                    )
                PlaceCategory.FITNESS ->
                    MarkerIcon(
                        iconName = "figure.run",
                        color = 0xFF9C27B0.toInt(), // Purple
                        isDefault = false
                    )
                PlaceCategory.ENTERTAINMENT ->
                    MarkerIcon(
                        iconName = "film",
                        color = 0xFFFF5722.toInt(), // Deep Orange
                        isDefault = false
                    )
                PlaceCategory.TRAVEL ->
                    MarkerIcon(
                        iconName = "airplane",
                        color = 0xFF00BCD4.toInt(), // Cyan
                        isDefault = false
                    )
                PlaceCategory.HEALTHCARE ->
                    MarkerIcon(
                        iconName = "cross.case.fill",
                        color = 0xFFF44336.toInt(), // Red
                        isDefault = false
                    )
                PlaceCategory.EDUCATION ->
                    MarkerIcon(
                        iconName = "book.fill",
                        color = 0xFF3F51B5.toInt(), // Indigo
                        isDefault = false
                    )
                PlaceCategory.RELIGIOUS ->
                    MarkerIcon(
                        iconName = "building.columns.fill",
                        color = 0xFF795548.toInt(), // Brown
                        isDefault = false
                    )
                PlaceCategory.SOCIAL ->
                    MarkerIcon(
                        iconName = "person.2.fill",
                        color = 0xFFCDDC39.toInt(), // Lime
                        isDefault = false
                    )
                PlaceCategory.OUTDOOR ->
                    MarkerIcon(
                        iconName = "tree.fill",
                        color = 0xFF8BC34A.toInt(), // Light Green
                        isDefault = false
                    )
                PlaceCategory.SERVICE ->
                    MarkerIcon(
                        iconName = "wrench.and.screwdriver.fill",
                        color = 0xFF607D8B.toInt(), // Blue Grey
                        isDefault = false
                    )
                PlaceCategory.OTHER ->
                    MarkerIcon(
                        iconName = "mappin",
                        color = 0xFF9E9E9E.toInt(), // Grey
                        isDefault = true
                    )
            }
        }

    /**
     * Get marker color for a category.
     */
    fun getColor(category: PlaceCategory): Int = getIcon(category).color

    /**
     * Get cluster marker color (neutral color for clusters).
     */
    fun getClusterColor(count: Int): Int =
        when {
            count < 10 -> 0xFF2196F3.toInt() // Blue
            count < 50 -> 0xFFFF9800.toInt() // Orange
            count < 100 -> 0xFFFF5722.toInt() // Deep Orange
            else -> 0xFFF44336.toInt() // Red
        }

    /**
     * Get route color based on transport type.
     * Uses Silent Waters palette: Harbor Blue for historical routes.
     * Active routes should be set via explicit color parameter using Coastal Path.
     */
    fun getRouteColor(transportType: String): Int {
        // Use Harbor Blue (0xFF5C8AA8) from Silent Waters palette for all routes
        // This provides a consistent, serene color that matches the app's aesthetic
        return 0xFF5C8AA8.toInt() // Harbor Blue - historical route color
    }
}
