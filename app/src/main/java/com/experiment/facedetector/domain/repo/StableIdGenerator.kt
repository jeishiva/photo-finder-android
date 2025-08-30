package com.experiment.facedetector.domain.repo

interface StableIdGenerator {
    fun generate(
        input: String,
    ): Long
}