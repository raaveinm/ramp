package com.raaveinm.chirro.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
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
import com.raaveinm.chirro.ui.screens.PlaylistScreen
import com.raaveinm.chirro.ui.screens.SettingsScreen

@Composable
fun MainScreen (modifier: Modifier) {
    val navController: NavHostController = rememberNavController()
    val contentPadding = WindowInsets.systemBars.asPaddingValues()
    Scaffold(
        modifier = modifier
            .padding(contentPadding),
        topBar = {
            TopBar(
                modifier = Modifier,
                navController = navController
            )
        }
    ) {

        NavHost(
            navController = navController,
            startDestination = NavData.PlayerScreen,
            modifier = Modifier.padding(it)
        ) {
            composable<NavData.PlayerScreen> {
                PlayerScreen(
                    modifier = Modifier,
                    navController = navController
                )
            }
            composable<NavData.SettingsScreen> {
                SettingsScreen(
                    modifier = Modifier,
                    navController = navController
                )
            }
            composable<NavData.PlaylistScreen> {
                PlaylistScreen(
                    modifier = Modifier,
                    navController = navController,
                    navigateToTrack = it.arguments?.getBoolean("isNavigating") ?: false
                )
            }
        }
    }
}