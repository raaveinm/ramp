package com.raaveinm.chirro.ui.fragments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.raaveinm.chirro.ui.components.ControlButtons
import com.raaveinm.chirro.ui.components.TrackCover
import com.raaveinm.chirro.ui.components.TrackInfoScreen
import com.raaveinm.chirro.ui.veiwmodel.PlayerViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun PlayerScreen(
    modifier: Modifier = Modifier,
    playerViewModel: PlayerViewModel = viewModel()
) {
    val uiState by playerViewModel.uiState.collectAsStateWithLifecycle()
    val currentTrack = uiState.currentTrack
    val isPlaying = uiState.isPlaying
    val currentPosition = uiState.currentPosition
    val totalDuration = uiState.totalDuration
    val isFavorite = uiState.isFavorite
    val sliderPosition = if (totalDuration > 0) currentPosition.toFloat() / totalDuration.toFloat() else 0f


    Column (
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        TrackCover(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth()
                .weight(1f),
            trackCover = currentTrack?.artUri ?: ""
        )

        Spacer(modifier = Modifier.height(16.dp))

        TrackInfoScreen(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            trackTitle = currentTrack?.title ?: "---",
            trackArtist = currentTrack?.artist ?: "---",
            trackAlbum = currentTrack?.album ?: "---",
            trackDuration = totalDuration
        )

        Spacer(modifier = Modifier.padding(36.dp))

        ControlButtons(
            modifier = Modifier.padding(bottom = 32.dp),
            isPlaying = isPlaying,
            currentDuration = sliderPosition,
            onPlayPauseClick = { playerViewModel.playPause() },
            onNextClick = { playerViewModel.skipNext() },
            onPreviousClick = { playerViewModel.skipPrevious() },
            onSeek = { newPositionPercent -> playerViewModel.seekTo(newPositionPercent) },
            isFavorite = isFavorite,
            onFavoriteClick = { playerViewModel.toggleFavorite() },
            onShuffleClick = { }
        )

        Spacer(modifier = Modifier.height(48.dp))
    }
}