package com.experiment.facedetector.data.local.source

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.os.Bundle
import android.provider.MediaStore
import com.experiment.facedetector.common.LogManager
import com.experiment.facedetector.domain.source.MediaSource
import com.experiment.facedetector.domain.source.MediaSourceType
import com.experiment.facedetector.domain.source.SourceMediaItem

class CameraMediaStoreSource(
    private val context: Context
) : MediaSource {

    override val sourceType: MediaSourceType = MediaSourceType.MediaStoreCamera

    override suspend fun list(offset: Int, limit: Int): List<SourceMediaItem> {
        LogManager.d(TAG, "camera list ($offset, $limit)")
        val results = mutableListOf<SourceMediaItem>()

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.RELATIVE_PATH
        )

        val args = Bundle().apply {
            putStringArray(
                ContentResolver.QUERY_ARG_SORT_COLUMNS,
                arrayOf(MediaStore.Images.Media.DATE_TAKEN)
            )
            putInt(
                ContentResolver.QUERY_ARG_SORT_DIRECTION,
                ContentResolver.QUERY_SORT_DIRECTION_DESCENDING
            )
            putInt(ContentResolver.QUERY_ARG_LIMIT, limit)
            putInt(ContentResolver.QUERY_ARG_OFFSET, offset)
            putString(
                ContentResolver.QUERY_ARG_SQL_SELECTION,
                "${MediaStore.Images.Media.RELATIVE_PATH} LIKE ?"
            )
            putStringArray(
                ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS,
                arrayOf("%DCIM/Camera%")
            )
        }

        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val resolver = context.contentResolver
        val cursor = resolver.query(uri, projection, args, null)

        cursor?.use { c ->
            val idCol = c.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            while (c.moveToNext()) {
                val id = c.getLong(idCol)
                val contentUri = ContentUris.withAppendedId(uri, id)
                results.add(
                    SourceMediaItem(
                        stableId = id,
                        contentUri = contentUri
                    )
                )
            }
        }

        return results
    }

    companion object {
        const val TAG = "CameraMediaStoreSource"
    }
}
