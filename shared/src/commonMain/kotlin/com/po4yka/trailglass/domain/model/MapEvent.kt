package com.po4yka.trailglass.domain.model

/**
 * Sealed class representing events that can occur on the map.
 *
 * This provides an event-driven API for handling user interactions with the map,
 * allowing better separation of concerns and testability.
 */
sealed class MapEvent {
    /**
     * User tapped on a marker.
     *
     * @param markerId The ID of the tapped marker
     */
    data class MarkerTapped(
        val markerId: String
    ) : MapEvent()

    /**
     * User tapped on a route.
     *
     * @param routeId The ID of the tapped route
     */
    data class RouteTapped(
        val routeId: String
    ) : MapEvent()

    /**
     * User tapped on the map (not on any marker or route).
     *
     * @param coordinate The coordinate where the user tapped
     */
    data class MapTapped(
        val coordinate: Coordinate
    ) : MapEvent()

    /**
     * Camera position changed (either by user gesture or programmatic movement).
     *
     * @param position The new camera position
     */
    data class CameraMoved(
        val position: CameraPosition
    ) : MapEvent()

    /**
     * Map has finished loading and is ready for interaction.
     */
    data object MapReady : MapEvent()
}

/**
 * Interface for sending map events from the UI layer to the business logic layer.
 *
 * This decouples the map view from the controller, making it easier to test
 * and allowing for more flexible event handling.
 */
interface MapEventSink {
    /**
     * Send a map event to be processed.
     *
     * @param event The event to send
     */
    fun send(event: MapEvent)
}
