package com.experiment.facedetector.di

import com.experiment.facedetector.data.local.repo.FaceDetectionRepoImpl
import com.experiment.facedetector.data.local.repo.FaceRepositoryImpl
import com.experiment.facedetector.data.local.repo.MediaRepositoryImpl
import com.experiment.facedetector.domain.repo.FaceDetectionRepo
import com.experiment.facedetector.domain.repo.FaceRepository
import com.experiment.facedetector.domain.repo.MediaRepository
import org.koin.dsl.module

val repositoryModule = module {

    single<MediaRepository> {
        MediaRepositoryImpl(
            mediaDao = get(),
        )
    }

    single<FaceRepository> {
        FaceRepositoryImpl(
            faceDao = get(),
        )
    }

    single<FaceDetectionRepo> {
        FaceDetectionRepoImpl(
            faceDetector = get(),
            imageHelper = get()
        )
    }

}
