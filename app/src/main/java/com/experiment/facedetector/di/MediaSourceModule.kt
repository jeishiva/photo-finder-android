
package com.experiment.facedetector.di
import com.experiment.facedetector.data.local.source.CameraMediaStoreSource
import org.koin.dsl.module

val mediaSourceModule = module {
    single<CameraMediaStoreSource> {
        CameraMediaStoreSource(get())
    }
}
