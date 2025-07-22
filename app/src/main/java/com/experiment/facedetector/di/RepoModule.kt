package com.experiment.facedetector.di


import com.experiment.facedetector.data.local.dao.MediaDao
import com.experiment.facedetector.data.local.paging.MediaWithFacesPagingSource
import com.experiment.facedetector.data.local.repo.FaceDetectionRepoImpl
import com.experiment.facedetector.data.local.repo.FaceSearchRepositoryImpl
import com.experiment.facedetector.domain.repo.MediaRepo
import com.experiment.facedetector.data.local.repo.MediaRepoImpl
import com.experiment.facedetector.data.local.repo.SearchQueryRepoImpl
import com.experiment.facedetector.domain.repo.FaceDetectionRepo
import com.experiment.facedetector.domain.repo.FaceSearchRepository
import com.experiment.facedetector.domain.repo.SearchQueryRepo
import org.koin.dsl.module

val repositoryModule = module {

    single<MediaRepo> {
        MediaRepoImpl(
            mediaDao = get(),
            faceDao = get(),
            pagingSourceFactory = get(),
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
            mediaRepo = get(),
        )
    }

    // create new paging source for each call
    factory<(MediaDao) -> MediaWithFacesPagingSource> {
        { dao -> MediaWithFacesPagingSource(dao) }
    }

}
