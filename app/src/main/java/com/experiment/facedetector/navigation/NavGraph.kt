package com.experiment.facedetector.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.experiment.facedetector.ui.HomeScreenParams
import com.experiment.facedetector.ui.screen.DetailScreen
import com.experiment.facedetector.ui.screen.FullImageScreen
import com.experiment.facedetector.ui.screen.GalleryScreen
import com.experiment.facedetector.ui.screen.HomeScreen
import com.experiment.facedetector.ui.screen.SplashScreen
import com.experiment.facedetector.viewmodel.HomeViewModel
import org.koin.compose.getKoin
import org.koin.core.qualifier.named

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
            val scope = getKoin().getOrCreateScope(NavigationScope.Home.name, named(NavigationScope.Home.name))
            val homeViewModel: HomeViewModel = scope.get()
            val homeScreenParams = HomeScreenParams(
                navController = navController,
                viewModel = homeViewModel,
                scope = scope
            )
            HomeScreen(homeScreenParams)
        }
        composable(AppRoute.DetailScreen.route) {
            DetailScreen(navController = navController)
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
    object Home : NavigationScope("HomeNavGraphScope")
}

sealed class AppRoute(val route: String) {
    object Splash : AppRoute("splash")
    object Gallery : AppRoute("gallery")
    object HomeScreen : AppRoute("HomeScreen")
    object DetailScreen : AppRoute("DetailScreen")
    object FullImage : AppRoute("fullImage/{mediaId}") {
        fun createRoute(mediaId: Long): String = "fullImage/$mediaId"
    }
}