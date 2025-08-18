package com.raaveinm.chirro.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.raaveinm.chirro.ui.navigation.PlayerScreen
import com.raaveinm.chirro.ui.navigation.PlaylistScreen
import com.raaveinm.chirro.ui.navigation.SettingsScreen
import com.raaveinm.chirro.ui.screens.PlayerScreen
import com.raaveinm.chirro.ui.screens.SettingsScreen

@Composable
fun MainScreen () {
    val navController: NavHostController = rememberNavController()
    Scaffold { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = PlayerScreen(navController),
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<PlayerScreen> {
                PlayerScreen(
                    modifier = Modifier.padding(innerPadding),
                    navController = navController
                )
            }
            composable<SettingsScreen> {
                SettingsScreen(
                    modifier = Modifier.padding(innerPadding),
                    navController = navController
                )
            }
            composable<PlaylistScreen> {
                PlayerScreen(
                    modifier = Modifier.padding(innerPadding),
                    navController = navController
                )
            }
        }
    }
}