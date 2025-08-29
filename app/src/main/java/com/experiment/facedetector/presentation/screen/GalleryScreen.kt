package com.experiment.facedetector.presentation.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.experiment.facedetector.R
import com.experiment.facedetector.common.LogManager
import com.experiment.facedetector.config.AppConfig
import com.experiment.facedetector.domain.entities.FaceDetectedItem
import com.experiment.facedetector.navigation.AppRoute
import com.experiment.facedetector.presentation.entities.GalleryScreenParams
import com.experiment.facedetector.presentation.entities.GalleryUiModel
import com.experiment.facedetector.presentation.entities.GalleryUiState
import com.experiment.facedetector.presentation.components.StatusMessage
import com.experiment.facedetector.presentation.entities.MediaItemUi
import com.experiment.facedetector.presentation.theme.AndroidFaceDetectorTheme
import com.experiment.facedetector.presentation.theme.GradientStartMildGrey
import com.experiment.facedetector.presentation.theme.MildGray
import com.experiment.facedetector.presentation.widgets.AppBar
import com.experiment.facedetector.viewmodel.HomeIntent
import com.experiment.facedetector.viewmodel.GalleryViewModel
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun GalleryScreen(
    params: GalleryScreenParams,
    viewModel: GalleryViewModel,
) {
    val navController = params.navController
    val uiState by viewModel.uiState.collectAsState()
    val selectedFaceIds by viewModel.selectedFaceIds.collectAsState()
    val mediaPagedItems = viewModel.pagedSyncedMediaFlow.collectAsLazyPagingItems()
    val actions = remember(navController, viewModel) {
        GalleryUiModel.Actions(onImageSelected = { uri ->
            viewModel.handleIntent(HomeIntent.Search(uri))
        }, onSearchClick = {
            viewModel.triggerSearch()
        }, toggleFaceSelection = { faceId ->
            viewModel.toggleFaceSelection(faceId)
        }, onFaceSelectionSheetShown = {
            viewModel.markShowSelectedFacesHandled()
        }, onThumbnailClicked = { mediaItemUi ->
            viewModel.handleIntent(
                HomeIntent.ImageSelected(mediaItemUi)
            )
        })
    }
    val uiModel = GalleryUiModel(
        actions = actions, state = uiState
    )
    GalleryContent(
        uiModel = uiModel,
        selectedFaceIds = selectedFaceIds,
        mediaPagedItems = mediaPagedItems,
    )
    // navigation to search screen
    LaunchedEffect(uiState.navigateToSearch) {
        if (uiState.navigateToSearch) {
            LogManager.d(TAG, "activeSessionId: ${uiState.sessionId}")
            navController.navigate(
                AppRoute.Search.createRoute(uiState.sessionId!!)
            )
            viewModel.markNavigationHandled()
        }
    }
}

@Composable
private fun GalleryGrid(
    mediaPagedItems: LazyPagingItems<MediaItemUi>,
    onThumbnailClicked: (MediaItemUi) -> Unit,
) {
    val gridState = rememberLazyGridState()
    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(AppConfig.GRID_SIZE),
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = {
            items(
                count = mediaPagedItems.itemCount,
                key = { index -> mediaPagedItems[index]?.id ?: "item-$index" }) { index ->
                mediaPagedItems[index]?.let { item ->
                    GalleryThumbnailItem(
                        item = item, onThumbnailClicked = onThumbnailClicked
                    )
                }
            }
        })
}

@Composable
fun GalleryThumbnailItem(
    item: MediaItemUi,
    onThumbnailClicked: (MediaItemUi) -> Unit,
) {
    val onClick = remember(item.id) {
        { onThumbnailClicked(item) }
    }
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
    ) {
        val context = LocalContext.current
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(context).data(item.thumbnailUri).crossfade(true).build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick),
            loading = {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(MildGray)
                )
            },
            error = {
                Image(
                    painter = painterResource(id = android.R.drawable.stat_notify_error),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Inside
                )
            })
    }
}

@Composable
fun GalleryContent(
    uiModel: GalleryUiModel,
    selectedFaceIds: Set<String>,
    mediaPagedItems: LazyPagingItems<MediaItemUi>,
) {
    AndroidFaceDetectorTheme {
        Scaffold(
            topBar = {
                AppBar(
                    stringResource(R.string.app_name),
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
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    GalleryGrid(
                        mediaPagedItems = mediaPagedItems, uiModel.actions.onThumbnailClicked
                    )
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
                        onFaceClick = { uiModel.actions.toggleFaceSelection(it) },
                        onDismiss = uiModel.actions.onFaceSelectionSheetShown
                    )
                }
                Column(
                    modifier = Modifier
                        .wrapContentSize()
                        .align(Alignment.BottomEnd)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    SelectPhotoIcon(uiModel.actions.onImageSelected)
                }
            }

        }
    }
}

@Composable
fun FaceListSection(
    faces: List<FaceDetectedItem>, selectedFaceIds: Set<String>, onFaceClick: (String) -> Unit,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(16.dp)
    ) {
        items(
            faces.size, key = { faces[it].faceId }) { faceIndex ->
            val item = faces[faceIndex]
            val isSelected = selectedFaceIds.contains(item.faceId)
            FaceListItem(
                face = item, isSelected = isSelected, onClick = {
                    onFaceClick(item.faceId)
                })
        }
    }
}

@Composable
fun FaceListItem(
    face: FaceDetectedItem, isSelected: Boolean, onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(72.dp)
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
    val dummyPagingItems = remember {
        MutableStateFlow(PagingData.empty<MediaItemUi>())
    }.collectAsLazyPagingItems()
    GalleryContent(
        uiModel = GalleryUiModel(
            actions = GalleryUiModel.Actions(),
            state = GalleryUiState(),
        ), selectedFaceIds = emptySet(), mediaPagedItems = dummyPagingItems
    )
}

@Composable
fun ImageOrPlaceholderRoundedFullWidth(
    imageUri: Uri?,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height = 270.dp)
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
fun FaceDetectedSheetSection(
    uiModel: GalleryUiModel,
    selectedFaceIds: Set<String>,
    onFaceClick: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    if (uiModel.showSelectedFaces) {
        FaceDetectedBottomSheetDialog(uiModel, selectedFaceIds, onFaceClick = {
            onFaceClick(it)
        }, onDismiss = {
            onDismiss()
        })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaceDetectedBottomSheetDialog(
    uiModel: GalleryUiModel,
    selectedFaceIds: Set<String>,
    onFaceClick: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .wrapContentHeight()
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            ImageOrPlaceholderRoundedFullWidth(
                imageUri = uiModel.state.selectedImageUri,
            )
            Spacer(modifier = Modifier.height(16.dp))
            FaceListSection(
                faces = uiModel.state.faceList,
                selectedFaceIds = selectedFaceIds,
                onFaceClick = onFaceClick
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    onDismiss()
                    uiModel.actions.onSearchClick()
                },
                enabled = selectedFaceIds.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GradientStartMildGrey
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 16.dp) // This creates margin around button
            ) {
                Text(
                    style = MaterialTheme.typography.titleMedium,
                    text = stringResource(R.string.search)
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

const val TAG = "HomeScreen"
