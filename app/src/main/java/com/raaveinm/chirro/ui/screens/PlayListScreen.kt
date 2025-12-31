package com.raaveinm.chirro.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.raaveinm.chirro.R
import com.raaveinm.chirro.ui.layouts.TrackInfoLayout
import com.raaveinm.chirro.ui.navigation.NavData
import com.raaveinm.chirro.ui.veiwmodel.AppViewModelProvider
import com.raaveinm.chirro.ui.veiwmodel.PlayerViewModel

@Composable
fun PlaylistScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val tracks by viewModel.allTracks.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(modifier = modifier) {
        items(tracks.size) { track ->
            Surface (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensionResource(R.dimen.medium_padding))
                    .padding(bottom = dimensionResource(R.dimen.small_size)),
                onClick = {
                    viewModel.playTrack(tracks[track])
                    navController.navigate(NavData.PlayerScreen) {
                        popUpTo(NavData.PlayerScreen) { this.inclusive = true }
                    }
                }
            ) {
                TrackInfoLayout(
                    modifier = Modifier.fillMaxSize(),
                    trackInfo = tracks[track],
                    pictureRequired = false,
                    containerColor =
                        if (tracks[track].id != uiState.currentTrack?.id)
                            MaterialTheme.colorScheme.surface
                        else MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}
