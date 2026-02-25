package com.raaveinm.chirro.data.datastore

import com.raaveinm.chirro.data.values.TrackInfo
import com.raaveinm.chirro.data.values.OrderMediaQueue
import com.raaveinm.chirro.ui.theme.AppTheme

data class PreferenceList(
    val trackPrimaryOrder: OrderMediaQueue = OrderMediaQueue.ALBUM,
    val trackSecondaryOrder: OrderMediaQueue = OrderMediaQueue.ID,
    val trackSortAscending: Boolean = true,
    val currentTheme: AppTheme = AppTheme.DYNAMIC,
    val currentTrack: TrackInfo? = null,
    val isSavedState: Boolean = false,
    val isShuffleMode: Boolean = false
)
