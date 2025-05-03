package com.raaveinm.chirro.ui.fragments

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.raaveinm.chirro.domain.PlayerService
import com.raaveinm.chirro.domain.managment.QueueManager
import com.raaveinm.chirro.domain.usecase.CommandResolver
import com.raaveinm.chirro.ui.components.ControlButtons
import com.raaveinm.chirro.ui.components.TrackCover
import com.raaveinm.chirro.ui.components.TrackInfoScreen


@Composable
fun PlayerScreen(
    modifier: Modifier,
    context: Context
) {
    var isPlaying by rememberSaveable { mutableStateOf(false) }

    Column (
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        TrackCover(
            modifier = Modifier.padding(16.dp).fillMaxWidth()
        )

        Spacer(modifier = Modifier.padding(16.dp))

        TrackInfoScreen(
            modifier = Modifier.fillMaxWidth(),
            trackTitle = "Bullets",
            trackArtist = "Archive",
            trackAlbum = "Controlling Crowds",
            trackDuration = 353000,
        )

        Spacer(modifier = Modifier.padding(36.dp))

        ControlButtons(
            isPlaying = isPlaying,
            currentDuration = 0.2345f,
            onPlayPauseClick = {  },
            onNextClick = {  },
            onPreviousClick = {  },
            onSeek = {},
            isFavorite = true,
            onFavoriteClick = {},
            onShuffleClick = {}
        )
        Spacer(Modifier.padding(38.dp))
    }
}
