package com.experiment.facedetector.data.processing

import com.experiment.facedetector.domain.repo.StableIdGenerator
import com.experiment.facedetector.domain.entities.MediaSourceType
import com.experiment.facedetector.domain.entities.SourceMediaItem
import java.nio.ByteBuffer
import java.security.MessageDigest

class StableIdGeneratorImpl : StableIdGenerator {
    override fun generate(
        input: String,
    ): Long {
        val digest = MessageDigest.getInstance("SHA-1").digest(input.toByteArray())
        // given the expected scale (tens of thousands of local photos),
        // using a 64-bit hash provides a practically collision-free identifier.
        // This is sufficient for our indexing use case on-device.
        return ByteBuffer.wrap(digest.copyOfRange(0, 8)).long
    }
}