package com.raaveinm.chirro.ui.layouts

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest

@Composable
fun BackgroundImageCover(
    modifier: Modifier = Modifier,
    imageUri: String,
    opacity: Float = .3f
) {
    SubcomposeAsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUri)
            .crossfade(true)
            .build(),
        contentDescription = "background_image",
        modifier = modifier
            .fillMaxSize()
            .blur(radius = 24.dp),
        contentScale = ContentScale.Crop,
        alpha = opacity
    )
}