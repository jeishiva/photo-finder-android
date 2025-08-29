package com.experiment.facedetector.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.experiment.facedetector.common.LogManager
import com.experiment.facedetector.presentation.entities.GalleryScreenParams
import com.experiment.facedetector.presentation.entities.SearchScreenParams
import com.experiment.facedetector.presentation.screen.GalleryScreen
import com.experiment.facedetector.presentation.screen.SearchScreen
import com.experiment.facedetector.presentation.screen.SplashScreen
import com.experiment.facedetector.presentation.screen.TAG
import com.experiment.facedetector.viewmodel.GalleryViewModel
import com.experiment.facedetector.viewmodel.SearchViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController, startDestination = AppRoute.Splash.route) {
        composable(AppRoute.Splash.route) {
            SplashScreen(navController)
        }

        navigation(startDestination = AppRoute.Home.route, route = AppRoute.PhotoSearch.route) {
            // select photo
            composable(AppRoute.Home.route) { backStackEntry ->
                val navBackStackEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(AppRoute.Home.route)
                }
                val selectPhotoViewModel : GalleryViewModel = koinViewModel(viewModelStoreOwner = navBackStackEntry)
                LogManager.d(TAG, "SelectPhotoViewModel: $selectPhotoViewModel")
                val homeScreenParams = GalleryScreenParams(
                    navController = navController,
                    selectPhotoViewModel
                )
                GalleryScreen(homeScreenParams, selectPhotoViewModel)
            }

            // search photo
            composable(
                route = AppRoute.Search.route,
                arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
            ) { backStackEntry ->
                val navBackStackEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(AppRoute.Home.route)
                }
                val selectPhotoViewModel : GalleryViewModel = koinViewModel(viewModelStoreOwner = navBackStackEntry)
                LogManager.d(TAG, "SelectPhotoViewModel: $selectPhotoViewModel")
                println("SelectPhotoViewModel: $selectPhotoViewModel")
                val searchViewModel: SearchViewModel = koinViewModel()
                val searchScreenParams = SearchScreenParams(
                    navController = navController,
                    searchViewModel = searchViewModel,
                    selectPhotoViewModel = selectPhotoViewModel
                )
                SearchScreen(searchScreenParams)
            }
        }
    }
}

sealed class NavigationScope(val name: String) {
    object Home : NavigationScope("HomeNavScope")
}

sealed class AppRoute(val route: String) {
    object Splash : AppRoute("splash")
    object PhotoSearch : AppRoute("photoSearch")
    object Home : AppRoute("homeScreen")
    object Search : AppRoute("searchScreen/{sessionId}") {
        fun createRoute(sessionId: String): String = "searchScreen/$sessionId"
    }
    object MediaFullView : AppRoute("fullImage/{mediaId}") {
        fun createRoute(mediaId: Long): String = "fullImage/$mediaId"
    }
}