package com.raaveinm.chirro.ui.veiwmodel

import com.raaveinm.chirro.data.values.TrackInfo

data class SearchBarUiState (
    val currentText: String? = null,
    val isSearching: Boolean = false,
    val searchResults: List<TrackInfo> = emptyList()
)