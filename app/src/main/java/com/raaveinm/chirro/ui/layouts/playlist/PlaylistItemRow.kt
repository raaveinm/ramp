package com.raaveinm.chirro.ui.layouts.playlist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment.Companion.CenterEnd
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import com.raaveinm.chirro.R
import com.raaveinm.chirro.data.values.TrackInfo
import com.raaveinm.chirro.ui.layouts.TrackInfoLayout
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistItemRow(
    track: TrackInfo,
    isExpanded: Boolean,
    isPlaying: Boolean,
    onExpandToggle: (Long) -> Unit,
    play: (TrackInfo) -> Unit,
    onDeleteSwipe: (TrackInfo) -> Boolean,
    navigateTo: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val dismissState = rememberSwipeToDismissBoxState(
        initialValue = SwipeToDismissBoxValue.Settled,
        positionalThreshold = { totalDistance -> totalDistance * .01f }
    )

    val animatedColor by animateColorAsState(
        targetValue = when (dismissState.targetValue) {
            SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.error
            else -> MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(durationMillis = 500)
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        onDismiss = {
            if (it == SwipeToDismissBoxValue.EndToStart) {
                val isDeleted = onDeleteSwipe(track)
                if (!isDeleted) {
                    scope.launch { dismissState.snapTo(SwipeToDismissBoxValue.Settled) }
                }
            }
        },
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = dimensionResource(R.dimen.small_padding))
                    .background(animatedColor, MaterialTheme.shapes.medium),
                contentAlignment = CenterEnd
            ) {
                if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.padding(end = dimensionResource(R.dimen.medium_padding)),
                        tint = MaterialTheme.colorScheme.onError
                    )
                }
            }
        },
        content = {
            Column (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensionResource(R.dimen.small_padding))
                    .animateContentSize()
                    .then(
                        if (isPlaying) {
                            Modifier
                                .padding(vertical = 4.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                        } else Modifier.clip(MaterialTheme.shapes.medium)
                    ),
            ) {
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    ExpandedTrackInteraction(
                        modifier = Modifier.fillMaxWidth(),
                        trackInfo = track,
                        playTrack = { play(track) }
                    )
                }
                TrackInfoLayout(
                    modifier = Modifier.fillMaxSize(),
                    trackInfo = track,
                    pictureRequired = false,
                    sidePictureRequired = true,
                    containerColor = if (isPlaying) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                    interactionModifier = Modifier.combinedClickable(
                        onClick = { play(track); navigateTo() },
                        onLongClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onExpandToggle(track.id)
                        }
                    )
                )
            }
        }
    )
}