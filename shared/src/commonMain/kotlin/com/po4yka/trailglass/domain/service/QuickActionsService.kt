package com.po4yka.trailglass.domain.service

/**
 * Service for managing quick actions (iOS 3D Touch/Haptic Touch) and app shortcuts.
 *
 * This is a platform-specific service for handling home screen quick actions:
 * - iOS: UIApplicationShortcutItem (3D Touch/Haptic Touch)
 * - Android: Handled via static XML shortcuts (already implemented)
 */
interface QuickActionsService {
    /**
     * Register dynamic quick actions.
     *
     * Dynamic quick actions are created programmatically at runtime and can change
     * based on app state (e.g., recent trips, favorite places).
     *
     * @param actions List of quick actions to register
     */
    fun registerQuickActions(actions: List<QuickAction>)

    /**
     * Clear all dynamic quick actions.
     */
    fun clearQuickActions()

    /**
     * Handle a quick action selection.
     *
     * This is called when user taps a quick action from home screen.
     *
     * @param actionType The type identifier of the action
     * @return true if handled, false otherwise
     */
    fun handleQuickAction(actionType: String): Boolean
}

/**
 * Represents a quick action item.
 */
data class QuickAction(
    val type: String,
    val title: String,
    val subtitle: String? = null,
    val icon: QuickActionIcon = QuickActionIcon.DEFAULT
)

/**
 * Icons available for quick actions.
 *
 * Maps to system icons on iOS and custom icons on Android.
 */
enum class QuickActionIcon {
    DEFAULT,
    ADD,
    PLAY,
    PAUSE,
    LOCATION,
    MAP,
    EXPORT,
    STATS
}
