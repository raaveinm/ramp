package com.raaveinm.chirro.ui.layouts

import android.graphics.drawable.BitmapDrawable
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.palette.graphics.Palette
import coil.imageLoader
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun rememberDominantColor(
    imageUri: String?,
    defaultColor: Color = MaterialTheme.colorScheme.background
): State<Color> {
    val context = LocalContext.current
    val dominantColor = remember { mutableStateOf(defaultColor) }

    LaunchedEffect(imageUri) {
        if (imageUri == null || imageUri == "none" || imageUri.isBlank()) {
            dominantColor.value = defaultColor
            return@LaunchedEffect
        }

        val request = ImageRequest.Builder(context)
            .data(imageUri)
            .allowHardware(false)
            .size(96)
            .build()

        when (val result = context.imageLoader.execute(request)) {
            is SuccessResult -> {
                val bitmap = (result.drawable as? BitmapDrawable)?.bitmap
                if (bitmap != null) {
                    withContext(Dispatchers.Default) {
                        val palette = Palette.from(bitmap).generate()
                        val rgb = palette.dominantSwatch?.rgb
                            ?: palette.vibrantSwatch?.rgb
                            ?: palette.mutedSwatch?.rgb

                        if (rgb != null) dominantColor.value = Color(rgb)
                        else dominantColor.value = defaultColor
                    }
                } else {
                    dominantColor.value = defaultColor
                }
            }
            is ErrorResult -> {
                dominantColor.value = defaultColor
            }
        }
    }

    return animateColorAsState(
        targetValue = dominantColor.value,
        animationSpec = tween(durationMillis = 800),
        label = "DominantColorAnimation"
    )
}
