// data/local/source/CameraMediaStoreSource.kt
package com.experiment.facedetector.data.local.source

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.os.Bundle
import android.provider.MediaStore
import com.experiment.facedetector.common.logging.LogManager
import com.experiment.facedetector.core.policy.MediaTimePolicy
import com.experiment.facedetector.core.policy.OrientationPolicy
import com.experiment.facedetector.data.common.CursorReader
import com.experiment.facedetector.domain.entities.ImageInfo
import com.experiment.facedetector.domain.entities.MediaKind
import com.experiment.facedetector.domain.entities.MediaSourceCursor
import com.experiment.facedetector.domain.entities.MediaSourcePage
import com.experiment.facedetector.domain.entities.MediaSourceType
import com.experiment.facedetector.domain.entities.SourceMediaItem
import com.experiment.facedetector.domain.source.IdentifiablePagedMediaSource

abstract class MediaStoreSource : IdentifiablePagedMediaSource

class CameraMediaStoreSource(
    private val context: Context,
) : MediaStoreSource() {

    override val sourceType: MediaSourceType = MediaSourceType.MediaStoreCamera

    /**
     * Cursor-based delta listing.
     * Order: GENERATION_MODIFIED ASC, _ID ASC
     * Lower bound: exclusive on (generation, _id) when cursor provided.
     */
    override suspend fun listAfter(
        mediaSourceCursor: MediaSourceCursor?,
        limit: Int,
    ): MediaSourcePage<SourceMediaItem> {
        LogManager.d(TAG, "listAfter: cursor=$mediaSourceCursor limit=$limit")

        val results = mutableListOf<SourceMediaItem>()
        val projection = getMediaStoreProjection()

        val sortColumns = arrayOf(
            MediaStore.MediaColumns.GENERATION_MODIFIED,
            MediaStore.Images.Media._ID
        )

        val (selection, selectionArgs) = buildSelection(mediaSourceCursor)

        val args = Bundle().apply {
            putStringArray(ContentResolver.QUERY_ARG_SORT_COLUMNS, sortColumns)
            putInt(
                ContentResolver.QUERY_ARG_SORT_DIRECTION,
                ContentResolver.QUERY_SORT_DIRECTION_ASCENDING
            )
            putInt(ContentResolver.QUERY_ARG_LIMIT, limit)
            putString(ContentResolver.QUERY_ARG_SQL_SELECTION, selection)
            putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, selectionArgs)
        }

        val baseUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val resolver = context.contentResolver
        val cursor = resolver.query(baseUri, projection, args, null)

        var lastGen: Long? = null
        var lastId: Long? = null

        cursor?.use { cursorObj ->
            while (cursorObj.moveToNext()) {
                val r = CursorReader(cursorObj)

                val id = r.getLongOrNull(MediaStore.Images.Media._ID) ?: continue
                val contentUri = ContentUris.withAppendedId(baseUri, id).toString()
                val mimeType = r.getStringOrNull(MediaStore.MediaColumns.MIME_TYPE) ?: "image/*"
                val width = r.getIntOrNull(MediaStore.MediaColumns.WIDTH) ?: 0
                val height = r.getIntOrNull(MediaStore.MediaColumns.HEIGHT) ?: 0
                val sizeBytes = r.getLongOrNull(MediaStore.MediaColumns.SIZE) ?: 0L

                val dateTakenMs = r.getMillisOrNull(MediaStore.Images.Media.DATE_TAKEN)
                val dateAddedMs = r.getEpochSecAsMillisOrNull(MediaStore.MediaColumns.DATE_ADDED)
                val dateModifiedMs =
                    r.getEpochSecAsMillisOrNull(MediaStore.MediaColumns.DATE_MODIFIED)
                val generationModified =
                    r.getLongOrNull(MediaStore.MediaColumns.GENERATION_MODIFIED)

                val createdAtMs = MediaTimePolicy.resolveCreatedAtMs(
                    MediaTimePolicy.Inputs(
                        exifDateTimeOriginalMs = null, // skipped for perf
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

                val bucketId = r.getLongOrNull(MediaStore.Images.Media.BUCKET_ID)
                val bucketName = r.getStringOrNull(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)

                val mediaStoreOrientationDeg =
                    r.getIntOrNull(MediaStore.Images.ImageColumns.ORIENTATION)
                val orientationDeg = OrientationPolicy.resolve(
                    exifOrientationTag = null,
                    mediaStoreOrientationDeg = mediaStoreOrientationDeg
                )
                LogManager.d(TAG, "fetched media item: $id")
                results.add(
                    SourceMediaItem(
                        sourceKey = sourceType.key,
                        sourceStableId = id,
                        contentPath = contentUri,
                        mimeType = mimeType,
                        width = width,
                        height = height,
                        sizeBytes = sizeBytes,
                        createdAtMs = createdAtMs,
                        lastModifiedAtMs = modifiedAtMs,
                        bucketId = bucketId,
                        bucketDisplayName = bucketName,
                        image = ImageInfo(orientationDeg),
                        generationModified = generationModified,
                        kind = MediaKind.IMAGE
                    )
                )
                lastGen = generationModified
                lastId = id
            }
        }

        val hasMore = results.size == limit
        val nextCursor = if (results.isNotEmpty()) {
            MediaSourceCursor(
                generationModified = lastGen,
                lastModifiedSeconds = null, // not used for this source
                lastId = lastId
            )
        } else {
            mediaSourceCursor // unchanged when empty page
        }

        LogManager.d(
            TAG,
            "listAfter done: fetched=${results.size} hasMore=$hasMore nextCursor=$nextCursor"
        )

        return MediaSourcePage(
            items = results,
            nextCursor = nextCursor,
            hasMore = hasMore
        )
    }

    /**
     * Build selection and args for Camera path + cursor lower bound.
     */
    private fun buildSelection(cursor: MediaSourceCursor?): Pair<String, Array<String>> {
        val cameraFilter = "${MediaStore.Images.Media.RELATIVE_PATH} LIKE ?"
        val args = mutableListOf(CAMERA_PATH)
        // No cursor means start from beginning
        if (cursor?.generationModified == null) {
            return cameraFilter to args.toTypedArray()
        }
        // Add pagination filter: (generation > cursor.gen) OR (generation = cursor.gen AND id > cursor.id)
        val paginationFilter = buildPaginationFilter(cursor)
        val selection = "$cameraFilter AND $paginationFilter"
        args.addAll(getPaginationArgs(cursor))
        return selection to args.toTypedArray()
    }

    private fun buildPaginationFilter(cursor: MediaSourceCursor): String {
        return "(" +
                "${MediaStore.MediaColumns.GENERATION_MODIFIED} > ? OR " +
                "(${MediaStore.MediaColumns.GENERATION_MODIFIED} = ? AND ${MediaStore.Images.Media._ID} > ?)" +
                ")"
    }

    private fun getPaginationArgs(cursor: MediaSourceCursor): List<String> {
        val generation = cursor.generationModified.toString()
        val lastId = (cursor.lastId ?: Long.MIN_VALUE).toString()
        return listOf(generation, generation, lastId)
    }

    private fun getMediaStoreProjection(): Array<String> = arrayOf(
        MediaStore.Images.Media._ID,
        MediaStore.MediaColumns.MIME_TYPE,
        MediaStore.MediaColumns.WIDTH,
        MediaStore.MediaColumns.HEIGHT,
        MediaStore.MediaColumns.SIZE,
        MediaStore.MediaColumns.DATE_MODIFIED,        // seconds
        MediaStore.MediaColumns.DATE_ADDED,           // seconds
        MediaStore.Images.Media.DATE_TAKEN,           // millis
        MediaStore.MediaColumns.GENERATION_MODIFIED,  // API 29+
        MediaStore.Images.ImageColumns.ORIENTATION,
        MediaStore.Images.Media.BUCKET_ID,
        MediaStore.Images.Media.BUCKET_DISPLAY_NAME
    )

    companion object {
        const val TAG = "CameraMediaStoreSource"
        const val CAMERA_PATH = "%DCIM/Camera%"
    }
}
