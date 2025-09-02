package com.experiment.facedetector.data.local.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.experiment.facedetector.data.local.entities.MediaWithFaces
import com.experiment.facedetector.data.local.entities.ProcessedState
import com.experiment.facedetector.domain.repo.MediaRepository

/**
 * Simple PagingSource for gallery items ordered by modifiedAtMs DESC.
 * Ensures no duplicate media entries (via DAO query).
 */
class GalleryKeySetPagingSource(
    private val mediaRepo: MediaRepository,
) : PagingSource<Pair<Long, Long>, MediaWithFaces>() {

    override fun getRefreshKey(state: PagingState<Pair<Long, Long>, MediaWithFaces>): Pair<Long, Long>? {
        return null
    }

    override suspend fun load(params: LoadParams<Pair<Long, Long>>): LoadResult<Pair<Long, Long>, MediaWithFaces> {
        return try {
            when (params) {
                is LoadParams.Refresh -> {
                    val newestCursor = Long.MAX_VALUE to Long.MAX_VALUE
                    val key = params.key ?: newestCursor
                    val items = mediaRepo.loadMediaBefore(
                        key.first, key.second, params.loadSize, ProcessedState.PROCESSED
                    )
                    LoadResult.Page(
                        data = items,
                        prevKey = null,
                        nextKey = items.lastOrNull()
                            ?.let { it.media.modifiedAtMs to it.media.mediaId }
                    )
                }
                is LoadParams.Append -> {
                    val key = params.key
                    val items = mediaRepo.loadMediaBefore(
                        key.first, key.second, params.loadSize, ProcessedState.PROCESSED
                    )
                    LoadResult.Page(
                        data = items,
                        prevKey = null,
                        nextKey = items.lastOrNull()
                            ?.let { it.media.modifiedAtMs to it.media.mediaId }
                    )
                }
                is LoadParams.Prepend -> {
                    LoadResult.Page(
                        data = emptyList(),
                        prevKey = null,
                        nextKey = params.key
                    )
                }
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
