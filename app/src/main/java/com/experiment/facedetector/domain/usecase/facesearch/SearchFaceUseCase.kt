package com.experiment.facedetector.domain.usecase.facesearch

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.filter
import androidx.paging.map
import com.experiment.facedetector.data.local.entities.toDomain
import com.experiment.facedetector.data.local.paging.SearchPhotoPagingSource
import com.experiment.facedetector.domain.entities.MediaWithFacesDomain
import com.experiment.facedetector.domain.matcher.FaceEmbeddingMatcher
import com.experiment.facedetector.domain.repo.MediaWithFacesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SearchSimilarPhotoUseCase(
    private val mediaWithFacesRepository: MediaWithFacesRepository,
    private val faceEmbeddingMatcher: FaceEmbeddingMatcher,
    ) {
    operator fun invoke(searchEmbeddings: List<FloatArray>, pageSize: Int = 25): Flow<PagingData<MediaWithFacesDomain>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                prefetchDistance = 10,
                enablePlaceholders = true
            ),
            pagingSourceFactory = {
                SearchPhotoPagingSource(mediaWithFacesRepository)
            }
        ).flow.map { pagingData ->
            pagingData.map { entity ->
                entity.toDomain()
            }.filter {
                faceEmbeddingMatcher.hasMatchingFaceEmbedding(
                    it, searchEmbeddings
                )
            }
        }
    }
}

