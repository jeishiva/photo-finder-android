
package com.experiment.facedetector.di
import com.experiment.facedetector.data.local.source.LocalMediaSource
import org.koin.dsl.module

val mediaSourceModule = module {
    single<LocalMediaSource> {
        LocalMediaSource(get())
    }
}
