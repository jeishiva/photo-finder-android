package com.experiment.facedetector.data.local.repo
import androidx.room.withTransaction
import com.experiment.facedetector.data.local.AppDatabase
import com.experiment.facedetector.data.local.dao.MediaSourceCursorDao
import com.experiment.facedetector.data.local.entities.SourceMediaCursor
import com.experiment.facedetector.domain.entities.MediaSourceCursor
import com.experiment.facedetector.domain.entities.MediaSourceType
import com.experiment.facedetector.domain.repo.MediaSourceCursorRepo

/**
 * Persists and advances per-source cursors in a monotonic way.
 *
 * Notes:
 * - On API 30+, prefer GENERATION_MODIFIED. We still keep DATE_MODIFIED (sec) as a fallback
 *   for heterogeneous sources (e.g., future cloud providers).
 * - Advancement is done inside a Room transaction to avoid races between workers.
 */
class MediaSourceCursorRepoImpl(
    private val appDatabase: AppDatabase
) : MediaSourceCursorRepo {

    private val dao: MediaSourceCursorDao = appDatabase.mediaSourceCursorDao()

    override suspend fun get(source: MediaSourceType): MediaSourceCursor? {
        val row = dao.getOnce(source.key)
        if (row == null) {
            return null
        }
        return row.toDomainCursor()
    }

    override suspend fun upsert(source: MediaSourceType, cursor: MediaSourceCursor, nowMs: Long) {
        val entity = SourceMediaCursor.fromDomain(
            sourceType = source,
            cursor = cursor,
            updatedAtMs = nowMs
        )
        dao.upsert(entity)
    }

    /**
     * Advance the stored cursor only if the new one is strictly newer.
     * This guards against out-of-order pages or concurrent workers.
     */
    override suspend fun advanceIfNewer(
        source: MediaSourceType,
        newCursor: MediaSourceCursor,
        nowMs: Long
    ) {
        appDatabase.withTransaction {
            val current = dao.getOnce(source.key)
            if (current == null) {
                dao.upsert(
                    SourceMediaCursor.fromDomain(
                        sourceType = source,
                        cursor = newCursor,
                        updatedAtMs = nowMs
                    )
                )
                return@withTransaction
            }
            val isNewer = isNewerCursor(current.toDomainCursor(), newCursor)
            // strictly update only if the new cursor is latest
            if (isNewer) {
                dao.updateCursor(
                    sourceKey = source.key,
                    lastGenerationModified = newCursor.generationModified,
                    lastDateModifiedSec = newCursor.lastModifiedSeconds,
                    lastId = newCursor.lastId,
                    updatedAtMs = nowMs
                )
            }
        }
    }

    /**
     * Determines if a cursor position is newer than another.
     *
     * Comparison priority:
     * 1. Generation number (if available) - higher generation = newer
     * 2. Date modified (if no generation) - later date = newer
     * 3. ID (as tiebreaker) - higher ID = newer
     *
     * @param current The current cursor position
     * @param new The new cursor position to compare
     * @return true if the new position is newer than the current one
     */
    private fun isNewerCursor(current: MediaSourceCursor, new: MediaSourceCursor): Boolean {
        return when {
            // Generation-based comparison (preferred when available)
            hasGenerationData(current, new) -> compareByGeneration(current, new)
            // Fallback to date-based comparison
            else -> compareByDateModified(current, new)
        }
    }

    private fun hasGenerationData(current: MediaSourceCursor, new: MediaSourceCursor): Boolean {
        return current.generationModified != null || new.generationModified != null
    }

    private fun compareByGeneration(current: MediaSourceCursor, new: MediaSourceCursor): Boolean {
        val currentGen = current.generationModified
        val newGen = new.generationModified
        return when {
            currentGen == null -> true  // No current generation, new one is newer
            newGen == null -> false     // Current has generation, new doesn't
            newGen > currentGen -> true
            newGen < currentGen -> false
            else -> compareByIdTiebreaker(current.lastId, new.lastId) // Same generation, use ID
        }
    }

    private fun compareByDateModified(current: MediaSourceCursor, new: MediaSourceCursor): Boolean {
        val currentMod = current.lastModifiedSeconds
        val newMod = new.lastModifiedSeconds
        return when {
            currentMod == null -> newMod != null  // No current date, new one exists
            newMod == null -> false               // Current has date, new doesn't
            newMod > currentMod -> true
            newMod < currentMod -> false
            else -> compareByIdTiebreaker(current.lastId, new.lastId) // Same date, use ID
        }
    }

    private fun compareByIdTiebreaker(currentId: Long?, newId: Long?): Boolean {
        val safeCurrentId = currentId ?: Long.MIN_VALUE
        val safeNewId = newId ?: Long.MIN_VALUE
        return safeNewId > safeCurrentId
    }
}
