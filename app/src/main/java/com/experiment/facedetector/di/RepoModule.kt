package com.experiment.facedetector.di


import com.experiment.facedetector.data.local.repo.FaceDetectionRepoImpl
import com.experiment.facedetector.domain.repo.IMediaRepo
import com.experiment.facedetector.data.local.repo.MediaRepo
import com.experiment.facedetector.domain.repo.FaceDetectionRepo
import org.koin.dsl.module

val repositoryModule = module {
    single<IMediaRepo> {
        MediaRepo(mediaDao = get(), faceDao = get())
    }

    single<FaceDetectionRepo> {
        FaceDetectionRepoImpl(faceDetector = get(), imageHelper = get())
    }
}
