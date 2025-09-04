package com.experiment.facedetector.presentation.app.model

import androidx.compose.runtime.Stable

@Stable
data class AppActions(
    val onPermissionGranted: () -> Unit = {},
)