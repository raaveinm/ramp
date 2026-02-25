package com.raaveinm.chirro.data

import android.app.Application
import com.raaveinm.chirro.data.datastore.SettingDataStoreRepository
import com.raaveinm.chirro.data.datastore.dataStore

class ChirroApplication : Application() {
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(
            this,
            SettingDataStoreRepository(dataStore)
        )
    }
}