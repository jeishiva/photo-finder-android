package com.experiment.facedetector.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.experiment.facedetector.presentation.theme.GradientEndMildBlack
import com.experiment.facedetector.presentation.theme.GradientStartMildGrey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(title: String, onBackClicked: (() -> Unit)? = null) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            if (onBackClicked == null) {
                null
            } else {
                IconButton(onClick = onBackClicked) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Navigation Menu"
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = GradientEndMildBlack,
            scrolledContainerColor = GradientStartMildGrey,
            navigationIconContentColor = Color.White,
            titleContentColor = Color.White,
            actionIconContentColor = GradientStartMildGrey
        )
    )
}