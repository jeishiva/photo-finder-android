package com.experiment.facedetector.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.experiment.facedetector.ui.screen.FullImageScreen
import com.experiment.facedetector.ui.screen.GalleryScreen
import com.experiment.facedetector.ui.screen.SplashScreen
import com.experiment.facedetector.ui.screen.TestScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController, startDestination = AppRoute.Splash.route) {
        composable(AppRoute.Splash.route) {
            SplashScreen(navController)
        }
        composable(AppRoute.Gallery.route) {
            GalleryScreen(
                navController = navController
            )
        }
        composable(AppRoute.TestScreen.route) {
            TestScreen(
                navController = navController
            )
        }
        composable(
            route = AppRoute.FullImage.route,
            arguments = listOf(navArgument("mediaId") { type = NavType.LongType })
        ) {
            FullImageScreen(navController)
        }
    }
}

sealed class AppRoute(val route: String) {
    object Splash : AppRoute("splash")
    object Gallery : AppRoute("gallery")
    object TestScreen : AppRoute("TestScreen")
    object FullImage : AppRoute("fullImage/{mediaId}") {
        fun createRoute(mediaId: Long): String = "fullImage/$mediaId"
    }
}