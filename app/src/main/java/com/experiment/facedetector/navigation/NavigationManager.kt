package com.experiment.facedetector.navigation

import androidx.navigation.NavHostController

class NavigationManager(private val navController: NavHostController) {

    fun navigateToGallery() {
        navController.navigate(NavigationRoute.Gallery) {
            popUpTo<NavigationRoute.Splash> {
                inclusive = true
            }
        }
    }

    fun navigateToSearch(sessionId: String) {
        navController.navigate(NavigationRoute.Search(sessionId = sessionId)) {
            popUpTo<NavigationRoute.Gallery> {
                inclusive = false
            }
        }
    }

    fun navigateBack(): Boolean {
        return navController.popBackStack()
    }

}