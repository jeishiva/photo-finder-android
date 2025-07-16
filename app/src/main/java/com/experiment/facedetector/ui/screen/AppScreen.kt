package com.experiment.facedetector.ui.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.experiment.facedetector.ui.theme.AndroidFaceDetectorTheme
import com.experiment.facedetector.ui.widgets.AppBar

@Composable
fun AppScreen(
    content: @Composable (PaddingValues) -> Unit,
    title: String,
    onClick: () -> Unit
) {
    AndroidFaceDetectorTheme {
        Scaffold(
            topBar = { AppBar(title, onClick) },
            containerColor = Color.Transparent,
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            content(innerPadding)
        }
    }
}