package com.experiment.facedetector.ui.entities

import com.experiment.facedetector.domain.entities.MediaWithFacesDomain

fun MediaWithFacesDomain.toUi(): MediaWithFacesUi {
    return MediaWithFacesUi(
        id = media.id,
        thumbnailUri = media.thumbnailUri,
    )
}
