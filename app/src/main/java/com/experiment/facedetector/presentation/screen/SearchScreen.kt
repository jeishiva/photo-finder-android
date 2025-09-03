package com.experiment.facedetector.presentation.screen

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import com.experiment.facedetector.presentation.entities.FaceSearchItemUi
import com.experiment.facedetector.presentation.entities.MediaItemUi
import com.experiment.facedetector.presentation.entities.SearchScreenParams
import com.experiment.facedetector.presentation.entities.SearchUiModel
import com.experiment.facedetector.presentation.entities.SearchUiState
import com.experiment.facedetector.presentation.theme.AndroidFaceDetectorTheme
import com.experiment.facedetector.presentation.theme.GradientStartMildGrey
import com.experiment.facedetector.presentation.theme.MildGray
import com.experiment.facedetector.presentation.widgets.AppBar
import com.experiment.facedetector.viewmodel.SearchViewModel.SearchIntent
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun SearchScreen(params: SearchScreenParams) {
    val navigationManager = params.navigationManager
    val searchViewModel = params.searchViewModel
    val selectPhotoViewModel = params.galleryViewModel
    val backClick: () -> Unit = remember {
        { navigationManager.navigateBack() }
    }
    val searchResultPagedItems = searchViewModel.pagedFaces.collectAsLazyPagingItems()
    LaunchedEffect(Unit) {
        val result = selectPhotoViewModel.getSelectedFaceSearchItems()
        LogManager.d("SearchViewModel", "Selected faces in search: ${result.size}")
        searchViewModel.handleIntent(SearchIntent.Start(selectPhotoViewModel.getSelectedFaceSearchItems()))
    }
    val uiState by searchViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val actions = remember(navigationManager, params.searchViewModel) {
        SearchUiModel.Actions(
            onBackClick = backClick,
            onThumbnailClicked = { mediaWithFacesUi ->
                searchViewModel.handleIntent(SearchIntent.ImageSelected(mediaWithFacesUi))
            },
            onPhotoPreviewDismissed = {
                searchViewModel.handleIntent(SearchIntent.PhotoPreviewHandled)
            },
            onShareClicked = { contentPath ->
                sharePhoto(context, contentPath)
            }
        )
    }
    val uiModel = SearchUiModel(
        actions = actions,
        uiState
    )
    ScreenContent(uiModel = uiModel, searchResultPagedItems)
}

@Composable
fun ScreenContent(
    uiModel: SearchUiModel,
    searchResultPagedItems: LazyPagingItems<MediaItemUi>,
) {
    AndroidFaceDetectorTheme {
        Scaffold(
            topBar = {
                AppBar(
                    title = stringResource(R.string.search_screen),
                    onBackClicked = uiModel.actions.onBackClick
                )
            },
            containerColor = Color.Transparent,
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                SearchHeaderCard(faces = uiModel.state.faceList)
                SearchResultSection(
                    searchResults = searchResultPagedItems,
                    onSearchItemClicked = uiModel.actions.onThumbnailClicked
                )
                PhotoPreviewSection(uiModel = uiModel)
            }
        }
    }
}

@Composable
fun SearchResultSection(
    searchResults: LazyPagingItems<MediaItemUi>,
    onSearchItemClicked: (MediaItemUi) -> Unit,
) {
    val isRefreshing = searchResults.loadState.refresh is LoadState.Loading
    val isAppending = searchResults.loadState.append is LoadState.Loading
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF4A495A)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        SearchResultsContent(
            searchResults = searchResults,
            isRefreshing = isRefreshing,
            isAppending = isAppending,
            onSearchItemClicked = onSearchItemClicked
        )
    }
}

@Composable
fun SearchHeaderCard(faces: List<FaceSearchItemUi>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF4A495A)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        FaceListSection(faces = faces)
    }
}

@Composable
private fun SearchResultsContent(
    searchResults: LazyPagingItems<MediaItemUi>,
    isRefreshing: Boolean,
    isAppending: Boolean,
    onSearchItemClicked: (MediaItemUi) -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        if (shouldShowInitialLoader(searchResults.itemCount, isRefreshing)) {
            InitialLoadingIndicator()
        } else {
            SearchResultsGrid(
                searchResults = searchResults,
                isAppending = isAppending,
                onSearchItemClicked = onSearchItemClicked
            )
        }
    }
}

private fun shouldShowInitialLoader(itemCount: Int, isLoading: Boolean): Boolean {
    return itemCount == 0 && isLoading
}

private fun shouldShowBottomLoader(itemCount: Int, isLoading: Boolean): Boolean {
    return isLoading && itemCount > 0
}

@Composable
private fun InitialLoadingIndicator() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Color.White)
    }
}

@Composable
private fun SearchResultsGrid(
    searchResults: LazyPagingItems<MediaItemUi>,
    isAppending: Boolean,
    onSearchItemClicked: (MediaItemUi) -> Unit,
) {
    println("isAppending in UI: $isAppending")
    val gridState = rememberLazyGridState()
    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(AppConfig.GRID_SIZE),
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = {
            items(
                count = searchResults.itemCount,
                key = { index -> searchResults[index]?.mediaId ?: "item-$index" }
            ) { index ->
                searchResults[index]?.let { item ->
                    ThumbnailItem(
                        item,
                        onThumbnailClicked = onSearchItemClicked
                    )
                }
            }
            if (shouldShowBottomLoader(searchResults.itemCount, isAppending)) {
                item(
                    key = "bottom-loader",
                    span = { GridItemSpan(3) }
                ) {
                    GridItemLoader()
                }
            }
        }
    )
}

@Composable
fun ThumbnailItem(
    item: MediaItemUi,
    onThumbnailClicked: (MediaItemUi) -> Unit,
) {
    val onClick = remember(item.mediaId) {
        {
            onThumbnailClicked(item)
        }
    }
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
    ) {
        val context = LocalContext.current
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(context)
                .data(item.thumbnailPath)
                .crossfade(true)
                .build(),
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
            }
        )
    }
}

@Composable
private fun GridItemLoader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = Color.White,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
@Preview(showBackground = true)
fun SearchScreenPreview() {
    val dummyPagingItems = remember {
        MutableStateFlow(PagingData.empty<MediaItemUi>())
    }.collectAsLazyPagingItems()
    ScreenContent(
        uiModel = SearchUiModel(
            actions = SearchUiModel.Actions(),
            state = SearchUiState()
        ),
        searchResultPagedItems = dummyPagingItems
    )
}

@Composable
fun FaceListSection(
    faces: List<FaceSearchItemUi>,
) {
    LogManager.d(SEARCH_SCREEN_TAG, "Faces: ${faces.size}")
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(12.dp)
    ) {
        items(faces.size, key = { faces[it].faceId }) { faceIndex ->
            val item = faces[faceIndex]
            FaceListItem(face = item)
        }
    }
}

@Composable
fun FaceListItem(face: FaceSearchItemUi) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
    ) {
        Image(
            bitmap = face.faceBitmap.asImageBitmap(),
            contentDescription = "Search Face",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
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

@Composable
fun PhotoPreviewSection(
    uiModel: SearchUiModel,
) {
    if (uiModel.state.previewPhotoPath != null) {
        PhotoPreviewSectionDialog(
            uiModel,
            uiModel.actions.onPhotoPreviewDismissed
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoPreviewSectionDialog(
    uiModel: SearchUiModel,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp),
                verticalArrangement = Arrangement.Top
            ) {
                ImagePreview(imagePath = uiModel.state.previewPhotoPath)
            }
            Button(
                onClick = {
                    onDismiss()
                    uiModel.state.previewPhotoPath?.let {
                        uiModel.actions.onShareClicked(it)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = GradientStartMildGrey
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    style = MaterialTheme.typography.titleMedium,
                    text = stringResource(R.string.share)
                )
            }
        }
    }
}

@Composable
fun ImagePreview(
    imagePath: String?,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
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
        if (imagePath != null) {
            AsyncImage(
                model = imagePath,
                contentDescription = "Full Image Preview",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(cornerRadius))
            )
        }
    }
}

fun sharePhoto(context: Context, contentPath: String?) {
    contentPath ?: return
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/*"
        putExtra(Intent.EXTRA_STREAM, contentPath.toUri())
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    val chooser = Intent.createChooser(intent, "Share Photo")
    context.startActivity(chooser)
}


const val SEARCH_SCREEN_TAG = "SearchScreen"


