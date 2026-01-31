package com.raaveinm.chirro.ui.veiwmodel

import com.raaveinm.chirro.data.datastore.OrderMediaQueue

data class SettingsUiState(
    val trackPrimaryOrder: OrderMediaQueue = OrderMediaQueue.ALBUM,
    val trackSecondaryOrder: OrderMediaQueue = OrderMediaQueue.TITLE
)
