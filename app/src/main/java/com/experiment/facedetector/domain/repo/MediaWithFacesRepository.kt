package com.experiment.facedetector.domain.repo

import androidx.paging.Pager
import com.experiment.facedetector.data.local.entities.MediaWithFaces
import com.experiment.facedetector.data.local.entities.ProcessedState

/**
 * Read-only access to media lists (with face joins where needed).
 * Uses forward-only keyset pagination (newest → older).
 */
interface MediaWithFacesRepository {
    suspend fun loadMediaBefore(
        cursorDate: Long,
        cursorId: Long,
        limit: Int,
        processed: ProcessedState,
    ): List<MediaWithFaces>
}
