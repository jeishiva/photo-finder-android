package com.experiment.facedetector.data.common

import android.database.Cursor

class CursorReader(
    private val cursor: Cursor
) {
    private val indexCache: MutableMap<String, Int?> = mutableMapOf()

    private fun indexOfOrNull(column: String): Int? {
        val cached = indexCache[column]
        if (cached != null || indexCache.containsKey(column)) {
            return cached
        }

        val idx = cursor.getColumnIndex(column)
        val value = if (idx >= 0) idx else null
        indexCache[column] = value
        return value
    }

    fun getStringOrNull(column: String): String? {
        val idx = indexOfOrNull(column)
        if (idx == null) {
            return null
        }

        if (cursor.isNull(idx)) {
            return null
        }

        return cursor.getString(idx)
    }

    fun getLongOrNull(column: String): Long? {
        val idx = indexOfOrNull(column)
        if (idx == null) {
            return null
        }

        if (cursor.isNull(idx)) {
            return null
        }

        return cursor.getLong(idx)
    }

    fun getIntOrNull(column: String): Int? {
        val idx = indexOfOrNull(column)
        if (idx == null) {
            return null
        }

        if (cursor.isNull(idx)) {
            return null
        }

        return cursor.getInt(idx)
    }

    /**
     * For MediaStore columns that are stored in **seconds**.
     * Returns milliseconds or null.
     */
    fun getEpochSecAsMillisOrNull(column: String): Long? {
        val sec = getLongOrNull(column)
        if (sec == null) {
            return null
        }

        if (sec <= 0L) {
            return null
        }

        return sec * 1000L
    }

    /**
     * For MediaStore columns that are already **milliseconds**.
     */
    fun getMillisOrNull(column: String): Long? {
        val ms = getLongOrNull(column)
        if (ms == null) {
            return null
        }

        if (ms <= 0L) {
            return null
        }

        return ms
    }
}
