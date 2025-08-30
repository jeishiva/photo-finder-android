package com.experiment.facedetector.presentation.screen

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.experiment.facedetector.R
import com.experiment.facedetector.common.LogManager
import com.experiment.facedetector.config.AppConfig
import com.experiment.facedetector.presentation.entities.FaceSearchItemUi
import com.experiment.facedetector.presentation.entities.MediaItemUi
import com.experiment.facedetector.presentation.entities.SearchScreenParams
import com.experiment.facedetector.presentation.entities.SearchUiModel
import com.experiment.facedetector.presentation.entities.SearchUiState
import com.experiment.facedetector.presentation.theme.AndroidFaceDetectorTheme
import com.experiment.facedetector.presentation.theme.MildGray
import com.experiment.facedetector.presentation.widgets.AppBar
import com.experiment.facedetector.viewmodel.SearchViewModel.SearchIntent
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun SearchScreen(searchScreenParams: SearchScreenParams) {
    val navController = searchScreenParams.navController
    val searchViewModel = searchScreenParams.searchViewModel
    val selectPhotoViewModel = searchScreenParams.selectPhotoViewModel
    val backClick: () -> Unit = remember {
        { navController.popBackStack() }
    }
    val searchResultPagedItems = searchViewModel.pagedFaces.collectAsLazyPagingItems()
    LaunchedEffect(Unit) {
        val result = selectPhotoViewModel.getSearchItems()
        LogManager.d("SearchViewModel", "Selected faces in search: ${result.size}")
        searchViewModel.handleIntent(SearchIntent.Start(selectPhotoViewModel.getSearchItems()))
    }
    val uiState by searchViewModel.uiState.collectAsState()
    val actions = remember(navController, searchScreenParams.searchViewModel) {
        SearchUiModel.Actions(
            onBackClick = backClick,
            onThumbnailClicked = { mediaWithFacesUi ->
                searchViewModel.handleIntent(SearchIntent.ImageSelected(mediaWithFacesUi))
            },
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FaceListSection(faces = faces)
        }
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
    val context = LocalContext.current
    val onClick = remember(item.mediaId) {
        {
            Toast.makeText(context, "Hello from Compose!", Toast.LENGTH_SHORT).show()
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
                .data(item.thumbnailUri)
                .crossfade(true)          // optional
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
    LogManager.d("SearchViewModel", "Faces: ${faces.size}")
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
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

