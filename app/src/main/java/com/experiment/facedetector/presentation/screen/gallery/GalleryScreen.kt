package com.experiment.facedetector.presentation.screen.gallery

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.experiment.facedetector.R
import com.experiment.facedetector.common.logging.LogManager
import com.experiment.facedetector.config.AppConfig
import com.experiment.facedetector.domain.entities.FaceDetectedItem
import com.experiment.facedetector.presentation.components.StatusMessage
import com.experiment.facedetector.presentation.model.MediaItemUi
import com.experiment.facedetector.presentation.screen.gallery.model.GalleryActions
import com.experiment.facedetector.presentation.screen.gallery.model.GalleryNavigationEvent
import com.experiment.facedetector.presentation.screen.gallery.model.GalleryScreenArgs
import com.experiment.facedetector.presentation.screen.gallery.model.GalleryUiModel
import com.experiment.facedetector.presentation.screen.gallery.model.GalleryUiState
import com.experiment.facedetector.presentation.theme.AndroidFaceDetectorTheme
import com.experiment.facedetector.presentation.theme.GradientStartMildGrey
import com.experiment.facedetector.presentation.theme.MildGray
import com.experiment.facedetector.presentation.components.AppTopBar
import com.experiment.facedetector.viewmodel.GalleryIntent
import com.experiment.facedetector.viewmodel.GalleryViewModel
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun GalleryScreen(
    params: GalleryScreenArgs,
    viewModel: GalleryViewModel,
) {
    val navigationManager = params.navigationManager
    val selectedFaceIds by viewModel.selectedFaceIds.collectAsState()
    val mediaPagedItems = viewModel.pagedSyncedMediaFlow.collectAsLazyPagingItems()

    val uiState by viewModel.uiState.collectAsState()
    val actions = remember(navigationManager, viewModel) {
        GalleryActions(
            onExternalImageSelected = imageSelected@{ imagePath ->
                imagePath ?: return@imageSelected
                viewModel.handleIntent(GalleryIntent.ImageSelected(imagePath))
            }, onSearchClick = {
                viewModel.handleIntent(GalleryIntent.LaunchSearch)
            }, toggleFaceSelection = { faceId ->
                viewModel.toggleFaceSelection(faceId)
            }, onFaceSelectionSheetShown = {
                viewModel.handleIntent(GalleryIntent.ResetImageSelection)
            }, onThumbnailClicked = thumbnailClicked@{ mediaItemUi ->
                mediaItemUi.contentPath ?: return@thumbnailClicked
                viewModel.handleIntent(
                    GalleryIntent.ImageSelected(mediaItemUi.contentPath)
                )
            },
            onRefreshClicked = {
                viewModel.handleIntent(GalleryIntent.GalleryRefreshed)
                mediaPagedItems.refresh()
            }
        )
    }
    val uiModel = GalleryUiModel(
        actions = actions,
        state = uiState
    )
    LaunchedEffect(Unit) {
        viewModel.navigationEvents.collect { event ->
            when (event) {
                is GalleryNavigationEvent.ToSearch -> {
                    LogManager.d(GALLERY_SCREEN_TAG, "activeSessionId: ${event.sessionId}")
                    navigationManager.navigateToSearch(event.sessionId)
                }
            }
        }
    }
    GalleryContent(
        uiModel = uiModel,
        selectedFaceIds = selectedFaceIds,
        mediaPagedItems = mediaPagedItems,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GalleryGrid(
    mediaPagedItems: LazyPagingItems<MediaItemUi>,
    onThumbnailClicked: (MediaItemUi) -> Unit,
    onRefreshClicked: () -> Unit,
) {
    val gridState = rememberLazyGridState()
    val pullToRefreshState = rememberPullToRefreshState()
    val isRefreshing = mediaPagedItems.loadState.refresh is LoadState.Loading
    PullToRefreshBox(
        state = pullToRefreshState,
        isRefreshing = isRefreshing,
        onRefresh = {
            onRefreshClicked()
        }
    ) {
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(AppConfig.GRID_SIZE),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(
                count = mediaPagedItems.itemCount,
                key = { index -> mediaPagedItems[index]?.mediaId ?: "placeholder-$index" }
            ) { index ->
                val item = mediaPagedItems[index] ?: return@items
                GalleryThumbnailItem(
                    item = item,
                    onThumbnailClicked = onThumbnailClicked
                )
            }
        }
    }
}


@Composable
fun RefreshButton(
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Button(
            onClick = onRefresh,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp) // lift above nav bar
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refresh"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Refresh")
        }
    }
}

@Composable
fun GalleryThumbnailItem(
    item: MediaItemUi,
    onThumbnailClicked: (MediaItemUi) -> Unit,
) {
    val onClick = remember(item.mediaId) {
        { onThumbnailClicked(item) }
    }
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
    ) {
        val context = LocalContext.current
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(context).data(item.thumbnailPath).crossfade(true).build(),
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
                AppTopBar(
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
                        mediaPagedItems = mediaPagedItems,
                        uiModel.actions.onThumbnailClicked,
                        uiModel.actions.onRefreshClicked
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
                    SelectPhotoIcon(uiModel.actions.onExternalImageSelected)
                }
                if (uiModel.state.showRefreshButton) {
                    RefreshButton(onRefresh = uiModel.actions.onRefreshClicked)
                }
            }
        }
    }
}

@Composable
fun FaceListSection(
    faces: List<FaceDetectedItem>,
    selectedFaceIds: Set<String>,
    onFaceClick: (String) -> Unit,
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
    face: FaceDetectedItem,
    isSelected: Boolean,
    onClick: () -> Unit,
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
            actions = GalleryActions(),
            state = GalleryUiState(),
        ), selectedFaceIds = emptySet(),
        mediaPagedItems = dummyPagingItems
    )
}

@Composable
fun PreviewImage(
    imageUri: Uri?,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.5f)
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
                contentDescription = "Full Image Preview",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(cornerRadius))
            )
        }
    }
}

@Composable
fun SelectPhotoIcon(onImageSelected: (String?) -> Unit) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(), onResult = { uri ->
            onImageSelected(uri.toString())
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
    if (uiModel.state.faceExtractionState.showFaceSelectionSheet) {
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
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val state = uiModel.state.faceExtractionState
    val pathUri = state.selectedImagePath?.toUri()

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        when {
            state.isInProgress -> FaceExtractionInProgressContent()
            state.isFaceNotFound -> FaceExtractionEmptyContent(pathUri)
            state.canShowFaces -> FaceExtractionFacesContent(
                uiModel = uiModel,
                selectedFaceIds = selectedFaceIds,
                onFaceClick = onFaceClick,
                onDismiss = onDismiss
            )
        }
    }
}

@Composable
fun FaceExtractionEmptyContent(imagePath: Uri?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PreviewImage(imageUri = imagePath)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.no_faces_found),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun FaceExtractionFacesContent(
    uiModel: GalleryUiModel,
    selectedFaceIds: Set<String>,
    onFaceClick: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    Column(
        modifier = Modifier
            .wrapContentHeight()
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        PreviewImage(
            imageUri = uiModel.state.faceExtractionState.selectedImagePath?.toUri(),
        )
        Spacer(modifier = Modifier.height(16.dp))
        FaceListSection(
            faces = uiModel.state.faceExtractionState.faceList,
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
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.search),
                style = MaterialTheme.typography.titleMedium
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun FaceExtractionInProgressContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.finding_faces),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}


const val GALLERY_SCREEN_TAG = "GalleryScreen"