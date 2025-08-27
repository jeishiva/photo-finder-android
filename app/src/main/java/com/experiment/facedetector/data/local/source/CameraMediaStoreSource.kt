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
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.DATE_MODIFIED
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
            val sizeCol = c.getColumnIndex(MediaStore.MediaColumns.SIZE)
            val modCol  = c.getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED)
            while (c.moveToNext()) {
                val id = c.getLong(idCol)
                val contentUri = ContentUris.withAppendedId(uri, id)
                val sizeBytes: Long = if (sizeCol >= 0) {
                    c.getLong(sizeCol)
                } else {
                    0
                }
                val modifiedSeconds: Long = if (modCol >= 0) {
                    c.getLong(modCol)
                } else {
                    0
                }
                results.add(
                    SourceMediaItem(
                        stableId = id,
                        contentUri = contentUri,
                        fileSize = sizeBytes,
                        lastModifiedTime = modifiedSeconds
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
