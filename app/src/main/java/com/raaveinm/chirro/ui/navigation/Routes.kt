package com.raaveinm.chirro.ui.navigation

sealed class Routes ( val route: String ) {
    data object Player : Routes ( "player" )
    data object Playlist : Routes ( "player/playlist" )
    data object Settings : Routes ( "player/settings" )
}