package com.po4yka.trailglass.data.auth

/**
 * Manages the current user session.
 *
 * This interface provides access to the authenticated user's information.
 * Supports three modes:
 * - Authenticated user (with account)
 * - Guest user (no account, local-only data)
 * - No user (not authenticated, not guest)
 */
interface UserSession {
    /**
     * Returns the current user's ID.
     * Returns null if no user is authenticated.
     */
    fun getCurrentUserId(): String?

    /**
     * Checks if a user is currently authenticated.
     */
    fun isAuthenticated(): Boolean

    /**
     * Checks if the current user is in guest mode.
     */
    fun isGuest(): Boolean = getCurrentUserId() == GUEST_USER_ID

    /**
     * Sets the current user ID (for future auth integration).
     */
    fun setUserId(userId: String?)

    companion object {
        /**
         * Special user ID for guest mode.
         * When a user continues without an account, this ID is used.
         */
        const val GUEST_USER_ID = "guest_user"

        /**
         * Default user ID for single-user scenarios (legacy).
         */
        const val DEFAULT_USER_ID = "default_user"
    }
}

/**
 * Default implementation of UserSession.
 *
 * Currently provides a default user ID for single-user scenarios.
 * This can be extended or replaced with a proper authentication system
 * that integrates with OAuth, Firebase Auth, or custom auth backends.
 */
class DefaultUserSession(
    private var currentUserId: String? = DEFAULT_USER_ID
) : UserSession {

    override fun getCurrentUserId(): String? = currentUserId

    override fun isAuthenticated(): Boolean = currentUserId != null

    override fun setUserId(userId: String?) {
        currentUserId = userId
    }

    companion object {
        /**
         * Default user ID for single-user scenarios.
         * This ensures existing functionality works while allowing
         * future multi-user support.
         */
        const val DEFAULT_USER_ID = "default_user"

        /**
         * Singleton instance for convenience.
         * In production, this should be injected via DI (Koin, etc.).
         */
        private var instance: UserSession? = null

        /**
         * Gets the shared UserSession instance.
         */
        fun getInstance(): UserSession {
            return instance ?: DefaultUserSession().also { instance = it }
        }

        /**
         * Sets a custom UserSession implementation.
         * Useful for testing or custom auth integrations.
         */
        fun setInstance(session: UserSession) {
            instance = session
        }
    }
}
