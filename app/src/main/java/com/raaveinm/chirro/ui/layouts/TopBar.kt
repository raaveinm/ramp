package com.raaveinm.chirro.ui.layouts

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.createGraph
import com.raaveinm.chirro.R.dimen.medium_size
import com.raaveinm.chirro.ui.navigation.NavData

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("RestrictedApi")
@Composable
fun TopBar(
    modifier: Modifier,
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentNav = navBackStackEntry?.destination?.route
    val currentDestination = navBackStackEntry?.destination

    CenterAlignedTopAppBar(
        modifier = modifier.fillMaxWidth(),
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent
        ),
        title = {
            Row(
                Modifier.fillMaxWidth(.95f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Log.d("rrr", currentNav.toString())
                when {
                    ///////////////////////////////////////////////
                    // SettingsScreen
                    ///////////////////////////////////////////////
                    currentDestination?.hasRoute<NavData.SettingsScreen>() == true -> {
                        TopIcons(
                            iconLeftDescription = "backStack",
                            iconRightDescription = "toPlaylist",
                            iconLeft = Icons.AutoMirrored.Filled.ArrowBack,
                            iconRight = Icons.AutoMirrored.Filled.QueueMusic,
                            iconLeftAction = { navController.popBackStack() },
                            iconRightAction = {
                                navController.navigate(NavData.PlaylistScreen(false)) {
                                    popUpTo(NavData.PlayerScreen) { this.inclusive = false }
                                }
                            }
                        )
                    }

                    ///////////////////////////////////////////////
                    // PlaylistScreen
                    ///////////////////////////////////////////////
                    currentDestination?.hasRoute<NavData.PlaylistScreen>() == true -> {
                        TopIcons(
                            iconLeftDescription = "backStack",
                            iconRightDescription = "toSettings",
                            iconLeft = Icons.AutoMirrored.Filled.ArrowBack,
                            iconRight = Icons.Default.Settings,
                            iconLeftAction = { navController.popBackStack() },
                            iconRightAction = {
                                navController.navigate(NavData.SettingsScreen) {
                                    popUpTo(NavData.PlayerScreen) { this.inclusive = false }
                                }
                            }
                        )
                    }

                    ///////////////////////////////////////////////
                    // PlayerScreen
                    ///////////////////////////////////////////////
                    else -> {
                        TopIcons(
                            iconLeftDescription = "toSettings",
                            iconRightDescription = "toPlaylist",
                            iconLeft = Icons.Default.Settings,
                            iconRight = Icons.AutoMirrored.Filled.QueueMusic,
                            iconLeftAction = {
                                navController.navigate(NavData.SettingsScreen) {
                                    popUpTo(NavData.PlayerScreen) { this.inclusive = false }
                                }
                            },
                            iconRightAction = {
                                navController.navigate(NavData.PlaylistScreen(false)) {
                                    popUpTo(NavData.PlayerScreen) { this.inclusive = false }
                                }
                            }
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun TopIcons(
    iconLeftDescription: String,
    iconRightDescription: String,
    iconLeft: ImageVector,
    iconRight: ImageVector,
    iconLeftAction: () -> Unit,
    iconRightAction: () -> Unit,
) {
    IconButton(
        onClick = iconLeftAction
    ) {

        Icon(
            imageVector = iconLeft,
            contentDescription = iconLeftDescription,
            modifier = Modifier
                .defaultMinSize(dimensionResource(medium_size)),
        )
    }

    IconButton(
        onClick = iconRightAction
    ) {
        Icon(
            imageVector = iconRight,
            contentDescription = iconRightDescription,
            modifier = Modifier
                .defaultMinSize(dimensionResource(medium_size)),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun TopBarPreview(){
    TopBar(
        modifier = Modifier,
        navController = rememberNavController(),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun OnSettingsScreenPreview() {
    val navController = rememberNavController()

    LaunchedEffect(Unit) {
        navController.graph = navController.createGraph(startDestination = NavData.SettingsScreen) {
            composable<NavData.PlayerScreen> { }
            composable<NavData.SettingsScreen> { }
            composable<NavData.PlaylistScreen> { }
        }
    }

    TopBar(
        modifier = Modifier,
        navController = navController,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun OnPlaylistScreenPreview() {
    val navController = rememberNavController()

    LaunchedEffect(Unit) {
        navController.graph = navController.createGraph(startDestination = NavData.PlaylistScreen(false)) {
            composable<NavData.PlayerScreen> { }
            composable<NavData.SettingsScreen> { }
            composable<NavData.PlaylistScreen> { }
        }
    }

    TopBar(
        modifier = Modifier,
        navController = navController,
    )
}