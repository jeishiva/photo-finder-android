package com.experiment.facedetector.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.experiment.facedetector.R
import com.experiment.facedetector.domain.entities.FaceDetectedItem
import com.experiment.facedetector.navigation.AppRoute
import com.experiment.facedetector.ui.TimeRange
import com.experiment.facedetector.ui.components.StatusMessage
import com.experiment.facedetector.ui.theme.AndroidFaceDetectorTheme
import com.experiment.facedetector.ui.widgets.AppBar
import com.experiment.facedetector.viewmodel.HomeIntent
import com.experiment.facedetector.viewmodel.HomeUiState
import com.experiment.facedetector.viewmodel.HomeViewModel
import org.koin.androidx.compose.koinViewModel


@Composable
fun HomeScreen(navController: NavHostController, viewModel: HomeViewModel = koinViewModel()) {
    var selectedImage by remember { mutableStateOf<Uri?>(null) }
    var selectedOption by remember { mutableStateOf<TimeRange?>(null) }
    val uiState by viewModel.uiState.collectAsState()

    val actions = remember(navController, viewModel) {
        HomeUiModel.Actions(
            onBackClick = { navController.popBackStack(AppRoute.Splash.route, inclusive = true) },
            onImageSelected = { uri -> selectedImage = uri },
            onOptionSelected = { option -> selectedOption = option },
            onSearchClick = {
                if (selectedImage != null && selectedOption != null) {
                    viewModel.handleIntent(
                        HomeIntent.Search(selectedImage!!, selectedOption!!)
                    )
                }
            },
            isFaceSelected = { faceId ->
                viewModel.isFaceSelected(faceId)
            },
            toggleFaceSelection = { faceId ->
                viewModel.toggleFaceSelection(faceId)
            }
        )
    }
    val uiModel = HomeUiModel(
        selectedImage = selectedImage,
        selectedOption = selectedOption,
        actions = actions,
        homeUiState = uiState
    )
    HomeContent(uiModel = uiModel)
}


@Composable
fun HomeContent(uiModel: HomeUiModel) {
    AndroidFaceDetectorTheme {
        Scaffold(
            topBar = {
                AppBar(
                    stringResource(R.string.home_screen),
                    onClick = uiModel.actions.onBackClick
                )
            },
            containerColor = Color.Transparent,
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        CircularImageOrPlaceholder(uiModel.selectedImage, size = 120.dp)

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(
                            modifier = Modifier.weight(1f), // take available space
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            GalleryImagePicker(onImageSelected = uiModel.actions.onImageSelected)
                            TimeRangeSelectorScreen(onOptionSelected = uiModel.actions.onOptionSelected)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (uiModel.selectedOption != null) {
                        Text(
                            text = stringResource(
                                R.string.search_photo_msg,
                                uiModel.selectedOption.label
                            ),
                            color = Color.White,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    StatusMessage(
                        isLoading = uiModel.homeUiState.isLoading,
                        errorMessage = uiModel.homeUiState.errorMessage,
                        message = uiModel.homeUiState.message
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    if (uiModel.homeUiState.faceList.isNotEmpty()) {
                        FaceListSection(
                            faces = uiModel.homeUiState.faceList,
                            isFaceSelected = { faceId -> uiModel.actions.isFaceSelected(faceId) },
                            onFaceClick = { uiModel.actions.toggleFaceSelection(it) }
                        )
                    }
                }

                if (uiModel.selectedImage != null && uiModel.selectedOption != null) {
                    Button(
                        onClick = uiModel.actions.onSearchClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .align(Alignment.BottomCenter)
                    ) {
                        Text(stringResource(R.string.search))
                    }
                }

            }
        }
    }
}

@Composable
fun FaceListSection(
    faces: List<FaceDetectedItem>,
    isFaceSelected: (String) -> Boolean,
    onFaceClick: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        items(
            faces.size,
            key = { faces[it].faceId }
        ) { faceIndex ->
            val item = faces[faceIndex]
            FaceListItem(
                face = item,
                isSelected = isFaceSelected(item.faceId),
                onClick = { onFaceClick(item.faceId) }
            )
        }
    }
}

@Composable
fun FaceListItem(
    face: FaceDetectedItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(50.dp)
            .clip(CircleShape)
            .clickable { onClick() }
    ) {
        Image(
            bitmap = face.faceBitmap.asImageBitmap(),
            contentDescription = "Detected Face",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        if (isSelected) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Selected",
                tint = Color.Green,
                modifier = Modifier
                    .size(16.dp)
                    .align(Alignment.BottomCenter)
            )
        }
    }
}



@Composable
@Preview(showBackground = true)
fun HomeContentPreview() {
    HomeContent(
        uiModel = HomeUiModel(
            selectedImage = null,
            selectedOption = null,
            actions = HomeUiModel.Actions(),
            homeUiState = HomeUiState()
        )
    )
}

data class HomeUiModel(
    val selectedImage: Uri?,
    val selectedOption: TimeRange?,
    val actions: Actions,
    val homeUiState: HomeUiState
) {
    @Stable
    data class Actions(
        val onBackClick: () -> Unit = {},
        val onImageSelected: (Uri?) -> Unit = {},
        val onOptionSelected: (TimeRange) -> Unit = {},
        val onSearchClick: () -> Unit = {},
        val isFaceSelected: (String) -> Boolean = { false },
        val toggleFaceSelection: (String) -> Unit = {},
        )
}

@Composable
fun CircularImageOrPlaceholder(
    imageUri: Uri?, modifier: Modifier = Modifier, size: Dp = 100.dp
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
fun TimeRangeSelectorScreen(onOptionSelected: (TimeRange) -> Unit) {
    var showBottomSheet by remember { mutableStateOf(false) }
    Button(onClick = { showBottomSheet = true }) {
        Text("Select Time Range")
    }
    if (showBottomSheet) {
        TimeRangeBottomSheetDialog(
            onOptionSelected = onOptionSelected, onDismiss = { showBottomSheet = false })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeRangeBottomSheetDialog(
    onOptionSelected: (TimeRange) -> Unit, onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        onDismissRequest = onDismiss, sheetState = sheetState
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            TimeRange.toList().forEach { option ->
                ListItem(
                    headlineContent = { Text(option.label) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onOptionSelected(option)
                            onDismiss()
                        })
            }
        }
    }
}

@Composable
fun GalleryImagePicker(
    onImageSelected: (Uri?) -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(), onResult = { uri -> onImageSelected(uri) })
    Button(onClick = { launcher.launch("image/*") }) {
        Text("Select Photo")
    }
}
