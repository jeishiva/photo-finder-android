package com.experiment.facedetector.domain.repo

import androidx.paging.PagingData
import com.experiment.facedetector.data.local.entities.MediaWithFaces
import kotlinx.coroutines.flow.Flow

interface FaceSearchRepository {
    suspend fun searchMatchingFacesPagedFlow(
        searchFaceEmbeddings: List<FloatArray>,
    ) : Flow<PagingData<MediaWithFaces>>
}
