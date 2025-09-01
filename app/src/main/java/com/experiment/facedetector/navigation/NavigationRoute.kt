package com.experiment.facedetector.navigation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class NavigationRoute {
    @Serializable
    @SerialName("Splash")
    object Splash : NavigationRoute()

    @Serializable
    @SerialName("GalleryGraph")
    data object GalleryGraph : NavigationRoute()

    @Serializable
    @SerialName("Gallery")
    object Gallery : NavigationRoute()

    @Serializable
    @SerialName("Search")
    data class Search(val sessionId: String) : NavigationRoute()

}
