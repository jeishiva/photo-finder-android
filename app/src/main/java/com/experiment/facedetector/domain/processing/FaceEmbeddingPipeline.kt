package com.experiment.facedetector.domain.processing

import com.experiment.facedetector.domain.entities.SourceMediaItem


/**
 * Given a content Uri, return zero or more embeddings (one per detected face).
 */
interface FaceEmbeddingPipeline {
    suspend fun extractEmbeddings(sourceMediaItem: SourceMediaItem): List<Pair<String, FloatArray>>
}
