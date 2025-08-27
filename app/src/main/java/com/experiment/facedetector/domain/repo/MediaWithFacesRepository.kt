package com.experiment.facedetector.domain.repo

import androidx.paging.Pager
import com.experiment.facedetector.data.local.entities.MediaWithFaces

/**
 * Read-only access to media lists (with face joins where needed).
 * Uses forward-only keyset pagination (newest → older).
 */
interface MediaWithFacesRepository {

    /**
     * Newest-first pager for all media.
     */
    fun pagerAll(pageSize: Int = 60): Pager<Pair<Long, Long>, MediaWithFaces>

    /**
     * Newest-first pager for media that has at least one face.
     */
    fun pagerFacesOnly(pageSize: Int = 60): Pager<Pair<Long, Long>, MediaWithFaces>


}
