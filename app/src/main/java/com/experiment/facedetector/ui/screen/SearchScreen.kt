package com.experiment.facedetector.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.experiment.facedetector.R
import com.experiment.facedetector.domain.entities.FaceDetectedItem
import com.experiment.facedetector.domain.entities.FaceSearchItem
import com.experiment.facedetector.ui.SearchScreenParams
import com.experiment.facedetector.ui.SearchUiModel
import com.experiment.facedetector.ui.SearchUiState
import com.experiment.facedetector.ui.theme.AndroidFaceDetectorTheme
import com.experiment.facedetector.ui.widgets.AppBar

@Composable
fun SearchScreen(searchScreenParams: SearchScreenParams) {
    val navController = searchScreenParams.navController
    val searchViewModel = searchScreenParams.searchViewModel
    val backClick: () -> Unit = remember {
        { navController.popBackStack() }
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
    ScreenContent(uiModel = uiModel)
}

@Composable
fun ScreenContent(
    uiModel: SearchUiModel,
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
                FaceListSection(faces = uiModel.state.faceList)
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun SearchScreenPreview() {
    ScreenContent(
        uiModel = SearchUiModel(
            actions = SearchUiModel.Actions(),
            state = SearchUiState()
        )
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

