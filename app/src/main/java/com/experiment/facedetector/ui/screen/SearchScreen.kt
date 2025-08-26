package com.experiment.facedetector.ui.screen

import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.experiment.facedetector.R
import com.experiment.facedetector.common.LogManager
import com.experiment.facedetector.data.local.entities.MediaWithFaces
import com.experiment.facedetector.domain.entities.FaceSearchItem
import com.experiment.facedetector.ui.SearchScreenParams
import com.experiment.facedetector.ui.SearchUiModel
import com.experiment.facedetector.ui.SearchUiState
import com.experiment.facedetector.ui.theme.AndroidFaceDetectorTheme
import com.experiment.facedetector.ui.theme.MildGray
import com.experiment.facedetector.ui.widgets.AppBar
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
    val searchResultPagedItems = searchViewModel.pagedSearchFlow.collectAsLazyPagingItems()
    LaunchedEffect(Unit) {
        val result = selectPhotoViewModel.getSearchItems()
        LogManager.d("SearchViewModel", "Selected faces in search: ${result.size}")
        searchViewModel.handleIntent(SearchIntent.Start(selectPhotoViewModel.getSearchItems()))
    }
    val uiState by searchViewModel.uiState.collectAsState()
    val actions = remember(navController, searchScreenParams.searchViewModel) {
        SearchUiModel.Actions(
            onBackClick = backClick
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
    searchResultPagedItems: LazyPagingItems<MediaWithFaces>,
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
            Column(modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                SearchHeaderCard(faces = uiModel.state.faceList)
                SearchResultSection(
                    searchResults = searchResultPagedItems,
                    isLoading =  searchResultPagedItems.loadState.append == LoadState.Loading
                )
            }
        }
    }
}

@Composable
fun SearchResultSection(
    searchResults: LazyPagingItems<MediaWithFaces>,
    isLoading: Boolean
) {
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
            isLoading = isLoading
        )
    }
}

@Composable
fun SearchHeaderCard(faces: List<FaceSearchItem>) {
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
    searchResults: LazyPagingItems<MediaWithFaces>,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        if (shouldShowInitialLoader(searchResults.itemCount, isLoading)) {
            InitialLoadingIndicator()
        } else {
            SearchResultsGrid(searchResults, isLoading)
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
    searchResults: LazyPagingItems<MediaWithFaces>,
    isLoading: Boolean
) {
    println("isAppending in UI: $isLoading")
    val gridState = rememberLazyGridState()
    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = {
            items(
                count = searchResults.itemCount,
                key = { index -> searchResults[index]?.media?.mediaId ?: "item-$index" }
            ) { index ->
                searchResults[index]?.let { item ->
                    ThumbnailItem(thumbnailUri = item.media.thumbnailUri)
                }
            }
            // Bottom loader as separate item
            if (shouldShowBottomLoader(searchResults.itemCount, isLoading)) {
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
fun ThumbnailItem(thumbnailUri: String?) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
    ) {
        AsyncImage(
            model = thumbnailUri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            placeholder = ColorPainter(MildGray),
            error = painterResource(id = android.R.drawable.stat_notify_error),
            onError = { error ->
                println("Error loading image: $error.message")
            },
        )
    }
}

@Composable
@Preview(showBackground = true)
fun SearchScreenPreview() {
    val dummyPagingItems = remember {
        MutableStateFlow(PagingData.empty<MediaWithFaces>())
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
    faces: List<FaceSearchItem>,
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
fun FaceListItem(face: FaceSearchItem) {
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

