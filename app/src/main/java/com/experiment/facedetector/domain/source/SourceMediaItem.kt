package com.experiment.facedetector.domain.source

import android.net.Uri

data class SourceMediaItem(
    val stableId: Long,
    val contentUri: Uri,
    val fileSize : Long,
    val lastModifiedTime : Long,
)