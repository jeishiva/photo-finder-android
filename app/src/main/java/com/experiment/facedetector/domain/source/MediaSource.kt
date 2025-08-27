package com.experiment.facedetector.domain.source

import com.experiment.facedetector.domain.entities.MediaSourceCursor
import com.experiment.facedetector.domain.entities.MediaSourcePage
import com.experiment.facedetector.domain.entities.MediaSourceType
import com.experiment.facedetector.domain.entities.SourceMediaItem

/**
 * Media source capable of cursor-based delta listing.
 *
 * Contract:
 * - Results MUST be ordered by:
 *     a) GENERATION_MODIFIED ASC, _ID ASC   when generation is available
 *     b) DATE_MODIFIED ASC, _ID ASC         otherwise
 * - The first item returned MUST be strictly greater than the provided cursor
 *   (compare by token, then by _ID).
 * - Implementations should always prefer GENERATION_MODIFIED on API 30+.
 */
interface MediaSource {

    val sourceType: MediaSourceType
    /**
     * List items *after* the given cursor, up to [limit].
     *
     * @param cursor The last-seen position (may be null for initial scan).
     * @param limit  Maximum number of items to return.
     *
     * @return A page of items with a cursor for the next call. If the data
     *         source was exhausted, [hasMore] should be false and [nextCursor] may be null.
     */
    suspend fun listAfter(
        cursor: MediaSourceCursor?,
        limit: Int
    ): MediaSourcePage<SourceMediaItem>
}