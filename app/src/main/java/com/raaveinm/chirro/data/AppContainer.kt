package com.raaveinm.chirro.data

import android.content.Context
import com.raaveinm.chirro.data.database.TrackDatabase
import com.raaveinm.chirro.data.datastore.SettingDataStoreRepository
import com.raaveinm.chirro.data.datastore.dataStore // Import the extension property
import com.raaveinm.chirro.data.repository.TrackRepository
import com.raaveinm.chirro.data.repository.TrackRepositoryImpl

interface AppContainer {
    val settingsRepository: SettingDataStoreRepository
    val trackRepository: TrackRepository
    val settingRepository: SettingDataStoreRepository
}

class DefaultAppContainer(
    private val context: Context,
    override val settingsRepository: SettingDataStoreRepository
): AppContainer {

    override val settingRepository: SettingDataStoreRepository by lazy {
        SettingDataStoreRepository(context.dataStore)
    }

    override val trackRepository: TrackRepository by lazy {
        TrackRepositoryImpl(
            context = context,
            trackDao = TrackDatabase.getDatabase(context).trackDao(),
            settingsRepository = settingRepository
        )
    }
}