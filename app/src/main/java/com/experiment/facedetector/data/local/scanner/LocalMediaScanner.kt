package com.experiment.facedetector.data.local.scanner

import com.experiment.facedetector.data.local.source.LocalMediaSource
import com.experiment.facedetector.domain.processing.FaceEmbeddingPipeline
import com.experiment.facedetector.domain.processing.ThumbnailGenerator
import com.experiment.facedetector.domain.repo.FaceRepository
import com.experiment.facedetector.domain.repo.MediaFingerPrint
import com.experiment.facedetector.domain.repo.MediaRepository
import com.experiment.facedetector.domain.repo.MediaSourceCursorRepo
import com.experiment.facedetector.domain.repo.StableIdGenerator

class LocalMediaScanner(
    cursorRepo: MediaSourceCursorRepo,
    mediaRepo: MediaRepository,
    faceRepo: FaceRepository,
    embeddings: FaceEmbeddingPipeline,
    thumbnailGenerator: ThumbnailGenerator,
    fingerPrint: MediaFingerPrint,
    cameraMediaStoreSource: LocalMediaSource,
    stableIdGenerator: StableIdGenerator,
) : BaseMediaScanner(
    cursorRepo = cursorRepo,
    mediaRepo = mediaRepo,
    faceRepo = faceRepo,
    embeddings = embeddings,
    thumbnails = thumbnailGenerator,
    fingerPrint = fingerPrint,
    source = cameraMediaStoreSource,
    stableIdGenerator = stableIdGenerator,
)
