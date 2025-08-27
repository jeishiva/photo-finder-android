package com.experiment.facedetector.data.local.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.experiment.facedetector.data.local.entities.MediaWithFaces

/**
 * Forward-only keyset PagingSource.
 * Provide a suspending loader that accepts (cursorDate, cursorId, limit).
 */
class ForwardKeysetPagingSource(
    private val initialCursorDate: Long = Long.MAX_VALUE,
    private val initialCursorId: Long = Long.MAX_VALUE,
    private val loader: suspend (cursorDate: Long, cursorId: Long, limit: Int) -> List<MediaWithFaces>
) : PagingSource<Pair<Long, Long>, MediaWithFaces>() {

    override fun getRefreshKey(
        state: PagingState<Pair<Long, Long>, MediaWithFaces>
    ): Pair<Long, Long>? {
        val anchor = state.anchorPosition ?: return null
        val closest = state.closestItemToPosition(anchor) ?: return null
        return Pair(closest.media.dateModified ?: 0, closest.media.mediaId)
    }

    override suspend fun load(
        params: LoadParams<Pair<Long, Long>>
    ): LoadResult<Pair<Long, Long>, MediaWithFaces> {
        return try {
            val key: Pair<Long, Long> = if (params.key != null) {
                params.key!!
            } else {
                Pair(initialCursorDate, initialCursorId)
            }
            val items: List<MediaWithFaces> = loader(key.first, key.second, params.loadSize)
            val nextKey: Pair<Long, Long>? = if (items.isNotEmpty()) {
                val last: MediaWithFaces = items.last()
                val lastDate: Long = last.media.dateModified ?: (System.currentTimeMillis() / 1000)
                val lastId: Long = last.media.mediaId
                Pair(lastDate, lastId)
            } else {
                null
            }
            LoadResult.Page(
                data = items, prevKey = null, nextKey = nextKey
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}

