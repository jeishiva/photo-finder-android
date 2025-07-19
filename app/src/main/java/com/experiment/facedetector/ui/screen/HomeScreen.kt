package com.experiment.facedetector.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.experiment.facedetector.R
import com.experiment.facedetector.navigation.AppRoute
import com.experiment.facedetector.ui.theme.AndroidFaceDetectorTheme
import com.experiment.facedetector.ui.widgets.AppBar

@Composable
fun HomeScreen(navController: NavHostController) {
    val backClick: () -> Unit = remember(navController) {
        { navController.popBackStack(AppRoute.Splash.route, inclusive = true) }
    }
    HomeContent(onBackClick = backClick)
}

@Composable
@Preview(showBackground = true)
fun HomeContent(onBackClick: () -> Unit = {}) {
    // val viewModel: HomeViewModel = koinViewModel()
    var selectedImage by remember { mutableStateOf<Uri?>(null) }
    AndroidFaceDetectorTheme {
        Scaffold(
            topBar = { AppBar(stringResource(R.string.home_screen), onClick = onBackClick) },
            containerColor = Color.Transparent,
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularImageOrPlaceholder(selectedImage, size = 125.dp)
                Spacer(modifier = Modifier.height(16.dp))
                GalleryImagePicker { uri ->
                    selectedImage = uri
                }
            }
        }
    }
}

@Composable
fun CircularImageOrPlaceholder(
    imageUri: Uri?,
    modifier: Modifier = Modifier,
    size: Dp = 100.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.Gray),
        contentAlignment = Alignment.Center
    ) {
        if (imageUri != null) {
            AsyncImage(
                model = imageUri,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Placeholder",
                tint = Color.White,
                modifier = Modifier.size(size / 2)
            )
        }
    }
}

@Composable
fun GalleryImagePicker(
    onImageSelected: (Uri?) -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> onImageSelected(uri) }
    )
    Button(onClick = { launcher.launch("image/*") }) {
        Text("Select Photo")
    }
}
