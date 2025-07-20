package com.experiment.facedetector.navigation

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.experiment.facedetector.ui.HomeScreenParams
import com.experiment.facedetector.ui.SearchScreenParams
import com.experiment.facedetector.ui.screen.SearchScreen
import com.experiment.facedetector.ui.screen.FullImageScreen
import com.experiment.facedetector.ui.screen.GalleryScreen
import com.experiment.facedetector.ui.screen.HomeScreen
import com.experiment.facedetector.ui.screen.SplashScreen
import com.experiment.facedetector.viewmodel.HomeViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.java.KoinJavaComponent.getKoin

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController, startDestination = AppRoute.HomeScreen.route) {
        composable(AppRoute.Splash.route) {
            SplashScreen(navController)
        }
        composable(AppRoute.Gallery.route) {
            GalleryScreen(navController = navController)
        }
        composable(AppRoute.HomeScreen.route) {
            val viewModel : HomeViewModel = koinViewModel()
            val homeScreenParams = HomeScreenParams(
                navController = navController,
                viewModel
            )
            HomeScreen(homeScreenParams, viewModel)
        }
        composable(AppRoute.SearchScreen.route) {
            val searchScreenParams = SearchScreenParams(
                navController = navController,
                searchViewModel = getKoin().get(),
            )
            SearchScreen(searchScreenParams)
        }

        composable(
            route = AppRoute.FullImage.route,
            arguments = listOf(navArgument("mediaId") { type = NavType.LongType })
        ) {
            FullImageScreen(navController)
        }
    }
}

sealed class NavigationScope(val name: String) {
    object Home : NavigationScope("HomeNavScope")
}

sealed class AppRoute(val route: String) {
    object Splash : AppRoute("splash")
    object Gallery : AppRoute("gallery")
    object HomeScreen : AppRoute("HomeScreen")
    object SearchScreen : AppRoute("SearchScreen")
    object FullImage : AppRoute("fullImage/{mediaId}") {
        fun createRoute(mediaId: Long): String = "fullImage/$mediaId"
    }
}