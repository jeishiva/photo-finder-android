package com.experiment.facedetector.domain.repo

import com.experiment.facedetector.domain.entities.FaceEmbedding
import com.experiment.facedetector.domain.entities.FaceSearchItem

interface FaceSearchRepository {
    suspend fun addFaces(faces: List<FaceSearchItem>)

    suspend fun getAllEmbeddings(): List<FaceEmbedding>

    suspend fun searchFace(targetEmbedding: FloatArray, threshold: Float = 0.7f): List<FaceEmbedding>
}
