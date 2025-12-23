package com.raaveinm.chirro.ui.veiwmodel

import com.raaveinm.chirro.data.database.TrackInfo


data class PlayerUiState(
    val currentTrack: TrackInfo? = null,
    var isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val totalDuration: Long = 0L,
    val isFavorite: Boolean = false
)