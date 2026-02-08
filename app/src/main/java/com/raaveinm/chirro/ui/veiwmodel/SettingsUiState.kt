package com.raaveinm.chirro.ui.veiwmodel

import com.raaveinm.chirro.data.values.OrderMediaQueue
import com.raaveinm.chirro.ui.theme.AppTheme

data class SettingsUiState(
    val trackPrimaryOrder: OrderMediaQueue = OrderMediaQueue.ALBUM,
    val trackSecondaryOrder: OrderMediaQueue = OrderMediaQueue.TITLE,
    val trackSortAscending: Boolean = true,
    val currentTheme: AppTheme = AppTheme.DYNAMIC,
    val isSavedState: Boolean = false,
    val isShuffleMode: Boolean = false
)
