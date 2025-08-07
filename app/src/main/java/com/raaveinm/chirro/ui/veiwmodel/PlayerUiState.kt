package com.raaveinm.chirro.ui.veiwmodel

import com.raaveinm.chirro.data.TrackInfo


data class PlayerUiState(
    val currentTrack: TrackInfo? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val totalDuration: Long = 0L,
    val isFavorite: Boolean = false
)