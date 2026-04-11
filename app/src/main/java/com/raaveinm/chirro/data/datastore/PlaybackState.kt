package com.raaveinm.chirro.data.datastore

import com.raaveinm.chirro.data.values.EqualizerPreferences
import com.raaveinm.chirro.data.values.TrackInfo

data class PlaybackState(
    val currentTrack: TrackInfo? = null,
    val isSavedState: Boolean = false,
    val equalizerPreferences: EqualizerPreferences? = null
)
