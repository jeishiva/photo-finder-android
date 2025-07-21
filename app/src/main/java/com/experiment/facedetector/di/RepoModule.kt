package com.experiment.facedetector.di


import com.experiment.facedetector.data.local.repo.FaceDetectionRepoImpl
import com.experiment.facedetector.data.local.repo.FaceSearchRepositoryImpl
import com.experiment.facedetector.domain.repo.IMediaRepo
import com.experiment.facedetector.data.local.repo.MediaRepo
import com.experiment.facedetector.data.local.repo.SearchQueryRepoImpl
import com.experiment.facedetector.domain.repo.FaceDetectionRepo
import com.experiment.facedetector.domain.repo.FaceSearchRepository
import com.experiment.facedetector.domain.repo.SearchQueryRepo
import org.koin.dsl.module

val repositoryModule = module {
    single<IMediaRepo> {
        MediaRepo(
            mediaDao = get(),
            faceDao = get()
        )
    }

    single<FaceDetectionRepo> {
        FaceDetectionRepoImpl(
            faceDetector = get(),
            imageHelper = get()
        )
    }

    single<SearchQueryRepo> {
        SearchQueryRepoImpl(
            searchFaceDao = get(),
            imageHelper = get()
        )
    }

    single<FaceSearchRepository> {
        FaceSearchRepositoryImpl(
            interpreterDeferred = get(),
            imageHelper = get()
        )
    }
}
