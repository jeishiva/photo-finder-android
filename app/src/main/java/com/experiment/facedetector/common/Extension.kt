package com.experiment.facedetector.common

import com.experiment.facedetector.config.ThumbnailConfig
import com.google.mlkit.vision.face.Face

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