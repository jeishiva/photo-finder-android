package com.experiment.facedetector.domain.entities


/**
 * Page wrapper for cursor-based listing.
 */
data class MediaSourcePage<T>(
    val items: List<T>,
    val nextCursor: MediaSourceCursor?,
    val hasMore: Boolean
)