package com.raaveinm.chirro.ui.layouts

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.min
import coil.compose.SubcomposeAsyncImage
import com.raaveinm.chirro.R
import com.raaveinm.chirro.data.values.TrackInfo

@Composable
fun PlayerMinimized(
    modifier: Modifier = Modifier,
    trackInfo: TrackInfo? = null,
    previousTrack: () -> Unit = {},
    nextTrack: () -> Unit = {},
    playPause: () -> Unit = {},
    onCoverClick: (() -> Unit)?,
    onSurfaceClick:  (() -> Unit)?,
    playPauseIcon: ImageVector
) {
    val track = trackInfo ?: TrackInfo.EMPTY

    Row(
    modifier = modifier
        .padding(bottom = dimensionResource(R.dimen.medium_padding))
        .padding(top = dimensionResource(R.dimen.small_padding))
        .clickable(false) {},
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = dimensionResource(R.dimen.medium_padding))
                .clickable(onSurfaceClick != null) { if (onSurfaceClick != null) onSurfaceClick() },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            SubcomposeAsyncImage(
                model = track.cover,
                contentDescription = "Album Art",
                modifier = Modifier
                    .size(
                        min(
                            dimensionResource(R.dimen.extra_large_size),
                            dimensionResource(R.dimen.extra_large_size)
                        )
                    )
                    .aspectRatio(1f)
                    .clip(shape = MaterialTheme.shapes.small)
                    .clickable(onCoverClick != null) { if (onCoverClick != null) onCoverClick() },
                contentScale = ContentScale.Crop,
                error = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.DarkGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(Color.White),
                            modifier = Modifier
                                .size(dimensionResource(R.dimen.small_size))
                                .shadow(
                                    elevation = dimensionResource(R.dimen.small_padding),
                                    shape = MaterialTheme.shapes.small,
                                    spotColor = Color.Black,
                                    ambientColor = Color.Black
                                )
                        )
                    }
                }
            )

            Column(
                modifier = Modifier.padding(start = dimensionResource(R.dimen.medium_padding)),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (track.title !== "") track.title else stringResource(id = R.string.unknown),
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (track.artist !== "") track.artist else stringResource(id = R.string.unknown),
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = dimensionResource(R.dimen.medium_padding)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(
                onClick = previousTrack
            ) {
                Icon(
                    imageVector = Icons.Default.FastRewind,
                    contentDescription = null,
                    Modifier.size(min(
                    dimensionResource(R.dimen.large_size),
                    dimensionResource(R.dimen.large_size)))
                )
            }

            IconButton(
                onClick = playPause
            ) {
                Icon(
                    imageVector = playPauseIcon,
                    contentDescription = null,
                    Modifier.size(min(
                        dimensionResource(R.dimen.large_size),
                        dimensionResource(R.dimen.large_size)))
                )
            }

            IconButton(
                onClick = nextTrack
            ) {
                Icon(
                    imageVector = Icons.Default.FastForward,
                    contentDescription = null,
                    Modifier.size(min(
                        dimensionResource(R.dimen.large_size),
                        dimensionResource(R.dimen.large_size)))
                )
            }
        }
    }
}

@Preview
@Composable
fun PlayerMinimizedPreview() {
    PlayerMinimized(
        modifier = Modifier.fillMaxWidth(),
        trackInfo = TrackInfo.EMPTY,
        previousTrack = {},
        nextTrack = {},
        playPause = {},
        null,
        null,
        playPauseIcon = Icons.Default.Pause
    )
}
