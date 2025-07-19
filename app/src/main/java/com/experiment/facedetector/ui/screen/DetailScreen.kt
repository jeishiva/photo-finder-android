package com.experiment.facedetector.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.trace
import androidx.navigation.NavHostController
import com.experiment.facedetector.R
import com.experiment.facedetector.navigation.AppRoute
import com.experiment.facedetector.ui.theme.AndroidFaceDetectorTheme
import com.experiment.facedetector.ui.widgets.AppBar
import com.experiment.facedetector.viewmodel.DetailsViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun DetailScreen(navController: NavHostController) {
    trace("DetailScreenComposition") {
        val context = LocalContext.current
        val viewModel: DetailsViewModel = koinViewModel()
        val backClick = remember { { navController.navigate(AppRoute.HomeScreen.route) } }
        AndroidFaceDetectorTheme {
            Scaffold(
                topBar = {
                    AppBar(
                        title = context.getString(R.string.details_screen),
                        onClick = backClick
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
                    verticalArrangement = Arrangement.Center
                ) {
                    TextContent()
                }
            }
        }
    }
}

/**
 * A test screen composable that displays a centered text.
 * Primarily used for testing the UI layout and theming.
 */
@Composable
@Preview(showBackground = true)
fun TextContent() {
    Text(
        text = stringResource(R.string.details_screen),
        style = MaterialTheme.typography.headlineLarge,
        color = Color.White
    )
}