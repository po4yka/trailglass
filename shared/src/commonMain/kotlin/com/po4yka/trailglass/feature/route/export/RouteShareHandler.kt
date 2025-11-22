package com.po4yka.trailglass.feature.route.export

/**
 * Platform-specific handler for sharing route files and map snapshots.
 * Implementations should use platform-specific sharing mechanisms.
 */
interface RouteShareHandler {
    /**
     * Share a route file (GPX or KML).
     *
     * @param fileName Name of the file
     * @param content File content
     * @param mimeType MIME type of the content
     */
    suspend fun shareRouteFile(
        fileName: String,
        content: String,
        mimeType: String
    ): Result<Unit>

    /**
     * Share a map snapshot image.
     *
     * @param imageData Image data (PNG bytes)
     * @param tripName Trip name for the file
     */
    suspend fun shareMapSnapshot(
        imageData: ByteArray,
        tripName: String
    ): Result<Unit>

    /**
     * Save route file to device storage.
     *
     * @param fileName Name of the file
     * @param content File content
     * @param mimeType MIME type of the content
     * @return URI of the saved file
     */
    suspend fun saveRouteFile(
        fileName: String,
        content: String,
        mimeType: String
    ): Result<String>
}
