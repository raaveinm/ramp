package com.raaveinm.chirro.ui.veiwmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raaveinm.chirro.data.datastore.OrderMediaQueue
import com.raaveinm.chirro.data.datastore.SettingDataStoreRepository
import com.raaveinm.chirro.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingDataStoreRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    val settingsFlow = settingsRepository.settingsPreferencesFlow

    init {
        viewModelScope.launch {
            settingsFlow.collect {
                _uiState.value = _uiState.value.copy(
                    trackPrimaryOrder = it.trackPrimaryOrder,
                    trackSecondaryOrder = it.trackSecondaryOrder,
                    currentTheme = it.currentTheme,
                    isSavedState = it.isSavedState,
                )
            }
        }
    }

    fun setTrackPrimaryOrder(string: String) {
        val order = try {
            OrderMediaQueue.valueOf(string.uppercase())
        } catch (_: IllegalArgumentException) {
            OrderMediaQueue.DEFAULT
        }

        viewModelScope.launch {
            if (_uiState.value.trackSecondaryOrder == order)
                settingsRepository.updateSecondaryOrder(_uiState.value.trackPrimaryOrder)
            settingsRepository.updatePrimaryOrder(order)
        }
    }

    fun setTrackSecondaryOrder(string: String) {
        val order = try {
            OrderMediaQueue.valueOf(string.uppercase())
        } catch (_: IllegalArgumentException) {
            OrderMediaQueue.DEFAULT
        }

        viewModelScope.launch {
            if (_uiState.value.trackPrimaryOrder == order)
                settingsRepository.updatePrimaryOrder(_uiState.value.trackSecondaryOrder)
            settingsRepository.updateSecondaryOrder(order)
        }
    }

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            settingsRepository.updateTheme(theme)
        }
    }

    fun setSavedState(state: Boolean) {
        viewModelScope.launch {
            settingsRepository.setSavedState(state)
        }
    }
}