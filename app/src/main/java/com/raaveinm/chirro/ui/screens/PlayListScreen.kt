package com.raaveinm.chirro.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.raaveinm.chirro.R
import com.raaveinm.chirro.ui.layouts.EmptyListComposable
import com.raaveinm.chirro.ui.layouts.SearchBar
import com.raaveinm.chirro.ui.layouts.TrackInfoLayout
import com.raaveinm.chirro.ui.navigation.NavData
import com.raaveinm.chirro.ui.veiwmodel.AppViewModelProvider
import com.raaveinm.chirro.ui.veiwmodel.PlayerViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.launch

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun PlaylistScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navigateToTrack: Boolean = false
) {
    ///////////////////////////////////////////////
    // Variables
    ///////////////////////////////////////////////
    val tracks by viewModel.allTracks.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as Activity
    var isNavigating by remember { mutableStateOf(navigateToTrack) }
    val listState = rememberLazyListState()
    var isFABVisible by remember { mutableStateOf(true) }
    val searchUiState by viewModel.searchUiState.collectAsState()
    val hazeState = remember { HazeState() }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { }
    val scope = rememberCoroutineScope()

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.background,
    ) {
        Box (modifier = Modifier.fillMaxSize()) {
            ///////////////////////////////////////////////
            // Jump To Current Track
            ///////////////////////////////////////////////
            if (isNavigating) {
                LaunchedEffect(key1 = uiState.currentTrack) {
                    uiState.currentTrack?.let { current ->
                        val index = tracks.indexOfFirst { it.id == current.id }
                        if (index > 6) {
                            listState.animateScrollToItem(index - 6)
                            isNavigating = false
                            return@LaunchedEffect
                        }
                    }
                }
            }

            // Display Empty Playlist Screen
            if (tracks.isEmpty()) {
                EmptyListComposable()
            } else {
                ///////////////////////////////////////////////
                // Playlist
                ///////////////////////////////////////////////
                LazyColumn(
                    modifier = modifier.hazeSource(hazeState),
                    state = listState
                ) {
                    items(
                        items = tracks,
                        key = { track -> track.id }
                    ) { track ->
                        val isPlaying = uiState.currentTrack?.id == track.id
                        val dismissState = rememberSwipeToDismissBoxState(
                            initialValue = SwipeToDismissBoxValue.Settled,
                            positionalThreshold = { totalDistance ->
                                totalDistance * .01f
                            }
                        )

                        val animatedColor by animateColorAsState(
                            targetValue = when (dismissState.targetValue) {
                                SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.surface
                            },
                            animationSpec = tween(durationMillis = 500)
                        )

                        ///////////////////////////////////////////////
                        // Delete Track on Swipe
                        ///////////////////////////////////////////////
                        SwipeToDismissBox(
                            state = dismissState,
                            enableDismissFromStartToEnd = false,
                            enableDismissFromEndToStart = true,
                            onDismiss = {
                                if (it == SwipeToDismissBoxValue.EndToStart) {
                                    val isDeleted = viewModel.deleteTrack(track, activity, launcher)
                                    if (!isDeleted) {
                                        scope.launch {
                                            returnState(dismissState)
                                        }
                                    }
                                }
                            },
                            backgroundContent = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = dimensionResource(R.dimen.small_padding))
                                        .background(animatedColor, MaterialTheme.shapes.medium),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            modifier = Modifier.padding(end = dimensionResource(R.dimen.medium_padding)),
                                            tint = MaterialTheme.colorScheme.onError
                                        )
                                    }
                                }
                            },
                            content = {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = dimensionResource(R.dimen.small_padding))
                                        .then(
                                            if (isPlaying) {
                                                Modifier
                                                    .padding(vertical = 4.dp)
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .border(
                                                        width = 1.dp,
                                                        color = MaterialTheme.colorScheme.onSurface.copy(
                                                            alpha = 0.3f
                                                        ),
                                                        shape = RoundedCornerShape(16.dp)
                                                    )
                                            } else Modifier
                                        ),
                                    shape = MaterialTheme.shapes.medium,
                                    color = Color.Transparent
                                ) {
                                    TrackInfoLayout(
                                        modifier = Modifier.fillMaxSize(),
                                        trackInfo = track,
                                        pictureRequired = false,
                                        containerColor = if (isPlaying) {
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                        } else {
                                            MaterialTheme.colorScheme.surface
                                        },
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

            ///////////////////////////////////////////////
            // Search FAB
            ///////////////////////////////////////////////
            AnimatedVisibility(
                visible = isFABVisible,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(dimensionResource(R.dimen.medium_padding))
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .hazeEffect(state = hazeState)
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = .05f)
                        )
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.02f),
                            shape = MaterialTheme.shapes.medium
                        )
                        .clickable(onClick = { isFABVisible = !isFABVisible }),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            ///////////////////////////////////////////////
            // Search Card
            ///////////////////////////////////////////////
            AnimatedVisibility(
                visible = !isFABVisible,
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .zIndex(1f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.3f))
                        .clickable { isFABVisible = !isFABVisible },
                    contentAlignment = Alignment.Center
                ) {
                    val configuration = LocalConfiguration.current
                    val screenHeight = configuration.screenHeightDp.dp

                    Card(
                        modifier = Modifier
                            .fillMaxWidth(.92f)
                            .heightIn(max = screenHeight * .64f)
                            .clip(RoundedCornerShape(16.dp))
                            .hazeEffect(state = hazeState)
                            .animateContentSize()
                            .clickable(enabled = false) {},
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.05f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Spacer(Modifier.padding(dimensionResource(R.dimen.small_padding)))

                        SearchBar(
                            modifier = Modifier
                                .fillMaxWidth(),
                            closeScreen = { isFABVisible = !isFABVisible }
                        )

                        Spacer(Modifier.size(dimensionResource(R.dimen.medium_padding)))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                        ) {
                            searchUiState.searchResults.forEach { track ->
                                TrackInfoLayout(
                                    modifier = Modifier.fillMaxWidth(),
                                    trackInfo = track,
                                    pictureRequired = false,
                                    containerColor = Color.Transparent,
                                    onClick = {
                                        viewModel.playTrack(track)
                                        navController.navigate(NavData.PlayerScreen) {
                                            popUpTo(NavData.PlayerScreen) {
                                                this.inclusive = true
                                            }
                                        }
                                        isNavigating = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

suspend fun returnState(dismissState: SwipeToDismissBoxState){
    dismissState.snapTo(SwipeToDismissBoxValue.Settled)
}