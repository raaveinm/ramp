package com.raaveinm.chirro.ui.layouts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.zIndex
import com.raaveinm.chirro.R
import com.raaveinm.chirro.ui.theme.ChirroTheme

@Composable
fun PlayerControlButtons(
    modifier: Modifier,
    dropDownModifier: Modifier = Modifier,
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onSeek: (Float) -> Unit,
    extendedMenu: @Composable () -> Unit,
    currentDuration: Long,
    isFavourite: Boolean,
    onShareClick: () -> Unit,
    trackLength: Long,
    onDismissRequest: () -> Unit = {}
) {
    var isExtended: Boolean by remember { mutableStateOf(false) }
    Column(
        modifier = modifier.padding(dimensionResource(R.dimen.medium_padding)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = dimensionResource(R.dimen.small_padding))
                .defaultMinSize(minHeight = dimensionResource(R.dimen.medium_size)),
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

            Box {
                IconButton(
                    onClick = { isExtended = !isExtended },
                    modifier = Modifier
                        .padding(vertical = dimensionResource(R.dimen.small_padding))
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "extend",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                DropdownMenu(
                    expanded = isExtended,
                    modifier = dropDownModifier
                        .zIndex(1f)
                        .align(Alignment.TopEnd)
                        .background(Color.Transparent)
                        .padding(dimensionResource(R.dimen.small_padding)),
                    onDismissRequest = {
                        isExtended = false
                        onDismissRequest()
                    },
                    shape = MaterialTheme.shapes.medium,
                    content = { extendedMenu() }
                )
            }
        }

        Spacer(Modifier.padding(dimensionResource(R.dimen.small_padding)))

        Slider(
            modifier = Modifier
                .padding(horizontal = dimensionResource(R.dimen.medium_padding))
                .fillMaxWidth(),
            value = currentDuration.toFloat(),
            onValueChange = { newValue -> onSeek(newValue) },
            valueRange = 0f..trackLength.toFloat().coerceAtLeast(1f)
        )
    }
}

@Preview
@Composable
fun ControlsPreview() {
    ChirroTheme {
        var isPlaying: Boolean by remember {mutableStateOf(false) }
        var isFavourite: Boolean by remember { mutableStateOf(false) }
        PlayerControlButtons(
            modifier = Modifier,
            isPlaying = isPlaying,
            onPlayPauseClick = { !isPlaying },
            onPreviousClick = {},
            onNextClick = {},
            onSeek = {},
            extendedMenu = {
                Column {
                    Text("Time left: 00:00")
                    Text("Time passed: 00:00")
                }
            },
            currentDuration = 0L,
            isFavourite = isFavourite,
            onShareClick = { !isFavourite },
            trackLength = 2000L
        )
    }
}