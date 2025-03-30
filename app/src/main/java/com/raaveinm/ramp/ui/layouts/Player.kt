package com.raaveinm.ramp.ui.layouts

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.raaveinm.ramp.snippets.TrackInfo
import com.raaveinm.ramp.ui.theme.RampTheme
import com.raaveinm.ramp.viewmodels.PlayerUiState
import com.raaveinm.ramp.viewmodels.PlayerViewModel
import java.util.concurrent.TimeUnit

@Composable
fun PlayerScreen(
    modifier: Modifier = Modifier,
    playerViewModel: PlayerViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by playerViewModel.uiState.collectAsStateWithLifecycle()
    playerViewModel.initializeController(context)

    // --- UI Composition ---
    RampTheme {
        Scaffold(
            topBar = { TopBarContent() },
            modifier = modifier
        ) { innerPadding ->

            when (val state = uiState) {
                is PlayerUiState.Initializing -> {
                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding),
                        contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                        Text("Connecting to player service...", Modifier.padding(top = 60.dp))
                    }
                }

                is PlayerUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding),
                        contentAlignment = Alignment.Center) {
                        Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                    }
                }

                is PlayerUiState.Ready -> {
                    PlayerContent(
                        state = state,
                        viewModel = playerViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun PlayerContent(
    state: PlayerUiState.Ready,
    viewModel: PlayerViewModel,
    modifier: Modifier = Modifier
) {
    val metadata = state.playerState.currentMediaMetadata
    val trackTitle = metadata?.title?.toString() ?: "No Title"
    val trackArtist = metadata?.artist?.toString() ?: "Unknown Artist"
    val coverArtRes = remember(metadata) { TrackInfo().songCover("default") }
    val isPlaying = state.playerState.isPlaying
    val currentPosition = state.currentPosition
    val totalDuration = state.totalDuration

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 25.dp),
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            contentDescription = "Album Art for $trackTitle",
            contentScale = ContentScale.Crop,
            painter = painterResource(id = coverArtRes),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .aspectRatio(1f)
                .clip(MaterialTheme.shapes.medium)
                .shadow(10.dp, MaterialTheme.shapes.medium)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- Track Info ---
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = trackTitle,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = trackArtist,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Seek Bar ---
        Column(modifier = Modifier.fillMaxWidth()) {
            Slider(
                value = currentPosition.toFloat(),
                onValueChange = { viewModel.seekTo(it.toLong()) },
                valueRange = 0f..(totalDuration.toFloat().coerceAtLeast(0.1f)),
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(formatDuration(currentPosition), style = MaterialTheme.typography.labelSmall)
                Text(formatDuration(totalDuration), style = MaterialTheme.typography.labelSmall)
            }
        }


        Spacer(modifier = Modifier.height(16.dp))

        // --- Player Controls ---
        PlayerControls(
            isPlaying = isPlaying,
            onPlayPauseClick = { viewModel.playPause() },
            onSkipPreviousClick = { viewModel.skipPrevious() },
            onSkipNextClick = { viewModel.skipNext() },
            // Add shuffle/repeat later if needed
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun PlayerControls(
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    onSkipPreviousClick: () -> Unit,
    onSkipNextClick: () -> Unit,
    modifier: Modifier = Modifier,
    onShuffleClick: () -> Unit = {},
) {
    var isAdded by rememberSaveable { mutableStateOf(false) }
    Row(
        modifier = modifier.padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        IconButton(
            onClick = { isAdded = !isAdded },
            modifier = Modifier
        ) {
            Icon(
                imageVector = if (isAdded) Icons.Default.Check else Icons.Default.Add,
                contentDescription = if (isAdded) "Added" else "Add",
                modifier = Modifier.fillMaxSize(),
            )
        }

        // --- Previous Button ---
        IconButton(onClick = onSkipPreviousClick) {
            Icon(
                imageVector = Icons.Filled.SkipPrevious,
                contentDescription = "Skip Previous",
                modifier = Modifier.size(48.dp)
            )
        }

        // --- Play/Pause Button ---
        IconButton(onClick = onPlayPauseClick) {
            Icon(
                imageVector = if (isPlaying) Icons.Filled.PauseCircleFilled else Icons.Filled.PlayCircleFilled,
                contentDescription = if (isPlaying) "Pause" else "Play",
                modifier = Modifier.size(72.dp)
            )
        }

        // --- Next Button ---
        IconButton(onClick = onSkipNextClick) {
            Icon(
                imageVector = Icons.Filled.SkipNext,
                contentDescription = "Skip Next",
                modifier = Modifier.size(48.dp)
            )
        }

        // ---  Shuffle ---
        IconButton(onClick = onShuffleClick) {
            Icon(
                imageVector = Icons.Filled.Shuffle,
                contentDescription = "Shuffle",
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

@SuppressLint("DefaultLocale")
fun formatDuration(millis: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
    return String.format("%02d:%02d", minutes, seconds)
}


@Composable
fun TopBarContent ( modifier: Modifier = Modifier,){
    Row (
        modifier = modifier.fillMaxWidth().padding(top = 30.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            modifier = Modifier.padding(start = 16.dp),
            onClick = { /* TODO: Open Playlist/Library */ },
        ){
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Playlist",
                modifier = Modifier.size(36.dp)
            )
        }

        IconButton(
            modifier = Modifier.padding(end = 16.dp),
            onClick = { /* TODO: Open Settings */ },
        ){
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                modifier = Modifier.size(36.dp)
            )
        }
    }
}



@Preview
@Composable
fun ButtonsPreview() {
    RampTheme {
        PlayerControls(false, {}, {}, {})
    }
}

@Preview
@Composable
fun TopBarContentPreview() {
    TopBarContent()
}

@Preview
@Composable
fun PlayerContentPreview() {

}


// for best time

@Composable
fun RainbowBackground(){
    val rainbowColorsBrush = remember {
        Brush.horizontalGradient(
            listOf(
                Color(0xFFf90101),
                Color(0xFFf9a101),
                Color(0xFFf9f903),
                Color(0xFF017d01),
                Color(0xFF1314e0),
                Color(0xFF7e027e),
                //Color(0xFFFF00FF)
            ))
    }

    val infiniteTransition = rememberInfiniteTransition(label = "")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = ""
    )
}