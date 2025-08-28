package com.experiment.facedetector.data.local.scanner

import com.experiment.facedetector.data.local.source.CameraMediaStoreSource
import com.experiment.facedetector.domain.processing.FaceEmbeddingPipeline
import com.experiment.facedetector.domain.processing.ThumbnailGenerator
import com.experiment.facedetector.domain.repo.FaceRepository
import com.experiment.facedetector.domain.repo.MediaFingerPrint
import com.experiment.facedetector.domain.repo.MediaRepository
import com.experiment.facedetector.domain.repo.MediaSourceCursorRepo

class CameraMediaScanner(
    cursorRepo: MediaSourceCursorRepo,
    mediaRepo: MediaRepository,
    faceRepo: FaceRepository,
    embeddings: FaceEmbeddingPipeline,
    thumbnails: ThumbnailGenerator,
    fingerPrint: MediaFingerPrint,
    cameraMediaStoreSource: CameraMediaStoreSource,
) : BaseMediaScanner(
    cursorRepo = cursorRepo,
    mediaRepo = mediaRepo,
    faceRepo = faceRepo,
    embeddings = embeddings,
    thumbnails = thumbnails,
    fingerPrint = fingerPrint,
    source = cameraMediaStoreSource,
)