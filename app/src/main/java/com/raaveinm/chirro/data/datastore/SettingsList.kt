package com.raaveinm.chirro.data.datastore

import com.raaveinm.chirro.data.values.OrderMediaQueue

data class SettingsList(
    val trackPrimaryOrder: OrderMediaQueue = OrderMediaQueue.ALBUM,
    val trackSecondaryOrder: OrderMediaQueue = OrderMediaQueue.ID,
    val trackSortAscending: Boolean = true,
    val isShuffleMode: Boolean = false
)