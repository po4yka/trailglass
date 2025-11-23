package com.po4yka.trailglass.ui.screens.routeview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Satellite
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.po4yka.trailglass.feature.route.MapStyle

/** Individual map style option in the selector. */
@Composable
internal fun MapStyleOption(
    style: MapStyle,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
        shape = MaterialTheme.shapes.medium,
        color =
            if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector =
                        when (style) {
                            MapStyle.STANDARD -> Icons.Default.Map
                            MapStyle.SATELLITE -> Icons.Default.Satellite
                            MapStyle.TERRAIN -> Icons.Default.Terrain
                            MapStyle.DARK -> Icons.Default.DarkMode
                        },
                    contentDescription = null,
                    tint =
                        if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                )
                Text(
                    text = style.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyLarge,
                    color =
                        if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                )
            }

            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
