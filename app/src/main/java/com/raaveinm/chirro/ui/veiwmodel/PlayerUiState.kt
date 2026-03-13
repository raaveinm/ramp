package com.raaveinm.chirro.ui.veiwmodel

import com.raaveinm.chirro.data.values.TrackInfo


data class PlayerUiState(
    val currentTrack: TrackInfo? = null,
    val isPlaying: Boolean = false,
    val isFavorite: Boolean = false
)
