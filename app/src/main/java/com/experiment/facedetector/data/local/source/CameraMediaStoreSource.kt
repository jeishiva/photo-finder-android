// data/local/source/CameraMediaStoreSource.kt
package com.experiment.facedetector.data.local.source

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.os.Bundle
import android.provider.MediaStore
import com.experiment.facedetector.common.LogManager
import com.experiment.facedetector.core.policy.MediaTimePolicy
import com.experiment.facedetector.core.policy.OrientationPolicy
import com.experiment.facedetector.data.common.CursorReader
import com.experiment.facedetector.domain.entities.ImageSourceItem
import com.experiment.facedetector.domain.source.MediaSource
import com.experiment.facedetector.domain.entities.MediaSourceType
import com.experiment.facedetector.domain.entities.SourceMediaItem
import com.experiment.facedetector.domain.factory.MediaItemFactory

class CameraMediaStoreSource(
    private val context: Context
) : MediaSource {

    override val sourceType: MediaSourceType = MediaSourceType.MediaStoreCamera

    override suspend fun list(offset: Int, limit: Int): List<SourceMediaItem> {
        LogManager.d(TAG, "camera list ($offset, $limit)")
        val results = mutableListOf<SourceMediaItem>()
        val projection = getMediaStoreProjection()
        val args = Bundle().apply {
            putStringArray(
                ContentResolver.QUERY_ARG_SORT_COLUMNS,
                arrayOf(MediaStore.Images.Media.DATE_TAKEN, MediaStore.Images.Media._ID)
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
                arrayOf(CAMERA_PATH)
            )
        }

        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val resolver = context.contentResolver
        val cursor = resolver.query(uri, projection, args, null)

        cursor?.use { cursor ->
            while (cursor.moveToNext()) {
                val reader = CursorReader(cursor)

                val id = reader.getLongOrNull(MediaStore.Images.Media._ID)!!
                val contentUri = ContentUris.withAppendedId(uri, id).toString()
                val mimeType = reader.getStringOrNull(MediaStore.MediaColumns.MIME_TYPE) ?: "image/*"
                val width = reader.getIntOrNull(MediaStore.MediaColumns.WIDTH) ?: 0
                val height = reader.getIntOrNull(MediaStore.MediaColumns.HEIGHT) ?: 0
                val sizeBytes = reader.getLongOrNull(MediaStore.MediaColumns.SIZE) ?: 0L
                val dateTakenMs = reader.getMillisOrNull(MediaStore.Images.Media.DATE_TAKEN)
                val dateAddedMs = reader.getEpochSecAsMillisOrNull(MediaStore.MediaColumns.DATE_ADDED)
                val dateModifiedMs = reader.getEpochSecAsMillisOrNull(MediaStore.MediaColumns.DATE_MODIFIED)
                val generationModified: Long? =
                    reader.getLongOrNull(MediaStore.MediaColumns.GENERATION_MODIFIED)
                val createdAtMs = MediaTimePolicy.resolveCreatedAtMs(
                    MediaTimePolicy.Inputs(
                        // expensive to open stream to read EXIF, so skipping for now
                        exifDateTimeOriginalMs = null,
                        dateTakenMs = dateTakenMs,
                        dateAddedMs = dateAddedMs,
                        dateModifiedMs = dateModifiedMs,
                        generationModified = generationModified
                    )
                )
                val modifiedAtMs = MediaTimePolicy.resolveModifiedAtMs(
                    dateModifiedMs = dateModifiedMs,
                    generationModified = generationModified
                )
                val bucketId: Long? = reader.getLongOrNull(MediaStore.Images.Media.BUCKET_ID)
                val bucketName: String? = reader.getStringOrNull(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
                val mediaStoreOrientationDeg: Int? = reader.getIntOrNull(MediaStore.Images.ImageColumns.ORIENTATION)
                val orientationDeg: Int = OrientationPolicy.resolve(
                    exifOrientationTag = null,
                    mediaStoreOrientationDeg = mediaStoreOrientationDeg
                )
                results.add(
                    MediaItemFactory.createImage(
                        stableId = id,
                        contentUri = contentUri,
                        mimeType = mimeType,
                        width = width,
                        height = height,
                        sizeBytes = sizeBytes,
                        createdAtMs = createdAtMs,
                        lastModifiedAtMs = modifiedAtMs,
                        bucketId = bucketId,
                        bucketDisplayName = bucketName,
                        orientationDeg = orientationDeg,
                        generationModified = generationModified
                    )
                )
            }
        }
        return results
    }

    private fun getMediaStoreProjection(): Array<String> = arrayOf(
        MediaStore.Images.Media._ID,
        MediaStore.MediaColumns.MIME_TYPE,
        MediaStore.MediaColumns.WIDTH,
        MediaStore.MediaColumns.HEIGHT,
        MediaStore.MediaColumns.SIZE,
        MediaStore.MediaColumns.DATE_MODIFIED,   // seconds
        MediaStore.MediaColumns.DATE_ADDED,      // seconds
        MediaStore.Images.Media.DATE_TAKEN,      // millis
        MediaStore.Images.ImageColumns.ORIENTATION,
        MediaStore.Images.Media.BUCKET_ID,
        MediaStore.Images.Media.BUCKET_DISPLAY_NAME
    )

    companion object {
        const val TAG = "CameraMediaStoreSource"
        const val CAMERA_PATH = "%DCIM/Camera%"
    }
}
