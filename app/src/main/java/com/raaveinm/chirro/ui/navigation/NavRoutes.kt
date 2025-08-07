package com.raaveinm.chirro.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
object SettingsScreen

@Serializable
object PlayerScreen

@Serializable
data class PlaylistScreen (val currentTrackId: Int)