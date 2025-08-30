package com.experiment.facedetector.data.local.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.experiment.facedetector.data.local.entities.MediaWithFaces

/**
 * PagingSource that supports bidirectional keyset pagination using compound cursor (modifiedAtMs, id).
 *
 * Assumptions:
 * - Both loaders return results ordered DESCENDING by (modifiedAtMs, id).
 * - forwardLoader → loads items "after" the cursor (newer).
 * - backwardLoader → loads items "before" the cursor (older).
 */
class BidirectionalKeysetPagingSource(
    private val initialCursorDate: Long = Long.MAX_VALUE,
    private val initialCursorId: Long = Long.MAX_VALUE,
    private val forwardLoader: suspend (cursorDate: Long, cursorId: Long, limit: Int) -> List<MediaWithFaces>,
    private val backwardLoader: suspend (cursorDate: Long, cursorId: Long, limit: Int) -> List<MediaWithFaces>,
) : PagingSource<Pair<Long, Long>, MediaWithFaces>() {

    override fun getRefreshKey(
        state: PagingState<Pair<Long, Long>, MediaWithFaces>,
    ): Pair<Long, Long>? {
        val anchorPos = state.anchorPosition ?: return null
        val closestItem = state.closestItemToPosition(anchorPos) ?: return null
        val date = closestItem.media.modifiedAtMs
        val id = closestItem.media.id
        return Pair(date, id)
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
        val key = params.key ?: Pair(initialCursorDate, initialCursorId)
        val items = forwardLoader(key.first, key.second, params.loadSize)

        return LoadResult.Page(
            data = items,
            prevKey = items.lastOrNull()?.let { Pair(it.media.modifiedAtMs, it.media.id) }, // older
            nextKey = items.firstOrNull()
                ?.let { Pair(it.media.modifiedAtMs, it.media.id) }  // newer
        )
    }

    private suspend fun handleAppend(
        params: LoadParams.Append<Pair<Long, Long>>,
    ): LoadResult.Page<Pair<Long, Long>, MediaWithFaces> {
        val key = params.key
        val items = forwardLoader(key.first, key.second, params.loadSize)

        return LoadResult.Page(
            data = items,
            prevKey = null,
            nextKey = items.firstOrNull()?.let { Pair(it.media.modifiedAtMs, it.media.id) } // newer
        )
    }

    private suspend fun handlePrepend(
        params: LoadParams.Prepend<Pair<Long, Long>>,
    ): LoadResult.Page<Pair<Long, Long>, MediaWithFaces> {
        val key = params.key
        val items = backwardLoader(key.first, key.second, params.loadSize)

        return LoadResult.Page(
            data = items,
            prevKey = items.lastOrNull()?.let { Pair(it.media.modifiedAtMs, it.media.id) }, // older
            nextKey = null
        )
    }
}
