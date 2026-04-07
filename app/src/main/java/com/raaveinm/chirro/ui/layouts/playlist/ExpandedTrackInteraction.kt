package com.raaveinm.chirro.ui.layouts.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.dimensionResource
import com.raaveinm.chirro.R
import com.raaveinm.chirro.data.values.TrackInfo

@Composable
fun ExpandedTrackInteraction(
    modifier: Modifier = Modifier,
    trackInfo: TrackInfo,
    playTrack: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    val title = if (trackInfo.title == "<unknown>") "" else trackInfo.title
    val artist = if (trackInfo.artist == "<unknown>") "" else trackInfo.artist
    val request = "https://www.google.com/search?q=${title}+${artist}"
    Column(
        modifier = modifier
            .padding(horizontal = dimensionResource(R.dimen.small_padding))
            .clip(shape = MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = .2f)),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Bottom
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Transparent,
            onClick = { uriHandler.openUri(request) }
        ) {
            Text(
                text = "Find track on web",
                modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.medium_padding))
            )
        }
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Transparent,
            onClick = { playTrack() }
        ) {
            Text(
                text = "Play track",
                modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.medium_padding))
            )
        }
    }
}