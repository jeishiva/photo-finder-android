package com.experiment.facedetector.common

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.experiment.facedetector.config.ThumbnailConfig
import com.experiment.facedetector.domain.entities.FaceBoundingBox
import com.google.mlkit.vision.face.Face
import java.io.ByteArrayOutputStream

fun Long.toFileName(): String {
    return this.toString().toFileName()
}

fun String.toFileName(): String {
    return StringBuilder().apply {
        append(ThumbnailConfig.THUMBNAIL_FILE_PREFIX)
        append(this@toFileName)
    }.toString()
}

fun Face.toFaceId(mediaId: Long): String = buildString {
    append("faceId-$mediaId-")
    append("${boundingBox.left}-${boundingBox.top}-${boundingBox.right}-${boundingBox.bottom}")
}

fun Face.toFaceBoundingBox(): FaceBoundingBox {
    return FaceBoundingBox(
        left = this.boundingBox.left,
        top = this.boundingBox.top,
        right = this.boundingBox.right,
        bottom = this.boundingBox.bottom,
    )
}

fun Bitmap.toByteArray(): ByteArray {
    val outputStream = ByteArrayOutputStream()
    this.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    return outputStream.toByteArray()
}

fun ByteArray.toBitmap(byteArray: ByteArray): Bitmap? {
    return try {
        BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}