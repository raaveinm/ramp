package com.raaveinm.chirro.ui.navigation

import kotlinx.serialization.Serializable

sealed class NavData {
    @Serializable object PlayerScreen : NavData()
    @Serializable object SettingsScreen : NavData()
    @Serializable data class PlaylistScreen(
        val isNavigating: Boolean = false
    ) : NavData()
}