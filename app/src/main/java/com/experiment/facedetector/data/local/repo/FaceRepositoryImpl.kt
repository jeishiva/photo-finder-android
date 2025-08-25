package com.experiment.facedetector.data.local.repo

import com.experiment.facedetector.data.local.dao.FaceDao
import com.experiment.facedetector.data.local.entities.FaceEntity
import com.experiment.facedetector.domain.repo.FaceRepository

class FaceRepositoryImpl(
    private val faceDao: FaceDao
) : FaceRepository {
    override suspend fun upsertAll(faces: List<FaceEntity>) {
        if (faces.isEmpty()) {
            return
        } else {
            faceDao.upsertAll(faces)
        }
    }
}
