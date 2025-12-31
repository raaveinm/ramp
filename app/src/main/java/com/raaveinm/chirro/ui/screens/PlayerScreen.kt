package com.raaveinm.chirro.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.raaveinm.chirro.R
import com.raaveinm.chirro.data.database.TrackInfo
import com.raaveinm.chirro.ui.layouts.PlayerControlButtons
import com.raaveinm.chirro.ui.layouts.TrackInfoLayout
import com.raaveinm.chirro.ui.theme.ChirroTheme
import com.raaveinm.chirro.ui.veiwmodel.AppViewModelProvider
import com.raaveinm.chirro.ui.veiwmodel.PlayerViewModel

@Composable
fun PlayerScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState = viewModel.uiState.collectAsState()
    val trackInfo: TrackInfo? = uiState.value.currentTrack

    Column(
        modifier = modifier.fillMaxSize(),
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
                id = 1,
                isFavourite = true,
            ),
        )

        Spacer(Modifier.padding(dimensionResource(R.dimen.medium_padding)))

        PlayerControlButtons(
            modifier = Modifier.fillMaxWidth(),
            isPlaying = uiState.value.isPlaying,
            onPlayPauseClick = {
                if (uiState.value.isPlaying) viewModel.pause()
                else viewModel.resume()
            },
            onPreviousClick = { viewModel.skipPrevious() },
            onNextClick = { viewModel.skipNext() },
            onSeek = { position -> viewModel.seekTo(position.toLong()) },
            onShareClick = { },
            currentDuration = uiState.value.currentPosition,
            isFavourite = uiState.value.isFavorite,
            onFavoriteClick = { viewModel.onFavoriteClicked(trackInfo ?: TrackInfo.EMPTY) },
            trackLength = uiState.value.totalDuration
        )
        Spacer(Modifier.padding(dimensionResource(R.dimen.large_size)))
    }
}

@Preview
@Composable
fun PlayerScreenPreview () {
    ChirroTheme {
        PlayerScreen(navController = NavHostController(LocalContext.current))
    }
}