package com.experiment.facedetector.di

import android.graphics.Bitmap
import com.experiment.facedetector.config.FullImageConfig
import com.experiment.facedetector.data.processing.FaceEmbeddingPipelineImpl
import com.experiment.facedetector.data.processing.MediaFingerPrintImpl
import com.experiment.facedetector.data.processing.StableIdGeneratorImpl
import com.experiment.facedetector.data.processing.ThumbnailGeneratorImpl
import com.experiment.facedetector.domain.processing.FaceEmbeddingPipeline
import com.experiment.facedetector.domain.processing.ThumbnailGenerator
import com.experiment.facedetector.domain.repo.MediaFingerPrint
import com.experiment.facedetector.domain.repo.StableIdGenerator
import com.experiment.facedetector.face.FaceDetectionProcessor
import org.koin.dsl.module

val processorModule = module {
    single {
        FaceDetectionProcessor(
            faceDetector = get(),
            imageHelper = get()
        )
    }

    single<ThumbnailGenerator> {
        ThumbnailGeneratorImpl(
            bitmapHelper = get(),
            thumbnailSize = 100,
            compressFormat = Bitmap.CompressFormat.PNG,
            quality = 100
        )
    }

    single<FaceEmbeddingPipeline> {
        FaceEmbeddingPipelineImpl(
            bitmapHelper = get(),
            faceDetectionProcessor = get(),
            extractEmbeddingsUseCase = get(),
            targetHeight = FullImageConfig.MAX_HEIGHT,
            targetWidth = FullImageConfig.MAX_WIDTH
        )
    }

    single<MediaFingerPrint> {
        MediaFingerPrintImpl()
    }

    single<StableIdGenerator> {
        StableIdGeneratorImpl()
    }
}
