package com.raaveinm.chirro.ui.layouts

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.raaveinm.chirro.R
import com.raaveinm.chirro.data.database.TrackInfo
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

@Composable
fun TrackInfoLayout(
    modifier: Modifier = Modifier,
    trackInfo: TrackInfo,
    pictureRequired: Boolean = true,
    containerColor: Color? = null,
    onClick: () -> Unit,
    onSwipeRTL: () -> Unit = {},
    onSwipeLTR: () -> Unit = {},
    onCoverClick: () -> Unit = {}
) {
    if (pictureRequired) {
        val offsetX = remember { Animatable(0f) }
        val scope = rememberCoroutineScope()

        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = dimensionResource(R.dimen.medium_padding)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            ///////////////////////////////////////////////
            // Track Album Card
            ///////////////////////////////////////////////
            Card(
                modifier = Modifier
                    .size(300.dp)
                    .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                    .shadow(16.dp, RoundedCornerShape(16.dp))
                    .clickable { onCoverClick() }
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                if (offsetX.value > 200) {
                                    onSwipeLTR()
                                } else if (offsetX.value < -200) {
                                    onSwipeRTL()
                                }
                                scope.launch {
                                    offsetX.animateTo(0f)
                                }
                            }
                        ) { change, dragAmount ->
                            change.consume()
                            scope.launch {
                                offsetX.snapTo(offsetX.value + dragAmount)
                            }
                        }
                    },
                shape = RoundedCornerShape(16.dp),
            ) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(trackInfo.cover)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Album Art",
                    modifier = Modifier.fillMaxSize(),
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
                                modifier = Modifier.size(dimensionResource(R.dimen.large_size))
                            )
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.medium_size)))

            ///////////////////////////////////////////////
            // Track Info Card
            ///////////////////////////////////////////////
            TextInfo(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensionResource(R.dimen.small_padding))
                    .padding(top = dimensionResource(R.dimen.large_size)),
                trackInfo = trackInfo,
                containerColor = containerColor,
                onClick = onClick
            )
        }
    } else {
        ///////////////////////////////////////////////
        // Track Info Without Picture
        ///////////////////////////////////////////////
        TextInfo(
            modifier = modifier,
            trackInfo = trackInfo,
            containerColor = containerColor,
            onClick = onClick
        )
    }
}

///////////////////////////////////////////////
// Track Info
///////////////////////////////////////////////
@Composable
private fun TextInfo(
    modifier: Modifier = Modifier,
    trackInfo: TrackInfo,
    containerColor: Color? = null,
    onClick: () -> Unit
){
    Card(
        modifier = modifier,
        colors = if (containerColor != null) {
            CardDefaults.cardColors(containerColor = containerColor)
        } else {
            CardDefaults.cardColors()
        },
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensionResource(R.dimen.medium_padding))
                .padding(vertical = dimensionResource(R.dimen.small_padding)),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = trackInfo.title,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Start,
                    softWrap = true,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                )

                Text(
                    text = "${trackInfo.artist} - ${trackInfo.album}",
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Start,
                    softWrap = true,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = formatDuration(trackInfo.duration),
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.End,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@SuppressLint("DefaultLocale")
fun formatDuration(durationMs: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) % 60
    return String.format("%02d:%02d", minutes, seconds)
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
            duration = 100000,
            isFavourite = false,
            date = 100000
        ),
        onClick = {}
    )
}