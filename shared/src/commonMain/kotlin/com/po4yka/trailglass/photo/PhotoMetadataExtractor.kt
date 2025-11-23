package com.po4yka.trailglass.photo

import com.po4yka.trailglass.domain.model.PhotoMetadata

/** Platform-specific photo metadata extractor. */
interface PhotoMetadataExtractor {
    /**
     * Extract EXIF and metadata from a photo URI.
     *
     * @param photoUri Platform-specific photo URI
     * @param photoId Photo ID to associate metadata with
     * @return Extracted metadata or null if extraction fails
     */
    suspend fun extractMetadata(
        photoUri: String,
        photoId: String
    ): PhotoMetadata?
}
