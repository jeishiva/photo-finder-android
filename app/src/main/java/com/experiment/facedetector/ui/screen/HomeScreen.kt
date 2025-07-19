package com.experiment.facedetector.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.experiment.facedetector.ui.TimeRangeOption
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularImageOrPlaceholder(selectedImage, size = 125.dp)
                Spacer(modifier = Modifier.height(16.dp))
                GalleryImagePicker { uri ->
                    selectedImage = uri
                }
                Spacer(modifier = Modifier.height(16.dp))
                TimeRangeSelectorScreen()
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
fun TimeRangeSelectorScreen() {
    var selectedOption by remember { mutableStateOf<TimeRangeOption?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }
    Button(onClick = { showBottomSheet = true }) {
        Text("Select Time Range")
    }
    selectedOption?.let {
        Spacer(Modifier.height(16.dp))
        Text(text = stringResource(R.string.search_photo_msg, it.label), color = Color.White)
    }
    if (showBottomSheet) {
        TimeRangeBottomSheetDialog(
            onOptionSelected = { selectedOption = it },
            onDismiss = { showBottomSheet = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeRangeBottomSheetDialog(
    onOptionSelected: (TimeRangeOption) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            TimeRangeOption.toList().forEach { option ->
                ListItem(
                    headlineContent = { Text(option.label) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onOptionSelected(option)
                            onDismiss()
                        }
                )
            }
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
