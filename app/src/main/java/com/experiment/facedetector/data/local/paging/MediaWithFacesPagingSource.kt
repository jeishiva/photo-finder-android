package com.experiment.facedetector.data.local.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.experiment.facedetector.common.LogManager
import com.experiment.facedetector.data.local.dao.MediaDao
import com.experiment.facedetector.data.local.entities.MediaWithFaces
import kotlin.collections.map

class MediaWithFacesPagingSource(
    private val mediaDao: MediaDao,
) : PagingSource<Int, MediaWithFaces>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MediaWithFaces> {
        return try {
            val offset = params.key ?: 0
            val pageSize = params.loadSize
            val mediaList = mediaDao.getPagedMediaWithOffset(limit = pageSize, offset = offset)
            val mediaIds = mediaList.map { it.mediaId }
            val faces = mediaDao.getFacesForMediaIds(mediaIds)
            val mediaWithFaces = mediaList.map { media ->
                MediaWithFaces(
                    media = media,
                    faces = faces.filter { it.mediaOwnerId == media.mediaId }
                )
            }
            LogManager.d("testfx", "mediaWithFaces: $mediaWithFaces")
            LoadResult.Page(
                data = mediaWithFaces,
                prevKey = if (offset == 0) {
                    null
                } else {
                    offset - pageSize
                },
                nextKey = if (mediaWithFaces.isEmpty()) {
                    null
                } else {
                    offset + pageSize
                }
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, MediaWithFaces>): Int? {
        return state.anchorPosition
    }
}
