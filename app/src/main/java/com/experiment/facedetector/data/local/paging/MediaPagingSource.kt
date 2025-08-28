package com.experiment.facedetector.data.local.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.experiment.facedetector.data.local.entities.MediaWithFaces


/**
 * Bidirectional keyset PagingSource supporting both forward and backward pagination.
 * Uses compound cursor (modifiedAtMs, id) for stable ordering.
 */
/**
 * Bidirectional keyset PagingSource supporting both forward and backward pagination.
 * Uses compound cursor (modifiedAtMs, id) for stable ordering.
 */
class BidirectionalKeysetPagingSource(
    private val initialCursorDate: Long = Long.MAX_VALUE,
    private val initialCursorId: Long = Long.MAX_VALUE,
    private val forwardLoader: suspend (cursorDate: Long, cursorId: Long, limit: Int) -> List<MediaWithFaces>,
    private val backwardLoader: suspend (cursorDate: Long, cursorId: Long, limit: Int) -> List<MediaWithFaces>
) : PagingSource<Pair<Long, Long>, MediaWithFaces>() {

    override fun getRefreshKey(
        state: PagingState<Pair<Long, Long>, MediaWithFaces>
    ): Pair<Long, Long>? {
        val anchor = state.anchorPosition ?: return null
        val closest = state.closestItemToPosition(anchor) ?: return null
        return Pair(
            closest.media.modifiedAtMs,
            closest.media.id
        )
    }

    override suspend fun load(
        params: LoadParams<Pair<Long, Long>>
    ): LoadResult<Pair<Long, Long>, MediaWithFaces> {
        return try {
            when (params) {
                is LoadParams.Refresh -> loadRefresh(params)
                is LoadParams.Append -> loadAppend(params)
                is LoadParams.Prepend -> loadPrepend(params)
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    private suspend fun loadRefresh(
        params: LoadParams.Refresh<Pair<Long, Long>>
    ): LoadResult<Pair<Long, Long>, MediaWithFaces> {
        val key = params.key ?: Pair(initialCursorDate, initialCursorId)
        val items = forwardLoader(key.first, key.second, params.loadSize)

        return LoadResult.Page(
            data = items,
            // Use first item as prevKey for backward pagination
            prevKey = if (items.isNotEmpty()) {
                val first = items.first()
                Pair(first.media.modifiedAtMs, first.media.id)
            } else null,
            // Use last item as nextKey for forward pagination
            nextKey = if (items.isNotEmpty()) {
                val last = items.last()
                Pair(last.media.modifiedAtMs, last.media.id)
            } else null
        )
    }

    private suspend fun loadAppend(
        params: LoadParams.Append<Pair<Long, Long>>
    ): LoadResult<Pair<Long, Long>, MediaWithFaces> {
        val key = params.key
        val items = forwardLoader(key.first, key.second, params.loadSize)

        return LoadResult.Page(
            data = items,
            prevKey = null, // Don't provide prevKey for append
            nextKey = if (items.isNotEmpty()) {
                val last = items.last()
                Pair(last.media.modifiedAtMs, last.media.id)
            } else null
        )
    }

    private suspend fun loadPrepend(
        params: LoadParams.Prepend<Pair<Long, Long>>
    ): LoadResult<Pair<Long, Long>, MediaWithFaces> {
        val key = params.key
        val items = backwardLoader(key.first, key.second, params.loadSize)

        return LoadResult.Page(
            data = items,
            prevKey = if (items.isNotEmpty()) {
                val first = items.first()
                Pair(first.media.modifiedAtMs, first.media.id)
            } else null,
            nextKey = null // Don't provide nextKey for prepend
        )
    }
}