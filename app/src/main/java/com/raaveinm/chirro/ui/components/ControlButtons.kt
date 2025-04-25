package com.raaveinm.chirro.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeCompilerApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun ControlButtons(
    modifier: Modifier = Modifier,
    isPlaying: Boolean,
    currentDuration: Float,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onSeek: (Float) -> Unit,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onShuffleClick: () -> Unit
){
    Column {
        Row(
            modifier = modifier.padding(horizontal = 16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(
                modifier = Modifier.padding(horizontal = 6.dp),
                onClick = onFavoriteClick
            ) {
                Icon(
                    modifier = Modifier.size(64.dp),
                    imageVector = if (!isFavorite) Icons.Default.FavoriteBorder else Icons.Default.Favorite,
                    contentDescription = "Favourite"
                )
            }

            IconButton(
                modifier = Modifier.padding(horizontal = 6.dp),
                onClick = onPreviousClick
            ) {
                Icon(
                    modifier = Modifier.size(64.dp),
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous"
                )
            }

            IconButton(
                modifier = Modifier.padding(horizontal = 6.dp),
                onClick = onPlayPauseClick
            ) {
                Icon(
                    modifier = Modifier.size(64.dp),
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play/Pause"
                )
            }

            IconButton(
                modifier = Modifier.padding(horizontal = 6.dp),
                onClick = onNextClick
            ) {
                Icon(
                    modifier = Modifier.size(64.dp),
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next"
                )
            }

            IconButton(
                modifier = Modifier.padding(horizontal = 6.dp),
                onClick = onShuffleClick
            ) {
                Icon(
                    modifier = Modifier.size(64.dp),
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = "Random"
                )
            }

        }
        Spacer(modifier = Modifier.size(16.dp))
        Row {
            Slider(
                modifier = Modifier.padding(horizontal = 21.dp),
                value = currentDuration,
                onValueChange = onSeek
            )
        }
    }
}

@Preview
@Composable
fun ControlButtonsPreview() {
    ControlButtons(
        isPlaying = false,
        currentDuration = 0.2345f,
        onPlayPauseClick = {},
        onNextClick = {},
        onPreviousClick = {},
        onSeek = {},
        isFavorite = false,
        onFavoriteClick = {},
        onShuffleClick = {}
    )
}