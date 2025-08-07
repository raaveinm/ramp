package com.raaveinm.chirro.ui.layouts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import com.raaveinm.chirro.R
import com.raaveinm.chirro.ui.theme.ChirroTheme

@Composable
fun PlayerControlButtons(
    modifier: Modifier,
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onSeek: () -> Unit,
    onShareClick: () -> Unit,
    currentDuration: Float,
    isFavourite: Boolean,
    onFavoriteClick: () -> Unit
) {
    Column(
        modifier = modifier.padding(dimensionResource(R.dimen.medium_padding)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = dimensionResource(R.dimen.small_padding)),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(
                onClick = onShareClick,
                modifier = Modifier
                    .padding(vertical = dimensionResource(R.dimen.small_padding))
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "share",
                    modifier = Modifier.fillMaxSize()
                )
            }

            IconButton(
                onClick = onPreviousClick,
                modifier = Modifier
                    .padding(vertical = dimensionResource(R.dimen.small_padding))
            ) {
                Icon(
                    imageVector = Icons.Default.FastRewind,
                    contentDescription = "previous",
                    modifier = Modifier.fillMaxSize()
                )
            }

            IconButton(
                onClick = onPlayPauseClick,
                modifier = Modifier
                    .padding(vertical = dimensionResource(R.dimen.small_padding))
            ) {
                Icon(
                    imageVector = if (isPlaying) {Icons.Default.Pause} else {Icons.Default.PlayArrow},
                    contentDescription = "play/pause",
                    modifier = Modifier.fillMaxSize()
                )
            }

            IconButton(
                onClick = onNextClick,
                modifier = Modifier
                    .padding(vertical = dimensionResource(R.dimen.small_padding))
            ) {
                Icon(
                    imageVector = Icons.Default.FastForward,
                    contentDescription = "next",
                    modifier = Modifier.fillMaxSize()
                )
            }

            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier
                    .padding(vertical = dimensionResource(R.dimen.small_padding))
            ) {
                Icon(
                    imageVector = if (isFavourite){Icons.Default.Favorite}else{Icons.Default.FavoriteBorder},
                    contentDescription = "next",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(Modifier.padding(dimensionResource(R.dimen.small_padding)))

        Slider(
            modifier = Modifier
                .padding(horizontal = dimensionResource(R.dimen.medium_padding))
                .fillMaxWidth(),
            value = currentDuration,
            onValueChange = { onSeek() },
            valueRange = 0f..100f
        )
    }
}

@Preview
@Composable
fun ControlsPreview() {
    ChirroTheme {
        PlayerControlButtons(
            modifier = Modifier.fillMaxSize(),
            isPlaying = true,
            onPlayPauseClick = {},
            onPreviousClick = {},
            onNextClick = {},
            onSeek = {},
            onShareClick = {},
            currentDuration = 0f,
            isFavourite = true,
            onFavoriteClick = {}
        )
    }
}