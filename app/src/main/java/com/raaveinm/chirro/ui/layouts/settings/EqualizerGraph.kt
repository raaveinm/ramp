package com.raaveinm.chirro.ui.layouts.settings

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raaveinm.chirro.R
import java.util.Locale
import kotlin.math.abs
import kotlin.math.pow

@SuppressLint("MutableCollectionMutableState")
@Composable
fun EqualizerGraph(
    bandGains: List<Float>,
    onBandGainChange: (Int, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val frequencies = listOf("60", "150", "400", "1k", "2.5k", "6k", "10k", "15k")
    var activePointIndex by remember { mutableStateOf<Int?>(null) }
    val currentGains by rememberUpdatedState(bandGains)
    val currentOnGainChange by rememberUpdatedState(onBandGainChange)
    val textMeasurer = rememberTextMeasurer()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(R.dimen.small_padding))
            .padding(bottom = dimensionResource(R.dimen.medium_padding))
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Box(modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
        ) {
            val lineColor = MaterialTheme.colorScheme.primary
            val controlDotColor = MaterialTheme.colorScheme.onPrimary
            val controlDotHighlight = MaterialTheme.colorScheme.secondary
            val contentColor = MaterialTheme.colorScheme.outline
            val gradientColors = listOf(
                MaterialTheme.colorScheme.onPrimary.copy(alpha = .6f),
                MaterialTheme.colorScheme.onPrimary.copy(alpha = .2f)
            )

            Canvas(
                modifier = Modifier

                    .padding(horizontal = dimensionResource(R.dimen.small_padding))
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            val down = awaitFirstDown()
                            val widthPerBand = size.width / (currentGains.size - 1)
                            val closestIndex = (down.position.x / widthPerBand).toInt().coerceIn(0, currentGains.size - 1)
                            val exactPointX = closestIndex * widthPerBand

                            if (abs(down.position.x - exactPointX) < 100f) {
                                activePointIndex = closestIndex
                                down.consume()

                                var pointer: PointerInputChange? = down
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val move = event.changes.find { it.id == pointer?.id }
                                    if (move == null || !move.pressed) {
                                        activePointIndex = null
                                        break
                                    }

                                    val positionDelta = move.position - move.previousPosition
                                    if (positionDelta != Offset.Zero) {
                                        val dbPerPixel = 24f / size.height
                                        val newGain = (currentGains[activePointIndex!!] - (positionDelta.y * dbPerPixel))
                                            .coerceIn(-12f, 12f)
                                        currentOnGainChange(activePointIndex!!, newGain)
                                        move.consume()
                                    }
                                    pointer = move
                                }
                            }
                        }
                    }
            ) {
                val width = size.width
                val height = size.height
                val halfHeight = height / 2f

                val points = bandGains.mapIndexed { index, gainDb ->
                    val x = index * (width / (bandGains.size - 1))
                    val y = halfHeight - (gainDb / 12f * halfHeight)
                    Offset(x, y)
                }

                ///////////////////////////////////////////////
                // Grid and Labels
                ///////////////////////////////////////////////

                val gridColor = lineColor.copy(alpha = .6f)
                val labelStyle = TextStyle(
                    color = contentColor,
                    fontSize = 10.sp
                )

                // Vertical Grid Lines
                points.forEach { point ->
                    drawLine(
                        color = gridColor,
                        start = Offset(point.x, 0f),
                        end = Offset(point.x, height),
                        strokeWidth = 1f
                    )
                }

                // Horizontal Grid Lines and Multiplier Labels
                val dbLevels = listOf(12f, 6f, 0f, -6f, -12f)
                dbLevels.forEach { db ->
                    val y = halfHeight - (db / 12f * halfHeight)

                    // Grid line
                    if (db != 0f) {
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1f
                        )
                    }

                    val multiplier = 10.0.pow(db / 20.0)
                    val multiplierText = if (multiplier >= 1.0) {
                        String.format(Locale.US, "%.1fx", multiplier)
                    } else {
                        String.format(Locale.US, "%.2fx", multiplier)
                    }

                    drawText(
                        textMeasurer = textMeasurer,
                        text = multiplierText,
                        style = labelStyle,
                        topLeft = Offset(4.dp.toPx(), y - 12.dp.toPx())
                    )
                }

                ///////////////////////////////////////////////
                // Base/Zero Line
                ///////////////////////////////////////////////

                drawLine(
                    color = lineColor.copy(alpha = 0.2f),
                    start = Offset(0f, halfHeight),
                    end = Offset(width, halfHeight),
                    strokeWidth = 2f
                )

                ///////////////////////////////////////////////
                // Curve
                ///////////////////////////////////////////////

                val curvePath = Path().apply {
                    moveTo(points.first().x, points.first().y)

                    for (i in 0 until points.size - 1) {
                        val current = points[i]
                        val next = points[i + 1]

                        val control1 = Offset((current.x + next.x) / 2f, current.y)
                        val control2 = Offset((current.x + next.x) / 2f, next.y)

                        cubicTo(
                            control1.x, control1.y,
                            control2.x, control2.y,
                            next.x, next.y
                        )
                    }
                }

                val fillPath = Path().apply {
                    addPath(curvePath)
                    lineTo(width, halfHeight)
                    lineTo(0f, halfHeight)
                    close()
                }

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = gradientColors
                    )
                )

                drawPath(
                    path = curvePath,
                    color = lineColor,
                    style = Stroke(width = 4.dp.toPx())
                )

                ///////////////////////////////////////////////
                // Control Dots
                ///////////////////////////////////////////////

                points.forEachIndexed { index, point ->
                    val isDragging = activePointIndex == index

                    drawCircle(
                        color = controlDotColor.copy(alpha = if (isDragging) 0.8f else 0.4f),
                        radius = if (isDragging) 12.dp.toPx() else 8.dp.toPx(),
                        center = point
                    )
                    // Inner dot
                    drawCircle(
                        color = controlDotHighlight,
                        radius = 5.dp.toPx(),
                        center = point
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            frequencies.forEach { freq ->
                Text(
                    text = freq,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
