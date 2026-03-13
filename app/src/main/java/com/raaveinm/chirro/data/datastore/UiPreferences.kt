package com.raaveinm.chirro.data.datastore

import com.raaveinm.chirro.ui.theme.AppTheme

data class UiPreferences(
    val currentTheme: AppTheme = AppTheme.DYNAMIC,
    val backgroundDynamicColor: Boolean = true
)
