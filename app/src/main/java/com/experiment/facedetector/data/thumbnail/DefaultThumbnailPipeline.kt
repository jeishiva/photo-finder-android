package com.experiment.facedetector.data.thumbnail
import com.experiment.facedetector.domain.processing.ThumbnailGenerator

import androidx.core.net.toUri
import com.experiment.facedetector.image.BitmapHelper
import com.experiment.facedetector.image.BitmapPool
import com.experiment.facedetector.common.LogManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * File-based thumbnail pipeline:
 * - Decodes a bounded, upright bitmap using BitmapHelper
 * - Scales to a square thumbnail (THUMBNAIL_SIZE from your config, or param)
 * - Saves using BitmapHelper.saveBitmap(...)
 */
class DefaultThumbnailPipeline(
    private val bitmapHelper: BitmapHelper,
    private val thumbnailSize: Int,
    private val compressFormat: android.graphics.Bitmap.CompressFormat,
    private val quality: Int
) : ThumbnailGenerator {
    override suspend fun generateFromFile(filePath: String, mediaId: Long): String? = withContext(Dispatchers.IO) {
        var decoded = null as android.graphics.Bitmap?
        var thumb = null as android.graphics.Bitmap?
        try {
            decoded = bitmapHelper.decodeBitmap(
                filePath.toUri(),
                thumbnailSize,
                thumbnailSize
            )

            thumb = bitmapHelper.scaleFromPool(
                decoded,
                thumbnailSize,
                thumbnailSize
            )

            val file = bitmapHelper.saveBitmap(
                thumb,
                filename = mediaId.toString(),
                format = compressFormat,
                quality = quality
            )
            if (file == null) {
                return@withContext null
            } else {
                return@withContext file.absolutePath
            }
        } catch (e: Exception) {
            LogManager.e("ThumbnailPipeline", "Failed to generate thumbnail for: $filePath", e)
            return@withContext null
        } finally {
            if (thumb != null) {
                BitmapPool.put(thumb)
                thumb = null
            }
            if (decoded != null) {
                BitmapPool.put(decoded)
                decoded = null
            }
        }
    }
}
