package com.experiment.facedetector.core.policy

object MediaTimePolicy {
    data class Inputs(
        val exifDateTimeOriginalMs: Long?,
        val dateTakenMs: Long?,
        val dateAddedMs: Long?,
        val dateModifiedMs: Long?,
        val generationModified: Long?
    )

    fun resolveCreatedAtMs(inputs: Inputs): Long {
        if (inputs.exifDateTimeOriginalMs != null && inputs.exifDateTimeOriginalMs > 0L) {
            return inputs.exifDateTimeOriginalMs
        }

        if (inputs.dateTakenMs != null && inputs.dateTakenMs > 0L) {
            return inputs.dateTakenMs
        }

        if (inputs.dateAddedMs != null && inputs.dateAddedMs > 0L) {
            return inputs.dateAddedMs
        }

        if (inputs.dateModifiedMs != null && inputs.dateModifiedMs > 0L) {
            return inputs.dateModifiedMs
        }

        return System.currentTimeMillis()
    }

    fun resolveModifiedAtMs(
        dateModifiedMs: Long?,
        generationModified: Long?
    ): Long {
        if (generationModified != null && generationModified > 0L) {
            return generationModified
        }

        if (dateModifiedMs != null && dateModifiedMs > 0L) {
            return dateModifiedMs
        }

        return 0L
    }
}