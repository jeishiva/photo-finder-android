package com.experiment.facedetector.domain.matcher

import kotlin.math.sqrt

class CosineEmbeddingMatcher : EmbeddingMatcher {

    override fun isSimilar(
        embedding1: FloatArray,
        embedding2: FloatArray,
        threshold: Float
    ): Boolean {
        val similarity = cosineSimilarity(embedding1, embedding2)
        return similarity >= threshold
    }

    private fun cosineSimilarity(vec1: FloatArray, vec2: FloatArray): Float {
        require(vec1.size == vec2.size) {
            "Vectors must be of the same size. Got ${vec1.size} and ${vec2.size}"
        }

        if (vec1.isEmpty()) return 0f

        var dot = 0f
        var norm1 = 0f
        var norm2 = 0f

        for (i in vec1.indices) {
            val v1 = vec1[i]
            val v2 = vec2[i]
            dot += v1 * v2
            norm1 += v1 * v1
            norm2 += v2 * v2
        }

        return if (norm1 == 0f || norm2 == 0f) {
            0f
        } else {
            dot / (sqrt(norm1) * sqrt(norm2))
        }
    }
}
