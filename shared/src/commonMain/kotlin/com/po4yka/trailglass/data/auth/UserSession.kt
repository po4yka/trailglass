package com.po4yka.trailglass.data.auth

/**
 * Manages the current user session.
 *
 * This interface provides access to the authenticated user's information.
 * In the current implementation, it provides a default user ID, but it's
 * designed to be extensible for future authentication systems (OAuth, JWT, etc.).
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
     * Sets the current user ID (for future auth integration).
     */
    fun setUserId(userId: String?)
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
