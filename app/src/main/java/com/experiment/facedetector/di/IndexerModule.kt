package com.experiment.facedetector.di

import com.experiment.facedetector.data.local.index.MediaIndexerImpl
import com.experiment.facedetector.domain.index.MediaIndexer
import com.experiment.facedetector.domain.source.MediaSource
import com.experiment.facedetector.domain.entities.MediaSourceType
import org.koin.core.qualifier.named
import org.koin.dsl.module

interface MediaIndexerFactory {
    fun getIndexer(type: MediaSourceType): MediaIndexer
}

val indexerModule = module {
    single<MediaIndexer>(named(MediaSourceType.MediaStoreCamera.key)) {
        MediaIndexerImpl(
            source = get<MediaSource>(qualifier = named(MediaSourceType.MediaStoreCamera.key)),
            mediaRepo = get(),
            faceRepo = get(),
            embeddings = get(),
            thumbnails = get(),
            fingerPrint = get(),
        )
    }

    single<MediaIndexerFactory> {
        object : MediaIndexerFactory {
            override fun getIndexer(type: MediaSourceType): MediaIndexer {
                return get(qualifier = named(type.key))
            }
        }
    }
}
