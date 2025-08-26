package com.experiment.facedetector.domain.repo

import androidx.paging.PagingData
import com.experiment.facedetector.data.local.entities.MediaWithFaces
import com.experiment.facedetector.domain.filter.MediaFilter
import kotlinx.coroutines.flow.Flow

interface MediaPagingRepository {
    fun pagedMedia(filter: MediaFilter): Flow<PagingData<MediaWithFaces>>
}