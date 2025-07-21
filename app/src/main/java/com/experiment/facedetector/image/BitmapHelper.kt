package com.experiment.facedetector.image

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
import com.experiment.facedetector.common.LogManager
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
        val file = getThumbnailPath(filename)
        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(format, quality, out)
                out.flush()
            }
            val sizeInKB = file.length() / 1024
            println("Saved thumbnail file size: $sizeInKB KB")
            return file
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    fun drawFaceBoundingBoxes(
        originalBitmap: Bitmap, faces: List<Face>
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

    fun getThumbnailPath(filename: String): File {
        return File(context.cacheDir, "$filename.webp")
    }

    private fun canUseForInBitmap(bitmap: Bitmap, width: Int, height: Int): Boolean {
        return bitmap.width == width && bitmap.height == height && !bitmap.isRecycled && bitmap.isMutable
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
        context.contentResolver.openInputStream(contentUri)?.let { inputStream ->
            BufferedInputStream(inputStream, 8192).use { bufferedStream ->
                bufferedStream.mark(Int.MAX_VALUE)
                val rotationDegrees = getImageRotation(bufferedStream)
                //  Get dimensions
                val (originalWidth, originalHeight) = getImageDimensions(bufferedStream)

                //  Adjust dimensions for rotation
                val (adjustedWidth, adjustedHeight) = if (rotationDegrees == 90 || rotationDegrees == 270) {
                    originalHeight to originalWidth
                } else {
                    originalWidth to originalHeight
                }

                // get from pool
                val sampleSize =
                    calculateInSampleSize(adjustedWidth, adjustedHeight, targetWidth, targetHeight)
                val finalWidth = adjustedWidth / sampleSize
                val finalHeight = adjustedHeight / sampleSize
                val pooledBitmap = BitmapPool.get(finalWidth, finalHeight, Bitmap.Config.ARGB_8888)
                // Decode bitmap
                val options = BitmapFactory.Options().apply {
                    inSampleSize = sampleSize
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                    if (canUseForInBitmap(pooledBitmap, finalWidth, finalHeight)) {
                        inBitmap = pooledBitmap
                        inMutable = true
                    }
                }

                // Reset stream to start for decoding
                bufferedStream.reset()
                val decodedBitmap = BitmapFactory.decodeStream(bufferedStream, null, options)
                    ?: throw IllegalArgumentException("Failed to decode bitmap from URI: $contentUri")

                val resultBitmap = rotateBitmapIfNeeded(decodedBitmap, rotationDegrees)
                if (resultBitmap != decodedBitmap) {
                    BitmapPool.put(decodedBitmap)
                }
                return resultBitmap
            }
        } ?: throw IOException("Unable to open input stream for URI: $contentUri")
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

    fun scaleFromPool(source : Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        return try {
            // Try to get bitmap from pool first
            val pooledBitmap = BitmapPool.get(targetWidth, targetHeight, source.config ?: Bitmap.Config.ARGB_8888)

            if (pooledBitmap.width == targetWidth && pooledBitmap.height == targetHeight && !pooledBitmap.isRecycled) {
                // Use existing bitmap from pool
                val canvas = Canvas(pooledBitmap)
                val matrix = Matrix().apply {
                    setScale(
                        targetWidth.toFloat() / source.width,
                        targetHeight.toFloat() / source.height
                    )
                }
                canvas.drawBitmap(source, matrix, null)
                pooledBitmap
            } else {
                // Fallback to creating new bitmap
                source.scale(targetWidth, targetHeight)
            }
        } catch (e: Exception) {
            LogManager.e("BitmapScale", "Failed to scale bitmap using pool, falling back to direct creation", e)
            source.scale(targetWidth, targetHeight)
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

    fun cropFaceFromBitmap(bitmap: Bitmap, box: FaceBoundingBox): Bitmap {
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
        return cropped.scale(112, 112)
    }

    fun loadBitmapFromPath(path: String): Bitmap? {
        return try {
            BitmapFactory.decodeFile(path)
        } catch (e: Exception) {
            null
        }
    }

}