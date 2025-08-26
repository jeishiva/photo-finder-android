package com.experiment.facedetector.data.processing

import com.experiment.facedetector.domain.repo.MediaFingerPrint
import java.security.MessageDigest

class MediaFingerPrintImpl : MediaFingerPrint {
    override fun generate(
        sourceStableId: String,
        lastModified: Long?,
        sizeBytes: Long?
    ): String {
        val input = buildString {
            append(sourceStableId)
            append("|")
            append(lastModified ?: 0L)
            append("|")
            append(sizeBytes ?: 0L)
        }
        return sha1(input.toByteArray())
    }

    private fun sha1(data: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-1")
        val hashBytes = digest.digest(data)
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}