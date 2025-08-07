package com.raaveinm.chirro

import android.app.Application
import com.raaveinm.chirro.data.AppContainer
import com.raaveinm.chirro.data.DefaultAppContainer

class ChirroApplication : Application() {
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}