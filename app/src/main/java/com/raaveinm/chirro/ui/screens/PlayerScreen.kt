package com.raaveinm.chirro.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.raaveinm.chirro.R
import com.raaveinm.chirro.data.values.Eggs
import com.raaveinm.chirro.data.values.TrackInfo
import com.raaveinm.chirro.ui.layouts.ArcEasterEgg
import com.raaveinm.chirro.ui.layouts.BackgroundImageCover
import com.raaveinm.chirro.ui.layouts.PlayerControlButtons
import com.raaveinm.chirro.ui.layouts.TimePickerScreen
import com.raaveinm.chirro.ui.layouts.TrackInfoLayout
import com.raaveinm.chirro.ui.layouts.formatDuration
import com.raaveinm.chirro.ui.navigation.NavData
import com.raaveinm.chirro.ui.veiwmodel.AppViewModelProvider
import com.raaveinm.chirro.ui.veiwmodel.PlayerViewModel
import kotlinx.coroutines.FlowPreview

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@SuppressLint("ContextCastToActivity")
@Composable
fun PlayerScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    hazeModifier: Modifier = Modifier,
    viewModel: PlayerViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState by viewModel.uiState.collectAsState()
    val progressionUiState by viewModel.progressionUiState.collectAsState()
    
    val trackInfo: TrackInfo? = uiState.currentTrack
    val isPlaying = uiState.isPlaying
    val activity = LocalContext.current as Activity
    var timePickerEnabled by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { }

    var isNextDirection by remember { mutableStateOf(true) }

    DisposableEffect(Unit) {
        viewModel.setPlayerScreenVisibility(true)
        onDispose {
            viewModel.setPlayerScreenVisibility(false)
        }
    }

    Surface(
        modifier = modifier,
        color = Color.Transparent
    ) {
        val backgroundEasterEgg = viewModel.backgroundEasterEgg()?: Eggs.NULL
        val backgroundImage = viewModel.backgroundImage.collectAsState(initial = false)

        Crossfade(
            targetState = backgroundEasterEgg,
            label = "BackgroundTransition",
            modifier = Modifier.fillMaxSize().zIndex(0f)
        ) { state ->
            when (state) {
                Eggs.ARC -> {
                    Surface(Modifier.fillMaxSize()) {
                        ArcEasterEgg()
                    }
                }
                Eggs.NULL -> {
                    if (backgroundImage.value)
                    BackgroundImageCover(
                        modifier = Modifier.fillMaxSize(),
                        imageUri = trackInfo?.cover ?: "",
                        opacity = viewModel.opacityValue.collectAsState(initial = .3f).value
                    )
                }
            }
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedContent(
                targetState = trackInfo ?: TrackInfo.EMPTY,
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
                    modifier = Modifier.padding(vertical = dimensionResource(R.dimen.large_padding))
                        .fillMaxWidth(),
                    interactionModifier = Modifier
                        .clickable { navController.navigate(NavData.PlaylistScreen(true)) },
                    pictureRequired = true,
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    onSwipeRTL = {
                        isNextDirection = true
                        viewModel.skipNext()
                    },
                    onSwipeLTR = {
                        isNextDirection = false
                        viewModel.skipPrevious()
                    },
                    onSwipeUp = {
                        navController.navigate(NavData.PlaylistScreen(true))
                    },
                    onCoverClick = { navController.navigate(NavData.PlaylistScreen(true)) }
                )
            }

            Spacer(Modifier.padding(dimensionResource(R.dimen.medium_padding)))

            PlayerControlButtons(
                modifier = Modifier.fillMaxWidth(),
                dropDownModifier = hazeModifier,
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
                currentDuration = progressionUiState.currentPosition,
                trackLength = progressionUiState.totalDuration,
                isFavourite = uiState.isFavorite,
                extendedMenu = {
                    Column(
                        modifier = Modifier,
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.End
                    ) {
                        val sleepTimerRemaining by viewModel.sleepTimerRemainingSeconds.collectAsState()
                        if (sleepTimerRemaining != null) {
                            Text(
                                text = "Timer: ${formatDuration(sleepTimerRemaining!! * 1000)}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        Button(
                            onClick = {
                                if (trackInfo != null) {
                                    viewModel.deleteTrack(
                                        track = trackInfo,
                                        activity = activity,
                                        launcher = launcher
                                    )
                                }
                            },
                            shape = MaterialTheme.shapes.small,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(
                                    red = .5f,
                                    green = .5f,
                                    blue = .5f
                                ),
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                            ) {
                            Text(
                                text = stringResource(R.string.delete_track),
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.End
                            )
                        }
                        Button(
                            onClick = { timePickerEnabled = !timePickerEnabled },
                            shape = MaterialTheme.shapes.small,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = .25f),
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(R.string.start_sleep_timer),
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.End
                            )
                        }

                        AnimatedVisibility(visible = timePickerEnabled) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                val hourState = rememberLazyListState(initialFirstVisibleItemIndex = 0)
                                val minuteState = rememberLazyListState(initialFirstVisibleItemIndex = 0)
                                val secondState = rememberLazyListState(initialFirstVisibleItemIndex = 30)

                                TimePickerScreen(
                                    hourState = hourState,
                                    minuteState = minuteState,
                                    secondState = secondState,
                                    powerSafeMode = viewModel.isPowerSaveMode.collectAsState().value
                                )

                                Button(
                                    onClick = {
                                        timePickerEnabled = false
                                        val toSeconds =
                                            (((hourState.firstVisibleItemIndex * 60) +
                                                    minuteState.firstVisibleItemIndex) * 60 +
                                                    secondState.firstVisibleItemIndex).toLong()
                                        viewModel.startSleepTimer(toSeconds)
                                    },
                                    modifier = Modifier.padding(dimensionResource(R.dimen.small_padding))
                                ) {
                                    Text(text = "Set")
                                }
                            }
                        }

                        Button(
                            onClick = {viewModel.stopSleepTimer()},
                            shape = MaterialTheme.shapes.small,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = .25f),
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(R.string.stop_sleep_timer),
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.End
                            )
                        }
                    }
                },
                onDismissRequest = { timePickerEnabled = false }
            )
            Spacer(Modifier.padding(dimensionResource(R.dimen.large_size)))
        }
    }
}
