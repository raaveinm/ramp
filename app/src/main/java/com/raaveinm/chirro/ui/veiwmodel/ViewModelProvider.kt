package com.raaveinm.chirro.ui.veiwmodel

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.raaveinm.chirro.data.ChirroApplication

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            PlayerViewModel(
                application = chirroApplication(),
                trackRepository = chirroApplication().container.trackRepository,
                settingsRepository = chirroApplication().container.settingsRepository
            )
        }
        initializer {
            SettingsViewModel(
                settingsRepository = chirroApplication().container.settingsRepository
            )
        }
    }
}

fun CreationExtras.chirroApplication(): ChirroApplication =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as ChirroApplication)