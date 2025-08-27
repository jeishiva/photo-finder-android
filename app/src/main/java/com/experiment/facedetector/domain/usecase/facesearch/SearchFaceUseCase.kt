package com.experiment.facedetector.domain.usecase.facesearch

import androidx.paging.Pager
import androidx.paging.PagingData
import androidx.paging.map
import com.experiment.facedetector.data.local.entities.toDomain
import com.experiment.facedetector.domain.entities.MediaWithFacesDomain
import com.experiment.facedetector.domain.repo.MediaWithFacesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class SearchPhotosPagedUseCase(
    private val mediaWithFacesRepository: MediaWithFacesRepository
) {
    operator fun invoke(): Flow<PagingData<MediaWithFacesDomain>> {
        return mediaWithFacesRepository.pagerFacesOnly().flow.map { pagingData ->
            pagingData.map { it.toDomain() }
        }
    }
}

