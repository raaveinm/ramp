package com.raaveinm.chirro.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.raaveinm.chirro.R
import com.raaveinm.chirro.data.database.TrackInfo
import com.raaveinm.chirro.domain.Eggs
import com.raaveinm.chirro.ui.layouts.ArcEasterEgg
import com.raaveinm.chirro.ui.layouts.PlayerControlButtons
import com.raaveinm.chirro.ui.layouts.TrackInfoLayout
import com.raaveinm.chirro.ui.navigation.NavData
import com.raaveinm.chirro.ui.veiwmodel.AppViewModelProvider
import com.raaveinm.chirro.ui.veiwmodel.PlayerViewModel

@SuppressLint("ContextCastToActivity")
@Composable
fun PlayerScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState by viewModel.uiState.collectAsState()
    val trackInfo: TrackInfo? = uiState.currentTrack
    val isPlaying = uiState.isPlaying
    val activity = LocalContext.current as Activity
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { }

    var isNextDirection by remember { mutableStateOf(true) }

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.background
    ) {
        val backgroundEasterEgg = viewModel.backgroundEasterEgg()?: Eggs.NULL
        AnimatedVisibility(backgroundEasterEgg != Eggs.NULL){
            when (backgroundEasterEgg) {
                Eggs.ARC -> {
                    Surface(Modifier
                        .fillMaxSize()
                        .zIndex(0f)) { ArcEasterEgg() }
                }
                Eggs.NULL -> {}
            }
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedContent(
                targetState = trackInfo ?: TrackInfo(
                    title = "Unknown",
                    artist = "Unknown",
                    album = "Unknown",
                    duration = 0,
                    uri = "null",
                    id = -1,
                    isFavourite = true,
                ),
                label = "TrackTransition",
                transitionSpec = {
                    if (isNextDirection) {
                        (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> -width } + fadeOut())
                    } else {
                        (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> width } + fadeOut())
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { currentTrack ->

                TrackInfoLayout(
                    trackInfo = currentTrack,
                    modifier = Modifier.fillMaxWidth(),
                    pictureRequired = true,
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    onClick = { navController.navigate(NavData.PlaylistScreen(true)) },

                    onSwipeRTL = {
                        isNextDirection = true
                        viewModel.skipNext()
                    },
                    onSwipeLTR = {
                        isNextDirection = false
                        viewModel.skipPrevious()
                    },
                    onCoverClick = { navController.navigate(NavData.PlaylistScreen(true)) }
                )
            }

            Spacer(Modifier.padding(dimensionResource(R.dimen.medium_padding)))

            PlayerControlButtons(
                modifier = Modifier.fillMaxWidth(),
                isPlaying = isPlaying,
                onPlayPauseClick = {
                    if (isPlaying) viewModel.pause()
                    else viewModel.resume()
                },
                onPreviousClick = {
                    isNextDirection = false
                    viewModel.skipPrevious()
                },
                onNextClick = {
                    isNextDirection = true
                    viewModel.skipNext()
                },
                onSeek = { position -> viewModel.seekTo(position.toLong()) },
                onShareClick = { viewModel.shareTrack(activity, trackInfo) },
                currentDuration = uiState.currentPosition,
                isFavourite = uiState.isFavorite,
                onFavoriteClick = {
                    if (trackInfo != null) {
                        viewModel.deleteTrack(
                            track = trackInfo,
                            activity = activity,
                            launcher = launcher
                        )
                    }
                },
                trackLength = uiState.totalDuration
            )
            Spacer(Modifier.padding(dimensionResource(R.dimen.large_size)))
        }
    }
}