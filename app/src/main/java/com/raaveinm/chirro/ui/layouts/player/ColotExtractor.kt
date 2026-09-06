package com.raaveinm.chirro.ui.layouts.player

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
import com.raaveinm.chirro.ui.features.AmbientRandomDefaultColors
import coil.imageLoader
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.pow
import kotlin.random.Random

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

/**
 * True if [this] and [other] differ by more than [thresholdUnits] (out of 255)
 * on any RGB channel.
 */
fun Color.differsBeyond(other: Color, thresholdUnits: Float = 12f): Boolean {
    val threshold = thresholdUnits / 255f
    return kotlin.math.abs(red - other.red) > threshold ||
        kotlin.math.abs(green - other.green) > threshold ||
        kotlin.math.abs(blue - other.blue) > threshold
}

fun colorSpectre(color: Color?): List<Color> {
    if (color == null) {
        return AmbientRandomDefaultColors
    }

    val min = 32f / 255f
    val max = 180f / 255f

    val dominant = color.copy(
        red = color.red.coerceIn(min, max),//.pow(1.025f),
        green = color.green.coerceIn(min, max),//.pow(1.025f),
        blue = color.blue.coerceIn(min, max),//.pow(1.025f)
    )

    val opposite = dominant.copy(
        red = dominant.red.pow(.5f),
        green = dominant.green.pow(.5f),
        blue = dominant.blue.pow(.5f)
    )

    return buildList {
        add(dominant)
        add(opposite)

        @Suppress("unused")
        for (i in 1..6) {
            val rStart = minOf(dominant.red, opposite.red).toDouble()
            val rEnd = maxOf(dominant.red, opposite.red).toDouble()
            val gStart = minOf(dominant.green, opposite.green).toDouble()
            val gEnd = maxOf(dominant.green, opposite.green).toDouble()
            val bStart = minOf(dominant.blue, opposite.blue).toDouble()
            val bEnd = maxOf(dominant.blue, opposite.blue).toDouble()

            add(
                Color(
                    red = if (rStart < rEnd) Random.nextDouble(rStart, rEnd).toFloat() else rStart.toFloat(),
                    green = if (gStart < gEnd) Random.nextDouble(gStart, gEnd).toFloat() else gStart.toFloat(),
                    blue = if (bStart < bEnd) Random.nextDouble(bStart, bEnd).toFloat() else bStart.toFloat(),
                    alpha = 1.0f
                )
            )
        }
    }
}