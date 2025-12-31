package com.raaveinm.chirro.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.raaveinm.chirro.ui.layouts.TopBar
import com.raaveinm.chirro.ui.navigation.NavData
import com.raaveinm.chirro.ui.screens.PlayerScreen
import com.raaveinm.chirro.ui.screens.SettingsScreen
import com.raaveinm.chirro.ui.screens.PlaylistScreen

@Composable
fun MainScreen (modifier: Modifier) {
    val navController: NavHostController = rememberNavController()
    Scaffold(
        modifier = modifier,
        topBar = {
            TopBar(
                modifier = Modifier,
                navController = navController
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavData.PlayerScreen,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<NavData.PlayerScreen> {
                PlayerScreen(
                    modifier = Modifier.padding(innerPadding),
                    navController = navController
                )
            }
            composable<NavData.SettingsScreen> {
                SettingsScreen(
                    modifier = Modifier.padding(innerPadding),
                    navController = navController
                )
            }
            composable<NavData.PlaylistScreen> {
                PlaylistScreen(
                    modifier = Modifier.padding(innerPadding),
                    navController = navController
                )
            }
        }
    }
}