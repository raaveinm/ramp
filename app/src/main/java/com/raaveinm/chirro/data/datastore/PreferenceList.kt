package com.raaveinm.chirro.data.datastore

import android.support.v4.media.session.PlaybackStateCompat

data class PreferenceList(
    val trackPrimaryOrder: OrderMediaQueue = OrderMediaQueue.ALBUM,
    val trackSecondaryOrder: OrderMediaQueue = OrderMediaQueue.ID,
    val isSearchAsFAB: Boolean = true
)
