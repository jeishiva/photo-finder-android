package com.experiment.facedetector.data.local.repo

import com.experiment.facedetector.common.toFileName
import com.experiment.facedetector.config.ThumbnailConfig
import com.experiment.facedetector.data.local.dao.SearchFaceDao
import com.experiment.facedetector.data.local.entities.SearchFaceEntity
import com.experiment.facedetector.domain.entities.FaceDetectedItem
import com.experiment.facedetector.domain.entities.SearchFaceItem
import com.experiment.facedetector.domain.repo.SearchQueryRepo
import com.experiment.facedetector.image.BitmapHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.String

class SearchQueryRepoImpl(
    private val searchFaceDao: SearchFaceDao,
    private val imageHelper: BitmapHelper,
) : SearchQueryRepo {

    override suspend fun save(faces: List<FaceDetectedItem>): String {
        val sessionId = UUID.randomUUID().toString()
        val mappedFaces = faces.mapNotNull { faceItem ->
            val file = withContext(Dispatchers.Default) {
                return@withContext imageHelper.saveBitmap(
                    faceItem.faceBitmap,
                    faceItem.toString().toFileName(),
                    ThumbnailConfig.THUMBNAIL_FORMAT,
                    ThumbnailConfig.THUMBNAIL_QUALITY
                )
            }
            file?.let {
                SearchFaceItem(
                    sessionId = sessionId,
                    faceId = faceItem.faceId,
                    faceBoundingBox = faceItem.faceBoundingBox,
                    thumbnailPath = it.absolutePath
                )
            }
        }
        searchFaceDao.insertAll(mappedFaces.map { SearchFaceEntity.fromDomain(it) })
        return sessionId
    }

    override suspend fun clearAll() {
        searchFaceDao.clearAll()
    }

    override suspend fun getAll(sessionId: String): List<SearchFaceItem> {
        return searchFaceDao.getAllBySession(sessionId).map { it.toDomainModel() }
    }
}
