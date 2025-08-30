package com.experiment.facedetector.data.local.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.experiment.facedetector.data.local.entities.MediaWithFaces

/**
 * PagingSource that supports bidirectional keyset pagination using compound cursor (modifiedAtMs, id).
 *
 * Assumptions:
 * - Results are ordered DESCENDING by (modifiedAtMs, id).
 * - forwardLoader → loads items "after" the cursor (newer).
 * - backwardLoader → loads items "before" the cursor (older).
 *
 * @param startFromNewest if true → initial load starts at newest (timeline view).
 *                        if false → initial load starts at oldest (archive view).
 */
class BidirectionalKeySetPagingSource(
    private val startFromNewest: Boolean = false,
    private val forwardLoader: suspend (cursorDate: Long, cursorId: Long, limit: Int) -> List<MediaWithFaces>,
    private val backwardLoader: suspend (cursorDate: Long, cursorId: Long, limit: Int) -> List<MediaWithFaces>,
) : PagingSource<Pair<Long, Long>, MediaWithFaces>() {

    override fun getRefreshKey(
        state: PagingState<Pair<Long, Long>, MediaWithFaces>,
    ): Pair<Long, Long>? {
        val anchorPos = state.anchorPosition ?: return null
        val anchorItem = state.closestItemToPosition(anchorPos) ?: return null
        return Pair(anchorItem.media.modifiedAtMs, anchorItem.media.mediaId)
    }

    override suspend fun load(
        params: LoadParams<Pair<Long, Long>>,
    ): LoadResult<Pair<Long, Long>, MediaWithFaces> {
        return try {
            when (params) {
                is LoadParams.Refresh -> handleRefresh(params)
                is LoadParams.Append -> handleAppend(params)
                is LoadParams.Prepend -> handlePrepend(params)
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    private suspend fun handleRefresh(
        params: LoadParams.Refresh<Pair<Long, Long>>,
    ): LoadResult.Page<Pair<Long, Long>, MediaWithFaces> {
        val defaultCursor = if (startFromNewest) {
            Pair(Long.MAX_VALUE, Long.MAX_VALUE) // start newest
        } else {
            Pair(Long.MIN_VALUE, Long.MIN_VALUE) // start oldest
        }
        val key = params.key ?: defaultCursor
        val items = if (startFromNewest) {
            forwardLoader(key.first, key.second, params.loadSize) // load newest
        } else {
            backwardLoader(key.first, key.second, params.loadSize) // load oldest
        }
        return LoadResult.Page(
            data = items,
            prevKey = items.firstOrNull()?.let {
                Pair(it.media.modifiedAtMs, it.media.mediaId) // prepend (newer)
            },
            nextKey = items.lastOrNull()?.let {
                Pair(it.media.modifiedAtMs, it.media.mediaId) // append (older)
            }
        )
    }

    private suspend fun handleAppend(
        params: LoadParams.Append<Pair<Long, Long>>,
    ): LoadResult.Page<Pair<Long, Long>, MediaWithFaces> {
        val key = params.key
        val items = backwardLoader(key.first, key.second, params.loadSize)
        return LoadResult.Page(
            data = items,
            prevKey = null,
            nextKey = items.lastOrNull()?.let {
                Pair(it.media.modifiedAtMs, it.media.mediaId)
            }
        )
    }

    private suspend fun handlePrepend(
        params: LoadParams.Prepend<Pair<Long, Long>>,
    ): LoadResult.Page<Pair<Long, Long>, MediaWithFaces> {
        val key = params.key
        val items = forwardLoader(key.first, key.second, params.loadSize)
        return LoadResult.Page(
            data = items,
            prevKey = items.firstOrNull()?.let {
                Pair(it.media.modifiedAtMs, it.media.mediaId)
            },
            nextKey = null
        )
    }
}
