package com.raaveinm.chirro.ui.layouts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import com.raaveinm.chirro.R
import com.raaveinm.chirro.data.TrackInfo
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun TrackInfoLayout(
    modifier: Modifier = Modifier,
    trackInfo: TrackInfo,
) {
    Row (
        modifier = modifier
            .padding(horizontal = dimensionResource(R.dimen.medium_padding))
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column (
            modifier = Modifier
                .padding(horizontal = dimensionResource(R.dimen.small_padding)),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = trackInfo.title,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.padding(dimensionResource(R.dimen.small_padding)))
            Text(
                text = "${trackInfo.artist} - ${trackInfo.album}",
                style = MaterialTheme.typography.titleSmall
            )
        }
        val timeFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
        Text(
            text = timeFormat.format(trackInfo.duration),
            style = MaterialTheme.typography.titleLarge,
        )
    }
}

@Preview
@Composable
fun TrackInfoLayoutPreview() {
    TrackInfoLayout(
        modifier = Modifier.fillMaxSize(),
        trackInfo = TrackInfo(
            id = 1,
            title = "Title",
            artist = "Artist",
            album = "Album",
            uri = "Uri",
            cover = "Cover",
            duration = 100000
        )
    )
}