package com.raaveinm.chirro.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@SuppressLint("DefaultLocale")
@Composable
fun TrackInfoScreen(
    modifier: Modifier,
    trackTitle: String,
    trackArtist: String,
    trackAlbum: String,
    trackDuration: Long,
) {
    val trackDurationMin = ((trackDuration / 1000)/60)
    val trackDurationSec = ((trackDuration / 1000)%60)

    Row (
        modifier = modifier.padding(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = trackTitle,
                fontSize = 24.sp
            )
            Text(text = "$trackArtist - $trackAlbum")
        }
        if (trackDuration != 0L) {
            Text(
                text = "$trackDurationMin : ${String.format("%02d", trackDurationSec)}",
                fontSize = 20.sp
            )
        } else { Text(text = "Duration") }
    }
}

@Preview(showBackground = true)
@Composable
fun TrackInfoScreenPreview(){
    TrackInfoScreen(
        modifier = Modifier.fillMaxWidth(),
        trackTitle = "Bullets",
        trackArtist = "Archive",
        trackAlbum = "Controlling Crowds",
        trackDuration = 353000,
    )
}