package com.experiment.facedetector.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.experiment.facedetector.R
import com.experiment.facedetector.navigation.AppRoute
import com.experiment.facedetector.viewmodel.TestViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun TestScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: TestViewModel = koinViewModel()
    AppScreen(
        content = { paddingValues -> TestContent(paddingValues = paddingValues) },
        title = context.getString(R.string.test_screen),
        onClick = { navController.navigate(AppRoute.Splash.route) }
    )
}

/**
 * A test screen composable that displays a centered text.
 * Primarily used for testing the UI layout and theming.
 */
@Composable
@Preview(showBackground = true)
fun TestContent(paddingValues: PaddingValues = PaddingValues(0.dp)) {
    Column(
        modifier = Modifier.fillMaxSize().padding(paddingValues),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.test_screen),
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White
        )
    }
}