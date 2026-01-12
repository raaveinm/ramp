package com.raaveinm.chirro.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
    val listState = rememberLazyListState()
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { }

    LaunchedEffect(key1 = uiState.currentTrack, key2 = tracks) {
        uiState.currentTrack?.let { current ->
            val index = tracks.indexOfFirst { it.id == current.id }
            if (index > 6)
                listState.animateScrollToItem(index - 4)
        }
    }

    if (tracks.isEmpty()) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(top = dimensionResource(R.dimen.large_padding))

        ) {
            Text(
                text = stringResource(R.string.tracklist_empty),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = dimensionResource(R.dimen.medium_padding))
                    .padding(horizontal = dimensionResource(R.dimen.medium_padding)),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.headlineMedium
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = dimensionResource(R.dimen.medium_padding))
            ) {
                Image(
                    painter = painterResource(R.drawable.sad),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(Color.Black.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .matchParentSize()
                        .offset(x = 4.dp, y = 4.dp)
                        .blur(radius = 8.dp)
                )

                Image(
                    painter = painterResource(R.drawable.sad),
                    contentDescription = "Sad",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

    }

    LazyColumn(
        modifier = modifier,
        state = listState
    ) {
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
                            .padding(horizontal = dimensionResource(R.dimen.small_padding))
                            //.padding(bottom = dimensionResource(R.dimen.small_size))
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
                            .padding(horizontal = dimensionResource(R.dimen.small_padding)),
                            //.padding(bottom = dimensionResource(R.dimen.small_size)),
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        TrackInfoLayout(
                            modifier = Modifier.fillMaxSize(),
                            trackInfo = track,
                            pictureRequired = false,
                            containerColor =
                                if (track.id != uiState.currentTrack?.id)
                                    MaterialTheme.colorScheme.surface
                                else MaterialTheme.colorScheme.onPrimary,
                            onClick = {
                                viewModel.playTrack(track)
                                navController.navigate(NavData.PlayerScreen) {
                                    popUpTo(NavData.PlayerScreen) { this.inclusive = true }
                                }
                            }
                        )
                    }
                }
            )
        }
    }
}
