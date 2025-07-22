package com.experiment.facedetector.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.runtime.remember
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import coil.compose.AsyncImage
import com.experiment.facedetector.R
import com.experiment.facedetector.data.local.entities.MediaWithFaces
import com.experiment.facedetector.domain.entities.FaceSearchItem
import com.experiment.facedetector.ui.SearchScreenParams
import com.experiment.facedetector.ui.SearchUiModel
import com.experiment.facedetector.ui.SearchUiState
import com.experiment.facedetector.ui.theme.AndroidFaceDetectorTheme
import com.experiment.facedetector.ui.theme.MildGray
import com.experiment.facedetector.ui.widgets.AppBar

@Composable
fun SearchScreen(searchScreenParams: SearchScreenParams) {
    val navController = searchScreenParams.navController
    val searchViewModel = searchScreenParams.searchViewModel
    val backClick: () -> Unit = remember {
        { navController.popBackStack() }
    }
    val searchResultPagedItems = searchViewModel.pagedSearchFlow.collectAsLazyPagingItems()
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
                    onClick = uiModel.actions.onBackClick
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
                Spacer(modifier = Modifier.height(8.dp))
                SearchResultsGrid(searchResults = searchResultPagedItems)
            }
        }
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

            ) {
            Text(
                text = stringResource(R.string.searching_for_faces),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))
            FaceListSection(faces = faces)
        }
    }
}

@Composable
fun SearchResultsGrid(
    searchResults: LazyPagingItems<MediaWithFaces>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF4A495A)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Matching Results",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                content = {
                    items(searchResults.itemCount) { index ->
                        val item = searchResults[index]
                        if (item != null) {
                            println("thumbnailUri: ${item.media.thumbnailUri}")
                            ThumbnailItem(thumbnailUri = item.media.thumbnailUri)
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun ThumbnailItem(thumbnailUri: String) {
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
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(16.dp)
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
            .size(50.dp)
            .clip(CircleShape)
    ) {
        AsyncImage(
            model = face.thumbnailPath,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    }
}

