package com.experiment.facedetector.domain.source

import com.experiment.facedetector.domain.entities.MediaSourceCursor
import com.experiment.facedetector.domain.entities.MediaSourcePage
import com.experiment.facedetector.domain.entities.SourceMediaItem

/**
 * Media source capable of cursor-based delta listing.
 *
 * Contract:
 * - Results MUST be ordered by:
 *     a) GENERATION_MODIFIED ASC, _ID ASC when generation is available
 *     b) DATE_MODIFIED ASC, _ID ASC   otherwise
 * - The first item returned MUST be strictly greater than the provided cursor
 *   (compare by token, then by _ID).
 * - Implementations should always prefer GENERATION_MODIFIED on API 30+.
 */
interface PagedMediaSource {
    suspend fun listAfter(
        cursor: MediaSourceCursor?,
        limit: Int,
    ): MediaSourcePage<SourceMediaItem>
}
