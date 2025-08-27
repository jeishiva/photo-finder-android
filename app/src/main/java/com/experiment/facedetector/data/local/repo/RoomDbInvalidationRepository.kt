package com.experiment.facedetector.data.local.repo

import androidx.room.RoomDatabase
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import com.experiment.facedetector.domain.repo.DbInvalidationRepository

class RoomDbInvalidationRepository(
    private val db: RoomDatabase
) : DbInvalidationRepository {

    private val externalBus = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    override fun changes(vararg tables: String): Flow<Unit> {
        if (tables.isEmpty()) {
            return externalBus
        }
        val roomFlow: Flow<Unit> =
            db.invalidationTracker
                .createFlow(*tables)
                .map { Unit }
        return merge(roomFlow, externalBus)
    }

    override fun notifyExternalChange() {
        externalBus.tryEmit(Unit)
    }
}
