package com.raaveinm.chirro.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current
    val activity = context as Activity
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { }

    LazyColumn(modifier = modifier) {
        items(
            items = tracks,
            key = { track -> track.id }
        ) { track ->
            val dismissState = rememberSwipeToDismissBoxState(
                confirmValueChange = { dismissValue ->
                    when (dismissValue) {
                        SwipeToDismissBoxValue.EndToStart -> {
                            val isDeletedImmediately = viewModel.deleteTrack(
                                track = track,
                                activity = activity,
                                launcher = launcher
                            )
                            isDeletedImmediately
                        }
                        else -> false
                    }
                }
            )

            SwipeToDismissBox(
                state = dismissState,
                enableDismissFromStartToEnd = false,
                backgroundContent = {
                    val color = when (dismissState.targetValue) {
                        SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.surface
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = dimensionResource(R.dimen.medium_padding))
                            .padding(bottom = dimensionResource(R.dimen.small_size))
                            .background(color, MaterialTheme.shapes.medium),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            modifier = Modifier.padding(end = dimensionResource(R.dimen.medium_padding)),
                            tint = MaterialTheme.colorScheme.onError
                        )
                    }
                },
                content = {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = dimensionResource(R.dimen.medium_padding))
                            .padding(bottom = dimensionResource(R.dimen.small_size)),
                        shape = MaterialTheme.shapes.medium,
                        onClick = {
                            viewModel.playTrack(track)
                            navController.navigate(NavData.PlayerScreen) {
                                popUpTo(NavData.PlayerScreen) { this.inclusive = true }
                            }
                        }
                    ) {
                        TrackInfoLayout(
                            modifier = Modifier.fillMaxSize(),
                            trackInfo = track,
                            pictureRequired = false,
                            containerColor =
                                if (track.id != uiState.currentTrack?.id)
                                    MaterialTheme.colorScheme.surface
                                else MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        }
    }
}
