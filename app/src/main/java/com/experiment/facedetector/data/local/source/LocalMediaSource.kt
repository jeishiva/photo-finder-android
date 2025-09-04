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
import com.experiment.facedetector.domain.entities.*
import com.experiment.facedetector.domain.source.IdentifiablePagedMediaSource

abstract class MediaStoreSource : IdentifiablePagedMediaSource

/**
 * Local media source implementation that reads images from device storage
 * using Android's MediaStore API with cursor-based pagination.
 *
 * Ordering: GENERATION_MODIFIED ASC, _ID ASC
 * Pagination: Exclusive lower bound on (generation, _id) when cursor provided
 */
class LocalMediaSource(
    private val context: Context,
) : MediaStoreSource() {

    override val sourceType: MediaSourceType = MediaSourceType.Local

    override suspend fun listAfter(
        mediaSourceCursor: MediaSourceCursor?,
        limit: Int,
    ): MediaSourcePage<SourceMediaItem> {
        LogManager.d(TAG, "listAfter: cursor=$mediaSourceCursor limit=$limit")

        if (sourceType.buckets.isEmpty()) {
            LogManager.e(TAG, "No buckets configured for Local source. Returning empty page.")
            return createEmptyPage(mediaSourceCursor)
        }

        val mediaItems = queryMediaItems(mediaSourceCursor, limit)
        val nextCursor = createNextCursor(mediaSourceCursor, mediaItems)
        val hasMorePages = mediaItems.size == limit

        LogManager.d(
            TAG,
            "listAfter completed: fetched=${mediaItems.size} hasMore=$hasMorePages nextCursor=$nextCursor"
        )

        return MediaSourcePage(
            items = mediaItems,
            nextCursor = nextCursor,
            hasMore = hasMorePages
        )
    }

    private fun createEmptyPage(cursor: MediaSourceCursor?): MediaSourcePage<SourceMediaItem> {
        return MediaSourcePage(
            items = emptyList(),
            nextCursor = cursor,
            hasMore = false
        )
    }

    private fun queryMediaItems(
        cursor: MediaSourceCursor?,
        limit: Int
    ): List<SourceMediaItem> {
        val mediaItems = mutableListOf<SourceMediaItem>()
        val queryArgs = createQueryBundle(cursor, limit)

        context.contentResolver.query(
            EXTERNAL_CONTENT_URI,
            MEDIA_PROJECTION,
            queryArgs,
            null
        )?.use { resultCursor ->
            while (resultCursor.moveToNext()) {
                val mediaItem = parseMediaItem(resultCursor)
                if (mediaItem != null) {
                    mediaItems.add(mediaItem)
                }
            }
        }
        return mediaItems
    }

    private fun createQueryBundle(cursor: MediaSourceCursor?, limit: Int): Bundle {
        val (selection, selectionArgs) = buildQuerySelection(cursor)

        return Bundle().apply {
            putStringArray(ContentResolver.QUERY_ARG_SORT_COLUMNS, SORT_COLUMNS)
            putInt(ContentResolver.QUERY_ARG_SORT_DIRECTION, ContentResolver.QUERY_SORT_DIRECTION_ASCENDING)
            putInt(ContentResolver.QUERY_ARG_LIMIT, limit)
            putString(ContentResolver.QUERY_ARG_SQL_SELECTION, selection)
            putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, selectionArgs)
        }
    }

    private fun parseMediaItem(cursor: android.database.Cursor): SourceMediaItem? {
        val reader = CursorReader(cursor)

        val id = reader.getLongOrNull(MediaStore.Images.Media._ID) ?: return null
        val contentUri = ContentUris.withAppendedId(EXTERNAL_CONTENT_URI, id).toString()

        val basicInfo = extractMediaInfo(reader)
        val timeInfo = extractTimeInfo(reader)
        val bucketInfo = extractBucketInfo(reader)
        val orientationDegrees = extractOrientation(reader)

        LogManager.d(TAG, "Parsed media item: $id")

        return SourceMediaItem(
            sourceKey = sourceType.key,
            sourceStableId = id,
            contentPath = contentUri,
            mediaInfo = basicInfo,
            timeInfo = timeInfo,
            bucketInfo = bucketInfo,
            image = ImageInfo(orientationDegrees),
            kind = MediaKind.IMAGE
        )
    }

    private fun extractMediaInfo(reader: CursorReader): MediaInfo {
        return MediaInfo(
            mimeType = reader.getStringOrNull(MediaStore.MediaColumns.MIME_TYPE) ?: "image/*",
            width = reader.getIntOrNull(MediaStore.MediaColumns.WIDTH) ?: 0,
            height = reader.getIntOrNull(MediaStore.MediaColumns.HEIGHT) ?: 0,
            sizeBytes = reader.getLongOrNull(MediaStore.MediaColumns.SIZE) ?: 0L,
        )
    }

    private fun extractTimeInfo(reader: CursorReader): MediaTimeInfo {
        val dateTakenMs = reader.getMillisOrNull(MediaStore.Images.Media.DATE_TAKEN)
        val dateAddedMs = reader.getEpochSecAsMillisOrNull(MediaStore.MediaColumns.DATE_ADDED)
        val dateModifiedMs = reader.getEpochSecAsMillisOrNull(MediaStore.MediaColumns.DATE_MODIFIED)
        val generationModified = reader.getLongOrNull(MediaStore.MediaColumns.GENERATION_MODIFIED)
        val createdAtMs = MediaTimePolicy.resolveCreatedAtMs(
            MediaTimePolicy.Inputs(
                exifDateTimeOriginalMs = null, // Skipped for performance
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
        return MediaTimeInfo(
            createdAtMs = createdAtMs,
            modifiedAtMs = modifiedAtMs,
            generationModified = generationModified
        )
    }

    private fun extractBucketInfo(reader: CursorReader): BucketInfo {
        return BucketInfo(
            bucketId = reader.getLongOrNull(MediaStore.Images.Media.BUCKET_ID),
            bucketName = reader.getStringOrNull(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
        )
    }

    private fun extractOrientation(reader: CursorReader): Int {
        val mediaStoreOrientationDeg = reader.getIntOrNull(MediaStore.Images.ImageColumns.ORIENTATION)
        return OrientationPolicy.resolve(
            exifOrientationTag = null,
            mediaStoreOrientationDeg = mediaStoreOrientationDeg
        )
    }

    private fun createNextCursor(
        currentCursor: MediaSourceCursor?,
        items: List<SourceMediaItem>,
    ): MediaSourceCursor? {
        if (items.isEmpty()) {
            return currentCursor // Unchanged when empty page
        }

        val lastItem = items.last()
        return MediaSourceCursor(
            generationModified = lastItem.timeInfo.modifiedAtMs,
            lastModifiedSeconds = null, // Not used for this source
            lastId = lastItem.sourceStableId
        )
    }

    // Query Selection Building
    private fun buildQuerySelection(cursor: MediaSourceCursor?): Pair<String, Array<String>> {
        val (bucketFilter, bucketArgs) = buildBucketFilter(sourceType.buckets)

        if (cursor?.generationModified == null) {
            return bucketFilter to bucketArgs.toTypedArray()
        }
        val paginationFilter = buildPaginationFilter()
        val combinedSelection = "$bucketFilter AND $paginationFilter"
        val combinedArgs = bucketArgs + getPaginationArgs(cursor)

        return combinedSelection to combinedArgs.toTypedArray()
    }

    private fun buildBucketFilter(buckets: List<Bucket>): Pair<String, List<String>> {
        val filterClauses = buckets.map {
            "${MediaStore.Images.Media.RELATIVE_PATH} LIKE ?"
        }
        val filterArgs = buckets.map {
            "%${it.relative.trimStart('/')}%"
        }

        val combinedFilter = filterClauses.joinToString(
            separator = " OR ",
            prefix = "(",
            postfix = ")"
        )

        return combinedFilter to filterArgs
    }

    private fun buildPaginationFilter(): String {
        return "(" +
                "${MediaStore.MediaColumns.GENERATION_MODIFIED} > ? OR " +
                "(${MediaStore.MediaColumns.GENERATION_MODIFIED} = ? AND ${MediaStore.Images.Media._ID} > ?)" +
                ")"
    }

    private fun getPaginationArgs(cursor: MediaSourceCursor): List<String> {
        val generation = cursor.generationModified.toString()
        val lastId = cursor.lastId?.toString() ?: Long.MIN_VALUE.toString()
        return listOf(generation, generation, lastId)
    }

    companion object {
        private const val TAG = "LocalMediaStoreSource"

        private val EXTERNAL_CONTENT_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        private val SORT_COLUMNS = arrayOf(
            MediaStore.MediaColumns.GENERATION_MODIFIED,
            MediaStore.Images.Media._ID
        )

        private val MEDIA_PROJECTION = arrayOf(
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
    }
}