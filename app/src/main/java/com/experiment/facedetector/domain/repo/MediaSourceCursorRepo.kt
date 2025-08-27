package com.experiment.facedetector.domain.repo

import com.experiment.facedetector.domain.entities.MediaSourceCursor
import com.experiment.facedetector.domain.entities.MediaSourceType


/**
 * Abstract storage for media source cursors.
 * Domain depends on this, data layer provides implementation.
 */
interface MediaSourceCursorRepo {

    suspend fun get(source: MediaSourceType): MediaSourceCursor?

    suspend fun upsert(source: MediaSourceType, cursor: MediaSourceCursor, nowMs: Long)

    suspend fun advanceIfNewer(source: MediaSourceType, newCursor: MediaSourceCursor, nowMs: Long)
}
