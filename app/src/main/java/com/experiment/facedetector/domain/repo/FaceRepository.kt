package com.experiment.facedetector.domain.repo

import com.experiment.facedetector.data.local.entities.FaceEntity

/**
 * Abstraction over face (embedding) persistence.
 * Implement in the data layer using Room DAOs.
 */
interface FaceRepository {
    suspend fun upsertAll(faces: List<FaceEntity>)
}
