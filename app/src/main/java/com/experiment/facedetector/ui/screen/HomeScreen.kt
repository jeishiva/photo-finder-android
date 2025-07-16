package com.experiment.facedetector.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.experiment.facedetector.R
import com.experiment.facedetector.navigation.AppRoute

@Composable
fun HomeScreen(navController: NavHostController) {
    val backClick = remember { { navController.navigate(AppRoute.Splash.route) } }
    AppScreen(
        content = { paddingValues -> HomeContent(
            navController = navController,
            paddingValues = paddingValues
        ) },
        title = stringResource(R.string.home_screen),
        onClick = backClick
    )
}

@Composable
fun HomeContent(paddingValues: PaddingValues, navController: NavHostController) {
    val launchDetail = remember { { navController.navigate(AppRoute.DetailScreen.route) } }
    Column(
        modifier = Modifier.fillMaxSize().padding(paddingValues),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.home_screen),
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White
        )
        Button(onClick = launchDetail, modifier = Modifier.padding(top = 16.dp)) {
            Text("Launch Detail Screen")
        }
    }
}