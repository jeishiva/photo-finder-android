package com.experiment.facedetector.domain.usecase.facesearch

import androidx.paging.PagingData
import com.experiment.facedetector.data.local.entities.MediaWithFaces
import com.experiment.facedetector.domain.repo.FaceSearchRepository
import kotlinx.coroutines.flow.Flow

class SearchPhotosPagedUseCase(
    private val repository: FaceSearchRepository
) {
    suspend operator fun invoke(
        searchEmbeddings: List<FloatArray>
    ): Flow<PagingData<MediaWithFaces>> {
        return repository.searchMatchingFacesPagedFlow(searchEmbeddings)
    }
}
