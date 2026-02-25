package com.raaveinm.chirro.ui

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.raaveinm.chirro.domain.findActivity
import com.raaveinm.chirro.ui.layouts.TopBar
import com.raaveinm.chirro.ui.layouts.rememberDominantColor
import com.raaveinm.chirro.ui.navigation.NavData
import com.raaveinm.chirro.ui.screens.PlayerScreen
import com.raaveinm.chirro.ui.screens.PlaylistScreen
import com.raaveinm.chirro.ui.screens.SettingsScreen
import com.raaveinm.chirro.ui.veiwmodel.AppViewModelProvider
import com.raaveinm.chirro.ui.veiwmodel.PlayerViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainScreen (modifier: Modifier) {
    val navController: NavHostController = rememberNavController()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val hazeState = remember { HazeState() }
    val playerViewModel: PlayerViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val uiState by playerViewModel.uiState.collectAsState()
    val backgroundColor by rememberDominantColor(
        imageUri = uiState.currentTrack?.cover,
        defaultColor = MaterialTheme.colorScheme.background
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val isPlayerScreen = navBackStackEntry?.destination?.hasRoute<NavData.PlayerScreen>() == true
    val activeBackgroundColor = if (isPlayerScreen) backgroundColor.copy(.085f) else Color.Transparent

    Scaffold(
        modifier = modifier,
        topBar = {
            TopBar(
                modifier = Modifier.hazeEffect(state = hazeState) {
                    noiseFactor = 0f
                },
                navController = navController,
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = NavData.PlayerScreen,
            modifier = Modifier.hazeSource(hazeState),
            enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
        ) {
            composable<NavData.PlayerScreen> {
                LaunchedEffect(Unit) {
                    scrollBehavior.state.heightOffset = 0f
                    scrollBehavior.state.contentOffset = 0f
                }
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
                                modifier = Modifier.weight(1f).background(activeBackgroundColor),
                                navController = navController
                            )
                            PlaylistScreen(
                                modifier = Modifier.weight(.8f).hazeSource(hazeState),
                                navController = navController,
                                navigateToTrack = true
                            )
                        }
                    }
                    else -> {
                        PlayerScreen(
                            modifier = Modifier.background(color = activeBackgroundColor),
                            navController = navController
                        )
                    }
                }
            }
            composable<NavData.SettingsScreen> {
                SettingsScreen(
                    modifier = Modifier,
                    scrollBehavior = scrollBehavior,
                    innerPadding = innerPadding
                )
            }
            composable<NavData.PlaylistScreen> {
                PlaylistScreen(
                    modifier = Modifier.hazeSource(hazeState),
                    navController = navController,
                    navigateToTrack = it.arguments?.getBoolean("isNavigating") ?: false,
                    scrollBehavior = scrollBehavior,
                    innerPadding = innerPadding
                )
            }
        }
    }
}