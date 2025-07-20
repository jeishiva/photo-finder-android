package com.experiment.facedetector.domain.repo

import com.experiment.facedetector.domain.entities.FaceDetectedItem
import com.experiment.facedetector.domain.entities.SearchFaceItem

interface SearchQueryRepo {
    suspend fun save(selectedFaceList: List<FaceDetectedItem>) : String
    suspend fun clearAll()
    suspend fun getAll(sessionId: String): List<SearchFaceItem>
}
