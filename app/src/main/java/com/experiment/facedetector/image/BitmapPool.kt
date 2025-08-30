package com.experiment.facedetector.image

import android.graphics.Bitmap
import androidx.collection.LruCache
import androidx.core.graphics.createBitmap
import com.experiment.facedetector.common.LogManager

object BitmapPool {
    private val maxSize = (Runtime.getRuntime().maxMemory() / 10).toInt()
    private val pool = object : LruCache<String, Bitmap>(maxSize) {

        override fun entryRemoved(
            evicted: Boolean, key: String, oldValue: Bitmap, newValue: Bitmap?
        ) {
            if (evicted && !oldValue.isRecycled) {
                oldValue.recycle()
            }
        }

        override fun sizeOf(key: String, value: Bitmap): Int {
            return try {
                if (value.isRecycled) 0 else value.byteCount
            } catch (e: Exception) {
                LogManager.e("BitmapPool", "size error in bitmap")
                0
            }
        }
    }

    fun get(width: Int, height: Int, config: Bitmap.Config): Bitmap {
        val key = "$width-$height-${config.name}"
        val existing =  pool.remove(key)
        if (existing != null) {
            LogManager.d("BitmapPool", "reusing bitmap $key")
            return existing
        }
        return createBitmap(width, height, config)
    }

    fun put(bitmap: Bitmap) {
        if (bitmap.isRecycled) {
            LogManager.e("BitmapPool", "Cannot put recycled bitmap")
            return
        }
        val key = "${bitmap.width}-${bitmap.height}-${bitmap.config?.name}"
        pool.put(key, bitmap)
    }

}