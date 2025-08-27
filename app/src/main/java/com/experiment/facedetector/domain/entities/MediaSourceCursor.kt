package com.experiment.facedetector.domain.entities

/**
 * Cursor describing "where to continue from" in a delta scan.
 *
 * Semantics:
 * - If [generationModified] is present, use it as the primary token.
 * - Otherwise, use [lastModifiedSeconds] (seconds) as the token.
 * - [lastId] is the deterministic tie-breaker when multiple rows share the same token.
 *
 * Ordering (ascending during scanning):
 *   1) token (generationModified OR dateModifiedSec)
 *   2) _ID
 *
 * Next page should start strictly *after* this cursor (exclusive on the pair).
 */
data class MediaSourceCursor(
    val generationModified: Long? = null, // API 29+, preferred on API 30+
    val lastModifiedSeconds: Long? = null,    // fallback token (seconds)
    val lastId: Long? = null              // tie-breaker for stable paging
)
