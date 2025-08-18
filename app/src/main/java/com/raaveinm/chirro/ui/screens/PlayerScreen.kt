package com.raaveinm.chirro.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.raaveinm.chirro.R
import com.raaveinm.chirro.data.TrackInfo
import com.raaveinm.chirro.ui.layouts.PlayerControlButtons
import com.raaveinm.chirro.ui.layouts.TrackInfoLayout
import com.raaveinm.chirro.ui.navigation.SettingsScreen
import com.raaveinm.chirro.ui.navigation.PlaylistScreen
import com.raaveinm.chirro.ui.theme.ChirroTheme
import com.raaveinm.chirro.ui.veiwmodel.PlayerViewModel

@Composable
fun PlayerScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = viewModel(),
) {
    val uiState = viewModel.uiState.collectAsState()
    val trackInfo: TrackInfo? = uiState.value.currentTrack
    val backgroundColor by animateColorAsState(
        targetValue = if (uiState.value.isPlaying) {
            Color.Cyan
        } else {
            Color.Transparent
        },
        label = "selectedTrack",
    )

    Column(
        modifier = modifier.background(backgroundColor),
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { navController.navigate(SettingsScreen) }
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "toSettings",
                    modifier = Modifier
                        .padding(horizontal = dimensionResource(R.dimen.medium_padding))
                        .defaultMinSize(dimensionResource(R.dimen.medium_size)),
                )
            }

            IconButton(
                onClick = {
                    navController.navigate(PlaylistScreen(trackInfo?.id ?: -1))
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                    contentDescription = "toPlaylist",
                    modifier = Modifier
                        .padding(horizontal = dimensionResource(R.dimen.medium_padding))
                        .defaultMinSize(dimensionResource(R.dimen.medium_size)),
                )
            }
        }

        TrackInfoLayout(
            trackInfo = trackInfo ?: TrackInfo(
                title = "Unknown",
                artist = "Unknown",
                album = "Unknown",
                duration = 0,
                uri = "null",
            )
        )

        Spacer(Modifier.padding(dimensionResource(R.dimen.medium_padding)))

        PlayerControlButtons(
            modifier = Modifier.fillMaxWidth(),
            isPlaying = uiState.value.isPlaying,
            onPlayPauseClick = {
                if (uiState.value.isPlaying) {
                    viewModel.pause()
                } else {
                    viewModel.resume()
                }
            },
            onPreviousClick = { viewModel.skipPrevious() },
            onNextClick = { viewModel.skipNext() },
            onSeek = { },
            onShareClick = { },
            currentDuration = uiState.value.currentPosition,
            isFavourite = uiState.value.isFavorite,
            onFavoriteClick = { }
        )
    }
}

@Preview
@Composable
fun PlayerScreenPreview () {
    ChirroTheme {
        PlayerScreen(navController = NavHostController(LocalContext.current))
    }
}