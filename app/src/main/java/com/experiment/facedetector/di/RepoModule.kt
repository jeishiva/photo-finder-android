package com.experiment.facedetector.di

import androidx.room.RoomDatabase
import com.experiment.facedetector.data.local.AppDatabase
import com.experiment.facedetector.data.local.repo.FaceDetectionRepoImpl
import com.experiment.facedetector.data.local.repo.FaceRepositoryImpl
import com.experiment.facedetector.data.local.repo.MediaRepositoryImpl
import com.experiment.facedetector.data.local.repo.MediaWithFacesRepositoryImpl
import com.experiment.facedetector.data.local.repo.RoomDbInvalidationRepository
import com.experiment.facedetector.domain.repo.DbInvalidationRepository
import com.experiment.facedetector.domain.repo.FaceDetectionRepo
import com.experiment.facedetector.domain.repo.FaceRepository
import com.experiment.facedetector.domain.repo.MediaRepository
import com.experiment.facedetector.domain.repo.MediaWithFacesRepository
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

    single<MediaWithFacesRepository> {
        MediaWithFacesRepositoryImpl(
            dao = get(),
        )
    }
}
