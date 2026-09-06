package com.raaveinm.chirro.ui.features

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

val AmbientRandomDefaultColors = listOf( // Embark should already pay me for promo
    Color(0xFF683997), Color(0xFF2f8ed7), Color(0xFF2d73ff), Color(0xFF3b1f64),
    Color(0xFF51103e), Color(0xFFbb244e)
)

@Preview
@Composable
fun AmbientRandom(
    modifier: Modifier = Modifier,
    circleCount: Int = 12,
    colorList: List<Color> = AmbientRandomDefaultColors
) {
    val circles = remember { List(circleCount) { AmbientCircle(colorList) } }
    var isInitialized by remember { mutableStateOf(false) }
    var deltaMillis by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        var lastTime = 0L
        while (true) {
            withFrameMillis { frameTime ->
                if (lastTime != 0L) {
                    deltaMillis = (frameTime - lastTime).toFloat().coerceIn(0f, 64f)
                }
                lastTime = frameTime
            }
        }
    }

    LaunchedEffect(colorList) {
        circles.forEach { it.updateColors(colorList) }
    }

    val blur = 128
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .blur(blur.dp)
    ) {
        val dt = deltaMillis

        if (!isInitialized && size.width > 0f && size.height > 0f) {
            circles.forEach { it.respawn(size, randomizeProgress = true) }
            isInitialized = true
        }

        if (isInitialized) {
            circles.forEach { circle ->
                circle.update(dt, size)

                drawCircle(
                    brush = circle.brush,
                    radius = circle.radius,
                    center = circle.currentCenter,
                    alpha = circle.alpha,
                    blendMode = BlendMode.Color
                )
            }
        }
    }
}


private class AmbientCircle(initialColors: List<Color>) {
    private var baseCenter = Offset.Zero
    private var initialDrift = Offset.Zero
    private var targetDrift = Offset.Zero
    private var durationMillis = 1f
    private var elapsedMillis = 0f
    var radius = 0f
        private set
    var brush: Brush = Brush.sweepGradient(emptyList())
        private set

    private var brushRadius = 0f
    private var brushCenter = Offset.Zero

    private var previousColors: List<Color> = initialColors
    var colors: List<Color> = initialColors
        private set
    private var colorElapsedMillis = COLOR_TRANSITION_MILLIS

    fun respawn(
        size: Size,
        randomizeProgress: Boolean = false
    ) {
        durationMillis = (3000L..27000L).random().toFloat()
        elapsedMillis = if (randomizeProgress) Random.nextFloat() * durationMillis else 0f
        radius = (142..384).random().toFloat()

        baseCenter = Offset(
            x = Random.nextDouble(0.0, size.width.toDouble().coerceAtLeast(1.0)).toFloat(),
            y = Random.nextDouble(0.0, size.height.toDouble().coerceAtLeast(1.0)).toFloat()
        )

        initialDrift = Offset(
            x = (0..64).random().toFloat(),
            y = (0..64).random().toFloat()
        )
        targetDrift = Offset(
            x = (0..64).random().toFloat(),
            y = (0..64).random().toFloat()
        )

        brushRadius = Random.nextDouble(0.0, size.height.toDouble().coerceAtLeast(1.0)).toFloat()
        brushCenter = Offset(
            Random.nextDouble(0.0, size.width.toDouble().coerceAtLeast(1.0)).toFloat(),
            Random.nextDouble(0.0, size.height.toDouble().coerceAtLeast(1.0)).toFloat()
        )

        // A respawning circle starts invisible (alpha ~0), so snap colors instead of
        // carrying a stale crossfade into its next lifecycle.
        previousColors = colors
        colorElapsedMillis = COLOR_TRANSITION_MILLIS
        brush = buildBrush(colors)
    }

    fun updateColors(newColors: List<Color>) {
        if (newColors == colors) return
        previousColors = currentColors()
        colors = newColors
        colorElapsedMillis = 0f
    }

    fun update(deltaMillis: Float, size: Size) {
        elapsedMillis += deltaMillis
        if (elapsedMillis >= durationMillis) {
            respawn(size)
            return
        }
        if (colorElapsedMillis < COLOR_TRANSITION_MILLIS) {
            colorElapsedMillis = (colorElapsedMillis + deltaMillis).coerceAtMost(COLOR_TRANSITION_MILLIS)
            brush = buildBrush(currentColors())
        }
    }

    private fun currentColors(): List<Color> {
        if (colorElapsedMillis >= COLOR_TRANSITION_MILLIS) return colors
        val t = colorElapsedMillis / COLOR_TRANSITION_MILLIS
        val count = colors.size.coerceAtLeast(1)
        return List(count) { i ->
            val position = if (count > 1) i / (count - 1f) else 0f
            lerp(sampleGradient(previousColors, position), colors[i], t)
        }
    }

    private fun buildBrush(stops: List<Color>): Brush = Brush.radialGradient(
        colors = stops,
        radius = brushRadius,
        center = brushCenter
    )

    private val progress: Float
        get() = (elapsedMillis / durationMillis).coerceIn(0f, 1f)

    val alpha: Float
        get() = sin(progress * PI).toFloat().coerceIn(0f, 1f)

    val currentCenter: Offset
        get() {
            val driftX = initialDrift.x + (targetDrift.x - initialDrift.x) * progress
            val driftY = initialDrift.y + (targetDrift.y - initialDrift.y) * progress
            return Offset(baseCenter.x + driftX, baseCenter.y + driftY)
        }

    private companion object {
        const val COLOR_TRANSITION_MILLIS = 900f
    }
}

/** Reads a color at a fractional [position] (0..1) along [colors] as if it were gradient stops. */
private fun sampleGradient(colors: List<Color>, position: Float): Color {
    if (colors.isEmpty()) return Color.Transparent
    if (colors.size == 1) return colors[0]
    val scaled = position.coerceIn(0f, 1f) * (colors.size - 1)
    val index = scaled.toInt().coerceIn(0, colors.size - 2)
    return lerp(colors[index], colors[index + 1], scaled - index)
}
