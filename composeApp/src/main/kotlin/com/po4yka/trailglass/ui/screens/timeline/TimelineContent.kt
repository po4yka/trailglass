package com.po4yka.trailglass.ui.screens.timeline

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.po4yka.trailglass.R
import com.po4yka.trailglass.feature.timeline.GetTimelineUseCase
import com.po4yka.trailglass.feature.timeline.TimelineFilter
import com.po4yka.trailglass.feature.timeline.TimelineZoomLevel

@Composable
fun EnhancedTimelineContent(
    items: List<GetTimelineUseCase.TimelineItemUI>,
    zoomLevel: TimelineZoomLevel,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items, key = { it.id }) { item ->
            when (item) {
                is GetTimelineUseCase.TimelineItemUI.DayStartUI -> {
                    DayMarkerCard(text = stringResource(R.string.timeline_day_start), icon = Icons.Default.WbSunny)
                }

                is GetTimelineUseCase.TimelineItemUI.DayEndUI -> {
                    DayMarkerCard(text = stringResource(R.string.timeline_day_end), icon = Icons.Default.NightsStay)
                }

                is GetTimelineUseCase.TimelineItemUI.VisitUI -> {
                    EnhancedVisitCard(visit = item.placeVisit)
                }

                is GetTimelineUseCase.TimelineItemUI.RouteUI -> {
                    EnhancedRouteCard(route = item.routeSegment)
                }

                is GetTimelineUseCase.TimelineItemUI.DaySummaryUI -> {
                    DaySummaryCard(summary = item)
                }

                is GetTimelineUseCase.TimelineItemUI.WeekSummaryUI -> {
                    WeekSummaryCard(summary = item)
                }

                is GetTimelineUseCase.TimelineItemUI.MonthSummaryUI -> {
                    MonthSummaryCard(summary = item)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineFilterBottomSheet(
    currentFilter: TimelineFilter,
    onFilterChanged: (TimelineFilter) -> Unit,
    onDismiss: () -> Unit
) {
    var localFilter by remember { mutableStateOf(currentFilter) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxHeight(0.8f)
    ) {
        Column(
            modifier =
                Modifier
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp)
        ) {
            Text(
                stringResource(R.string.timeline_filter_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Transport types
                item {
                    FilterSection(title = stringResource(R.string.timeline_filter_transport_types)) {
                        TransportTypeFilterChips(
                            selectedTypes = localFilter.transportTypes,
                            onTypeToggled = { type ->
                                localFilter =
                                    if (type in localFilter.transportTypes) {
                                        localFilter.copy(transportTypes = localFilter.transportTypes - type)
                                    } else {
                                        localFilter.copy(transportTypes = localFilter.transportTypes + type)
                                    }
                            }
                        )
                    }
                }

                // Place categories
                item {
                    FilterSection(title = stringResource(R.string.timeline_filter_place_categories)) {
                        PlaceCategoryFilterChips(
                            selectedCategories = localFilter.placeCategories,
                            onCategoryToggled = { category ->
                                localFilter =
                                    if (category in localFilter.placeCategories) {
                                        localFilter.copy(
                                            placeCategories =
                                                localFilter.placeCategories - category
                                        )
                                    } else {
                                        localFilter.copy(
                                            placeCategories =
                                                localFilter.placeCategories + category
                                        )
                                    }
                            }
                        )
                    }
                }

                // Favorites only
                item {
                    FilterSection(title = stringResource(R.string.timeline_filter_options)) {
                        FavoritesFilterSwitch(
                            showOnlyFavorites = localFilter.showOnlyFavorites,
                            onToggled = { localFilter = localFilter.copy(showOnlyFavorites = it) }
                        )
                    }
                }

                // Action buttons
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                localFilter = TimelineFilter()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.action_reset))
                        }
                        Button(
                            onClick = {
                                onFilterChanged(localFilter)
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.action_apply))
                        }
                    }
                }
            }
        }
    }
}
