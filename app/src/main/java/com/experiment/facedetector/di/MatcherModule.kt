package com.experiment.facedetector.di

import com.experiment.facedetector.domain.matcher.CosineEmbeddingMatcher
import com.experiment.facedetector.domain.matcher.EmbeddingMatcher
import com.experiment.facedetector.domain.matcher.FaceEmbeddingMatcher
import org.koin.dsl.module

val matcherModule = module {
    single<EmbeddingMatcher> {
        CosineEmbeddingMatcher()
    }

    single<FaceEmbeddingMatcher> {
        FaceEmbeddingMatcher(embeddingMatcher = get())
    }
}