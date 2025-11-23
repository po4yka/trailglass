package com.po4yka.trailglass.ui.examples

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.po4yka.trailglass.ui.theme.MorphableShapes
import com.po4yka.trailglass.ui.theme.animateShapeMorph
import com.po4yka.trailglass.ui.theme.expressiveShapeMorphSpring

/**
 * Example implementations of shape morphing for reference and testing.
 *
 * These examples demonstrate:
 * - Basic shape morphing
 * - Interactive state-based morphing
 * - Category-based semantic morphing
 * - Performance-optimized patterns
 *
 * Use these as templates for implementing shape morphing in new components.
 */

/** Example 1: Basic FAB with tracking state morphing. Morphs from circle to triangle when tracking is active. */
@Composable
fun Example1_TrackingFAB() {
    var isTracking by remember { mutableStateOf(false) }

    val fabShape by animateShapeMorph(
        targetShape = if (isTracking) MorphableShapes.Triangle else MorphableShapes.Circle,
        animationSpec = expressiveShapeMorphSpring()
    )

    FloatingActionButton(
        onClick = { isTracking = !isTracking },
        shape = fabShape,
        containerColor =
            if (isTracking) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.secondaryContainer
            }
    ) {
        Icon(
            imageVector = Icons.Default.Navigation,
            contentDescription = if (isTracking) "Stop Tracking" else "Start Tracking"
        )
    }
}

/** Example 2: Interactive shape selector. Demonstrates morphing between all available shapes. */
@Composable
fun Example2_ShapeSelector() {
    val shapes =
        listOf(
            "Circle" to MorphableShapes.Circle,
            "Triangle" to MorphableShapes.Triangle,
            "Hexagon" to MorphableShapes.Hexagon,
            "Diamond" to MorphableShapes.Diamond,
            "Square" to MorphableShapes.RoundedSquare,
            "Wave" to MorphableShapes.Wave,
            "Petal" to MorphableShapes.Petal
        )

    var selectedIndex by remember { mutableStateOf(0) }
    val (shapeName, currentShape) = shapes[selectedIndex]

    val morphedShape by animateShapeMorph(
        targetShape = currentShape,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
    )

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Current Shape: $shapeName",
            style = MaterialTheme.typography.titleMedium
        )

        Surface(
            modifier =
                Modifier
                    .size(120.dp)
                    .clickable {
                        selectedIndex = (selectedIndex + 1) % shapes.size
                    },
            shape = morphedShape,
            color = MaterialTheme.colorScheme.primary
        ) {}

        Text(
            text = "Tap to cycle through shapes",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Example 3: Category badge showcase. Shows all category types with their semantic shapes. */
@Composable
fun Example3_CategoryShowcase() {
    val categories =
        listOf(
            "water",
            "transport",
            "food",
            "work",
            "flight",
            "bike",
            "cafe",
            "nature"
        )

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Category Badge Examples",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        categories.chunked(2).forEach { rowCategories ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowCategories.forEach { category ->
                    com.po4yka.trailglass.ui.components.CategoryBadge(
                        category = category,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/** Example 4: Loading state morphing. Shows morphing loading indicator with shape cycling. */
@Composable
fun Example4_LoadingStates() {
    var isLoading by remember { mutableStateOf(true) }

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        com.po4yka.trailglass.ui.components.MorphingLoadingIndicator(
            visible = isLoading,
            size = 64.dp
        )

        Button(onClick = { isLoading = !isLoading }) {
            Text(if (isLoading) "Stop Loading" else "Start Loading")
        }
    }
}

/** Example 5: Card with press state morphing. Demonstrates tactile feedback through shape changes. */
@Composable
fun Example5_InteractiveCard() {
    var isPressed by remember { mutableStateOf(false) }

    val cardShape by animateShapeMorph(
        targetShape = if (isPressed) MorphableShapes.RoundedSquare else MorphableShapes.Circle,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
    )

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable { isPressed = !isPressed },
        shape = cardShape,
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Interactive Card",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Click to see shape morph",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Preview composable showing all examples. */
@Preview(showBackground = true)
@Composable
private fun ShapeMorphingExamplesPreview() {
    MaterialTheme {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Shape Morphing Examples",
                style = MaterialTheme.typography.headlineMedium
            )

            Example1_TrackingFAB()
            Divider()

            Example2_ShapeSelector()
            Divider()

            Example3_CategoryShowcase()
            Divider()

            Example4_LoadingStates()
            Divider()

            Example5_InteractiveCard()
        }
    }
}
