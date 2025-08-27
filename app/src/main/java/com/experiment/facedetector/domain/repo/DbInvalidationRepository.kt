package com.experiment.facedetector.domain.repo

import kotlinx.coroutines.flow.Flow

/**
 * Emits whenever given Room tables change.
 * Decouples UI/domain from Room's InvalidationTracker.
 */
interface DbInvalidationRepository {

    /**
     * Emits Unit for any change in the specified tables.
     * If no tables provided, emits for any table.
     */
    fun changes(vararg tables: String): Flow<Unit>

    /**
     * Optional: manually notify listeners (e.g., after non-Room writes).
     */
    fun notifyExternalChange()
}
