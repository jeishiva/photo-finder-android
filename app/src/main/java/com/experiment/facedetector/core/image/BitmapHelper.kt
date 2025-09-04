package com.experiment.facedetector.core.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import com.google.mlkit.vision.face.Face
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import androidx.core.graphics.scale
import com.experiment.facedetector.common.logging.LogManager
import com.experiment.facedetector.config.ThumbnailConfig.THUMBNAIL_SIZE
import androidx.core.net.toUri
import com.experiment.facedetector.domain.entities.FaceBoundingBox


class BitmapHelper(val context: Context) {
    fun saveBitmap(
        bitmap: Bitmap,
        filename: String,
        format: Bitmap.CompressFormat,
        quality: Int
    ): File? {
        val file = getThumbnailFile(context, filename, format)
        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(format, quality, out)
                out.flush()
            }
            val sizeInKB = file.length() / 1024
            LogManager.d(message = "Saved thumbnail file size: $sizeInKB KB")
            return file
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    fun getThumbnailFile(
        context: Context,
        baseName: String,
        compressFormat: Bitmap.CompressFormat
    ): File {
        require(baseName.isNotBlank()) { "Base filename must not be blank" }
        val extension = compressFormat.toFileExtension()
        val fileName = "$baseName$extension"
        return File(context.cacheDir, fileName).apply {
            parentFile?.mkdirs() ?: throw IllegalStateException("Failed to access cache directory")
        }
    }

    private fun Bitmap.CompressFormat.toFileExtension(): String = when (this) {
        Bitmap.CompressFormat.JPEG -> ".jpg"
        Bitmap.CompressFormat.PNG -> ".png"
        Bitmap.CompressFormat.WEBP_LOSSY,
        Bitmap.CompressFormat.WEBP_LOSSLESS,
        Bitmap.CompressFormat.WEBP -> ".webp"
    }

    fun drawFaceBoundingBoxes(
        originalBitmap: Bitmap,
        faces: List<Face>
    ): Bitmap {
        if (faces.isEmpty()) {
            return originalBitmap
        }
        val mutableBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)
        val paint = Paint().apply {
            color = Color.GREEN
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }
        for (face in faces) {
            val bounds = face.boundingBox
            canvas.drawRect(bounds, paint)
        }
        return mutableBitmap
    }


    fun decodeBitmap(
        uriString: String,
        targetHeight: Int,
        targetWidth: Int
    ): Bitmap {
        val uri = uriString.toUri()
        return decodeBitmap(uri, targetHeight, targetWidth)
    }

    fun decodeBitmap(
        contentUri: Uri,
        targetHeight: Int,
        targetWidth: Int
    ): Bitmap {
        LogManager.d(TAG, "Decoding bitmap from $contentUri")

        return context.contentResolver.openInputStream(contentUri)?.use { inputStream ->
            BufferedInputStream(inputStream, 8192).use { bufferedStream ->
                var pooledBitmap: Bitmap? = null

                try {
                    // Mark the stream for reset capability
                    bufferedStream.mark(Int.MAX_VALUE)

                    // Get rotation
                    val rotationDegrees = getImageRotation(bufferedStream)
                    LogManager.v(TAG, "Image rotation: $rotationDegrees degrees")

                    // Reset stream and get dimensions
                    bufferedStream.reset()
                    val (originalWidth, originalHeight) = getImageDimensions(bufferedStream)
                    LogManager.v(TAG, "Original dimensions: ${originalWidth}x${originalHeight}")

                    // Adjust dimensions for rotation
                    val (adjustedWidth, adjustedHeight) = if (rotationDegrees == 90 || rotationDegrees == 270) {
                        originalHeight to originalWidth
                    } else {
                        originalWidth to originalHeight
                    }
                    LogManager.v(TAG, "Adjusted dimensions: ${adjustedWidth}x${adjustedHeight}")

                    // Calculate sample size
                    val sampleSize = calculateInSampleSize(adjustedWidth, adjustedHeight, targetWidth, targetHeight)
                    val finalWidth = adjustedWidth / sampleSize
                    val finalHeight = adjustedHeight / sampleSize
                    LogManager.v(TAG, "Sample size: $sampleSize, Final dimensions: ${finalWidth}x${finalHeight}")

                    // Try to get pooled bitmap
                    pooledBitmap = BitmapPool.get(finalWidth, finalHeight, Bitmap.Config.ARGB_8888)

                    // Setup decode options
                    val options = BitmapFactory.Options().apply {
                        inSampleSize = sampleSize
                        inPreferredConfig = Bitmap.Config.ARGB_8888
                        inMutable = true
                        inBitmap = null // Default to null
                    }

                    // Check if we can use the pooled bitmap
                    if (!pooledBitmap.isRecycled) {
                        val canReuse = canUseForInBitmap(pooledBitmap, finalWidth, finalHeight)
                        if (canReuse) {
                            LogManager.v(TAG, "Using pooled bitmap: ${pooledBitmap.width}x${pooledBitmap.height}")
                            options.inBitmap = pooledBitmap
                        } else {
                            LogManager.v(TAG, "Pooled bitmap not compatible - returning to pool")
                            BitmapPool.put(pooledBitmap)
                            pooledBitmap = null
                        }
                    } else {
                        LogManager.v(TAG, "No suitable pooled bitmap available")
                        pooledBitmap.let { BitmapPool.put(it) }
                        pooledBitmap = null
                    }

                    // Reset stream to start for decoding
                    bufferedStream.reset()

                    // Decode bitmap with fallback strategy
                    val decodedBitmap = try {
                        BitmapFactory.decodeStream(bufferedStream, null, options)
                    } catch (e: IllegalArgumentException) {
                        LogManager.e(TAG, "Failed to decode with pooled bitmap, retrying without pool", e)

                        // Return pooled bitmap to pool
                        pooledBitmap?.let { BitmapPool.put(it) }
                        pooledBitmap = null

                        // Retry without pooled bitmap
                        options.inBitmap = null
                        bufferedStream.reset()
                        BitmapFactory.decodeStream(bufferedStream, null, options)
                    } ?: throw IllegalArgumentException("Failed to decode bitmap from URI: $contentUri")

                    LogManager.v(TAG, "Decoded bitmap: ${decodedBitmap.width}x${decodedBitmap.height}")

                    // Apply rotation if needed
                    val resultBitmap = rotateBitmapIfNeeded(decodedBitmap, rotationDegrees)

                    // Clean up if rotation created a new bitmap
                    if (resultBitmap != decodedBitmap) {
                        BitmapPool.put(decodedBitmap)
                    }

                    LogManager.d(TAG, "Successfully decoded and processed bitmap")
                    resultBitmap

                } catch (e: Exception) {
                    // Clean up pooled bitmap on error
                    pooledBitmap?.let {
                        if (!it.isRecycled) {
                            BitmapPool.put(it)
                        }
                    }
                    throw e
                }
            }
        } ?: throw IOException("Unable to open input stream for URI: $contentUri")
    }

    // Enhanced compatibility check
    private fun canUseForInBitmap(
        candidate: Bitmap?,
        targetWidth: Int,
        targetHeight: Int
    ): Boolean {
        if (candidate == null || candidate.isRecycled) {
            return false
        }
        // Check if bitmap has enough space
        val candidateByteCount = candidate.allocationByteCount
        val targetByteCount = targetWidth * targetHeight * getBytesPerPixel(candidate.config ?: Bitmap.Config.ARGB_8888)
        val canReuse = candidateByteCount >= targetByteCount
        LogManager.v(TAG, "Bitmap reuse check - Candidate: ${candidate.width}x${candidate.height} (${candidateByteCount} bytes), " +
                "Target: ${targetWidth}x${targetHeight} (${targetByteCount} bytes), Can reuse: $canReuse")
        return canReuse
    }

    private fun getBytesPerPixel(config: Bitmap.Config): Int {
        return when (config) {
            Bitmap.Config.ARGB_8888 -> 4
            Bitmap.Config.RGB_565 -> 2
            Bitmap.Config.ARGB_4444 -> 2
            Bitmap.Config.ALPHA_8 -> 1
            else -> 4
        }
    }
    /**
     * Retrieves the rotation angle from EXIF metadata using a provided stream.
     *
     * @param inputStream The buffered input stream (must support mark/reset).
     * @return The rotation angle in degrees (0, 90, 180, 270).
     */
    private fun getImageRotation(inputStream: BufferedInputStream): Int {
        inputStream.mark(Int.MAX_VALUE)
        return try {
            val exif = ExifInterface(inputStream)
            when (exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
            )) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } finally {
            inputStream.reset()
        }
    }

    private fun getImageDimensions(inputStream: BufferedInputStream): Pair<Int, Int> {
        inputStream.mark(Int.MAX_VALUE)
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeStream(inputStream, null, options)
        inputStream.reset()
        return options.outWidth to options.outHeight
    }

    private fun rotateBitmapIfNeeded(bitmap: Bitmap, rotationDegrees: Int): Bitmap {
        if (rotationDegrees == 0) return bitmap
        val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
        val rotatedBitmap =
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        return rotatedBitmap
    }

    private fun calculateInSampleSize(
        width: Int, height: Int, reqWidth: Int, reqHeight: Int
    ): Int {
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    fun scaleFromPool(source: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        // Input validation
        if (source.isRecycled) {
            throw IllegalArgumentException("Source bitmap is recycled")
        }
        if (targetWidth <= 0 || targetHeight <= 0) {
            throw IllegalArgumentException("Target dimensions must be positive: ${targetWidth}x${targetHeight}")
        }

        // Check if scaling is needed
        if (source.width == targetWidth && source.height == targetHeight) {
            LogManager.v("BitmapScale", "No scaling needed, returning original bitmap")
            return source
        }

        var pooledBitmap: Bitmap? = null

        return try {
            // Get pooled bitmap
            pooledBitmap = BitmapPool.get(targetWidth, targetHeight, source.config ?: Bitmap.Config.ARGB_8888)

            // Check if pooled bitmap is usable
            val canUsePooledBitmap = pooledBitmap?.let { bitmap ->
                !bitmap.isRecycled &&
                        bitmap.isMutable &&
                        bitmap.width == targetWidth &&
                        bitmap.height == targetHeight &&
                        bitmap.config == (source.config ?: Bitmap.Config.ARGB_8888)
            } ?: false

            if (canUsePooledBitmap) {
                LogManager.v("BitmapScale", "Using pooled bitmap for scaling: ${targetWidth}x${targetHeight}")

                // Clear the pooled bitmap first
                pooledBitmap.eraseColor(Color.TRANSPARENT)

                // Create canvas and draw scaled bitmap
                val canvas = Canvas(pooledBitmap)
                val matrix = Matrix().apply {
                    setScale(
                        targetWidth.toFloat() / source.width,
                        targetHeight.toFloat() / source.height
                    )
                }

                // Use high quality paint for better scaling
                val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
                canvas.drawBitmap(source, matrix, paint)

                LogManager.v("BitmapScale", "Successfully scaled using pooled bitmap")
                pooledBitmap

            } else {
                // Return unusable pooled bitmap to pool
                pooledBitmap.let {
                    if (!it.isRecycled) {
                        BitmapPool.put(it)
                    }
                }
                pooledBitmap = null

                LogManager.v("BitmapScale", "Pooled bitmap not suitable, creating new scaled bitmap")
                createScaledBitmap(source, targetWidth, targetHeight)
            }

        } catch (e: Exception) {
            LogManager.e("BitmapScale", "Failed to scale bitmap using pool", e)
            // Clean up pooled bitmap on error
            pooledBitmap?.let { bitmap ->
                if (!bitmap.isRecycled) {
                    try {
                        BitmapPool.put(bitmap)
                    } catch (cleanupException: Exception) {
                        LogManager.e("BitmapScale", "Failed to return bitmap to pool during cleanup", cleanupException)
                    }
                }
            }
            // Fallback to direct creation
            createScaledBitmap(source, targetWidth, targetHeight)
        }
    }
    /**
     * Creates a new scaled bitmap using the most appropriate method
     */
    private fun createScaledBitmap(source: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        return try {
            // Use Bitmap.createScaledBitmap for better quality and performance
            source.scale(targetWidth, targetHeight)
        } catch (e: OutOfMemoryError) {
            LogManager.e("BitmapScale", "OutOfMemoryError creating scaled bitmap, trying alternative method", e)
            // Fallback: try with filtering disabled
            try {
                source.scale(targetWidth, targetHeight, false)
            } catch (e2: OutOfMemoryError) {
                LogManager.e("BitmapScale", "Still out of memory, using extension function", e2)
                // Last resort: use extension function (assuming you have this)
                source.scale(targetWidth, targetHeight)
            }
        }
    }

    fun drawFaceBoundingBoxesOnThumbnail(
        originalBitmap: Bitmap,
        faces: List<Face>,
        thumbnailSize: Int
    ): Bitmap {
        if (faces.isEmpty()) {
            return originalBitmap.scale(thumbnailSize, thumbnailSize)
        }
        val thumbnail = scaleFromPool(
            originalBitmap,
            THUMBNAIL_SIZE,
            THUMBNAIL_SIZE
        )
        val scaleX = thumbnailSize / originalBitmap.width.toFloat()
        val scaleY = thumbnailSize / originalBitmap.height.toFloat()

        val canvas = Canvas(thumbnail)
        val paint = Paint().apply {
            color = Color.CYAN
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
        }

        for (face in faces) {
            val bounds = face.boundingBox
            val left = bounds.left * scaleX
            val top = bounds.top * scaleY
            val right = bounds.right * scaleX
            val bottom = bounds.bottom * scaleY

            canvas.drawRect(left, top, right, bottom, paint)
        }
        return thumbnail
    }

    fun saveEmbeddedFaceBoundingBoxes(
        originalBitmap: Bitmap,
        faces: List<Face>,
        thumbnailSize: Int
    ): Bitmap {
        if (faces.isEmpty()) {
            return originalBitmap.scale(thumbnailSize, thumbnailSize)
        }
        val thumbnail = scaleFromPool(
            originalBitmap,
            THUMBNAIL_SIZE,
            THUMBNAIL_SIZE
        )
        val scaleX = thumbnailSize / originalBitmap.width.toFloat()
        val scaleY = thumbnailSize / originalBitmap.height.toFloat()

        val canvas = Canvas(thumbnail)
        val paint = Paint().apply {
            color = Color.CYAN
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
        }

        for (face in faces) {
            val bounds = face.boundingBox
            val left = bounds.left * scaleX
            val top = bounds.top * scaleY
            val right = bounds.right * scaleX
            val bottom = bounds.bottom * scaleY

            canvas.drawRect(left, top, right, bottom, paint)
        }
        return thumbnail
    }

    fun cropFaceFromBitmap(bitmap: Bitmap, box: FaceBoundingBox): Bitmap? {
        try {
            val safeRect = Rect(
                box.left.coerceAtLeast(0),
                box.top.coerceAtLeast(0),
                box.right.coerceAtMost(bitmap.width),
                box.bottom.coerceAtMost(bitmap.height)
            )
            val cropped = Bitmap.createBitmap(
                bitmap,
                safeRect.left,
                safeRect.top,
                safeRect.width(),
                safeRect.height()
            )
            return cropped
        } catch (ex: Exception) {
            return null
        }
    }

    fun cropAndResizeFace(original: Bitmap, boundingBox: Rect, size: Int): Bitmap? {
        val safeBox = Rect(
            boundingBox.left.coerceAtLeast(0),
            boundingBox.top.coerceAtLeast(0),
            boundingBox.right.coerceAtMost(original.width),
            boundingBox.bottom.coerceAtMost(original.height)
        )
        return try {
            val cropped = Bitmap.createBitmap(
                original,
                safeBox.left,
                safeBox.top,
                safeBox.width(),
                safeBox.height()
            )
            cropped.scale(size, size)
        } catch (e: Exception) {
            null
        }
    }

    fun loadBitmapFromPath(path: String): Bitmap? {
        return try {
            BitmapFactory.decodeFile(path)
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        private const val TAG = "BitmapHelper"
    }

}