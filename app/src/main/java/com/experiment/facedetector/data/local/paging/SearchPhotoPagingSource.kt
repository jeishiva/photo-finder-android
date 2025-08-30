package com.experiment.facedetector.data.local.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.experiment.facedetector.data.local.entities.MediaWithFaces
import com.experiment.facedetector.data.local.entities.ProcessedState
import com.experiment.facedetector.domain.repo.MediaWithFacesRepository


class SearchPhotoPagingSource(
    private val mediaWithFacesRepository: MediaWithFacesRepository,
) : PagingSource<Pair<Long, Long>, MediaWithFaces>() {

    override fun getRefreshKey(state: PagingState<Pair<Long, Long>, MediaWithFaces>): Pair<Long, Long>? {
        val anchorPos = state.anchorPosition ?: return null
        val anchorItem = state.closestItemToPosition(anchorPos) ?: return null
        return anchorItem.media.modifiedAtMs to anchorItem.media.mediaId
    }

    override suspend fun load(params: LoadParams<Pair<Long, Long>>): LoadResult<Pair<Long, Long>, MediaWithFaces> {
        return try {
            when (params) {
                is LoadParams.Refresh -> {
                    val newestCursor = Long.MAX_VALUE to Long.MAX_VALUE
                    val key = params.key ?: newestCursor
                    val items = mediaWithFacesRepository.loadMediaBefore(
                        key.first,
                        key.second,
                        limit = params.loadSize,
                        ProcessedState.PROCESSED
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
                    val items = mediaWithFacesRepository.loadMediaBefore(
                        key.first,
                        key.second,
                        limit = params.loadSize,
                        ProcessedState.PROCESSED
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
