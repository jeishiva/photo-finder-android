package com.experiment.facedetector.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.experiment.facedetector.common.logging.LogManager
import com.experiment.facedetector.presentation.entities.AppUiModel
import com.experiment.facedetector.presentation.entities.SearchScreenArgs
import com.experiment.facedetector.presentation.screen.gallery.GALLERY_SCREEN_TAG
import com.experiment.facedetector.presentation.screen.gallery.GalleryScreen
import com.experiment.facedetector.presentation.screen.SEARCH_SCREEN_TAG
import com.experiment.facedetector.presentation.screen.SearchScreen
import com.experiment.facedetector.presentation.screen.SplashScreen
import com.experiment.facedetector.presentation.screen.gallery.model.GalleryScreenArgs
import com.experiment.facedetector.viewmodel.GalleryViewModel
import com.experiment.facedetector.viewmodel.SearchViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    appUiModel: AppUiModel,
) {
    val navigationManager = remember(navController) {
        NavigationManager(navController)
    }

    NavHost(
        navController = navController,
        startDestination = NavigationRoute.Splash
    ) {
        // Splash
        splashComposable(
            navigationManager = navigationManager,
            appUiModel = appUiModel
        )

        // Gallery
        navigation<NavigationRoute.GalleryGraph>(
            startDestination = NavigationRoute.Gallery
        ) {
            galleryComposable(navigationManager)
            searchComposable(navigationManager, navController)
        }
    }
}

private fun NavGraphBuilder.splashComposable(
    navigationManager: NavigationManager,
    appUiModel: AppUiModel,
) {
    composable<NavigationRoute.Splash> {
        SplashScreen(
            navigationManager = navigationManager,
            appUiModel = appUiModel
        )
    }
}

private fun NavGraphBuilder.galleryComposable(
    navigationManager: NavigationManager,
) {
    composable<NavigationRoute.Gallery> { backStackEntry ->
        val galleryViewModel = koinViewModel<GalleryViewModel>(
            viewModelStoreOwner = backStackEntry
        )
        LogManager.d(GALLERY_SCREEN_TAG, "GalleryViewModel: $galleryViewModel")
        GalleryScreen(
            params = GalleryScreenArgs(
                navigationManager = navigationManager,
                viewModel = galleryViewModel
            ),
            viewModel = galleryViewModel
        )
    }
}

private fun NavGraphBuilder.searchComposable(
    navigationManager: NavigationManager,
    navController: NavHostController,
) {
    composable<NavigationRoute.Search> { backStackEntry ->
        val galleryViewModel = koinViewModel<GalleryViewModel>(
            viewModelStoreOwner = remember(backStackEntry) {
                navController.getBackStackEntry<NavigationRoute.Gallery>()
            }
        )
        LogManager.d(SEARCH_SCREEN_TAG, "GalleryViewModel: $galleryViewModel")
        val args = backStackEntry.toRoute<NavigationRoute.Search>()
        LogManager.d(SEARCH_SCREEN_TAG, "Search with sessionId: ${args.sessionId}")
        val searchViewModel = koinViewModel<SearchViewModel>()
        SearchScreen(
            params = SearchScreenArgs(
                navigationManager = navigationManager,
                searchViewModel = searchViewModel,
                galleryViewModel = galleryViewModel,
                sessionId = args.sessionId
            )
        )
    }
}
