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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.experiment.facedetector.R
import com.experiment.facedetector.common.LogManager
import com.experiment.facedetector.domain.entities.FaceDetectedItem
import com.experiment.facedetector.navigation.AppRoute
import com.experiment.facedetector.ui.HomeScreenParams
import com.experiment.facedetector.ui.HomeUiModel
import com.experiment.facedetector.ui.HomeUiState
import com.experiment.facedetector.ui.TimeRange
import com.experiment.facedetector.ui.components.StatusMessage
import com.experiment.facedetector.ui.theme.AndroidFaceDetectorTheme
import com.experiment.facedetector.ui.theme.Emerald
import com.experiment.facedetector.ui.theme.GradientStartMildGrey
import com.experiment.facedetector.ui.widgets.AppBar
import com.experiment.facedetector.viewmodel.HomeIntent
import com.experiment.facedetector.viewmodel.HomeViewModel


@Composable
fun HomeScreen(
    homeScreenParams: HomeScreenParams, viewModel: HomeViewModel
) {
    var selectedOption by remember { mutableStateOf<TimeRange>(TimeRange.OneMonth) }
    val navController = homeScreenParams.navController
    val uiState by viewModel.uiState.collectAsState()
    val selectedFaceIds by viewModel.selectedFaceIds.collectAsState()
    val actions = remember(navController, viewModel) {
        HomeUiModel.Actions(onImageSelected = { uri ->
            viewModel.setSelectedImage(uri)
        }, onOptionSelected = { option ->
            selectedOption = option
        }, onSearchClick = {
            viewModel.saveSelectedFaces()
        }, toggleFaceSelection = { faceId ->
            viewModel.toggleFaceSelection(faceId)
        })
    }
    LaunchedEffect(uiState.selectedImageUri) {
        uiState.selectedImageUri?.let { selectedUri ->
            viewModel.handleIntent(
                HomeIntent.Search(selectedUri, selectedOption)
            )
        }
    }
    LaunchedEffect(uiState.navigateToSearch) {
        if (uiState.navigateToSearch) {
            LogManager.d(TAG, "activeSessionId: ${uiState.sessionId}")
            navController.navigate(
                AppRoute.Search.createRoute(uiState.sessionId!!)
            )
            viewModel.markNavigationHandled()
        }
    }
    val uiModel = HomeUiModel(
        selectedOption = selectedOption, actions = actions, state = uiState
    )
    HomeContent(uiModel = uiModel, selectedFaceIds)
}

@Composable
fun HomeContent(uiModel: HomeUiModel, selectedFaceIds: Set<String>) {
    AndroidFaceDetectorTheme {
        Scaffold(
            topBar = {
                AppBar(
                    stringResource(R.string.search_photos),
                )
            }, containerColor = Color.Transparent, modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Column(
                    modifier = Modifier
                        .wrapContentSize()
                        .align(Alignment.BottomEnd)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    TimeRangeIcon(onOptionSelected = uiModel.actions.onOptionSelected)
                    SelectPhotoIcon(uiModel.actions.onImageSelected)
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Row(
                    modifier = Modifier.weight(0.4f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ImageOrPlaceholderRoundedFullWidth(
                        imageUri = uiModel.state.selectedImageUri
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(0.5f)
                        .clip(RoundedCornerShape(16.dp)),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    StatusMessage(
                        isLoading = uiModel.state.isLoading,
                        errorMessage = uiModel.state.errorMessage,
                        message = uiModel.state.message
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    FaceDetectedSheetSection(
                        uiModel = uiModel,
                        selectedFaceIds,
                        onFaceClick = { uiModel.actions.toggleFaceSelection(it) })
                }
            }
        }
    }
}

@Composable
fun FaceListSection(
    faces: List<FaceDetectedItem>,
    selectedFaceIds: Set<String>,
    onFaceClick: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        items(
            faces.size, key = { faces[it].faceId }) { faceIndex ->
            val item = faces[faceIndex]
            val isSelected = selectedFaceIds.contains(item.faceId)
            FaceListItem(
                face = item,
                isSelected = isSelected,
                onClick = {
                    onFaceClick(item.faceId)
                }
            )
        }
    }
}

@Composable
fun FaceListItem(
    face: FaceDetectedItem, isSelected: Boolean, onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .clickable { onClick() }) {
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
            actions = HomeUiModel.Actions(),
            state = HomeUiState(),
        ), selectedFaceIds = emptySet()
    )
}

@Composable
fun ImageOrPlaceholderRoundedFullWidth(
    imageUri: Uri?, modifier: Modifier = Modifier, cornerRadius: Dp = 24.dp
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(
                RoundedCornerShape(
                    topStart = cornerRadius,
                    topEnd = cornerRadius,
                    bottomStart = 0.dp,
                    bottomEnd = 0.dp
                )
            )
            .background(Color.Gray), contentAlignment = Alignment.Center
    ) {
        if (imageUri != null) {
            AsyncImage(
                model = imageUri,
                contentDescription = "Selected Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(cornerRadius))
            )
        }
    }
}

@Composable
fun SelectPhotoIcon(onImageSelected: (Uri?) -> Unit) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(), onResult = { uri ->
            onImageSelected(uri)
        })
    Icon(
        imageVector = Icons.Default.Add,
        contentDescription = "Select Image",
        tint = Color.White.copy(alpha = 0.85f),
        modifier = Modifier
            .size(48.dp)
            .clickable { launcher.launch("image/*") }
            .background(Color.Black.copy(alpha = 0.4f), shape = CircleShape)
            .padding(8.dp))
}

@Composable
fun TimeRangeIcon(onOptionSelected: (TimeRange) -> Unit) {
    var showBottomSheet by remember { mutableStateOf(false) }
    Icon(
        imageVector = Icons.Default.DateRange,
        contentDescription = "Select Image",
        tint = Color.White.copy(alpha = 0.85f),
        modifier = Modifier
            .size(48.dp)
            .clickable { showBottomSheet = true }
            .background(Color.Black.copy(alpha = 0.4f), shape = CircleShape)
            .padding(8.dp))
    if (showBottomSheet) {
        TimeRangeBottomSheetDialog(
            onOptionSelected = onOptionSelected, onDismiss = { showBottomSheet = false })
    }
}

@Composable
fun FaceDetectedSheetSection(
    uiModel: HomeUiModel,
    selectedFaceIds: Set<String>,
    onFaceClick: (String) -> Unit,
) {
    if (uiModel.hasFaces.not()) {
        return
    }
    var showBottomSheet by remember { mutableStateOf(true) }
    if (uiModel.hasFaces && showBottomSheet) {
        FaceDetectedBottomSheetDialog(
            uiModel,
            selectedFaceIds,
            onFaceClick = {
                onFaceClick(it)
            },
            onDismiss = {
                showBottomSheet = false
            })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaceDetectedBottomSheetDialog(
    uiModel: HomeUiModel,
    selectedFaceIds: Set<String>,
    onFaceClick: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            FaceListSection(
                faces = uiModel.state.faceList,
                selectedFaceIds = selectedFaceIds,
                onFaceClick = onFaceClick
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    onDismiss()
                    uiModel.actions.onSearchClick()
                },
                enabled = selectedFaceIds.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedFaceIds.isNotEmpty()) {
                        Emerald
                    } else {
                        GradientStartMildGrey
                    }
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 16.dp) // This creates margin around button
            ) {
                Text(stringResource(R.string.search))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeRangeBottomSheetDialog(
    onOptionSelected: (TimeRange) -> Unit, onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
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

const val TAG = "HomeScreen"
