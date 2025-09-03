package com.experiment.facedetector.domain.usecase

import androidx.paging.PagingData
import com.experiment.facedetector.data.local.repo.MediaPagingRepository
import com.experiment.facedetector.domain.entities.MediaWithFacesDomain
import com.experiment.facedetector.domain.matcher.FaceEmbeddingMatcher
import kotlinx.coroutines.flow.Flow

import androidx.paging.map
import androidx.paging.filter
import kotlinx.coroutines.flow.map

class SearchSimilarPhotoUseCase(
    private val mediaPagingRepository: MediaPagingRepository,
    private val faceEmbeddingMatcher: FaceEmbeddingMatcher,
) {
    operator fun invoke(
        searchEmbeddings: List<FloatArray>,
    ): Flow<PagingData<MediaWithFacesDomain>> {
        val searchPagingFlow = mediaPagingRepository.getSearchFaceFlow()
        return searchPagingFlow.map { pagingData ->
            pagingData
                .map { it }
                .filter { media ->
                    faceEmbeddingMatcher.hasMatchingFaceEmbedding(
                        media,
                        searchEmbeddings
                    )
                }
        }
    }
}

