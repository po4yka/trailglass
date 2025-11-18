package com.po4yka.trailglass.feature.map

import com.po4yka.trailglass.domain.model.Coordinate
import com.po4yka.trailglass.domain.model.EnhancedMapMarker
import com.po4yka.trailglass.domain.model.HeatmapGradient
import com.po4yka.trailglass.domain.model.PlaceCategory
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.floats.shouldBeBetween
import kotlin.test.Test

/**
 * Unit tests for HeatmapGenerator.
 */
class HeatmapGeneratorTest {

    private val generator = HeatmapGenerator()

    @Test
    fun `generate with empty markers returns empty heatmap`() {
        // Arrange
        val markers = emptyList<EnhancedMapMarker>()

        // Act
        val heatmap = generator.generate(markers)

        // Assert
        heatmap.points.shouldBeEmpty()
    }

    @Test
    fun `generate with uniform intensity creates equal intensity points`() {
        // Arrange
        val markers = listOf(
            createMarker("1", 0.0, 0.0, visitCount = 1),
            createMarker("2", 1.0, 1.0, visitCount = 5),
            createMarker("3", 2.0, 2.0, visitCount = 10)
        )

        // Act
        val heatmap = generator.generate(
            markers = markers,
            intensityMode = HeatmapGenerator.IntensityMode.UNIFORM
        )

        // Assert
        heatmap.points shouldHaveSize 3
        heatmap.points.forEach { point ->
            point.intensity shouldBe 1.0f
        }
    }

    @Test
    fun `generate with visit count intensity scales by visit frequency`() {
        // Arrange
        val markers = listOf(
            createMarker("1", 0.0, 0.0, visitCount = 1),
            createMarker("2", 1.0, 1.0, visitCount = 5),
            createMarker("3", 2.0, 2.0, visitCount = 10)
        )

        // Act
        val heatmap = generator.generate(
            markers = markers,
            intensityMode = HeatmapGenerator.IntensityMode.VISIT_COUNT
        )

        // Assert
        heatmap.points shouldHaveSize 3

        // Marker with visitCount=10 should have intensity 1.0 (max)
        val maxPoint = heatmap.points.find { it.coordinate.latitude == 2.0 }
        maxPoint shouldNotBe null
        maxPoint!!.intensity shouldBe 1.0f

        // Marker with visitCount=1 should have intensity 0.1 (1/10)
        val minPoint = heatmap.points.find { it.coordinate.latitude == 0.0 }
        minPoint shouldNotBe null
        minPoint!!.intensity shouldBe 0.1f
    }

    @Test
    fun `generate with density intensity accounts for nearby markers`() {
        // Arrange
        // Create cluster of nearby markers
        val markers = listOf(
            createMarker("1", 0.0, 0.0),
            createMarker("2", 0.001, 0.001), // Very close to marker 1
            createMarker("3", 0.002, 0.002), // Very close to marker 1
            createMarker("4", 10.0, 10.0)    // Far away
        )

        // Act
        val heatmap = generator.generate(
            markers = markers,
            intensityMode = HeatmapGenerator.IntensityMode.DENSITY
        )

        // Assert
        heatmap.points shouldHaveSize 4

        // Markers near origin should have higher intensity due to density
        val densePoint = heatmap.points.find { it.coordinate.latitude == 0.0 }
        val sparsePoint = heatmap.points.find { it.coordinate.latitude == 10.0 }

        densePoint shouldNotBe null
        sparsePoint shouldNotBe null

        // Dense point should have higher intensity
        densePoint!!.intensity shouldBe 1.0f // Max intensity (most neighbors)
        sparsePoint!!.intensity shouldBe 0.0f // No neighbors
    }

    @Test
    fun `generate uses custom radius and opacity`() {
        // Arrange
        val markers = listOf(createMarker("1", 0.0, 0.0))
        val customRadius = 50
        val customOpacity = 0.8f

        // Act
        val heatmap = generator.generate(
            markers = markers,
            radius = customRadius,
            opacity = customOpacity
        )

        // Assert
        heatmap.radius shouldBe customRadius
        heatmap.opacity shouldBe customOpacity
    }

    @Test
    fun `generate uses custom gradient`() {
        // Arrange
        val markers = listOf(createMarker("1", 0.0, 0.0))
        val customGradient = HeatmapGradient.COOL

        // Act
        val heatmap = generator.generate(
            markers = markers,
            gradient = customGradient
        )

        // Assert
        heatmap.gradient shouldBe customGradient
    }

    @Test
    fun `generateFromCoordinates creates uniform intensity heatmap`() {
        // Arrange
        val coordinates = listOf(
            Coordinate(0.0, 0.0),
            Coordinate(1.0, 1.0),
            Coordinate(2.0, 2.0)
        )

        // Act
        val heatmap = generator.generateFromCoordinates(coordinates)

        // Assert
        heatmap.points shouldHaveSize 3
        heatmap.points.forEach { point ->
            point.intensity shouldBe 1.0f
        }
    }

    @Test
    fun `generateWeighted scales intensity by weights`() {
        // Arrange
        val coordinatesWithWeights = listOf(
            Coordinate(0.0, 0.0) to 1.0f,
            Coordinate(1.0, 1.0) to 5.0f,
            Coordinate(2.0, 2.0) to 10.0f
        )

        // Act
        val heatmap = generator.generateWeighted(coordinatesWithWeights)

        // Assert
        heatmap.points shouldHaveSize 3

        // Max weight should have intensity 1.0
        val maxPoint = heatmap.points.find { it.coordinate.latitude == 2.0 }
        maxPoint shouldNotBe null
        maxPoint!!.intensity shouldBe 1.0f

        // Min weight should have proportional intensity
        val minPoint = heatmap.points.find { it.coordinate.latitude == 0.0 }
        minPoint shouldNotBe null
        minPoint!!.intensity shouldBe 0.1f
    }

    @Test
    fun `generateWeighted with empty list returns empty heatmap`() {
        // Arrange
        val coordinatesWithWeights = emptyList<Pair<Coordinate, Float>>()

        // Act
        val heatmap = generator.generateWeighted(coordinatesWithWeights)

        // Assert
        heatmap.points.shouldBeEmpty()
    }

    @Test
    fun `intensity values are always between 0 and 1`() {
        // Arrange
        val markers = listOf(
            createMarker("1", 0.0, 0.0, visitCount = 100),
            createMarker("2", 1.0, 1.0, visitCount = 1),
            createMarker("3", 2.0, 2.0, visitCount = 50)
        )

        // Act
        val heatmap = generator.generate(
            markers = markers,
            intensityMode = HeatmapGenerator.IntensityMode.VISIT_COUNT
        )

        // Assert
        heatmap.points.forEach { point ->
            point.intensity.shouldBeBetween(0.0f, 1.0f, 0.0f)
        }
    }

    private fun createMarker(
        id: String,
        lat: Double,
        lng: Double,
        visitCount: Int = 1
    ): EnhancedMapMarker {
        return EnhancedMapMarker(
            id = id,
            coordinate = Coordinate(lat, lng),
            title = "Test Marker $id",
            snippet = null,
            placeVisitId = "visit_$id",
            category = PlaceCategory.OTHER,
            isFavorite = false,
            visitCount = visitCount
        )
    }
}
