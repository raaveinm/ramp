@file:OptIn(FlowPreview::class)

package com.raaveinm.chirro.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.raaveinm.chirro.R
import com.raaveinm.chirro.ui.layouts.playlist.EmptyListComposable
import com.raaveinm.chirro.ui.layouts.playlist.PlayerMinimized
import com.raaveinm.chirro.ui.layouts.playlist.PlaylistItemRow
import com.raaveinm.chirro.ui.layouts.playlist.SearchBar
import com.raaveinm.chirro.ui.layouts.TrackInfoLayout
import com.raaveinm.chirro.ui.navigation.NavData
import com.raaveinm.chirro.ui.veiwmodel.AppViewModelProvider
import com.raaveinm.chirro.ui.veiwmodel.PlayerViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.FlowPreview
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun PlaylistScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navigateToTrack: Boolean = false,
    scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(),
    innerPadding: PaddingValues = PaddingValues()
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
    var isFabVisibleOnScroll by remember { mutableStateOf(true) }
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < -15) {
                    isFabVisibleOnScroll = false
                } else if (available.y > 15) {
                    isFabVisibleOnScroll = true
                }
                return Offset.Zero
            }
        }
    }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { }
    var expandedTrackId by remember { mutableStateOf<Long?>(null) }

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.background,
    ) {
        Box (modifier = Modifier.fillMaxSize()) {
            ///////////////////////////////////////////////
            // Jump To Current Track
            ///////////////////////////////////////////////
            if (isNavigating) {
                LaunchedEffect(uiState.currentTrack, tracks) {
                    if (isNavigating && tracks.isNotEmpty()) {
                        val currentTrackId = uiState.currentTrack?.id

                        if (currentTrackId != null) {
                            val targetIndex = tracks.indexOfFirst { it.id == currentTrackId }
                            if (targetIndex != -1) {
                                val scrollTarget = (targetIndex - 3).coerceAtLeast(0)

                                if (!viewModel.isPowerSaveMode.value &&
                                    abs(listState.firstVisibleItemIndex - targetIndex) < 64) {
                                    listState.animateScrollToItem(scrollTarget)
                                } else {
                                    listState.scrollToItem(scrollTarget)
                                }
                                isNavigating = false
                            }
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
                    modifier = modifier
                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                        .hazeSource(hazeState)
                        .nestedScroll(nestedScrollConnection)
                        .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                        .drawWithContent {
                            drawContent()

                            val fadeHeightPx = 72.dp.toPx()
                            val topFadeStop = fadeHeightPx / size.height
                            val bottomFadeStop = 1f - (fadeHeightPx / size.height)

                            val fadeBrush = Brush.verticalGradient(
                                0f to Color.Transparent,
                                topFadeStop to Color.Black,
                                bottomFadeStop to Color.Black,
                                .95f to Color.Transparent
                            )

                            drawRect(
                                brush = fadeBrush,
                                blendMode = BlendMode.DstIn
                            )
                        }
                        .padding(bottom = dimensionResource(R.dimen.medium_padding)),
                    state = listState,
                    contentPadding = PaddingValues(
                        bottom = dimensionResource(R.dimen.extra_large_size),
                        top = innerPadding.calculateTopPadding()
                    )
                ) {
                    items(
                        items = tracks,
                        key = { track -> track.id }
                    ) { track ->
                        val isExpanded = expandedTrackId == track.id
                        val isPlaying = uiState.currentTrack?.id == track.id
                        PlaylistItemRow(
                            track = track,
                            isExpanded = isExpanded,
                            isPlaying = isPlaying,
                            onExpandToggle = { id ->
                                expandedTrackId = if (expandedTrackId == id) null else id
                            },
                            play = { clickedTrack -> viewModel.playTrack(clickedTrack) },
                            navigateTo = {
                                navController.navigate(NavData.PlayerScreen) {
                                    popUpTo(NavData.PlayerScreen) { this.inclusive = true }
                                }
                            },
                            onDeleteSwipe = { swipedTrack ->
                                viewModel.deleteTrack(swipedTrack, activity, launcher)
                            }
                        )
                    }
                }
            }

            ///////////////////////////////////////////////
            // Search FAB
            ///////////////////////////////////////////////
            AnimatedVisibility(
                visible = isFABVisible && isFabVisibleOnScroll,
                enter = slideInVertically(initialOffsetY = { it * 2 }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it * 2 }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 90.dp, end = dimensionResource(R.dimen.medium_padding))
                    .zIndex(2f)
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
                        .padding(top = innerPadding.calculateTopPadding())
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

                        LazyColumn(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(
                                items = searchUiState.searchResults,
                                key = { track -> track.id }
                            ) { track ->
                                TrackInfoLayout(
                                    modifier = Modifier.fillMaxWidth(),
                                    interactionModifier = Modifier.clickable {
                                        viewModel.playTrack(track)
                                        navController.navigate(NavData.PlayerScreen) {
                                            popUpTo(NavData.PlayerScreen) {
                                                this.inclusive = true
                                            }
                                        }
                                    },
                                    trackInfo = track,
                                    pictureRequired = false,
                                    sidePictureRequired = !viewModel.isPowerSaveMode.collectAsState().value,
                                    containerColor = Color.Transparent,
                                )
                            }
                        }
                    }
                }
            }

            PlayerMinimized(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .hazeEffect(hazeState)
                    .padding(horizontal = dimensionResource(R.dimen.small_padding))
                    .zIndex(2.5f),
                trackInfo = uiState.currentTrack,
                previousTrack = { viewModel.skipPrevious() },
                nextTrack = { viewModel.skipNext() },
                playPause = {
                    if (viewModel.isPlaying) viewModel.pause()
                    else viewModel.resume()
                },
                onCoverClick = { isNavigating = true },
                onSurfaceClick = { navController.popBackStack() },
                playPauseIcon =
                    if (viewModel.isPlaying) Icons.Default.Pause
                    else Icons.Default.PlayArrow
            )
        }
    }
}