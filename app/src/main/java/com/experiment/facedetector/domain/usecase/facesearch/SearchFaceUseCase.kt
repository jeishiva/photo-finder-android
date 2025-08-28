package com.experiment.facedetector.domain.usecase.facesearch

import androidx.paging.PagingData
import androidx.paging.filter
import androidx.paging.map
import com.experiment.facedetector.data.local.entities.toDomain
import com.experiment.facedetector.domain.entities.MediaWithFacesDomain
import com.experiment.facedetector.domain.matcher.FaceEmbeddingMatcher
import com.experiment.facedetector.domain.repo.MediaWithFacesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class SearchSimilarPhotoUseCase(
    private val mediaWithFacesRepository: MediaWithFacesRepository,
    private val faceEmbeddingMatcher: FaceEmbeddingMatcher,
) {
    operator fun invoke(searchEmbeddings: List<FloatArray>): Flow<PagingData<MediaWithFacesDomain>> {
        return mediaWithFacesRepository.pagerFacesOnly(pageSize = 10).flow.map { pagingData ->
            pagingData.map {
                it.toDomain()
            }.filter {
                faceEmbeddingMatcher.hasMatchingFaceEmbedding(
                    it, searchEmbeddings
                )
            }
        }
    }
}

