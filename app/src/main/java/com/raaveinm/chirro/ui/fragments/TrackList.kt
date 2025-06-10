package com.raaveinm.chirro.ui.fragments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.raaveinm.chirro.ui.components.TrackInfoScreen
import com.raaveinm.chirro.ui.veiwmodel.TrackListViewModel

@Composable
fun TrackList (
    modifier: Modifier = Modifier,
    trackListViewModel: TrackListViewModel = viewModel()
) {
    val trackList by trackListViewModel.trackList.collectAsState()

    Column(modifier = modifier) {

        TrackInfoScreen(
            trackTitle = "Title",
            trackArtist = "Artist",
            trackAlbum = "Album",
            trackDuration = 0L,
            modifier = Modifier.fillMaxWidth().background(color = MaterialTheme.colorScheme.inverseOnSurface)
        )

        LazyColumn {
            items(items = trackList, key = { track -> track.id }) { track ->
                TrackInfoScreen(
                    trackTitle = track.title,
                    trackArtist = track.artist,
                    trackAlbum = track.album,
                    trackDuration = track.duration,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                )
            }
        }
    }
}