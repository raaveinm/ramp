package com.raaveinm.chirro.ui.theme

import androidx.compose.ui.graphics.vector.ImageVector

data class ThemeOption(
    val theme: AppTheme,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
)
