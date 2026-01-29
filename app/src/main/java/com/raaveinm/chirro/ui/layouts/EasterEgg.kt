package com.raaveinm.chirro.ui.layouts

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun ArcEasterEgg() {
    val infiniteTransition = rememberInfiniteTransition(label = "PulseTransition")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
//            animation = tween(35000, easing = CubicBezierEasing(1.2f, 1.4f, .8f, .5f)),
            animation = tween(35000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseProgress"
    )

    Spacer(modifier = Modifier.fillMaxSize().drawWithCache {
        val width = size.width
        val height = size.height

        val lineSpacing = width * 0.055f
        val strokeWidth = lineSpacing * 0.65f
        val startX = width * 0.08f
        val curveBreakY = height * 0.75f
        val endX = width * 1.2f

        val masterPath = Path().apply {
            moveTo(startX, -100f)

            lineTo(startX, curveBreakY)

            quadraticTo(
                startX, height,
                endX, height
            )
        }

        val pathMeasure = PathMeasure()
        pathMeasure.setPath(masterPath, false)
        val totalLength = pathMeasure.length

        onDrawBehind {
            val colors = listOf(Color.Cyan, Color.Green, Color.Yellow, Color.Red)
            colors.forEachIndexed { index, color ->
                val xOffset = index * lineSpacing

                translate(left = xOffset, top = 0f) {
                    drawPath(
                        path = masterPath,
                        color = color.copy(alpha = 0.3f),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Pulse
                    val tailLength = totalLength * 0.4f
                    val animationRange = totalLength + tailLength
                    val currentPos = progress * animationRange
                    val stopDistance = currentPos.coerceAtMost(totalLength)
                    val startDistance = (currentPos - tailLength).coerceAtLeast(0f)
                    if (stopDistance > startDistance + 0.1f) {
                        val pulseSegment = Path()
                        pathMeasure.getSegment(startDistance, stopDistance, pulseSegment, true)

                        val headPos = pathMeasure.getPosition(stopDistance)
                        val tailPos = pathMeasure.getPosition(startDistance)

                        drawPath(
                            path = pulseSegment,
                            brush = Brush.linearGradient(
                                0.0f to color.copy(alpha = 0f),
                                1.0f to color,
                                start = tailPos,
                                end = headPos
                            ),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }
            }
        }
    })
}

@Preview
@Composable
fun PreviewCurvedPulseLine() {
    ArcEasterEgg()
}
