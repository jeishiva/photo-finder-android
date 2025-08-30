package com.experiment.facedetector.di

import com.experiment.facedetector.data.local.scanner.CameraMediaScanner
import org.koin.dsl.module

val scannerModule = module {
    single<CameraMediaScanner> {
        CameraMediaScanner(
            cameraMediaStoreSource = get() ,
            mediaRepo = get(),
            faceRepo = get(),
            embeddings = get(),
            thumbnailGenerator = get(),
            fingerPrint = get(),
            cursorRepo = get(),
            stableIdGenerator = get(),
        )
    }
}
