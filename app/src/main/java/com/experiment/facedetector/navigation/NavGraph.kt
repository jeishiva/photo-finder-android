package com.experiment.facedetector.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.experiment.facedetector.ui.HomeScreenParams
import com.experiment.facedetector.ui.SearchScreenParams
import com.experiment.facedetector.ui.screen.FullImageScreen
import com.experiment.facedetector.ui.screen.GalleryScreen
import com.experiment.facedetector.ui.screen.HomeScreen
import com.experiment.facedetector.ui.screen.SearchScreen
import com.experiment.facedetector.ui.screen.SplashScreen
import com.experiment.facedetector.viewmodel.HomeViewModel
import com.experiment.facedetector.viewmodel.SearchViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController, startDestination = AppRoute.Splash.route) {
        composable(AppRoute.Splash.route) {
            SplashScreen(navController)
        }
        composable(AppRoute.Gallery.route) {
            GalleryScreen(navController = navController)
        }
        composable(AppRoute.Home.route) {
            val viewModel : HomeViewModel = koinViewModel()
            val homeScreenParams = HomeScreenParams(
                navController = navController,
                viewModel
            )
            HomeScreen(homeScreenParams, viewModel)
        }

        composable(
            route = AppRoute.Search.route,
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
        ) {
            val searchViewModel: SearchViewModel = koinViewModel()
            val searchScreenParams = SearchScreenParams(
                navController = navController,
                searchViewModel = searchViewModel
            )
            SearchScreen(searchScreenParams)
        }

        composable(
            route = AppRoute.MediaFullView.route,
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
    object Home : AppRoute("homeScreen")
    object Search : AppRoute("searchScreen/{sessionId}") {
        fun createRoute(sessionId: String): String = "searchScreen/$sessionId"
    }
    object MediaFullView : AppRoute("fullImage/{mediaId}") {
        fun createRoute(mediaId: Long): String = "fullImage/$mediaId"
    }
}