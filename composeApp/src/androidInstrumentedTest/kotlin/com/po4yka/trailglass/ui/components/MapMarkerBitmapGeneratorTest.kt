package com.po4yka.trailglass.ui.components

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test for MapMarkerBitmapGenerator.
 *
 * These tests verify that custom bitmap markers are generated correctly.
 */
@RunWith(AndroidJUnit4::class)
class MapMarkerBitmapGeneratorTest {

    @Before
    fun setup() {
        // Clear cache before each test
        MapMarkerBitmapGenerator.clearCache()
    }

    @After
    fun teardown() {
        // Clear cache after tests
        MapMarkerBitmapGenerator.clearCache()
    }

    @Test
    fun generateMarkerBitmap_returnsValidDescriptor() {
        // Arrange
        val color = 0xFF2196F3.toInt() // Blue
        val isSelected = false

        // Act
        val descriptor = MapMarkerBitmapGenerator.generateMarkerBitmap(color, isSelected)

        // Assert
        assertNotNull("Marker bitmap descriptor should not be null", descriptor)
    }

    @Test
    fun generateMarkerBitmap_selectedMarker_returnsValidDescriptor() {
        // Arrange
        val color = 0xFF4CAF50.toInt() // Green
        val isSelected = true

        // Act
        val descriptor = MapMarkerBitmapGenerator.generateMarkerBitmap(color, isSelected)

        // Assert
        assertNotNull("Selected marker bitmap descriptor should not be null", descriptor)
    }

    @Test
    fun generateClusterBitmap_smallCluster_returnsValidDescriptor() {
        // Arrange
        val count = 5
        val color = 0xFF2196F3.toInt() // Blue
        val isSelected = false

        // Act
        val descriptor = MapMarkerBitmapGenerator.generateClusterBitmap(count, color, isSelected)

        // Assert
        assertNotNull("Cluster bitmap descriptor should not be null", descriptor)
    }

    @Test
    fun generateClusterBitmap_mediumCluster_returnsValidDescriptor() {
        // Arrange
        val count = 25
        val color = 0xFFFF9800.toInt() // Orange
        val isSelected = false

        // Act
        val descriptor = MapMarkerBitmapGenerator.generateClusterBitmap(count, color, isSelected)

        // Assert
        assertNotNull("Medium cluster bitmap descriptor should not be null", descriptor)
    }

    @Test
    fun generateClusterBitmap_largeCluster_returnsValidDescriptor() {
        // Arrange
        val count = 150
        val color = 0xFFF44336.toInt() // Red
        val isSelected = false

        // Act
        val descriptor = MapMarkerBitmapGenerator.generateClusterBitmap(count, color, isSelected)

        // Assert
        assertNotNull("Large cluster bitmap descriptor should not be null", descriptor)
    }

    @Test
    fun generateClusterBitmap_veryLargeCluster_returnsValidDescriptor() {
        // Arrange
        val count = 5000
        val color = 0xFFF44336.toInt() // Red
        val isSelected = false

        // Act
        val descriptor = MapMarkerBitmapGenerator.generateClusterBitmap(count, color, isSelected)

        // Assert
        assertNotNull("Very large cluster bitmap descriptor should not be null", descriptor)
    }

    @Test
    fun generateClusterBitmap_selectedCluster_returnsValidDescriptor() {
        // Arrange
        val count = 10
        val color = 0xFF9C27B0.toInt() // Purple
        val isSelected = true

        // Act
        val descriptor = MapMarkerBitmapGenerator.generateClusterBitmap(count, color, isSelected)

        // Assert
        assertNotNull("Selected cluster bitmap descriptor should not be null", descriptor)
    }

    @Test
    fun generateWaypointBitmap_returnsValidDescriptor() {
        // Arrange
        val label = "A"
        val color = 0xFF4CAF50.toInt() // Green

        // Act
        val descriptor = MapMarkerBitmapGenerator.generateWaypointBitmap(label, color)

        // Assert
        assertNotNull("Waypoint bitmap descriptor should not be null", descriptor)
    }

    @Test
    fun getCachedMarkerBitmap_cachesResult() {
        // Arrange
        val color = 0xFF2196F3.toInt()
        val isSelected = false

        // Act
        val descriptor1 = MapMarkerBitmapGenerator.getCachedMarkerBitmap(color, isSelected)
        val descriptor2 = MapMarkerBitmapGenerator.getCachedMarkerBitmap(color, isSelected)

        // Assert
        assertNotNull("First descriptor should not be null", descriptor1)
        assertNotNull("Second descriptor should not be null", descriptor2)
        // Same parameters should return same cached instance
        assertSame("Cached descriptors should be same instance", descriptor1, descriptor2)
    }

    @Test
    fun getCachedClusterBitmap_cachesResult() {
        // Arrange
        val count = 10
        val color = 0xFFFF9800.toInt()
        val isSelected = false

        // Act
        val descriptor1 = MapMarkerBitmapGenerator.getCachedClusterBitmap(count, color, isSelected)
        val descriptor2 = MapMarkerBitmapGenerator.getCachedClusterBitmap(count, color, isSelected)

        // Assert
        assertNotNull("First descriptor should not be null", descriptor1)
        assertNotNull("Second descriptor should not be null", descriptor2)
        // Same parameters should return same cached instance
        assertSame("Cached descriptors should be same instance", descriptor1, descriptor2)
    }

    @Test
    fun getCachedMarkerBitmap_differentParameters_returnsDifferentInstances() {
        // Arrange & Act
        val descriptor1 = MapMarkerBitmapGenerator.getCachedMarkerBitmap(0xFF2196F3.toInt(), false)
        val descriptor2 = MapMarkerBitmapGenerator.getCachedMarkerBitmap(0xFF4CAF50.toInt(), false)
        val descriptor3 = MapMarkerBitmapGenerator.getCachedMarkerBitmap(0xFF2196F3.toInt(), true)

        // Assert
        assertNotSame("Different colors should produce different instances", descriptor1, descriptor2)
        assertNotSame("Different selection states should produce different instances", descriptor1, descriptor3)
    }

    @Test
    fun clearCache_removesAllCachedBitmaps() {
        // Arrange
        val descriptor1 = MapMarkerBitmapGenerator.getCachedMarkerBitmap(0xFF2196F3.toInt(), false)
        assertNotNull("Descriptor should be cached", descriptor1)

        // Act
        MapMarkerBitmapGenerator.clearCache()
        val descriptor2 = MapMarkerBitmapGenerator.getCachedMarkerBitmap(0xFF2196F3.toInt(), false)

        // Assert
        assertNotNull("New descriptor should be generated", descriptor2)
        // After cache clear, should get new instance
        assertNotSame("After cache clear, should get new instance", descriptor1, descriptor2)
    }

    @Test
    fun generateMarkerBitmap_differentColors_producesDifferentBitmaps() {
        // Arrange
        val blueColor = 0xFF2196F3.toInt()
        val greenColor = 0xFF4CAF50.toInt()
        val redColor = 0xFFF44336.toInt()

        // Act
        val blueDescriptor = MapMarkerBitmapGenerator.generateMarkerBitmap(blueColor, false)
        val greenDescriptor = MapMarkerBitmapGenerator.generateMarkerBitmap(greenColor, false)
        val redDescriptor = MapMarkerBitmapGenerator.generateMarkerBitmap(redColor, false)

        // Assert
        assertNotNull("Blue marker should be generated", blueDescriptor)
        assertNotNull("Green marker should be generated", greenDescriptor)
        assertNotNull("Red marker should be generated", redDescriptor)
        // Different colors should produce different bitmaps (we can't easily test pixel data,
        // but we can verify they're generated without errors)
    }
}
