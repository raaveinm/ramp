package com.raaveinm.chirro.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.raaveinm.chirro.R
import com.raaveinm.chirro.data.database.TrackInfo
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

    Surface(
        modifier = modifier.fillMaxSize(),
        color = androidx.compose.material3.MaterialTheme.colorScheme.background // Ensure this color is opaque in your theme
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TrackInfoLayout(
                trackInfo = trackInfo ?: TrackInfo(
                    title = "Unknown",
                    artist = "Unknown",
                    album = "Unknown",
                    duration = 0,
                    uri = "null",
                    id = -1,
                    isFavourite = true,
                ),
                modifier = Modifier.fillMaxWidth(),
                pictureRequired = true,
                onClick = { navController.navigate(NavData.PlaylistScreen(true)) },
                onSwipeRTL = { viewModel.skipNext() },
                onSwipeLTR = { viewModel.skipPrevious() },
                onCoverClick = { navController.navigate(NavData.PlaylistScreen(true)) }
            )

            Spacer(Modifier.padding(dimensionResource(R.dimen.medium_padding)))

            PlayerControlButtons(
                modifier = Modifier.fillMaxWidth(),
                isPlaying = isPlaying,
                onPlayPauseClick = {
                    if (isPlaying) viewModel.pause()
                    else viewModel.resume()
                },
                onPreviousClick = { viewModel.skipPrevious() },
                onNextClick = { viewModel.skipNext() },
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
