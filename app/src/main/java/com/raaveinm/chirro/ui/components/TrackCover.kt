package com.raaveinm.chirro.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import coil3.compose.rememberAsyncImagePainter
import com.raaveinm.chirro.R

@Composable
fun TrackCover(
    modifier: Modifier = Modifier,
    trackCover: String = ""
) {
    val trackCoverUri: Painter = if (trackCover!="") rememberAsyncImagePainter(trackCover.toUri()) else painterResource(R.drawable.ic_launcher_background)
    Image(painter = trackCoverUri, contentDescription = null, modifier = modifier)
}

@Preview
@Composable
fun Preview(){
    TrackCover(Modifier.fillMaxSize())
}