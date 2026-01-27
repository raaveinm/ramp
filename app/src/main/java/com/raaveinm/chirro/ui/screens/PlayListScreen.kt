package com.raaveinm.chirro.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
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
import androidx.compose.ui.graphics.Color
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
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild

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

    Surface(
        modifier = Modifier.fillMaxSize(),
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
                    modifier = modifier.haze(hazeState),
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

                        ///////////////////////////////////////////////
                        // Delete Track on Swipe
                        ///////////////////////////////////////////////
                        SwipeToDismissBox(
                            state = dismissState,
                            enableDismissFromStartToEnd = true,
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

            ///////////////////////////////////////////////
            // Search FAB
            ///////////////////////////////////////////////
            AnimatedVisibility(
                visible = isFABVisible,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(dimensionResource(R.dimen.medium_padding))
            ) {
                FloatingActionButton(
                    onClick = {
                        /* TODO: search functionality */
                        /*dummy*/
                        isFABVisible = !isFABVisible
                    },
                    shape = MaterialTheme.shapes.medium,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 8.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
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
                    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
                    val screenHeight = configuration.screenHeightDp.dp

                    Card(
                        modifier = Modifier
//                            .padding(horizontal = dimensionResource(R.dimen.small_padding))
                            .fillMaxWidth(.92f)
                            .heightIn(max = screenHeight * .64f)
                            .clip(RoundedCornerShape(16.dp))
                            .hazeChild(state = hazeState)
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
                                        navController.navigate(NavData.PlayerScreen)
                                        isNavigating = true
                                    }
                                )
                            }
                        }
                        Spacer(Modifier.size(dimensionResource(R.dimen.medium_padding)))
                    }
                }
            }
        }
    }
}
