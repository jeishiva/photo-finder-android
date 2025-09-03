package com.experiment.facedetector.data.processing

import android.graphics.Bitmap
import com.experiment.facedetector.domain.processing.ThumbnailGenerator
import androidx.core.net.toUri
import com.experiment.facedetector.core.image.BitmapHelper
import com.experiment.facedetector.core.image.BitmapPool
import com.experiment.facedetector.common.logging.LogManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * File-based thumbnail pipeline:
 * - Decodes a bounded, upright bitmap using BitmapHelper
 * - Scales to a square thumbnail (THUMBNAIL_SIZE from your config, or param)
 * - Saves using BitmapHelper.saveBitmap(...)
 */
class ThumbnailGeneratorImpl(
    private val bitmapHelper: BitmapHelper,
    private val thumbnailSize: Int,
    private val compressFormat: Bitmap.CompressFormat,
    private val quality: Int,
) : ThumbnailGenerator {

    override suspend fun extractFromFile(filePath: String, mediaId: Long): Result<String> {
        return withContext(Dispatchers.Default) {
            var decoded: Bitmap? = null
            var thumb: Bitmap? = null
            try {
                decoded = bitmapHelper.decodeBitmap(
                    filePath.toUri(), thumbnailSize, thumbnailSize
                )
                thumb = bitmapHelper.scaleFromPool(
                    decoded, thumbnailSize, thumbnailSize
                )
                val file = bitmapHelper.saveBitmap(
                    thumb, filename = mediaId.toString(), format = compressFormat, quality = quality
                )
                if (file == null) {
                    Result.failure(IllegalStateException("Failed to save thumbnail"))
                } else {
                    Result.success(file.absolutePath)
                }
            } catch (e: Exception) {
                LogManager.e("ThumbnailPipeline", "Failed to generate thumbnail for: $filePath", e)
                Result.failure(e)
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
}
