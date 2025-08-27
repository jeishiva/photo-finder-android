package com.experiment.facedetector.domain.usecase.facesearch

import androidx.paging.Pager
import com.experiment.facedetector.data.local.entities.MediaWithFaces
import com.experiment.facedetector.domain.repo.MediaWithFacesRepository


class SearchPhotosPagedUseCase(val mediaWithFacesRepository: MediaWithFacesRepository) {
    operator fun invoke() : Pager<Pair<Long, Long>, MediaWithFaces> {
        return mediaWithFacesRepository.pagerFacesOnly()
    }
}
