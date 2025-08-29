package com.experiment.facedetector.presentation.widgets

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.experiment.facedetector.presentation.theme.GradientEndMildBlack
import com.experiment.facedetector.presentation.theme.GradientStartMildGrey
import com.experiment.facedetector.presentation.theme.MildGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(title: String, onBackClicked: (() -> Unit)? = null) {
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

@Composable
fun AppCircularProgressIndicator() {
    CircularProgressIndicator(
        modifier = Modifier
            .size(56.dp)
            .padding(8.dp),
        color = Color.White,
        strokeWidth = 4.dp
    )
}

/**
 * Reusable component for displaying full-screen images with face detection support.
 *
 * ⚠️ Do not change [ContentScale] — it is set intentionally to ensure
 * correct coordinate mapping between the original image and face bounding boxes.
 *
 * Use this component wherever full image rendering with face overlays is required.
 */

@Composable
fun AppFullScreenImage(imageBitmap: ImageBitmap, description: String? = null) {
    return Image(
        bitmap = imageBitmap,
        contentDescription = description,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Fit
    )
}

@Composable
fun ThumbnailItem(thumbnailUri: String?) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
    ) {
        val context = LocalContext.current
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(context)
                .data(thumbnailUri)
                .crossfade(true)          // optional
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            loading = {
                Box(Modifier.fillMaxSize().background(MildGray))
            },
            error = {
                Image(
                    painter = painterResource(id = android.R.drawable.stat_notify_error),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Inside
                )
            }
        )
    }
}