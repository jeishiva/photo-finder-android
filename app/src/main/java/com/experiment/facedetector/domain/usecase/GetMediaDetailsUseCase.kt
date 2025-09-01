package com.experiment.facedetector.domain.usecase

import com.experiment.facedetector.data.local.entities.toDomain
import com.experiment.facedetector.domain.entities.MediaDomain
import com.experiment.facedetector.domain.repo.MediaRepository

class GetMediaDetailsUseCase(
    private val mediaRepository: MediaRepository
) {
    suspend operator fun invoke(mediaId : Long): MediaDomain? {
        return mediaRepository.getMedia(mediaId)?.toDomain()
    }
}
