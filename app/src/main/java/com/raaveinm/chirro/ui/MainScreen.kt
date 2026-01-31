package com.raaveinm.chirro.ui

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.Scaffold
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.raaveinm.chirro.domain.findActivity
import com.raaveinm.chirro.ui.layouts.TopBar
import com.raaveinm.chirro.ui.navigation.NavData
import com.raaveinm.chirro.ui.screens.PlayerScreen
import com.raaveinm.chirro.ui.screens.PlaylistScreen
import com.raaveinm.chirro.ui.screens.SettingsScreen

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun MainScreen (modifier: Modifier) {
    val navController: NavHostController = rememberNavController()
    val contentPadding = WindowInsets.systemBars.asPaddingValues()
    Scaffold(
        modifier = modifier.padding(contentPadding),
        topBar = { TopBar(modifier = Modifier, navController = navController) }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = NavData.PlayerScreen,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
        ) {
            composable<NavData.PlayerScreen> {
                val context = LocalContext.current
                val activity = context.findActivity()
                val windowWidthClass = if (activity != null) {
                    calculateWindowSizeClass(activity).widthSizeClass
                } else {
                    WindowWidthSizeClass.Compact
                }
                when (windowWidthClass) {
                    WindowWidthSizeClass.Expanded -> {
                        Row(
                            Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PlayerScreen(
                                modifier = Modifier.weight(1f),
                                navController = navController
                            )
                            PlaylistScreen(
                                modifier = Modifier.weight(.8f),
                                navController = navController,
                                navigateToTrack = it.arguments?.getBoolean("isNavigating") ?: false
                            )
                        }
                    }
                    else -> {
                        PlayerScreen(
                            modifier = Modifier,
                            navController = navController
                        )
                    }
                }
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