package com.experiment.facedetector.core.policy

/**
 * Policy for resolving an image's display orientation (in degrees clockwise).
 *
 * Priority:
 * 1) EXIF orientation tag (maps to 0/90/180/270)
 * 2) MediaStore orientation column (must be 0/90/180/270)
 * 3) Fallback to 0
 *
 * This policy is domain-safe: it does not depend on Android framework types.
 * Pass in the EXIF orientation tag integer if you have it (ExifInterface tag value),
 * otherwise pass null.
 *
 * EXIF tag values of interest (others are mirrored variants, which we map to the same degrees):
 * 1 = 0°, 3 = 180°, 6 = 90°, 8 = 270°
 * 2 ~ 1 (mirror), 4 ~ 3 (mirror), 5 ~ 6 (mirror), 7 ~ 8 (mirror)
 */
object OrientationPolicy {

    /**
     * Resolve the final orientation to apply in degrees.
     * Always returns one of {0, 90, 180, 270}.
     */
    fun resolve(
        exifOrientationTag: Int?,
        mediaStoreOrientationDeg: Int?
    ): Int {
        val fromExif: Int? = exifToDegrees(exifOrientationTag)
        if (fromExif != null) {
            return fromExif
        }
        val fromMediaStore: Int? = normalizeMediaStoreDegrees(mediaStoreOrientationDeg)
        if (fromMediaStore != null) {
            return fromMediaStore
        }
        return 0
    }

    /**
     * Convert an EXIF orientation tag value into degrees clockwise.
     * Returns null if the tag is null or unrecognized.
     *
     * Mirrors are mapped to the same rotation degrees (flip is ignored here).
     */
    fun exifToDegrees(tag: Int?): Int? {
        if (tag == null) {
            return null
        }

        // Primary orientations
        if (tag == 1) {
            return 0
        }
        if (tag == 3) {
            return 180
        }
        if (tag == 6) {
            return 90
        }
        if (tag == 8) {
            return 270
        }

        // Mirrored variants -> same rotation (flip not handled at this stage)
        if (tag == 2) {
            return 0
        }
        if (tag == 4) {
            return 180
        }
        if (tag == 5) {
            return 90
        }
        if (tag == 7) {
            return 270
        }

        return null
    }

    /**
     * Normalize MediaStore's orientation column to one of {0,90,180,270}.
     * Returns null if the value is null or invalid.
     */
    fun normalizeMediaStoreDegrees(value: Int?): Int? {
        if (value == null) {
            return null
        }
        if (value == 0) {
            return 0
        }
        if (value == 90) {
            return 90
        }
        if (value == 180) {
            return 180
        }
        if (value == 270) {
            return 270
        }

        val candidates = intArrayOf(0, 90, 180, 270)
        var nearest = 0
        var minDiff = Int.MAX_VALUE
        for (candidate in candidates) {
            val diff = kotlin.math.abs(candidate - value)
            if (diff < minDiff) {
                minDiff = diff
                nearest = candidate
            }
        }

        return nearest
    }
}
