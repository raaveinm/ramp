package com.raaveinm.chirro.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.raaveinm.chirro.ui.fragments.PlayerScreen
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.*
import com.raaveinm.chirro.ui.fragments.Settings
import com.raaveinm.chirro.ui.fragments.TrackList
import com.raaveinm.chirro.ui.navigation.Routes

@Composable
fun MainScreen () {
    Scaffold { innerPadding ->
        val navController: NavHostController = rememberNavController()
        NavHost(navController = navController, startDestination = Routes.Player.route) {
            composable(Routes.Player.route) {
                PlayerScreen(modifier = Modifier.padding(innerPadding), navController = navController)
            }
            composable(Routes.Playlist.route) { TrackList(modifier = Modifier.padding(innerPadding)) }
            composable(Routes.Settings.route) { Settings() }
        }
    }
}