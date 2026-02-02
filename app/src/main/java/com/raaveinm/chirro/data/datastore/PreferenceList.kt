package com.raaveinm.chirro.data.datastore

import com.raaveinm.chirro.ui.theme.AppTheme

data class PreferenceList(
    val trackPrimaryOrder: OrderMediaQueue = OrderMediaQueue.ALBUM,
    val trackSecondaryOrder: OrderMediaQueue = OrderMediaQueue.ID,
    val currentTheme: AppTheme = AppTheme.DYNAMIC
)
