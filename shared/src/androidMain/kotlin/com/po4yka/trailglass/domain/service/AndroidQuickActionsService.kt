package com.po4yka.trailglass.domain.service

import io.github.oshai.kotlinlogging.KotlinLogging
import me.tatarka.inject.annotations.Inject

private val logger = KotlinLogging.logger {}

/**
 * Android implementation of QuickActionsService.
 *
 * On Android, app shortcuts are primarily handled via static XML (shortcuts.xml).
 * This service is a placeholder for potential dynamic shortcut management.
 *
 * Note: Android supports dynamic shortcuts via ShortcutManager, but static shortcuts
 * are preferred for simplicity. If you need dynamic shortcuts, you can use
 * ShortcutManagerCompat to add/remove shortcuts programmatically.
 */
@Inject
class AndroidQuickActionsService : QuickActionsService {
    override fun registerQuickActions(actions: List<QuickAction>) {
        logger.debug { "Android shortcuts are managed via static XML (shortcuts.xml)" }
        logger.debug { "Dynamic shortcuts request: ${actions.size} actions" }

        // TODO: Implement dynamic shortcuts using ShortcutManagerCompat if needed
        // Example:
        // val shortcutManager = ShortcutManagerCompat.getInstance(context)
        // val shortcuts = actions.map { action ->
        //     ShortcutInfoCompat.Builder(context, action.type)
        //         .setShortLabel(action.title)
        //         .setLongLabel(action.subtitle ?: action.title)
        //         .setIcon(mapIcon(action.icon))
        //         .setIntent(createIntent(action.type))
        //         .build()
        // }
        // shortcutManager.setDynamicShortcuts(shortcuts)
    }

    override fun clearQuickActions() {
        logger.debug { "Clearing dynamic shortcuts (not implemented)" }

        // TODO: Implement if using dynamic shortcuts
        // ShortcutManagerCompat.getInstance(context).removeAllDynamicShortcuts()
    }

    override fun handleQuickAction(actionType: String): Boolean {
        logger.info { "Quick action handled via deep link: $actionType" }

        // On Android, shortcuts use deep links which are handled in MainActivity
        // This method is not needed for Android
        return true
    }
}
