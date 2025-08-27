
package com.experiment.facedetector.di
import com.experiment.facedetector.data.local.source.CameraMediaStoreSource
import com.experiment.facedetector.domain.source.MediaSource
import com.experiment.facedetector.domain.entities.MediaSourceType
import org.koin.core.qualifier.named
import org.koin.dsl.module

val mediaSourceModule = module {

    single<MediaSource>(qualifier = named(MediaSourceType.MediaStoreCamera.identifier)) {
        CameraMediaStoreSource(get())
    }

    single<MediaSource> {
        get(qualifier = named(MediaSourceType.MediaStoreCamera.identifier))
    }
}
