package com.experiment.facedetector.domain.matcher

interface EmbeddingMatcher {
    fun isSimilar(embedding1: FloatArray,
                  embedding2: FloatArray,
                  threshold: Float): Boolean
}